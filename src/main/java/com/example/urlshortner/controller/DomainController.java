package com.example.urlshortner.controller;

import com.example.urlshortner.entity.Domain;
import com.example.urlshortner.service.DomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/domains")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;

    @PostMapping("/register")
    public ResponseEntity<?> registerDomain(@RequestBody Map<String, String> payload) {
        String domainName = payload.get("domainName");
        try {
            Domain domain = domainService.registerDomain(domainName);
            return ResponseEntity.ok(Map.of(
                "domain", domain.getDomainName(),
                "token", domain.getVerificationToken(),
                "message", "Add the following TXT record to your DNS: _urlshortener." + domainName + " TXT " + domain.getVerificationToken()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyDomain(@RequestBody Map<String, String> payload) {
        String domainName = payload.get("domainName");
        String token = payload.get("token");
        boolean verified = domainService.verifyDomain(domainName, token);
        if (verified) {
            return ResponseEntity.ok(Map.of("message", "Domain verified successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Verification failed"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkDomainStatus(@RequestParam String domainName) {
        boolean verified = domainService.isDomainVerified(domainName);
        return ResponseEntity.ok(Map.of("domain", domainName, "verified", verified));
    }
}