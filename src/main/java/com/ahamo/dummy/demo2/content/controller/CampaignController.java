package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.service.CampaignService;
import com.ahamo.dummy.demo2.content.dto.CampaignResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Slf4j
public class CampaignController {
    
    private final CampaignService campaignService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCampaigns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("キャンペーン一覧API呼び出し: page={}, limit={}", page, limit);
        
        Page<CampaignResponse> campaigns = campaignService.getCampaigns(page, limit);
        
        Map<String, Object> response = Map.of(
            "campaigns", campaigns.getContent(),
            "total", campaigns.getTotalElements(),
            "page", page,
            "limit", limit
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaignById(@PathVariable String id) {
        log.info("キャンペーン詳細API呼び出し: id={}", id);
        
        try {
            Long campaignId = Long.parseLong(id);
            CampaignResponse campaign = campaignService.getCampaignById(campaignId);
            
            if (campaign == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(campaign);
        } catch (NumberFormatException e) {
            log.warn("無効なキャンペーンID: {}", id);
            return ResponseEntity.badRequest().build();
        }
    }
}
