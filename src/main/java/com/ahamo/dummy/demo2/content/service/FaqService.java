package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.Faq;
import com.ahamo.dummy.demo2.content.repository.FaqRepository;
import com.ahamo.dummy.demo2.content.dto.FaqResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FaqService {
    
    private final FaqRepository faqRepository;
    
    public Page<FaqResponse> getFaqs(int page, int limit) {
        log.info("FAQ一覧取得: page={}, limit={}", page, limit);
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Faq> faqs = faqRepository.findActiveFaqs(pageable);
        
        return faqs.map(this::convertToResponse);
    }
    
    public Page<FaqResponse> getFaqsByCategory(String category, int page, int limit) {
        log.info("カテゴリ別FAQ一覧取得: category={}, page={}, limit={}", category, page, limit);
        
        try {
            Faq.FaqCategory faqCategory = Faq.FaqCategory.valueOf(category.toUpperCase());
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<Faq> faqs = faqRepository.findActiveFaqsByCategory(faqCategory, pageable);
            
            return faqs.map(this::convertToResponse);
        } catch (IllegalArgumentException e) {
            log.warn("無効なカテゴリ: {}", category);
            return Page.empty();
        }
    }
    
    public FaqResponse getFaqById(Long id) {
        log.info("FAQ詳細取得: id={}", id);
        
        Faq faq = faqRepository.findActiveFaqById(id);
        if (faq == null) {
            log.warn("FAQが見つかりません: id={}", id);
            return null;
        }
        
        return convertToResponse(faq);
    }
    
    private FaqResponse convertToResponse(Faq faq) {
        return FaqResponse.builder()
            .id(faq.getId().toString())
            .question(faq.getQuestion())
            .answer(faq.getAnswer())
            .category(faq.getCategory().getDisplayName())
            .createdAt(faq.getCreatedAt())
            .updatedAt(faq.getUpdatedAt())
            .isActive(faq.getIsActive())
            .build();
    }
}
