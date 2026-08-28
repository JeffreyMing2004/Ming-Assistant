package com.ming.server.gift;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gift_records", indexes = @Index(name = "idx_gift_user", columnList = "user_id"))
public class GiftRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(name = "bilibili_uid", length = 32)
    private String bilibiliUid;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "gift_type", nullable = false, length = 50)
    private String giftType;

    /** 后台登记的快递单号，用户在 App 端可查询物流。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(name = "tracking_number", length = 64)
    private String trackingNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}