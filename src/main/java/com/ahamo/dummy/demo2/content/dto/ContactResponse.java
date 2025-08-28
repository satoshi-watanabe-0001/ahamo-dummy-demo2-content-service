package com.ahamo.dummy.demo2.content.dto;

import com.ahamo.dummy.demo2.content.entity.Contact.ContactStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    private String id;
    private ContactStatus status;
    private String estimatedResponseTime;
}
