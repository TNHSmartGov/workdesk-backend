package com.tnh.baseware.core.entities.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tnh.baseware.core.entities.audit.Auditable;
import com.tnh.baseware.core.entities.audit.Category;
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
public class Menu extends Auditable<String> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(unique = true, nullable = false)
    String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_type_id", nullable = false)
    Category menuType;

    @Column(unique = true, nullable = false)
    String alias;

    @Column
    String note;

    @Column(nullable = false)
    String path;

    @Column(nullable = false)
    String link;

    @Column(nullable = false)
    Integer published; // 0: unpublished, 1: published

    @Column(nullable = false)
    Integer browserNav; // 0: open the current window, 1: open a new window

    @Column(nullable = false)
    String icon;

    @Column(nullable = false)
    Integer menuOrder;
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Menu parent;

    @JsonIgnore
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    Set<Menu> children = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "menus_roles",
            joinColumns = @JoinColumn(name = "menu_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @Builder.Default
    Set<Role> roles = new HashSet<>();
}
