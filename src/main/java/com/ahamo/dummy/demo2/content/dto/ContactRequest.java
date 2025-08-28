package com.ahamo.dummy.demo2.content.dto;

import com.ahamo.dummy.demo2.content.entity.Contact.ContactCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {
    
    @NotBlank(message = "名前は必須です")
    private String name;
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;
    
    private String phone;
    
    @NotNull(message = "カテゴリは必須です")
    private ContactCategory category;
    
    @NotBlank(message = "メッセージは必須です")
    private String message;
}
