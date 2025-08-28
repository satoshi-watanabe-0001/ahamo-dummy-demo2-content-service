package com.ahamo.dummy.demo2.content.repository;

import com.ahamo.dummy.demo2.content.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    Page<Campaign> findActiveCampaigns(Pageable pageable);
    
    @Query("SELECT c FROM Campaign c WHERE c.isActive = true AND c.id = :id")
    Campaign findActiveCampaignById(Long id);
}
