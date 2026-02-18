package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto.GreetingResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.policy.GreetingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AnnotationGreetingService {

    private final GreetingPolicy greetingPolicy;
    private final NameNormalizer nameNormalizer;

    @Autowired
    public AnnotationGreetingService(
            @Qualifier("formalGreetingPolicy") GreetingPolicy greetingPolicy,
            NameNormalizer nameNormalizer
    ) {
        this.greetingPolicy = greetingPolicy;
        this.nameNormalizer = nameNormalizer;
    }

    public GreetingResponse greet(String rawName) {
        String normalizedName = nameNormalizer.normalize(rawName);
        String message = greetingPolicy.createMessage(normalizedName);
        return new GreetingResponse(message, "formalGreetingPolicy", "@Autowired + @Inject");
    }
}
