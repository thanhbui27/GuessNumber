package com.guessgame.repository;

import com.guessgame.entity.User;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query("select u from User u where u.username = :value or u.email = :value")
    Optional<User> findByUsernameOrEmail(@Param("value") String value);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    List<User> findTop10ByOrderByScoreDescCreatedAtAscIdAsc();

    @Query("""
            select count(u) from User u
            where u.score > :score
               or (u.score = :score and u.createdAt < :createdAt)
               or (u.score = :score and u.createdAt = :createdAt and u.id < :id)
            """)
    long countUsersAhead(@Param("score") int score, @Param("createdAt") java.time.LocalDateTime createdAt, @Param("id") Long id);
}
