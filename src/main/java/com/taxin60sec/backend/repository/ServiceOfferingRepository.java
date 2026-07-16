package com.taxin60sec.backend.repository;

import com.taxin60sec.backend.entity.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long>, JpaSpecificationExecutor<ServiceOffering> {
}
