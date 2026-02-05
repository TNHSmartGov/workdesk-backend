package com.tnh.baseware.core.dtos.user;

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
public class UserProfileDTO {

    UUID userId;
    String firstName;
    String lastName;
    String fullName;
    String avatarUrl;
    String email;
    String coverUrl;
    Instant joinDate;
    String description;
    String phone;
    Instant birthday;
    String address;
    String idn;
    Integer ial;
    // Stats
    Integer totalTasks;
    Integer participatedTasks;
    Integer completedTasks;
    Integer completedProjects;
    Double performance;
    Instant lastStatsUpdate;
}
