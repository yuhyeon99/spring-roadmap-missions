package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class NameNormalizer {

    private final NameSanitizer nameSanitizer;

    @Inject
    public NameNormalizer(NameSanitizer nameSanitizer) {
        this.nameSanitizer = nameSanitizer;
    }

    public String normalize(String rawName) {
        return nameSanitizer.sanitize(rawName);
    }
}
