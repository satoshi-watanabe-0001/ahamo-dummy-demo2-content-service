package com.ahamo.dummy.demo2.content.repository;

import com.ahamo.dummy.demo2.content.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    @Query("SELECT n FROM News n WHERE n.isPublished = true ORDER BY n.publishedDate DESC")
    Page<News> findPublishedNews(Pageable pageable);
    
    @Query("SELECT n FROM News n WHERE n.isPublished = true AND n.id = :id")
    News findPublishedNewsById(Long id);
}
