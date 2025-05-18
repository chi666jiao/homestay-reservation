package com.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content
 * @method
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("travel_request")
public class TravelRequest {
    @TableField("id")
    private Long id;

    @TableField("user_id")
    private Long userId;
    @TableField("interests")
    private String interests;    // 兴趣：自然风光,历史文化
    @TableField("destination")
    private String destination;   // 目的地：北京
    @TableField("budget")
    private Integer budget;      // 预算：5000
    @TableField("days")
    private Integer days;        // 天数：7
    @TableField("activities")
    private String activities;   // 偏好活动：徒步
    @TableField("request_time")
    private LocalDateTime requestTime;
}
