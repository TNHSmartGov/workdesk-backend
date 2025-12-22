package com.tnh.baseware.core.repositories.user;

import com.tnh.baseware.core.entities.audit.Category;
import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.repositories.IGenericRepository;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface IUserOrganizationRepository extends IGenericRepository<UserOrganization, UUID> {
    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    @Query("""
                select uo.user.id
                from UserOrganization uo
                where uo.organization.id = :orgId
                  and uo.active = true
            """)
    Set<UUID> findActiveUserIdsByOrganizationId(@Param("orgId") UUID orgId);

    Optional<UserOrganization> findByUserIdAndOrganizationId(
            UUID userId,
            UUID organizationId
    );

    @Query("""
                select uo
                from UserOrganization uo
                join fetch uo.user u
                left join fetch uo.title t
                where uo.organization.id = :orgId
                  and uo.active = true
            """)
    List<UserOrganization> findActiveByOrganizationId(@Param("orgId") UUID orgId);

    @Query(
            value = """
                        select uo
                        from UserOrganization uo
                        where uo.organization.id = :orgId
                          and uo.active = true
                    """,
            countQuery = """
                        select count(uo)
                        from UserOrganization uo
                        where uo.organization.id = :orgId
                          and uo.active = true
                    """
    )
    Page<UserOrganization> findActiveByOrganizationId(
            @Param("orgId") UUID orgId,
            Pageable pageable
    );

    @Modifying
    @Query("""
                update UserOrganization uo
                set uo.active = false
                where uo.organization.id = :orgId
                  and uo.user.id in :userIds
            """)
    int deactivateUsers(
            @Param("orgId") UUID orgId,
            @Param("userIds") List<UUID> userIds
    );

    @Modifying
    @Query("""
                update UserOrganization uo
                set uo.active = true
                where uo.organization.id = :orgId
                  and uo.user.id in :userIds
            """)
    int activateUsers(
            @Param("orgId") UUID orgId,
            @Param("userIds") List<UUID> userIds
    );

    @Modifying
    @Query("""
                update UserOrganization uo
                set uo.title = :title
                where uo.organization.id = :orgId
                  and uo.user.id = :userId
                  and uo.active = true
            """)
    int updateTitle(
            @Param("orgId") UUID orgId,
            @Param("userId") UUID userId,
            @Param("title") Category title
    );

    List<UserOrganization> findByOrganizationIdAndUserIdInAndActiveTrue(UUID orgId, Collection<UUID> userIds);
    Optional<UserOrganization> findByUserIdAndOrganizationIdAndActiveTrue(UUID userId, UUID  orgId);
}
