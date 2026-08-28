package com.ming.server.auth.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUidRequest {

    /** 允许为空（清空则不再获取头像），非空必须为纯数字且长度不超过20位。 */
    @Pattern(regexp = "\\d{0,20}", message = "B站UID必须为数字")
    private String bilibiliUid;
}