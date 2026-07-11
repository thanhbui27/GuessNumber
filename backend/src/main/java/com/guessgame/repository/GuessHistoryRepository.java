package com.guessgame.repository;

import com.guessgame.entity.GuessHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuessHistoryRepository extends JpaRepository<GuessHistory, Long> {
    Page<GuessHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
