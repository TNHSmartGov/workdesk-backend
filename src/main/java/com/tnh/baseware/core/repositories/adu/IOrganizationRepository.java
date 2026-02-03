package com.tnh.baseware.core.repositories.adu;

import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IOrganizationRepository extends IGenericRepository<Organization, UUID> {

    @EntityGraph(attributePaths = { "parent" })
    @Query("SELECT o FROM Organization o")
    List<Organization> findAllWithParent();

    Optional<Organization> findByIsSystem(Boolean isSystem);

    /**
     * Check if childOrgId is a descendant (child, grandchild, etc.) of parentOrgId
     * Uses recursive CTE to traverse organization hierarchy
     * 
     * @param parentOrgId Parent organization ID
     * @param childOrgId  Child/descendant organization ID to check
     * @return true if childOrgId is under parentOrgId in the hierarchy
     */
    @Query(value = """
            WITH RECURSIVE org_tree AS (
                -- Base case: direct children
                SELECT id, parent_id, 1 as level
                FROM organization
                WHERE parent_id = :parentOrgId

                UNION ALL

                -- Recursive case: children of children
                SELECT o.id, o.parent_id, ot.level + 1
                FROM organization o
                INNER JOIN org_tree ot ON o.parent_id = ot.id
                WHERE ot.level < 100  -- Prevent infinite loop
            )
            SELECT EXISTS(SELECT 1 FROM org_tree WHERE id = :childOrgId)
            """, nativeQuery = true)
    boolean isDescendantOf(UUID parentOrgId, UUID childOrgId);
}
