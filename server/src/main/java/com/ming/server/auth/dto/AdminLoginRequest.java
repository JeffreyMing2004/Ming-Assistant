package com.ming.server.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {

    @NotBlank(message = "请输入管理员用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;

    /** 极验 GT4 验证结果（lot_number / captcha_output / pass_token / gen_time）。 */
    private String lotNumber;
    private String captchaOutput;
    private String passToken;
    private String genTime;
}