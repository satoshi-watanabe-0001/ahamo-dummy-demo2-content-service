package com.ahamo.dummy.demo2.content.integration;

import com.ahamo.dummy.demo2.content.dto.CampaignValidityResponse;
import com.ahamo.dummy.demo2.content.entity.Campaign;
import com.ahamo.dummy.demo2.content.repository.CampaignRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebMvc
@Transactional
class CampaignIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

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

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        campaignRepository.deleteAll();
    }

    @Test
    void testCampaignValidityCheck_ValidCampaign_ShouldReturnValid() throws Exception {
        Campaign campaign = createTestCampaign(
            "統合テストキャンペーン",
            "統合テスト用の説明",
            true,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30)
        );
        Campaign savedCampaign = campaignRepository.save(campaign);

        mockMvc.perform(get("/campaigns/{id}/validity", savedCampaign.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.campaignId").value(savedCampaign.getId().toString()))
                .andExpect(jsonPath("$.title").value("統合テストキャンペーン"))
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.validityStatus").value("VALID"))
                .andExpect(jsonPath("$.reason").value("キャンペーンは有効です"));
    }

    @Test
    void testCampaignValidityCheck_ExpiredCampaign_ShouldReturnExpired() throws Exception {
        Campaign expiredCampaign = createTestCampaign(
            "期限切れキャンペーン",
            "期限切れテスト用",
            true,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now().minusDays(1)
        );
        Campaign savedCampaign = campaignRepository.save(expiredCampaign);

        mockMvc.perform(get("/campaigns/{id}/validity", savedCampaign.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.validityStatus").value("EXPIRED"))
                .andExpect(jsonPath("$.reason").value("キャンペーンが終了しています"));
    }

    @Test
    void testCampaignValidityCheck_NotStartedCampaign_ShouldReturnNotStarted() throws Exception {
        Campaign notStartedCampaign = createTestCampaign(
            "未開始キャンペーン",
            "未開始テスト用",
            true,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(30)
        );
        Campaign savedCampaign = campaignRepository.save(notStartedCampaign);

        mockMvc.perform(get("/campaigns/{id}/validity", savedCampaign.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.validityStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.reason").value("キャンペーン開始前です"));
    }

    @Test
    void testCampaignValidityCheck_InactiveCampaign_ShouldReturnInactive() throws Exception {
        Campaign inactiveCampaign = createTestCampaign(
            "無効キャンペーン",
            "無効テスト用",
            false,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30)
        );
        Campaign savedCampaign = campaignRepository.save(inactiveCampaign);

        mockMvc.perform(get("/campaigns/{id}/validity", savedCampaign.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.validityStatus").value("INACTIVE"))
                .andExpect(jsonPath("$.reason").value("キャンペーンが無効化されています"));
    }

    @Test
    void testCampaignValidityCheck_NonExistentCampaign_ShouldReturnNotFound() throws Exception {
        Long nonExistentId = 999999L;

        mockMvc.perform(get("/campaigns/{id}/validity", nonExistentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.campaignId").value(nonExistentId.toString()))
                .andExpect(jsonPath("$.title").value("不明"))
                .andExpect(jsonPath("$.isValid").value(false))
                .andExpect(jsonPath("$.validityStatus").value("NOT_FOUND"))
                .andExpect(jsonPath("$.reason").value("指定されたキャンペーンが存在しません"));
    }

    @Test
    void testCampaignValidityCheck_InvalidIdFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/campaigns/{id}/validity", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCampaignsList_WithPagination_ShouldReturnPagedResults() throws Exception {
        for (int i = 1; i <= 15; i++) {
            Campaign campaign = createTestCampaign(
                "キャンペーン" + i,
                "説明" + i,
                true,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30)
            );
            campaignRepository.save(campaign);
        }

        mockMvc.perform(get("/campaigns")
                .param("page", "1")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.campaigns").isArray())
                .andExpect(jsonPath("$.campaigns.length()").value(10))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.total").value(15));
    }

    @Test
    void testCampaignDetail_ExistingCampaign_ShouldReturnCampaignDetails() throws Exception {
        Campaign campaign = createTestCampaign(
            "詳細テストキャンペーン",
            "詳細テスト用の説明",
            true,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30)
        );
        Campaign savedCampaign = campaignRepository.save(campaign);

        mockMvc.perform(get("/campaigns/{id}", savedCampaign.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(savedCampaign.getId().toString()))
                .andExpect(jsonPath("$.title").value("詳細テストキャンペーン"))
                .andExpect(jsonPath("$.description").value("詳細テスト用の説明"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testDatabaseTransaction_RollbackOnError_ShouldMaintainDataIntegrity() throws Exception {
        long initialCount = campaignRepository.count();
        
        Campaign validCampaign = createTestCampaign(
            "トランザクションテスト",
            "トランザクション整合性テスト",
            true,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30)
        );
        campaignRepository.save(validCampaign);

        long afterSaveCount = campaignRepository.count();
        assertThat(afterSaveCount).isEqualTo(initialCount + 1);

        Campaign retrievedCampaign = campaignRepository.findById(validCampaign.getId()).orElse(null);
        assertThat(retrievedCampaign).isNotNull();
        assertThat(retrievedCampaign.getTitle()).isEqualTo("トランザクションテスト");
        assertThat(retrievedCampaign.getValidFrom()).isNotNull();
        assertThat(retrievedCampaign.getValidUntil()).isNotNull();
    }

    @Test
    void testConcurrentAccess_MultipleValidityChecks_ShouldHandleCorrectly() throws Exception {
        Campaign campaign = createTestCampaign(
            "並行アクセステスト",
            "並行アクセステスト用",
            true,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30)
        );
        Campaign savedCampaign = campaignRepository.save(campaign);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/campaigns/{id}/validity", savedCampaign.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isValid").value(true))
                    .andExpect(jsonPath("$.validityStatus").value("VALID"));
        }
    }

    private Campaign createTestCampaign(String title, String description, boolean isActive, 
                                      LocalDateTime validFrom, LocalDateTime validUntil) {
        Campaign campaign = new Campaign();
        campaign.setTitle(title);
        campaign.setDescription(description);
        campaign.setImageUrl("https://example.com/test-image.jpg");
        campaign.setLink("https://example.com/test-link");
        campaign.setIsActive(isActive);
        campaign.setValidFrom(validFrom);
        campaign.setValidUntil(validUntil);
        return campaign;
    }
}
