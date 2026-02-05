package com.tnh.baseware.core.repositories.user;

import com.tnh.baseware.core.entities.user.UserOrganization;
import com.tnh.baseware.core.enums.OrganizationRole;
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
                        UUID organizationId);

        @Query("""
                            select uo
                            from UserOrganization uo
                            join fetch uo.user u
                            left join fetch uo.title t
                            where uo.organization.id = :orgId
                              and uo.active = true
                        """)
        List<UserOrganization> findActiveByOrganizationId(@Param("orgId") UUID orgId);

        @Query(value = """
                            select uo
                            from UserOrganization uo
                            where uo.organization.id = :orgId
                              and uo.active = true
                        """, countQuery = """
                            select count(uo)
                            from UserOrganization uo
                            where uo.organization.id = :orgId
                              and uo.active = true
                        """)
        Page<UserOrganization> findActiveByOrganizationId(
                        @Param("orgId") UUID orgId,
                        Pageable pageable);

        @Modifying
        @Query("""
                            update UserOrganization uo
                            set uo.active = false
                            where uo.organization.id = :orgId
                              and uo.user.id in :userIds
                        """)
        int deactivateUsers(
                        @Param("orgId") UUID orgId,
                        @Param("userIds") List<UUID> userIds);

        @Modifying
        @Query("""
                            update UserOrganization uo
                            set uo.active = true
                            where uo.organization.id = :orgId
                              and uo.user.id in :userIds
                        """)
        int activateUsers(
                        @Param("orgId") UUID orgId,
                        @Param("userIds") List<UUID> userIds);

        @Modifying
        @Query("""
                            update UserOrganization uo
                            set uo.organizationRole = :role
                            where uo.organization.id = :orgId
                              and uo.user.id = :userId
                              and uo.active = true
                        """)
        int updateOrganizationRole(
                        @Param("orgId") UUID orgId,
                        @Param("userId") UUID userId,
                        @Param("role") OrganizationRole role);

        Boolean existsByOrganization_IdAndOrganizationRole(UUID organizationId, OrganizationRole organizationRole);

        List<UserOrganization> findByOrganizationIdAndUserIdInAndActiveTrue(UUID orgId, Collection<UUID> userIds);

        @Query("""
                            select uo
                            from UserOrganization uo
                            where uo.organization.id = :orgId
                              and uo.active = true
                            order by case uo.organizationRole
                                when com.tnh.baseware.core.enums.OrganizationRole.UNIT_LEADER then 1
                                when com.tnh.baseware.core.enums.OrganizationRole.DEPUTY then 2
                                when com.tnh.baseware.core.enums.OrganizationRole.STAFF then 3
                                else 4 end, uo.user.fullName asc
                        """)
        Page<UserOrganization> findByOrganizationIdAndActiveTrue(@Param("orgId") UUID orgId, Pageable pageable);

        @Query("""
                            select uo
                            from UserOrganization uo
                            where uo.organization.id = :orgId
                              and uo.active = true
                            order by case uo.organizationRole
                                when com.tnh.baseware.core.enums.OrganizationRole.UNIT_LEADER then 1
                                when com.tnh.baseware.core.enums.OrganizationRole.DEPUTY then 2
                                when com.tnh.baseware.core.enums.OrganizationRole.STAFF then 3
                                else 4 end, uo.user.fullName asc
                        """)
        Set<UserOrganization> findByOrganizationIdAndActiveTrue(@Param("orgId") UUID orgId);

        Optional<UserOrganization> findByUserIdAndOrganizationIdAndActiveTrue(UUID userId, UUID orgId);

        Set<UserOrganization> findByUserIdAndActiveTrue(UUID userId);

        @Query("""
                            select uo
                            from UserOrganization uo
                            where uo.user.username = :username
                              and uo.active = true
                        """)
        List<UserOrganization> findByUsernameAndActiveTrue(String username);

        boolean existsByUserIdAndOrganizationIdAndActiveTrue(
                        UUID userId, UUID organizationId);
}
