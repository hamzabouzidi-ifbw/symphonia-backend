package com.example.licenses.Repositories;

import com.example.licenses.Entities.LicenceDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;



@Repository
public interface LicenceDefinitionRepository extends JpaRepository<LicenceDefinition, UUID> {}