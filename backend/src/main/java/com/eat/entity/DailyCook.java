package com.eat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(
    name = "daily_cooks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"family_id", "cook_date"}),
    indexes = {
        @Index(columnList = "family_id, cook_date"),
        @Index(columnList = "cook_user_id")
    }
)
public class DailyCook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "cook_user_id", nullable = false, length = 64)
    private String cookUserId;

    @Column(name = "cook_date", nullable = false)
    private LocalDate cookDate;

    @Column(name = "set_at")
    private LocalDateTime setAt;
}
