package com.ming.server.gift;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiftRepository extends JpaRepository<GiftRecord, Long> {

    List<GiftRecord> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}