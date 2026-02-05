package com.tnh.baseware.core.mappers.notification;

import com.tnh.baseware.core.dtos.notification.NotificationDTO;
import com.tnh.baseware.core.entities.notification.Notification;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface INotificationMapper {

    @Mapping(target = "recipientId", source = "recipient.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", source = "sender.fullName")
    @Mapping(target = "senderAvatar", source = "sender.avatarUrl")
    NotificationDTO entityToDTO(Notification entity);

    // No form to entity needed for now as we use manual build or internal logic
}
