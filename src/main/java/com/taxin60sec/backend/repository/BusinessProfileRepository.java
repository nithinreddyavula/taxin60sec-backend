package com.taxin60sec.backend.repository;
import com.taxin60sec.backend.entity.BusinessProfile; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface BusinessProfileRepository extends JpaRepository<BusinessProfile,Long> { List<BusinessProfile> findByClientProfileIdAndDeletedFalse(Long clientProfileId); }
