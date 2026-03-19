package com.example.urlshortner.service;

import com.example.urlshortner.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickService {

    private final UrlRepository urlRepository;

    @Async
    public void incrementClickCount(String shortCode) {
        urlRepository.findByShortCode(shortCode).ifPresent(url -> {
            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);
        });
    }
}
