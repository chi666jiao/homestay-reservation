package com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 谭俊
 * @date 2025/5/6
 * @content
 * @method
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    @NotBlank(message = "输入内容不能为空")
    private String message;

    @Min(value = 500, message = "预算最低500元")
    private Integer budget;

    @Range(min = 1, max = 30, message = "天数需在1-30之间")
    private Integer days;

    private List<String> interests=new ArrayList<>();
    private List<String> activities=new ArrayList<>();
}