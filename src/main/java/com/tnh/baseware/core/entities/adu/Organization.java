package com.tnh.baseware.core.entities.adu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.enums.OrganizationLevel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Organization extends Auditable<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true)
    String name;

    @Column(nullable = false, unique = true)
    String code;

    String countryCode;
    String provinceCode;
    String communeCode;
    String address;
    String phone;
    String email;
    String website;
    String description;
    Double latitude;
    Double longitude;

    Boolean isSystem;

    @Column(nullable = false)
    @Builder.Default
    Integer level = OrganizationLevel.PROVINCE.getValue();

    @OneToMany(mappedBy = "organization")
    Set<UserOrganization> users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Organization parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    Set<Organization> children = new HashSet<>();
}
