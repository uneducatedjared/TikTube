package com.buguagaoshu.tiktube.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;



@Data
public class LoginDetails {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 100)
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Size(max = 8)
    private String verifyCode;

    private Boolean rememberMe;
}
