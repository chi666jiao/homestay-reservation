package com.service.impl;

import com.service.DeepseekService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content
 * @method
 */
@Service
public class DeepseekServiceImpl  implements DeepseekService {
    @Autowired
    private  RestTemplate restTemplate;

    @Override
    public String getRecommendation(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:11434/api/chat",
                entity,
                String.class
        );
        return response.getBody();
    }
}