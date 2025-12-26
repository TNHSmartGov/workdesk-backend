package com.tnh.baseware.core.dtos.basic;

import java.util.UUID;

import com.tnh.baseware.core.entities.audit.Identifiable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasicUserDTO implements Identifiable<UUID> {
    UUID id;
    String username;
    String fullName;
    String email;
    String phone;
    String avatar;
}
