package com.example.urlshortner.dto;

import lombok.Data;

@Data
public class UrlRequest {
    private String longUrl;
    private Integer expirationDays; // optional, for later use
}