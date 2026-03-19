package com.example.urlshortner.service;

import com.example.urlshortner.dto.UrlRequest;
import com.example.urlshortner.dto.UrlResponse;
import com.example.urlshortner.entity.Url;
import com.example.urlshortner.repository.UrlRepository;
import com.example.urlshortner.util.Base62;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final ClickService clickService;
    private static final SecureRandom random = new SecureRandom();
    private static final int SHORT_CODE_LENGTH = 6;

    @Transactional
    public UrlResponse createShortUrl(UrlRequest request) {
        String longUrl = request.getLongUrl();
        // Ensure URL starts with http:// or https://
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "http://" + longUrl;
        }

        // Generate a unique short code
        String shortCode;
        do {
            shortCode = generateRandomCode(SHORT_CODE_LENGTH);
        } while (urlRepository.existsByShortCode(shortCode));

        // Create and save entity
        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode(shortCode);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            url.setExpiresAt(LocalDateTime.now().plusDays(request.getExpirationDays()));
        }
        urlRepository.save(url);

        // Prepare response
        UrlResponse response = new UrlResponse();
        response.setShortCode(shortCode);
        response.setShortUrl("http://localhost:8081/" + shortCode);
        response.setLongUrl(url.getLongUrl());
        response.setCreatedAt(url.getCreatedAt());
        return response;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "urls", key = "#shortCode", unless = "#result == null")
    public Optional<String> getLongUrl(String shortCode) {
        Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode);
        if (urlOpt.isPresent()) {
            Url url = urlOpt.get();
            // Check if expired
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
                return Optional.empty();
            }
            // Increment click count asynchronously
            clickService.incrementClickCount(shortCode);
            return Optional.of(url.getLongUrl());
        }
        return Optional.empty();
    }

    private String generateRandomCode(int length) {
        long randomLong = random.nextLong() & Long.MAX_VALUE; // positive only
        String encoded = Base62.encode(randomLong);
        // Pad with leading zeros if needed
        while (encoded.length() < length) {
            encoded = "0" + encoded;
        }
        return encoded.substring(0, length);
    }
}