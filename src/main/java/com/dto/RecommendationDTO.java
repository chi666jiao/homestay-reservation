package com.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content
 * @method
 */
// 响应DTO
// 首先需要创建对应的DTO结构
@Data // Lombok注解
public class RecommendationDTO {
    private String replyMessage;   // 回复消息
    private List<DayItinerary> itineraries = new ArrayList<>(); // 每日行程
    private Map<String, Integer> costDetails = new HashMap<>(); // 费用明细
    private Integer totalCost;     // 总费用
}

