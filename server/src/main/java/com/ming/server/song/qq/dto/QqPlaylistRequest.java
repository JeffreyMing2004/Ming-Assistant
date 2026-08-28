package com.ming.server.song.qq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QqPlaylistRequest {

    @NotBlank(message = "请输入QQ音乐歌单链接或歌单ID")
    @Size(max = 300, message = "链接过长")
    private String url;
}