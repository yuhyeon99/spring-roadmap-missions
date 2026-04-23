package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaselineTrace {
}
