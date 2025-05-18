package com.controller;

import com.annotation.IgnoreAuth;
import com.dto.ChatRequestDTO;
import com.dto.DayItinerary;
import com.dto.RecommendationDTO;
import com.entity.TravelRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.DeepseekService;
import com.service.TravelRequestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 谭俊
 * @date 2025/4/26
 * @content 这个是推荐的接口
 * @method
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecommendationController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    private final DeepseekService deepseekService;
    private final TravelRequestService requestService;

    @IgnoreAuth
    @PostMapping("/ai-chat")
    public ResponseEntity<?> parseRequest(@RequestBody Map<String, String> request) {
        String rawText = request.get("message");

        // 使用正则表达式解析
        ChatRequestDTO dto = new ChatRequestDTO();

        // 解析预算（支持多种格式）
        Matcher budgetMatcher = Pattern.compile("预算\\s*(\\d+)").matcher(rawText);
        if (budgetMatcher.find()) {
            dto.setBudget(Integer.parseInt(budgetMatcher.group(1)));
        }

        // 解析天数（支持中文单位）
        Matcher daysMatcher = Pattern.compile("天数\\s*(\\d+)").matcher(rawText);
        if (daysMatcher.find()) {
            dto.setDays(Integer.parseInt(daysMatcher.group(1)));
        }
//         1. 参数校验
        if (dto.getBudget() == null || dto.getDays() == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "预算和天数为必填项")
            );
        }

        // 2. 构建Deepseek请求体
        Map<String, Object> deepseekRequest = Map.of(
                "model", "deepseek-r1:1.5b",
                "messages", Arrays.asList(
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user", "content", buildUserContent(dto))
                ),
                "stream", false
        );

        // 3. 调用Deepseek服务
        String aiResponse = deepseekService.getRecommendation(deepseekRequest);
        // 4. 解析响应
        RecommendationDTO recommendation = new UnifiedResponseParser().parseAiResponse(aiResponse);

        // 5. 存储到数据库
        Integer savedRequest = requestService.saveRequest(dto);

        // 6. 返回结构化响应
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "replyMessage", recommendation.getReplyMessage(),
                "itineraries", recommendation.getItineraries(),
                "costDetails", recommendation.getCostDetails(),
                "totalCost", recommendation.getTotalCost()
        ));
    }

    private String buildSystemPrompt() {
        return "作为旅游规划专家，你需要根据以下要素生成攻略：\n"
                + "1. 兴趣类型（自然/文化/美食）\n"
                + "2. 预算范围（精确到千元）\n"
                + "3. 每日行程安排\n"
                + "4. 推荐住宿区域\n"
                + "响应格式要求：\n"
                + "【概览】开头总结\n"
                + "【每日行程】分点列出\n"
                + "【预算分配】表格形式";
    }

    private String buildUserContent(ChatRequestDTO dto) {
        return String.format("兴趣：%s | 预算：%d元 | 天数：%d天 | 活动偏好：%s",
                // 设置兴趣默认值
                dto.getInterests().isEmpty() ? "美食/景点" : String.join(",", dto.getInterests()),
                dto.getBudget(),
                dto.getDays(),
                // 设置活动默认值
                dto.getActivities().isEmpty() ? "休闲活动" : String.join(",", dto.getActivities()));
    }

    // 解析方法实现
    public  class UnifiedResponseParser {
        private static final Pattern SECTION_PATTERN = Pattern.compile(
                "\\【(.*?)\\】\\s*([\\s\\S]*?)(?=\\【|$)"
        );
        private static final Pattern ITEM_PATTERN = Pattern.compile(
                "(\\d+\\.|•|-) (.*?)(?=\\d+\\.|•|-|$)", Pattern.DOTALL
        );
        private static final Pattern COST_PATTERN = Pattern.compile(
                "([\\u4e00-\\u9fa5]+/)?([\\u4e00-\\u9fa5]+)[:：]?\\s*(\\d+)(元|¥)"
        );

        public RecommendationDTO parseAiResponse(String content) {
            RecommendationDTO dto = new RecommendationDTO();
            try {
                // 阶段1：按中文章节划分（兼容###标记）
                Map<String, String> sections = parseSections(content.replace("###", "【】"));

                // 阶段2：提取核心字段
                dto.setReplyMessage(extractSummary(sections));
                dto.setItineraries(extractItineraries(sections.get("每日行程")));
                dto.setCostDetails(extractCosts(sections.get("预算分配")));
                dto.setTotalCost(calculateTotalCost(sections));

            } catch (Exception e) {
                logger.warn("部分解析失败: {}", e.getMessage());
                // 降级处理：提取所有数字型预算
                dto.setCostDetails(fallbackCostExtract(content));
            }
            return dto;
        }

        private Map<String, String> parseSections(String content) {
            Matcher matcher = SECTION_PATTERN.matcher(content);
            Map<String, String> sections = new LinkedHashMap<>();
            while(matcher.find()) {
                String title = matcher.group(1).trim();
                String body = matcher.group(2).trim()
                        .replaceAll("\\*\\*", "") // 清理常见标记
                        .replaceAll("<br/?>", "\n");
                sections.put(title, body);
            }
            return sections;
        }

        private List<DayItinerary> extractItineraries(String itineraryText) {
            List<DayItinerary> list = new ArrayList<>();
            Matcher dayMatcher = ITEM_PATTERN.matcher(itineraryText);
            while(dayMatcher.find()) {
                String[] parts = dayMatcher.group(2).split("\\n");
                DayItinerary day = new DayItinerary();
                day.setDayTitle(parts[0].replaceAll(":$", "").trim());
                day.setActivities(Arrays.stream(parts)
                        .skip(1)
                        .map(String::trim)
                        .collect(Collectors.joining("\n")));
                list.add(day);
            }
            return list;
        }

        private Map<String, Integer> extractCosts(String costText) {
            Map<String, Integer> costs = new LinkedHashMap<>();
            Matcher costMatcher = COST_PATTERN.matcher(costText);
            while(costMatcher.find()) {
                String category = Optional.ofNullable(costMatcher.group(1))
                        .orElse(costMatcher.group(2));
                costs.put(category, Integer.parseInt(costMatcher.group(3)));
            }
            return costs;
        }

        // 降级解析方法
        private Map<String, Integer> fallbackCostExtract(String content) {
            return Pattern.compile("(\\d+)(?=元|¥)")
                    .matcher(content)
                    .results()
                    .collect(Collectors.toMap(
                            m -> "项目" + (m.start()/10), // 生成伪键
                            m -> Integer.parseInt(m.group(1)),
                            (oldVal, newVal) -> oldVal,
                            LinkedHashMap::new
                    ));
        }
        private String extractSummary(Map<String, String> sections) {
            // 优先从【概览】章节提取，兼容多种标题变体
            String summary = Stream.of("概览", "行程概览", "总体说明")
                    .map(sections::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseGet(() -> {
                        // 降级策略：取首段非空内容
                        return sections.values().stream()
                                .filter(text -> !text.isEmpty())
                                .findFirst()
                                .map(text -> text.split("\\n\\n+")[0])
                                .orElse("为您推荐以下定制行程");
                    });

            // 清理特殊符号并截断
            return summary.replaceAll("\\*+", "")
                    .replaceAll("【.*?】", "")
                    .replaceAll("\\s+", " ")
                    .substring(0, Math.min(summary.length(), 150));
        }

        private Integer calculateTotalCost(Map<String, String> sections) {
            // 方案1：直接提取总预算（优先）
            Pattern totalPattern = Pattern.compile("总(预算|费用)[:：]\\s*(\\d+)元?");
            Matcher matcher = totalPattern.matcher(sections.getOrDefault("预算分配", ""));
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(2));
            }

            // 方案2：累加分项费用（兼容无总预算情况）
            return sections.entrySet().stream()
                    .filter(e -> e.getKey().contains("预算") || e.getKey().contains("费用"))
                    .flatMap(e -> COST_PATTERN.matcher(e.getValue()).results())
                    .mapToInt(m -> Integer.parseInt(m.group(3)))
                    .sum();
        }
    }
}