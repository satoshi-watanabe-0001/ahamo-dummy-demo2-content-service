package com.ahamo.dummy.demo2.content.repository;

import com.ahamo.dummy.demo2.content.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    
    @Query("SELECT f FROM Faq f WHERE f.isActive = true ORDER BY f.createdAt DESC")
    Page<Faq> findActiveFaqs(Pageable pageable);
    
    @Query("SELECT f FROM Faq f WHERE f.isActive = true AND f.category = :category ORDER BY f.createdAt DESC")
    Page<Faq> findActiveFaqsByCategory(@Param("category") Faq.FaqCategory category, Pageable pageable);
    
    @Query("SELECT f FROM Faq f WHERE f.isActive = true AND f.id = :id")
    Faq findActiveFaqById(Long id);
}
