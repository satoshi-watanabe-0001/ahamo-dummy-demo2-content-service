package com.ahamo.dummy.demo2.content.service;

import com.ahamo.dummy.demo2.content.entity.Contact;
import com.ahamo.dummy.demo2.content.repository.ContactRepository;
import com.ahamo.dummy.demo2.content.dto.ContactRequest;
import com.ahamo.dummy.demo2.content.dto.ContactResponse;
import com.ahamo.dummy.demo2.content.dto.ContactCategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContactService {
    
    private final ContactRepository contactRepository;
    
    public ContactResponse submitContact(ContactRequest request) {
        log.info("お問い合わせ受付: email={}, category={}", request.getEmail(), request.getCategory());
        
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setCategory(request.getCategory());
        contact.setMessage(request.getMessage());
        contact.setStatus(Contact.ContactStatus.RECEIVED);
        contact.setEstimatedResponseTime("1-2営業日以内");
        
        Contact savedContact = contactRepository.save(contact);
        
        log.info("お問い合わせ保存完了: id={}", savedContact.getId());
        
        return new ContactResponse(
            savedContact.getId().toString(),
            savedContact.getStatus(),
            savedContact.getEstimatedResponseTime()
        );
    }
    
    @Transactional(readOnly = true)
    public List<ContactCategoryResponse> getContactCategories() {
        log.info("お問い合わせカテゴリ一覧取得");
        
        return Arrays.stream(Contact.ContactCategory.values())
            .map(category -> new ContactCategoryResponse(
                category.getCode(),
                category.getDisplayName(),
                category.getDisplayName() + "に関するお問い合わせ"
            ))
            .collect(Collectors.toList());
    }
}
