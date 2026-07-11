package com.guessgame.repository;

import com.guessgame.entity.PurchaseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    Page<PurchaseHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
