package com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content
 * @method
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayItinerary {
    private String dayTitle;       // 例如"第一天：自然风光与探索"
    private String activities;     // 活动详情
}