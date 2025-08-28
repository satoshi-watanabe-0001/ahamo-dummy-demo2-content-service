package com.ahamo.dummy.demo2.content.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsResponse {
    private String id;
    private String title;
    private String content;
    private String link;
    private String date;
    private LocalDateTime publishedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublished;
}
