package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.service.NewsService;
import com.ahamo.dummy.demo2.content.dto.NewsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("ニュース一覧API呼び出し: page={}, limit={}", page, limit);
        
        Page<NewsResponse> news = newsService.getNews(page, limit);
        
        Map<String, Object> response = Map.of(
            "news", news.getContent(),
            "total", news.getTotalElements(),
            "page", page,
            "limit", limit
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NewsResponse> getNewsById(@PathVariable String id) {
        log.info("ニュース詳細API呼び出し: id={}", id);
        
        try {
            Long newsId = Long.parseLong(id);
            NewsResponse news = newsService.getNewsById(newsId);
            
            if (news == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(news);
        } catch (NumberFormatException e) {
            log.warn("無効なニュースID: {}", id);
            return ResponseEntity.badRequest().build();
        }
    }
}
