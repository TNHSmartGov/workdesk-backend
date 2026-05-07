package com.tnh.baseware.core.dtos.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tnh.baseware.core.dtos.audit.CategoryDTO;
import com.tnh.baseware.core.entities.audit.Identifiable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.hateoas.RepresentationModel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO extends RepresentationModel<UserDTO> implements Identifiable<UUID> {

    UUID id;
    String username;

    @JsonIgnore
    String password;
    String firstName;
    String lastName;
    String fullName;
    String phone;
    String email;
    String avatarUrl;
    String idn;
    Instant birthday;
    String address;
    Integer ial;
    Boolean enabled;
    Boolean locked;
    Instant lockTime;
    Instant accountExpiryDate;
    Integer failedLoginAttempts;
    Boolean superAdmin;
    String userType;
    CategoryDTO level;
    String organizationRole;
    List<RoleDTO> roles;
}
