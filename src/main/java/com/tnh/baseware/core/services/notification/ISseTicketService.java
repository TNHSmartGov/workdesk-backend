package com.tnh.baseware.core.services.notification;

import java.util.UUID;

public interface ISseTicketService {
    String generateTicket(UUID userId);

    UUID validateAndRemoveTicket(String ticket);
}
