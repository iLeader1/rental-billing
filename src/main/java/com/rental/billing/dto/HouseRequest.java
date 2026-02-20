package com.rental.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HouseRequest {

    @NotBlank(message = "房屋名称不能为空")
    @Size(max = 50, message = "名称长度不能超过50个字符")
    private String houseName;

    @Size(max = 255, message = "地址长度不能超过255个字符")
    private String address;
}
