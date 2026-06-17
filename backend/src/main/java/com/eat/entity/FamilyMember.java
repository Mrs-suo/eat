package com.eat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(
    name = "family_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"family_id", "user_id"}),
    indexes = {
        @Index(columnList = "user_id"),
        @Index(columnList = "family_id")
    }
)
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
}
