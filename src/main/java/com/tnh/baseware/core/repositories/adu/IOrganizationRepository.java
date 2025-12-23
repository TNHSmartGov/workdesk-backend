package com.tnh.baseware.core.repositories.adu;

import com.tnh.baseware.core.entities.adu.Organization;
import com.tnh.baseware.core.repositories.IGenericRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IOrganizationRepository extends IGenericRepository<Organization, UUID> {

    @EntityGraph(attributePaths = { "parent" })
    @Query("SELECT o FROM Organization o")
    List<Organization> findAllWithParent();

//    List<Organization> findAllByUserOrganizations_Users_Id(UUID userId);
}
