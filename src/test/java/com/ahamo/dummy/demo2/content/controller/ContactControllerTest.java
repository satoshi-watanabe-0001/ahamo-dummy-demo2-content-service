package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.config.SecurityConfig;
import com.ahamo.dummy.demo2.content.dto.ContactRequest;
import com.ahamo.dummy.demo2.content.dto.ContactResponse;
import com.ahamo.dummy.demo2.content.dto.ContactCategoryResponse;
import com.ahamo.dummy.demo2.content.entity.Contact;
import com.ahamo.dummy.demo2.content.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitContact_ValidRequest_ShouldReturnSuccessResponse() throws Exception {
        ContactRequest request = new ContactRequest(
            "田中太郎",
            "tanaka@example.com",
            "090-1234-5678",
            Contact.ContactCategory.PLAN,
            "お問い合わせ内容です"
        );

        ContactResponse response = new ContactResponse(
            "1",
            Contact.ContactStatus.RECEIVED,
            "1-2営業日以内"
        );

        when(contactService.submitContact(any(ContactRequest.class))).thenReturn(response);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.estimatedResponseTime").value("1-2営業日以内"));
    }

    @Test
    void submitContact_MissingName_ShouldReturnBadRequest() throws Exception {
        ContactRequest request = new ContactRequest(
            "",
            "tanaka@example.com",
            "090-1234-5678",
            Contact.ContactCategory.PLAN,
            "お問い合わせ内容です"
        );

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitContact_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        ContactRequest request = new ContactRequest(
            "田中太郎",
            "invalid-email",
            "090-1234-5678",
            Contact.ContactCategory.PLAN,
            "お問い合わせ内容です"
        );

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitContact_MissingCategory_ShouldReturnBadRequest() throws Exception {
        ContactRequest request = new ContactRequest(
            "田中太郎",
            "tanaka@example.com",
            "090-1234-5678",
            null,
            "お問い合わせ内容です"
        );

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitContact_MissingMessage_ShouldReturnBadRequest() throws Exception {
        ContactRequest request = new ContactRequest(
            "田中太郎",
            "tanaka@example.com",
            "090-1234-5678",
            Contact.ContactCategory.PLAN,
            ""
        );

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getContactCategories_ShouldReturnSuccessResponse() throws Exception {
        List<ContactCategoryResponse> categories = List.of(
            new ContactCategoryResponse("plan", "料金プラン", "料金プランに関するお問い合わせ"),
            new ContactCategoryResponse("device", "端末", "端末に関するお問い合わせ"),
            new ContactCategoryResponse("application", "申し込み", "申し込みに関するお問い合わせ")
        );

        when(contactService.getContactCategories()).thenReturn(categories);

        mockMvc.perform(get("/contact/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("plan"))
                .andExpect(jsonPath("$[0].name").value("料金プラン"))
                .andExpect(jsonPath("$[0].description").value("料金プランに関するお問い合わせ"))
                .andExpect(jsonPath("$[1].id").value("device"))
                .andExpect(jsonPath("$[2].id").value("application"));
    }


    @Test
    void submitContact_WithOptionalPhone_ShouldReturnSuccessResponse() throws Exception {
        ContactRequest request = new ContactRequest(
            "田中太郎",
            "tanaka@example.com",
            null,
            Contact.ContactCategory.PLAN,
            "電話番号なしのお問い合わせです"
        );

        ContactResponse response = new ContactResponse(
            "2",
            Contact.ContactStatus.RECEIVED,
            "1-2営業日以内"
        );

        when(contactService.submitContact(any(ContactRequest.class))).thenReturn(response);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2"));
    }
}
