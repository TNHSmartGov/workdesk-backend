package com.tnh.baseware.core.dtos.basic;

import java.util.UUID;

import com.tnh.baseware.core.entities.audit.Identifiable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicProjectDTO implements Identifiable<UUID> {
    UUID id;
    String name;
    String code;
    String description;
}
