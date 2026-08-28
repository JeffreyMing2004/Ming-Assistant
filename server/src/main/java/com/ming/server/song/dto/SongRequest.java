package com.ming.server.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SongRequest {

    @NotBlank(message = "歌名不能为空")
    @Size(max = 200, message = "歌名过长")
    private String title;

    @Size(max = 100, message = "歌手过长")
    private String artist;

    @Size(max = 500, message = "备注过长")
    private String note;
}