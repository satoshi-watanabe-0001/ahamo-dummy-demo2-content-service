package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.Faq;
import com.ahamo.dummy.demo2.content.repository.FaqRepository;
import com.ahamo.dummy.demo2.content.dto.FaqResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private FaqService faqService;

    @Test
    void getFaqs_ShouldReturnPagedFaqs() {
        Faq faq = new Faq();
        faq.setId(1L);
        faq.setQuestion("テストFAQ質問");
        faq.setAnswer("テストFAQ回答");
        faq.setCategory(Faq.FaqCategory.PLAN);
        faq.setCreatedAt(LocalDateTime.now());
        faq.setUpdatedAt(LocalDateTime.now());
        faq.setIsActive(true);

        Page<Faq> faqPage = new PageImpl<>(List.of(faq));
        when(faqRepository.findActiveFaqs(any(Pageable.class))).thenReturn(faqPage);

        Page<FaqResponse> result = faqService.getFaqs(1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("1");
        assertThat(result.getContent().get(0).getQuestion()).isEqualTo("テストFAQ質問");
        assertThat(result.getContent().get(0).getAnswer()).isEqualTo("テストFAQ回答");
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("料金プラン");
        assertThat(result.getContent().get(0).isActive()).isTrue();
    }

    @Test
    void getFaqsByCategory_ValidCategory_ShouldReturnFilteredFaqs() {
        Faq faq = new Faq();
        faq.setId(2L);
        faq.setQuestion("端末FAQ質問");
        faq.setAnswer("端末FAQ回答");
        faq.setCategory(Faq.FaqCategory.DEVICE);
        faq.setCreatedAt(LocalDateTime.now());
        faq.setUpdatedAt(LocalDateTime.now());
        faq.setIsActive(true);

        Page<Faq> faqPage = new PageImpl<>(List.of(faq));
        when(faqRepository.findActiveFaqsByCategory(Faq.FaqCategory.DEVICE, PageRequest.of(0, 10))).thenReturn(faqPage);

        Page<FaqResponse> result = faqService.getFaqsByCategory("device", 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("端末");
    }

    @Test
    void getFaqsByCategory_InvalidCategory_ShouldReturnEmptyPage() {
        Page<FaqResponse> result = faqService.getFaqsByCategory("invalid", 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getFaqById_ExistingId_ShouldReturnFaq() {
        Faq faq = new Faq();
        faq.setId(1L);
        faq.setQuestion("特定FAQ質問");
        faq.setAnswer("特定FAQ回答");
        faq.setCategory(Faq.FaqCategory.SUPPORT);
        faq.setCreatedAt(LocalDateTime.now());
        faq.setUpdatedAt(LocalDateTime.now());
        faq.setIsActive(true);

        when(faqRepository.findActiveFaqById(1L)).thenReturn(faq);

        FaqResponse result = faqService.getFaqById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getQuestion()).isEqualTo("特定FAQ質問");
        assertThat(result.getAnswer()).isEqualTo("特定FAQ回答");
        assertThat(result.getCategory()).isEqualTo("サポート");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getFaqById_NonExistingId_ShouldReturnNull() {
        when(faqRepository.findActiveFaqById(999L)).thenReturn(null);

        FaqResponse result = faqService.getFaqById(999L);

        assertThat(result).isNull();
    }

    @Test
    void getFaqs_ShouldUseCorrectPageable() {
        Faq faq = new Faq();
        faq.setId(1L);
        faq.setQuestion("ページネーションテスト");
        faq.setAnswer("ページネーションのテスト");
        faq.setCategory(Faq.FaqCategory.OTHER);
        faq.setCreatedAt(LocalDateTime.now());
        faq.setUpdatedAt(LocalDateTime.now());
        faq.setIsActive(true);

        Page<Faq> faqPage = new PageImpl<>(List.of(faq));
        when(faqRepository.findActiveFaqs(PageRequest.of(1, 5))).thenReturn(faqPage);

        Page<FaqResponse> result = faqService.getFaqs(2, 5);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getQuestion()).isEqualTo("ページネーションテスト");
    }
}
