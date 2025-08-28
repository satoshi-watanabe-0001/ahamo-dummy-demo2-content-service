package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.News;
import com.ahamo.dummy.demo2.content.repository.NewsRepository;
import com.ahamo.dummy.demo2.content.dto.NewsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NewsService {
    
    private final NewsRepository newsRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public Page<NewsResponse> getNews(int page, int limit) {
        log.info("ニュース一覧取得: page={}, limit={}", page, limit);
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<News> news = newsRepository.findPublishedNews(pageable);
        
        return news.map(this::convertToResponse);
    }
    
    public NewsResponse getNewsById(Long id) {
        log.info("ニュース詳細取得: id={}", id);
        
        News news = newsRepository.findPublishedNewsById(id);
        if (news == null) {
            log.warn("ニュースが見つかりません: id={}", id);
            return null;
        }
        
        return convertToResponse(news);
    }
    
    private NewsResponse convertToResponse(News news) {
        return new NewsResponse(
            news.getId().toString(),
            news.getTitle(),
            news.getContent(),
            news.getLink(),
            news.getPublishedDate().format(dateFormatter),
            news.getPublishedDate(),
            news.getCreatedAt(),
            news.getUpdatedAt(),
            news.getIsPublished()
        );
    }
}
