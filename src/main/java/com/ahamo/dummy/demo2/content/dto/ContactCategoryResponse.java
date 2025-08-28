package com.ahamo.dummy.demo2.content.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactCategoryResponse {
    private String id;
    private String name;
    private String description;
}
