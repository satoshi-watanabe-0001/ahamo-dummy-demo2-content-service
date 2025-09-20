package com.ahamo.dummy.demo2.content.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignValidityResponse {
    private String campaignId;
    private String title;
    @JsonProperty("isValid")
    private boolean isValid;
    private String validityStatus;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String reason;
}
