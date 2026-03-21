package com.example.urlshortner.controller;

import com.example.urlshortner.dto.UrlRequest;
import com.example.urlshortner.dto.UrlResponse;
import com.example.urlshortner.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody UrlRequest request) {
        UrlResponse response = urlService.createShortUrl(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public void redirectToLongUrl(@PathVariable String shortCode,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        String host = request.getServerName();
        Optional<String> longUrlOpt = urlService.getLongUrl(shortCode, host);
        if (longUrlOpt.isPresent()) {
            response.sendRedirect(longUrlOpt.get());
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL not found or expired");
        }
    }
}