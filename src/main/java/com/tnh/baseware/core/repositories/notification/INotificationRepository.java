package com.tnh.baseware.core.repositories.notification;

import com.tnh.baseware.core.entities.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

    @Query("select n from Notification n " +
            "where n.recipient.id = :recipientId and n.createdDate >= :from " +
            "order by n.createdDate asc")
    List<Notification> findForResume(UUID recipientId, Instant from, Pageable pageable);

    Page<Notification> findByRecipientId(UUID recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndIsReadFalse(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(UUID recipientId);

    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.isRead = true, n.readAt = :readAt " +
            "where n.id = :id and n.recipient.id = :recipientId and n.isRead = false")
    int markReadById(UUID id, UUID recipientId, Instant readAt);

    @Modifying(clearAutomatically = true)
    @Query("update Notification n set n.isRead = true, n.readAt = :readAt " +
            "where n.recipient.id = :recipientId and n.isRead = false")
    int markAllRead(UUID recipientId, Instant readAt);

    long deleteByCreatedDateBefore(Instant cutoff);
}
