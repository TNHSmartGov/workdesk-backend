package com.tnh.baseware.core.dtos.basic;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicTaskDTO {
    UUID id;
    String title;
    String description;
    Instant dueDate;
}
