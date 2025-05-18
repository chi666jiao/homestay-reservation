package com.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
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
// 推荐攻略表
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName( "recommendation")
public class Recommendation {
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Long id;

    @TableField("request_id")  // 存储外键ID
    private Long requestId;

    @TableField("content")
    private String content;      // 存储AI生成的攻略文本
    @TableField("generate_time")
    private LocalDateTime generateTime;
}