package com.example.urlshortner.repository;

import com.example.urlshortner.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DomainRepository extends JpaRepository<Domain, Long> {
    Optional<Domain> findByDomainName(String domainName);
    Optional<Domain> findByDomainNameAndVerifiedTrue(String domainName);
}