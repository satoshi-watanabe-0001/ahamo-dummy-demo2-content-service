package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.config.SecurityConfig;
import com.ahamo.dummy.demo2.content.dto.NewsResponse;
import com.ahamo.dummy.demo2.content.service.NewsService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NewsController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsService newsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getNews_ShouldReturnSuccessResponse() throws Exception {
        NewsResponse news = new NewsResponse(
            "1",
            "重要なお知らせ",
            "サービス更新に関するお知らせです",
            "https://example.com/news",
            "2024-01-01",
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        Page<NewsResponse> newsPage = new PageImpl<>(List.of(news));
        when(newsService.getNews(anyInt(), anyInt())).thenReturn(newsPage);

        mockMvc.perform(get("/news")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news").isArray())
                .andExpect(jsonPath("$.news[0].id").value("1"))
                .andExpect(jsonPath("$.news[0].title").value("重要なお知らせ"))
                .andExpect(jsonPath("$.news[0].content").value("サービス更新に関するお知らせです"))
                .andExpect(jsonPath("$.news[0].link").value("https://example.com/news"))
                .andExpect(jsonPath("$.news[0].isPublished").value(true))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    void getNews_WithDefaultParameters_ShouldReturnSuccessResponse() throws Exception {
        NewsResponse news = new NewsResponse(
            "1",
            "デフォルトニュース",
            "デフォルトパラメータでのニュース",
            "https://example.com/default-news",
            "2024-01-01",
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        Page<NewsResponse> newsPage = new PageImpl<>(List.of(news));
        when(newsService.getNews(1, 10)).thenReturn(newsPage);

        mockMvc.perform(get("/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news").isArray())
                .andExpect(jsonPath("$.news[0].title").value("デフォルトニュース"));
    }

    @Test
    void getNewsById_ExistingId_ShouldReturnNews() throws Exception {
        NewsResponse news = new NewsResponse(
            "1",
            "特定ニュース",
            "ID指定でのニュース詳細",
            "https://example.com/specific-news",
            "2024-01-01",
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        when(newsService.getNewsById(1L)).thenReturn(news);

        mockMvc.perform(get("/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("特定ニュース"))
                .andExpect(jsonPath("$.content").value("ID指定でのニュース詳細"));
    }

    @Test
    void getNewsById_NonExistingId_ShouldReturnNotFound() throws Exception {
        when(newsService.getNewsById(999L)).thenReturn(null);

        mockMvc.perform(get("/news/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNewsById_InvalidIdFormat_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/news/invalid"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getNews_WithPaginationParameters_ShouldReturnCorrectPage() throws Exception {
        NewsResponse news = new NewsResponse(
            "2",
            "ページネーションニュース",
            "2ページ目のニュース",
            "https://example.com/page2-news",
            "2024-01-01",
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        Page<NewsResponse> newsPage = new PageImpl<>(
            List.of(news), 
            org.springframework.data.domain.PageRequest.of(1, 5), 
            25
        );
        when(newsService.getNews(2, 5)).thenReturn(newsPage);

        mockMvc.perform(get("/news")
                        .param("page", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news[0].title").value("ページネーションニュース"))
                .andExpect(jsonPath("$.total").value(25))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.limit").value(5));
    }
}
