package com.service;

import com.entity.TravelRequest;
import com.dto.ChatRequestDTO;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content
 * @method
 */
public interface TravelRequestService {
    Integer saveRequest(ChatRequestDTO dto);
}