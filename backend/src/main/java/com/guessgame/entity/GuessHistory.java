package com.guessgame.entity;

import com.guessgame.enums.GuessResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "guess_history")
public class GuessHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "guessed_number", nullable = false, columnDefinition = "TINYINT")
    private Byte guessedNumber;

    @Column(name = "server_number", nullable = false, columnDefinition = "TINYINT")
    private Byte serverNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GuessResult result;

    @Column(name = "score_after", nullable = false)
    private Integer scoreAfter;

    @Column(name = "turns_after", nullable = false)
    private Integer turnsAfter;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public GuessHistory(User user, int guessedNumber, int serverNumber, GuessResult result, int scoreAfter, int turnsAfter) {
        this.user = user;
        this.guessedNumber = (byte) guessedNumber;
        this.serverNumber = (byte) serverNumber;
        this.result = result;
        this.scoreAfter = scoreAfter;
        this.turnsAfter = turnsAfter;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
