package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.service.FaqService;
import com.ahamo.dummy.demo2.content.dto.FaqResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/faq")
@RequiredArgsConstructor
@Slf4j
public class FaqController {
    
    private final FaqService faqService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFaqs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category) {
        
        log.info("FAQ一覧API呼び出し: page={}, limit={}, category={}", page, limit, category);
        
        Page<FaqResponse> faqs;
        if (category != null && !category.trim().isEmpty()) {
            faqs = faqService.getFaqsByCategory(category, page, limit);
        } else {
            faqs = faqService.getFaqs(page, limit);
        }
        
        Map<String, Object> response = Map.of(
            "faqs", faqs.getContent(),
            "total", faqs.getTotalElements(),
            "page", page,
            "limit", limit,
            "category", category != null ? category : "all"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FaqResponse> getFaqById(@PathVariable String id) {
        log.info("FAQ詳細API呼び出し: id={}", id);
        
        try {
            Long faqId = Long.parseLong(id);
            FaqResponse faq = faqService.getFaqById(faqId);
            
            if (faq == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(faq);
        } catch (NumberFormatException e) {
            log.warn("無効なFAQ ID: {}", id);
            return ResponseEntity.badRequest().build();
        }
    }
}
