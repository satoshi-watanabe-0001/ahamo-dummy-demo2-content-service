package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.Campaign;
import com.ahamo.dummy.demo2.content.repository.CampaignRepository;
import com.ahamo.dummy.demo2.content.dto.CampaignResponse;
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
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignService campaignService;

    @Test
    void getCampaigns_ShouldReturnPagedCampaigns() {
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setTitle("テストキャンペーン");
        campaign.setDescription("テスト用のキャンペーンです");
        campaign.setImageUrl("https://example.com/test.jpg");
        campaign.setLink("https://example.com/test");
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        campaign.setIsActive(true);

        Page<Campaign> campaignPage = new PageImpl<>(List.of(campaign));
        when(campaignRepository.findActiveCampaigns(any(Pageable.class))).thenReturn(campaignPage);

        Page<CampaignResponse> result = campaignService.getCampaigns(1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("1");
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("テストキャンペーン");
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("テスト用のキャンペーンです");
        assertThat(result.getContent().get(0).getIsActive()).isTrue();
    }

    @Test
    void getCampaignById_ExistingId_ShouldReturnCampaign() {
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setTitle("特定キャンペーン");
        campaign.setDescription("ID指定のキャンペーン");
        campaign.setImageUrl("https://example.com/specific.jpg");
        campaign.setLink("https://example.com/specific");
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        campaign.setIsActive(true);

        when(campaignRepository.findActiveCampaignById(1L)).thenReturn(campaign);

        CampaignResponse result = campaignService.getCampaignById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getTitle()).isEqualTo("特定キャンペーン");
        assertThat(result.getDescription()).isEqualTo("ID指定のキャンペーン");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void getCampaignById_NonExistingId_ShouldReturnNull() {
        when(campaignRepository.findActiveCampaignById(999L)).thenReturn(null);

        CampaignResponse result = campaignService.getCampaignById(999L);

        assertThat(result).isNull();
    }

    @Test
    void getCampaigns_ShouldUseCorrectPageable() {
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setTitle("ページネーションテスト");
        campaign.setDescription("ページネーションのテスト");
        campaign.setImageUrl("https://example.com/page.jpg");
        campaign.setLink("https://example.com/page");
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        campaign.setIsActive(true);

        Page<Campaign> campaignPage = new PageImpl<>(List.of(campaign));
        when(campaignRepository.findActiveCampaigns(PageRequest.of(1, 5))).thenReturn(campaignPage);

        Page<CampaignResponse> result = campaignService.getCampaigns(2, 5);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("ページネーションテスト");
    }
}
