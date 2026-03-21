package com.example.urlshortner.service;

import com.example.urlshortner.entity.Domain;
import com.example.urlshortner.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DomainService {

    private final DomainRepository domainRepository;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Register a new domain. Generates a random token for verification.
     */
    @Transactional
    public Domain registerDomain(String domainName) {
        // Check if already exists
        if (domainRepository.findByDomainName(domainName).isPresent()) {
            throw new RuntimeException("Domain already registered");
        }

        Domain domain = new Domain();
        domain.setDomainName(domainName);
        domain.setVerificationToken(generateToken());
        domain.setVerified(false);
        return domainRepository.save(domain);
    }

    /**
     * Verify a domain by checking the DNS TXT record matches the token.
     * For demonstration, we'll simulate DNS lookup. In real world, you'd perform a DNS TXT query.
     */
    public boolean verifyDomain(String domainName, String token) {
        Optional<Domain> domainOpt = domainRepository.findByDomainName(domainName);
        if (domainOpt.isEmpty()) return false;

        Domain domain = domainOpt.get();
        // Simulate DNS check: compare token with what the user claims
        // In production, you would actually query the DNS TXT record for "_urlshortener.yourdomain.com"
        // We'll simply compare with the stored token for now.
        if (domain.getVerificationToken().equals(token)) {
            domain.setVerified(true);
            domain.setVerifiedAt(LocalDateTime.now());
            domainRepository.save(domain);
            return true;
        }
        return false;
    }

    /**
     * Check if a domain is verified.
     */
    public boolean isDomainVerified(String domainName) {
        return domainRepository.findByDomainNameAndVerifiedTrue(domainName).isPresent();
    }

    private String generateToken() {
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}