package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotifyOnException {

    String value() default "";

    String alertTarget() default "slack://ops-alert";
}
