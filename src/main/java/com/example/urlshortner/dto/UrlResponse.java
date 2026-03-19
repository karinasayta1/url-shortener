package com.example.urlshortner.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UrlResponse {
    private String shortCode;
    private String shortUrl;   // e.g., http://localhost:8080/abc123
    private String longUrl;
    private LocalDateTime createdAt;
}