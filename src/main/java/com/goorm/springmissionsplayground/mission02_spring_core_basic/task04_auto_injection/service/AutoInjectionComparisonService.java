package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.AutoInjectionComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AutoInjectionComparisonService {

    private final AutowiredOnlyService autowiredOnlyService;
    private final QualifierInjectionService qualifierInjectionService;
    private final PrimaryInjectionService primaryInjectionService;

    public AutoInjectionComparisonService(
            AutowiredOnlyService autowiredOnlyService,
            QualifierInjectionService qualifierInjectionService,
            PrimaryInjectionService primaryInjectionService
    ) {
        this.autowiredOnlyService = autowiredOnlyService;
        this.qualifierInjectionService = qualifierInjectionService;
        this.primaryInjectionService = primaryInjectionService;
    }

    public AutoInjectionComparisonResponse compare(int amount) {
        List<InjectionCaseResult> comparisons = List.of(
                autowiredOnlyService.compare(amount),
                qualifierInjectionService.compare(amount),
                primaryInjectionService.compare(amount)
        );
        return new AutoInjectionComparisonResponse(amount, comparisons);
    }
}
