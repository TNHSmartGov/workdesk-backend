package com.tnh.baseware.core.entities.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@ToString
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<T> {

    @CreatedBy
    @Column(nullable = false, updatable = false)
    T createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    Instant createdDate;

    @LastModifiedBy
    @Column(nullable = false)
    T modifiedBy;

    @LastModifiedDate
    @Column(nullable = false)
    Instant modifiedDate;

    @Column(nullable = false)
    Boolean deleted = false;
}
