package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.annotation;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain.AopUserRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    AopUserRole value();

    String action();
}
