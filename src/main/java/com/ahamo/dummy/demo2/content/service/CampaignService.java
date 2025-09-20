package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.Campaign;
import com.ahamo.dummy.demo2.content.repository.CampaignRepository;
import com.ahamo.dummy.demo2.content.dto.CampaignResponse;
import com.ahamo.dummy.demo2.content.dto.CampaignValidityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CampaignService {
    
    private final CampaignRepository campaignRepository;
    
    public Page<CampaignResponse> getCampaigns(int page, int limit) {
        log.info("キャンペーン一覧取得: page={}, limit={}", page, limit);
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Campaign> campaigns = campaignRepository.findActiveCampaigns(pageable);
        
        return campaigns.map(this::convertToResponse);
    }
    
    public CampaignResponse getCampaignById(Long id) {
        log.info("キャンペーン詳細取得: id={}", id);
        
        Campaign campaign = campaignRepository.findActiveCampaignById(id);
        if (campaign == null) {
            log.warn("キャンペーンが見つかりません: id={}", id);
            return null;
        }
        
        return convertToResponse(campaign);
    }
    
    public CampaignValidityResponse checkCampaignValidity(Long id) {
        log.info("キャンペーン有効性チェック: id={}", id);
        
        Campaign campaign = campaignRepository.findById(id).orElse(null);
        if (campaign == null) {
            log.warn("キャンペーンが見つかりません: id={}", id);
            return new CampaignValidityResponse(
                id.toString(),
                "不明",
                false,
                "NOT_FOUND",
                null,
                null,
                "指定されたキャンペーンが存在しません"
            );
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean isValid = campaign.getIsActive() && 
                         (campaign.getValidFrom() == null || !now.isBefore(campaign.getValidFrom())) &&
                         (campaign.getValidUntil() == null || !now.isAfter(campaign.getValidUntil()));
        
        String status;
        String reason;
        
        if (!campaign.getIsActive()) {
            status = "INACTIVE";
            reason = "キャンペーンが無効化されています";
        } else if (campaign.getValidFrom() != null && now.isBefore(campaign.getValidFrom())) {
            status = "NOT_STARTED";
            reason = "キャンペーン開始前です";
        } else if (campaign.getValidUntil() != null && now.isAfter(campaign.getValidUntil())) {
            status = "EXPIRED";
            reason = "キャンペーンが終了しています";
        } else {
            status = "VALID";
            reason = "キャンペーンは有効です";
        }
        
        return new CampaignValidityResponse(
            campaign.getId().toString(),
            campaign.getTitle(),
            isValid,
            status,
            campaign.getValidFrom(),
            campaign.getValidUntil(),
            reason
        );
    }
    
    private CampaignResponse convertToResponse(Campaign campaign) {
        return new CampaignResponse(
            campaign.getId().toString(),
            campaign.getTitle(),
            campaign.getDescription(),
            campaign.getImageUrl(),
            campaign.getLink(),
            campaign.getCreatedAt(),
            campaign.getUpdatedAt(),
            campaign.getIsActive()
        );
    }
}
