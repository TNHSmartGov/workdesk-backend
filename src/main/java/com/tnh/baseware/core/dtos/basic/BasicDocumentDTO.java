package com.tnh.baseware.core.dtos.basic;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicDocumentDTO {
    UUID id;
    String documentNumber;
    String summary;
}
