package com.ahamo.dummy.demo2.content.controller;

import com.ahamo.dummy.demo2.content.service.ContactService;
import com.ahamo.dummy.demo2.content.dto.ContactRequest;
import com.ahamo.dummy.demo2.content.dto.ContactResponse;
import com.ahamo.dummy.demo2.content.dto.ContactCategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ContactController {
    
    private final ContactService contactService;
    
    @PostMapping
    public ResponseEntity<ContactResponse> submitContact(@Valid @RequestBody ContactRequest request) {
        log.info("お問い合わせAPI呼び出し: email={}, category={}", request.getEmail(), request.getCategory());
        
        ContactResponse response = contactService.submitContact(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/categories")
    public ResponseEntity<List<ContactCategoryResponse>> getContactCategories() {
        log.info("お問い合わせカテゴリAPI呼び出し");
        
        List<ContactCategoryResponse> categories = contactService.getContactCategories();
        return ResponseEntity.ok(categories);
    }
}
