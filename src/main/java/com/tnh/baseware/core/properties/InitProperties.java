package com.tnh.baseware.core.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "init")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitProperties {

    List<RoleInfo> roles;
    List<UserInfo> users;
    List<OrganizationInfo> organizations;

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RoleInfo {
        String name;
        String description;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserInfo {
        String username;
        String password;
        String firstName;
        String lastName;
        String fullName;
        String phone;
        String email;
        String avatarUrl;
        String idn;
        int ial;
        boolean enabled;
        boolean isSuperAdmin;
        String role;
    }

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrganizationInfo {
        String name;
        String description;
        String code;
        boolean isSystem;
    }
}