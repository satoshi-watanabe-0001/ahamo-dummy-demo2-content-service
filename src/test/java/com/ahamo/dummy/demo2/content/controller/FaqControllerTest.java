package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.config.SecurityConfig;
import com.ahamo.dummy.demo2.content.dto.FaqResponse;
import com.ahamo.dummy.demo2.content.service.FaqService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FaqController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class FaqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaqService faqService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFaqs_ShouldReturnSuccessResponse() throws Exception {
        FaqResponse faq = FaqResponse.builder()
            .id("1")
            .question("ahamoの料金プランについて教えてください")
            .answer("ahamoは月額2,970円（税込）で20GBまで利用できます")
            .category("料金プラン")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isActive(true)
            .build();

        Page<FaqResponse> faqPage = new PageImpl<>(List.of(faq));
        when(faqService.getFaqs(anyInt(), anyInt())).thenReturn(faqPage);

        mockMvc.perform(get("/faq")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faqs").isArray())
                .andExpect(jsonPath("$.faqs[0].id").value("1"))
                .andExpect(jsonPath("$.faqs[0].question").value("ahamoの料金プランについて教えてください"))
                .andExpect(jsonPath("$.faqs[0].answer").value("ahamoは月額2,970円（税込）で20GBまで利用できます"))
                .andExpect(jsonPath("$.faqs[0].category").value("料金プラン"))
                .andExpect(jsonPath("$.faqs[0].active").value(true))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.category").value("all"));
    }

    @Test
    void getFaqs_WithDefaultParameters_ShouldReturnSuccessResponse() throws Exception {
        FaqResponse faq = FaqResponse.builder()
            .id("1")
            .question("デフォルトFAQ")
            .answer("デフォルトパラメータでのFAQ")
            .category("サポート")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isActive(true)
            .build();

        Page<FaqResponse> faqPage = new PageImpl<>(List.of(faq));
        when(faqService.getFaqs(1, 10)).thenReturn(faqPage);

        mockMvc.perform(get("/faq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faqs").isArray())
                .andExpect(jsonPath("$.faqs[0].question").value("デフォルトFAQ"));
    }

    @Test
    void getFaqs_WithCategory_ShouldReturnFilteredResponse() throws Exception {
        FaqResponse faq = FaqResponse.builder()
            .id("2")
            .question("端末の設定方法を教えてください")
            .answer("設定アプリから「モバイル通信」を選択してください")
            .category("端末")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isActive(true)
            .build();

        Page<FaqResponse> faqPage = new PageImpl<>(List.of(faq));
        when(faqService.getFaqsByCategory(anyString(), anyInt(), anyInt())).thenReturn(faqPage);

        mockMvc.perform(get("/faq")
                        .param("page", "1")
                        .param("limit", "10")
                        .param("category", "device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faqs").isArray())
                .andExpect(jsonPath("$.faqs[0].category").value("端末"))
                .andExpect(jsonPath("$.category").value("device"));
    }

    @Test
    void getFaqById_ExistingId_ShouldReturnFaq() throws Exception {
        FaqResponse faq = FaqResponse.builder()
            .id("1")
            .question("特定のFAQ質問")
            .answer("特定のFAQ回答")
            .category("サポート")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isActive(true)
            .build();

        when(faqService.getFaqById(1L)).thenReturn(faq);

        mockMvc.perform(get("/faq/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.question").value("特定のFAQ質問"))
                .andExpect(jsonPath("$.answer").value("特定のFAQ回答"));
    }

    @Test
    void getFaqById_NonExistingId_ShouldReturnNotFound() throws Exception {
        when(faqService.getFaqById(999L)).thenReturn(null);

        mockMvc.perform(get("/faq/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFaqById_InvalidIdFormat_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/faq/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFaqs_WithPaginationParameters_ShouldReturnCorrectPage() throws Exception {
        FaqResponse faq = FaqResponse.builder()
            .id("2")
            .question("ページネーションFAQ")
            .answer("2ページ目のFAQ")
            .category("その他")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isActive(true)
            .build();

        Page<FaqResponse> faqPage = new PageImpl<>(
            List.of(faq), 
            org.springframework.data.domain.PageRequest.of(1, 5), 
            25
        );
        when(faqService.getFaqs(2, 5)).thenReturn(faqPage);

        mockMvc.perform(get("/faq")
                        .param("page", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faqs[0].question").value("ページネーションFAQ"))
                .andExpect(jsonPath("$.total").value(25))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.limit").value(5));
    }
}
