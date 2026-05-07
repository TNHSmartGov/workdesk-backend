package com.tnh.baseware.core.utils;

import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.entities.user.CustomUserDetails;
import com.tnh.baseware.core.entities.user.User;
import com.tnh.baseware.core.enums.OrganizationRole;
import com.tnh.baseware.core.exceptions.BWCValidationException;
import com.tnh.baseware.core.repositories.adu.IOrganizationRepository;
import com.tnh.baseware.core.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public final class SecurityUtils {

    private final MessageService messageService;
    private final IOrganizationRepository organizationRepository;

    public CustomUserDetails currentUserDetails() {
        var context = SecurityContextHolder.getContext();
        if (context == null) {
            throw new BWCValidationException(messageService.getMessage("security.context.null"));
        }

        var auth = context.getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BWCValidationException(messageService.getMessage("user.not.authenticated"));
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof CustomUserDetails cud)) {
            throw new BWCValidationException(
                    "Expected CustomUserDetails but got: "
                            + principal.getClass().getName());
        }

        return cud;
    }

    public UUID currentOrgId() {
        UUID organizationId = currentUserDetails().getOrganizationId();
        if (organizationId == null) {
            throw new BWCValidationException("Current user's organization ID is null");
        }
        return organizationId;
    }

    public User currentUser() {
        return currentUserDetails().getUser();
    }

    public Boolean checkIsSuperAdmin() {
        return currentUserDetails().getUser().getSuperAdmin();
    }

    /**
     * Check if current user can access stats of a specific organization
     * 
     * Authorization Rules:
     * 1. Super Admin OR user from System Organization → Can view ANY org
     * 2. UNIT_LEADER/DEPUTY at org with is_unit=true → Can view their org + all
     * child orgs
     * 3. Regular user at org with is_unit=false → Can ONLY view their own org
     * 
     * @param targetOrgId Organization ID to check access for
     * @return true if user can access this organization's stats
     */
    public boolean canAccessOrganizationStats(UUID targetOrgId) {
        try {
            CustomUserDetails userDetails = currentUserDetails();
            User user = userDetails.getUser();
            UUID userOrgId = userDetails.getOrganizationId();

            // Rule 1: Super Admin can view everything
            if (user.getSuperAdmin() != null && user.getSuperAdmin()) {
                return true;
            }

            // Rule 1: User from System Organization can view everything
            if (isUserFromSystemOrganization(userOrgId)) {
                return true;
            }

            // Rule 2 & 3: Check based on user's organization properties and title
            return canAccessBasedOnOrgHierarchy(user, userOrgId, targetOrgId);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user is from a System Organization (is_system = true)
     */
    private boolean isUserFromSystemOrganization(UUID orgId) {
        if (orgId == null)
            return false;

        try {
            // Query organization to check is_system flag
            // This requires IOrganizationRepository injection
            return organizationRepository.findById(orgId)
                    .map(org -> org.getIsSystem() != null && org.getIsSystem())
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check access based on organization hierarchy and user title
     * 
     * Logic:
     * - If user's org has is_unit=true AND user is UNIT_LEADER/DEPUTY:
     * → Can view own org + all child orgs (recursive)
     * - Otherwise:
     * → Can ONLY view own org
     */
    private boolean canAccessBasedOnOrgHierarchy(User user, UUID userOrgId, UUID targetOrgId) {
        // User must belong to an organization
        if (userOrgId == null)
            return false;

        // If target is user's own org → Always allowed
        if (userOrgId.equals(targetOrgId)) {
            return true;
        }

        // Check if user's org is a Unit (is_unit = true)
        Organization userOrg = organizationRepository.findById(userOrgId).orElse(null);
        if (userOrg == null)
            return false;

        // If org is NOT a unit → User can ONLY view own org
        if (userOrg.getIsUnit() == null || !userOrg.getIsUnit()) {
            return false; // Already checked own org above, this is different org
        }

        // Org IS a unit → Check if user has UNIT_LEADER or DEPUTY title
        if (!hasOrganizationLeadershipTitle(user, userOrgId)) {
            return false; // User doesn't have leadership title
        }

        // User is leader at unit org → Check if target is a child org
        return isChildOrganization(userOrgId, targetOrgId);
    }

    /**
     * Check if user has UNIT_LEADER or DEPUTY title in ORGANIZATION_TITLE category
     * for a specific organization
     */
    private boolean hasOrganizationLeadershipTitle(User user, UUID orgId) {
        try {
            return user.getOrganizations().stream()
                    .filter(userOrg -> userOrg.getOrganization().getId().equals(orgId))
                    .filter(userOrg -> userOrg.getTitle() != null)
                    .anyMatch(userOrg -> {
                        OrganizationRole role = userOrg.getOrganizationRole();
                        return role == OrganizationRole.UNIT_LEADER ||
                                role == OrganizationRole.DEPUTY;
                    });
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if targetOrgId is a child (or descendant) of parentOrgId
     * Uses recursive query to check entire hierarchy
     */
    private boolean isChildOrganization(UUID parentOrgId, UUID targetOrgId) {
        try {
            return organizationRepository.isDescendantOf(parentOrgId, targetOrgId);
        } catch (Exception e) {
            return false;
        }
    }

    // ============ DEPRECATED - Keeping for backward compatibility ============

    /**
     * @deprecated Use canAccessOrganizationStats() instead
     */
    @Deprecated
    public boolean hasOrganizationAdmin(UUID orgId) {
        return canAccessOrganizationStats(orgId);
    }

    /**
     * @deprecated Use canAccessOrganizationStats() instead
     */
    @Deprecated
    public boolean isUnitLeader() {
        try {
            UUID userOrgId = currentOrgId();
            return hasOrganizationLeadershipTitle(currentUser(), userOrgId);
        } catch (Exception e) {
            return false;
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Check if current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        try {
            Set<String> roleSet = Set.of(roles);
            return currentUserDetails().getAuthorities().stream()
                    .anyMatch(auth -> {
                        String authority = auth.getAuthority();
                        String roleName = authority.startsWith("ROLE_")
                                ? authority.substring(5)
                                : authority;
                        return roleSet.contains(roleName) || roleSet.contains(authority);
                    });
        } catch (Exception e) {
            return false;
        }
    }
}
