package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.Campaign;
import com.ahamo.dummy.demo2.content.repository.CampaignRepository;
import com.ahamo.dummy.demo2.content.dto.CampaignResponse;
import com.ahamo.dummy.demo2.content.dto.CampaignValidityResponse;
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
import java.util.Optional;

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

    @Test
    void checkCampaignValidity_ValidActiveCampaign_ShouldReturnValidResponse() {
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setTitle("テストキャンペーン");
        campaign.setDescription("テスト説明");
        campaign.setImageUrl("https://example.com/image.jpg");
        campaign.setLink("https://example.com/link");
        campaign.setCreatedAt(LocalDateTime.now().minusDays(1));
        campaign.setUpdatedAt(LocalDateTime.now());
        campaign.setIsActive(true);
        campaign.setValidFrom(LocalDateTime.now().minusDays(1));
        campaign.setValidUntil(LocalDateTime.now().plusDays(30));
        
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        CampaignValidityResponse result = campaignService.checkCampaignValidity(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCampaignId()).isEqualTo("1");
        assertThat(result.getTitle()).isEqualTo("テストキャンペーン");
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValidityStatus()).isEqualTo("VALID");
        assertThat(result.getReason()).isEqualTo("キャンペーンは有効です");
    }

    @Test
    void checkCampaignValidity_ExpiredCampaign_ShouldReturnExpiredResponse() {
        Campaign expiredCampaign = new Campaign();
        expiredCampaign.setId(1L);
        expiredCampaign.setTitle("期限切れキャンペーン");
        expiredCampaign.setDescription("テスト説明");
        expiredCampaign.setImageUrl("https://example.com/image.jpg");
        expiredCampaign.setLink("https://example.com/link");
        expiredCampaign.setCreatedAt(LocalDateTime.now().minusDays(2));
        expiredCampaign.setUpdatedAt(LocalDateTime.now());
        expiredCampaign.setIsActive(true);
        expiredCampaign.setValidFrom(LocalDateTime.now().minusDays(30));
        expiredCampaign.setValidUntil(LocalDateTime.now().minusDays(1));
        
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(expiredCampaign));

        CampaignValidityResponse result = campaignService.checkCampaignValidity(1L);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidityStatus()).isEqualTo("EXPIRED");
        assertThat(result.getReason()).isEqualTo("キャンペーンが終了しています");
    }

    @Test
    void checkCampaignValidity_NotStartedCampaign_ShouldReturnNotStartedResponse() {
        Campaign notStartedCampaign = new Campaign();
        notStartedCampaign.setId(1L);
        notStartedCampaign.setTitle("未開始キャンペーン");
        notStartedCampaign.setDescription("テスト説明");
        notStartedCampaign.setImageUrl("https://example.com/image.jpg");
        notStartedCampaign.setLink("https://example.com/link");
        notStartedCampaign.setCreatedAt(LocalDateTime.now().minusDays(1));
        notStartedCampaign.setUpdatedAt(LocalDateTime.now());
        notStartedCampaign.setIsActive(true);
        notStartedCampaign.setValidFrom(LocalDateTime.now().plusDays(1));
        notStartedCampaign.setValidUntil(LocalDateTime.now().plusDays(30));
        
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(notStartedCampaign));

        CampaignValidityResponse result = campaignService.checkCampaignValidity(1L);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidityStatus()).isEqualTo("NOT_STARTED");
        assertThat(result.getReason()).isEqualTo("キャンペーン開始前です");
    }

    @Test
    void checkCampaignValidity_InactiveCampaign_ShouldReturnInactiveResponse() {
        Campaign inactiveCampaign = new Campaign();
        inactiveCampaign.setId(1L);
        inactiveCampaign.setTitle("無効キャンペーン");
        inactiveCampaign.setDescription("テスト説明");
        inactiveCampaign.setImageUrl("https://example.com/image.jpg");
        inactiveCampaign.setLink("https://example.com/link");
        inactiveCampaign.setCreatedAt(LocalDateTime.now().minusDays(1));
        inactiveCampaign.setUpdatedAt(LocalDateTime.now());
        inactiveCampaign.setIsActive(false);
        inactiveCampaign.setValidFrom(LocalDateTime.now().minusDays(1));
        inactiveCampaign.setValidUntil(LocalDateTime.now().plusDays(30));
        
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(inactiveCampaign));

        CampaignValidityResponse result = campaignService.checkCampaignValidity(1L);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidityStatus()).isEqualTo("INACTIVE");
        assertThat(result.getReason()).isEqualTo("キャンペーンが無効化されています");
    }

    @Test
    void checkCampaignValidity_CampaignWithNullDates_ShouldReturnValidResponse() {
        Campaign campaignWithNullDates = new Campaign();
        campaignWithNullDates.setId(1L);
        campaignWithNullDates.setTitle("日付なしキャンペーン");
        campaignWithNullDates.setDescription("テスト説明");
        campaignWithNullDates.setImageUrl("https://example.com/image.jpg");
        campaignWithNullDates.setLink("https://example.com/link");
        campaignWithNullDates.setCreatedAt(LocalDateTime.now().minusDays(1));
        campaignWithNullDates.setUpdatedAt(LocalDateTime.now());
        campaignWithNullDates.setIsActive(true);
        campaignWithNullDates.setValidFrom(null);
        campaignWithNullDates.setValidUntil(null);
        
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaignWithNullDates));

        CampaignValidityResponse result = campaignService.checkCampaignValidity(1L);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValidityStatus()).isEqualTo("VALID");
        assertThat(result.getReason()).isEqualTo("キャンペーンは有効です");
        assertThat(result.getValidFrom()).isNull();
        assertThat(result.getValidUntil()).isNull();
    }

    @Test
    void checkCampaignValidity_NonExistentCampaign_ShouldReturnNotFoundResponse() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        CampaignValidityResponse result = campaignService.checkCampaignValidity(999L);

        assertThat(result).isNotNull();
        assertThat(result.getCampaignId()).isEqualTo("999");
        assertThat(result.getTitle()).isEqualTo("不明");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidityStatus()).isEqualTo("NOT_FOUND");
        assertThat(result.getReason()).isEqualTo("指定されたキャンペーンが存在しません");
    }
}
