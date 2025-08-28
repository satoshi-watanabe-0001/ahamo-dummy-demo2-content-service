package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.News;
import com.ahamo.dummy.demo2.content.repository.NewsRepository;
import com.ahamo.dummy.demo2.content.dto.NewsResponse;
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
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    void getNews_ShouldReturnPagedNews() {
        News news = new News();
        news.setId(1L);
        news.setTitle("テストニュース");
        news.setContent("テスト用のニュースです");
        news.setLink("https://example.com/news");
        news.setPublishedDate(LocalDateTime.now());
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        news.setIsPublished(true);

        Page<News> newsPage = new PageImpl<>(List.of(news));
        when(newsRepository.findPublishedNews(any(Pageable.class))).thenReturn(newsPage);

        Page<NewsResponse> result = newsService.getNews(1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("1");
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("テストニュース");
        assertThat(result.getContent().get(0).getContent()).isEqualTo("テスト用のニュースです");
        assertThat(result.getContent().get(0).getIsPublished()).isTrue();
    }

    @Test
    void getNewsById_ExistingId_ShouldReturnNews() {
        News news = new News();
        news.setId(1L);
        news.setTitle("特定ニュース");
        news.setContent("ID指定のニュース");
        news.setLink("https://example.com/specific-news");
        news.setPublishedDate(LocalDateTime.now());
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        news.setIsPublished(true);

        when(newsRepository.findPublishedNewsById(1L)).thenReturn(news);

        NewsResponse result = newsService.getNewsById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getTitle()).isEqualTo("特定ニュース");
        assertThat(result.getContent()).isEqualTo("ID指定のニュース");
        assertThat(result.getIsPublished()).isTrue();
    }

    @Test
    void getNewsById_NonExistingId_ShouldReturnNull() {
        when(newsRepository.findPublishedNewsById(999L)).thenReturn(null);

        NewsResponse result = newsService.getNewsById(999L);

        assertThat(result).isNull();
    }

    @Test
    void getNews_ShouldUseCorrectPageable() {
        News news = new News();
        news.setId(1L);
        news.setTitle("ページネーションニュース");
        news.setContent("ページネーションのテスト");
        news.setLink("https://example.com/page-news");
        news.setPublishedDate(LocalDateTime.now());
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        news.setIsPublished(true);

        Page<News> newsPage = new PageImpl<>(List.of(news));
        when(newsRepository.findPublishedNews(PageRequest.of(1, 5))).thenReturn(newsPage);

        Page<NewsResponse> result = newsService.getNews(2, 5);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("ページネーションニュース");
    }
}
