package com.service;

import java.util.Map;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content
 * @method
 */
public interface DeepseekService {
    String getRecommendation(Map<String, Object> requestBody);
}