package com.ming.server.gift.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TrackingRequest {

    @Size(max = 64, message = "快递单号过长")
    private String trackingNumber;
}