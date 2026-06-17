package com.eat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(
    name = "family_invitations",
    indexes = {
        @Index(columnList = "family_id"),
        @Index(columnList = "invitee_user_id"),
        @Index(columnList = "status")
    }
)
public class FamilyInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "inviter_user_id", nullable = false, length = 64)
    private String inviterUserId;

    @Column(name = "invitee_user_id", nullable = false, length = 64)
    private String inviteeUserId;

    @Column(name = "invitee_phone", nullable = false, length = 20)
    private String inviteePhone;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 24)
    private String requestType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
