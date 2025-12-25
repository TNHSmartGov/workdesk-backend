package com.tnh.baseware.core.events.type;

import java.util.UUID;

public record UserCreatedEvent(UUID userId, String username) {}
