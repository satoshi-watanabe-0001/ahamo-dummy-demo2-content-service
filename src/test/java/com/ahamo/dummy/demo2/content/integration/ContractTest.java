package com.ahamo.dummy.demo2.content.integration;

import com.ahamo.dummy.demo2.content.entity.Campaign;
import com.ahamo.dummy.demo2.content.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebMvc
@Transactional
class ContractTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("contractdb")
            .withUsername("contract")
            .withPassword("contract");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CampaignRepository campaignRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        campaignRepository.deleteAll();
    }

    @Test
    void contractTest_CampaignValidityAPI_ShouldMatchExpectedSchema() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setTitle("契約テストキャンペーン");
        campaign.setDescription("API契約テスト用");
        campaign.setImageUrl("https://example.com/contract-test.jpg");
        campaign.setLink("https://example.com/contract");
        campaign.setIsActive(true);
        campaign.setValidFrom(LocalDateTime.now().minusDays(1));
        campaign.setValidUntil(LocalDateTime.now().plusDays(30));
        
        Campaign savedCampaign = campaignRepository.save(campaign);

        mockMvc.perform(get("/campaigns/{id}/validity", savedCampaign.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.campaignId").exists())
                .andExpect(jsonPath("$.campaignId").isString())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.isValid").exists())
                .andExpect(jsonPath("$.isValid").isBoolean())
                .andExpect(jsonPath("$.validityStatus").exists())
                .andExpect(jsonPath("$.validityStatus").isString())
                .andExpect(jsonPath("$.validFrom").exists())
                .andExpect(jsonPath("$.validUntil").exists())
                .andExpect(jsonPath("$.reason").exists())
                .andExpect(jsonPath("$.reason").isString());
    }

    @Test
    void contractTest_CampaignListAPI_ShouldMatchExpectedPaginationSchema() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setTitle("ページネーション契約テスト");
        campaign.setDescription("ページネーション契約テスト用");
        campaign.setImageUrl("https://example.com/pagination-test.jpg");
        campaign.setLink("https://example.com/pagination");
        campaign.setIsActive(true);
        campaignRepository.save(campaign);

        mockMvc.perform(get("/campaigns")
                .param("page", "1")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.campaigns").exists())
                .andExpect(jsonPath("$.campaigns").isArray())
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.limit").exists())
                .andExpect(jsonPath("$.limit").isNumber())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    void contractTest_CampaignDetailAPI_ShouldMatchExpectedSchema() throws Exception {
        Campaign campaign = new Campaign();
        campaign.setTitle("詳細契約テスト");
        campaign.setDescription("詳細契約テスト用");
        campaign.setImageUrl("https://example.com/detail-test.jpg");
        campaign.setLink("https://example.com/detail");
        campaign.setIsActive(true);
        campaign.setValidFrom(LocalDateTime.now().minusDays(1));
        campaign.setValidUntil(LocalDateTime.now().plusDays(30));
        
        Campaign savedCampaign = campaignRepository.save(campaign);

        mockMvc.perform(get("/campaigns/{id}", savedCampaign.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").isString())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.description").isString())
                .andExpect(jsonPath("$.imageUrl").exists())
                .andExpect(jsonPath("$.imageUrl").isString())
                .andExpect(jsonPath("$.link").exists())
                .andExpect(jsonPath("$.link").isString())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.isActive").exists())
                .andExpect(jsonPath("$.isActive").isBoolean());
    }

    @Test
    void contractTest_ErrorResponse_ShouldMatchExpectedSchema() throws Exception {
        mockMvc.perform(get("/campaigns/{id}/validity", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void contractTest_ValidityStatusEnum_ShouldOnlyReturnValidValues() throws Exception {
        Campaign validCampaign = createCampaignWithStatus(true, 
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        Campaign savedValid = campaignRepository.save(validCampaign);

        Campaign expiredCampaign = createCampaignWithStatus(true, 
            LocalDateTime.now().minusDays(30), LocalDateTime.now().minusDays(1));
        Campaign savedExpired = campaignRepository.save(expiredCampaign);

        Campaign notStartedCampaign = createCampaignWithStatus(true, 
            LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30));
        Campaign savedNotStarted = campaignRepository.save(notStartedCampaign);

        Campaign inactiveCampaign = createCampaignWithStatus(false, 
            LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        Campaign savedInactive = campaignRepository.save(inactiveCampaign);

        mockMvc.perform(get("/campaigns/{id}/validity", savedValid.getId()))
                .andExpect(jsonPath("$.validityStatus").value("VALID"));

        mockMvc.perform(get("/campaigns/{id}/validity", savedExpired.getId()))
                .andExpect(jsonPath("$.validityStatus").value("EXPIRED"));

        mockMvc.perform(get("/campaigns/{id}/validity", savedNotStarted.getId()))
                .andExpect(jsonPath("$.validityStatus").value("NOT_STARTED"));

        mockMvc.perform(get("/campaigns/{id}/validity", savedInactive.getId()))
                .andExpect(jsonPath("$.validityStatus").value("INACTIVE"));

        mockMvc.perform(get("/campaigns/{id}/validity", 999999L))
                .andExpect(jsonPath("$.validityStatus").value("NOT_FOUND"));
    }

    private Campaign createCampaignWithStatus(boolean isActive, LocalDateTime validFrom, LocalDateTime validUntil) {
        Campaign campaign = new Campaign();
        campaign.setTitle("ステータステスト");
        campaign.setDescription("ステータステスト用");
        campaign.setImageUrl("https://example.com/status-test.jpg");
        campaign.setLink("https://example.com/status");
        campaign.setIsActive(isActive);
        campaign.setValidFrom(validFrom);
        campaign.setValidUntil(validUntil);
        return campaign;
    }
}
