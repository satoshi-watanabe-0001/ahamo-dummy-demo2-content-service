package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.config.SecurityConfig;
import com.ahamo.dummy.demo2.content.dto.CampaignResponse;
import com.ahamo.dummy.demo2.content.service.CampaignService;
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

@WebMvcTest(CampaignController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CampaignService campaignService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCampaigns_ShouldReturnSuccessResponse() throws Exception {
        CampaignResponse campaign = new CampaignResponse(
            "1",
            "春のキャンペーン",
            "お得な春のキャンペーン実施中",
            "https://example.com/image.jpg",
            "https://example.com/campaign",
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        Page<CampaignResponse> campaignPage = new PageImpl<>(List.of(campaign));
        when(campaignService.getCampaigns(anyInt(), anyInt())).thenReturn(campaignPage);

        mockMvc.perform(get("/campaigns")
                        .param("page", "1")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns").isArray())
                .andExpect(jsonPath("$.campaigns[0].id").value("1"))
                .andExpect(jsonPath("$.campaigns[0].title").value("春のキャンペーン"))
                .andExpect(jsonPath("$.campaigns[0].description").value("お得な春のキャンペーン実施中"))
                .andExpect(jsonPath("$.campaigns[0].link").value("https://example.com/campaign"))
                .andExpect(jsonPath("$.campaigns[0].isActive").value(true))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(10));
    }

    @Test
    void getCampaigns_WithDefaultParameters_ShouldReturnSuccessResponse() throws Exception {
        CampaignResponse campaign = new CampaignResponse(
            "1",
            "デフォルトキャンペーン",
            "デフォルトパラメータでのキャンペーン",
            "https://example.com/default.jpg",
            "https://example.com/default",
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        Page<CampaignResponse> campaignPage = new PageImpl<>(List.of(campaign));
        when(campaignService.getCampaigns(1, 10)).thenReturn(campaignPage);

        mockMvc.perform(get("/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns").isArray())
                .andExpect(jsonPath("$.campaigns[0].title").value("デフォルトキャンペーン"));
    }

    @Test
    void getCampaignById_ExistingId_ShouldReturnCampaign() throws Exception {
        CampaignResponse campaign = new CampaignResponse(
            "1",
            "特定キャンペーン",
            "ID指定でのキャンペーン詳細",
            "https://example.com/specific.jpg",
            "https://example.com/specific",
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        when(campaignService.getCampaignById(1L)).thenReturn(campaign);

        mockMvc.perform(get("/campaigns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("特定キャンペーン"))
                .andExpect(jsonPath("$.description").value("ID指定でのキャンペーン詳細"));
    }

    @Test
    void getCampaignById_NonExistingId_ShouldReturnNotFound() throws Exception {
        when(campaignService.getCampaignById(999L)).thenReturn(null);

        mockMvc.perform(get("/campaigns/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCampaignById_InvalidIdFormat_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/campaigns/invalid"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getCampaigns_WithPaginationParameters_ShouldReturnCorrectPage() throws Exception {
        CampaignResponse campaign = new CampaignResponse(
            "2",
            "ページネーションキャンペーン",
            "2ページ目のキャンペーン",
            "https://example.com/page2.jpg",
            "https://example.com/page2",
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );

        Page<CampaignResponse> campaignPage = new PageImpl<>(
            List.of(campaign), 
            org.springframework.data.domain.PageRequest.of(1, 5), 
            25
        );
        when(campaignService.getCampaigns(2, 5)).thenReturn(campaignPage);

        mockMvc.perform(get("/campaigns")
                        .param("page", "2")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns[0].title").value("ページネーションキャンペーン"))
                .andExpect(jsonPath("$.total").value(25))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.limit").value(5));
    }
}
