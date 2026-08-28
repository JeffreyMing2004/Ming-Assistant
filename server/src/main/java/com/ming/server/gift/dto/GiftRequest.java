package com.ming.server.gift.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GiftRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名过长")
    private String nickname;

    @Pattern(regexp = "\\d{0,20}", message = "B站UID必须为数字")
    private String bilibiliUid;

    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话过长")
    private String phone;

    @NotBlank(message = "住址不能为空")
    @Size(max = 255, message = "住址过长")
    private String address;

    @NotBlank(message = "礼物类型不能为空")
    @Size(max = 50, message = "礼物类型过长")
    private String giftType;
}