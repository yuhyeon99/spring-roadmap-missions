package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.config;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.converter.StringToLocalDateConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TypeConverterWebConfig implements WebMvcConfigurer {

    private final StringToLocalDateConverter stringToLocalDateConverter;

    public TypeConverterWebConfig(StringToLocalDateConverter stringToLocalDateConverter) {
        this.stringToLocalDateConverter = stringToLocalDateConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDateConverter);
    }
}
