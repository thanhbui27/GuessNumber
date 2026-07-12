package com.guessgame.repository;

import com.guessgame.entity.PurchaseHistory;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
    Page<PurchaseHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PurchaseHistory p join fetch p.user where p.transactionCode = :transactionCode")
    Optional<PurchaseHistory> findByTransactionCodeForUpdate(String transactionCode);
}
