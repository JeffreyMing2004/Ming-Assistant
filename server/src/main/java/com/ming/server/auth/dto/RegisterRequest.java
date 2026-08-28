package com.ming.server.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "B站UID不能为空")
    @Pattern(regexp = "\\d{1,20}", message = "B站UID必须为数字")
    private String bilibiliUid;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度需在3-30之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在6-64之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}