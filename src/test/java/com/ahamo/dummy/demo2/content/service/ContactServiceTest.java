package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.Contact;
import com.ahamo.dummy.demo2.content.repository.ContactRepository;
import com.ahamo.dummy.demo2.content.dto.ContactRequest;
import com.ahamo.dummy.demo2.content.dto.ContactResponse;
import com.ahamo.dummy.demo2.content.dto.ContactCategoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    void submitContact_ShouldReturnContactResponse() {
        ContactRequest request = new ContactRequest(
            "田中太郎",
            "tanaka@example.com",
            "090-1234-5678",
            Contact.ContactCategory.PLAN,
            "お問い合わせ内容です"
        );

        Contact savedContact = new Contact();
        savedContact.setId(1L);
        savedContact.setName("田中太郎");
        savedContact.setEmail("tanaka@example.com");
        savedContact.setPhone("090-1234-5678");
        savedContact.setCategory(Contact.ContactCategory.PLAN);
        savedContact.setMessage("お問い合わせ内容です");
        savedContact.setStatus(Contact.ContactStatus.RECEIVED);
        savedContact.setEstimatedResponseTime("1-2営業日以内");

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        ContactResponse result = contactService.submitContact(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getStatus()).isEqualTo(Contact.ContactStatus.RECEIVED);
        assertThat(result.getEstimatedResponseTime()).isEqualTo("1-2営業日以内");
    }

    @Test
    void submitContact_WithoutPhone_ShouldReturnContactResponse() {
        ContactRequest request = new ContactRequest(
            "田中太郎",
            "tanaka@example.com",
            null,
            Contact.ContactCategory.DEVICE,
            "技術的なお問い合わせです"
        );

        Contact savedContact = new Contact();
        savedContact.setId(2L);
        savedContact.setName("田中太郎");
        savedContact.setEmail("tanaka@example.com");
        savedContact.setPhone(null);
        savedContact.setCategory(Contact.ContactCategory.DEVICE);
        savedContact.setMessage("技術的なお問い合わせです");
        savedContact.setStatus(Contact.ContactStatus.RECEIVED);
        savedContact.setEstimatedResponseTime("1-2営業日以内");

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        ContactResponse result = contactService.submitContact(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("2");
        assertThat(result.getStatus()).isEqualTo(Contact.ContactStatus.RECEIVED);
    }

    @Test
    void getContactCategories_ShouldReturnAllCategories() {
        List<ContactCategoryResponse> result = contactService.getContactCategories();

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(Contact.ContactCategory.values().length);
        
        ContactCategoryResponse firstCategory = result.get(0);
        assertThat(firstCategory.getId()).isNotNull();
        assertThat(firstCategory.getName()).isNotNull();
        assertThat(firstCategory.getDescription()).isNotNull();
        assertThat(firstCategory.getDescription()).contains("に関するお問い合わせ");
    }

    @Test
    void getContactCategories_ShouldIncludeAllExpectedCategories() {
        List<ContactCategoryResponse> result = contactService.getContactCategories();

        List<String> categoryIds = result.stream()
            .map(ContactCategoryResponse::getId)
            .toList();

        for (Contact.ContactCategory category : Contact.ContactCategory.values()) {
            assertThat(categoryIds).contains(category.getCode());
        }
    }
}
