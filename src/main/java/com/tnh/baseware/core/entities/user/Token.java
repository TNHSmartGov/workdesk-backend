package com.tnh.baseware.core.entities.user;

import com.tnh.baseware.core.entities.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Token extends Auditable<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(unique = true, nullable = false)
    String jti;

    @Column(nullable = false)
    UUID sessionId;

    @Column(nullable = false)
    String tokenType;

    @Column(nullable = false)
    String deviceId;

    @Column(nullable = false)
    String device;

    @Column(nullable = false)
    String ipAddress;

    @Column(nullable = false)
    String platform;

    @Column(nullable = false)
    String browser;

    @Column(nullable = false)
    @Builder.Default
    Boolean revoked = Boolean.FALSE;

    @Column(nullable = false)
    @Builder.Default
    Boolean expired = Boolean.FALSE;

    @Column(nullable = false)
    Instant expiration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    User user;
}