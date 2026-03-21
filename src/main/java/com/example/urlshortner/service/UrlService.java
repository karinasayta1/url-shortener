package com.example.urlshortner.service;

import com.example.urlshortner.dto.UrlRequest;
import com.example.urlshortner.dto.UrlResponse;
import com.example.urlshortner.entity.Url;
import com.example.urlshortner.repository.UrlRepository;
import com.example.urlshortner.util.Base62;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlService {

    @Value("${app.port:8081}")
    private String appPort;

    private final UrlRepository urlRepository;
    private final ClickService clickService;
    private final DomainService domainService;
    private static final SecureRandom random = new SecureRandom();
    private static final int SHORT_CODE_LENGTH = 6;

    @Transactional
    public UrlResponse createShortUrl(UrlRequest request) {
        String longUrl = request.getLongUrl();
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "http://" + longUrl;
        }

        String customDomain = request.getCustomDomain();
        if (customDomain != null && !customDomain.isEmpty()) {
            if (!domainService.isDomainVerified(customDomain)) {
                throw new RuntimeException("Custom domain is not verified: " + customDomain);
            }
        }

        String shortCode;
        do {
            shortCode = generateRandomCode(SHORT_CODE_LENGTH);
        } while (urlRepository.existsByShortCode(shortCode));

        Url url = new Url();
        url.setLongUrl(longUrl);
        url.setShortCode(shortCode);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            url.setExpiresAt(LocalDateTime.now().plusDays(request.getExpirationDays()));
        }
        url.setCustomDomain(customDomain);
        urlRepository.save(url);

        // Build the full short URL with correct protocol, domain, and port
        String baseUrl;
        if (customDomain != null && !customDomain.isEmpty()) {
            baseUrl = "http://" + customDomain;
        } else {
            baseUrl = "http://localhost";
        }
        // Append port only if it's not the default HTTP/HTTPS port
        if (!"80".equals(appPort) && !"443".equals(appPort)) {
            baseUrl += ":" + appPort;
        }

        UrlResponse response = new UrlResponse();
        response.setShortCode(shortCode);
        response.setShortUrl(baseUrl + "/" + shortCode);
        response.setLongUrl(url.getLongUrl());
        response.setCreatedAt(url.getCreatedAt());
        return response;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "urls", key = "#shortCode + '_' + #host", unless = "#result == null")
    public Optional<String> getLongUrl(String shortCode, String host) {
        Optional<Url> urlOpt = urlRepository.findByShortCodeAndCustomDomain(shortCode, host);
        if (urlOpt.isEmpty()) {
            urlOpt = urlRepository.findByShortCodeAndCustomDomainIsNull(shortCode);
        }

        if (urlOpt.isPresent()) {
            Url url = urlOpt.get();
            if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
                return Optional.empty();
            }
            clickService.incrementClickCount(shortCode);
            return Optional.of(url.getLongUrl());
        }
        return Optional.empty();
    }

    private String generateRandomCode(int length) {
        long randomLong = random.nextLong() & Long.MAX_VALUE;
        String encoded = Base62.encode(randomLong);
        while (encoded.length() < length) {
            encoded = "0" + encoded;
        }
        return encoded.substring(0, length);
    }
}