package com.goorm.springmissionsplayground.mission04_spring_mvc.task06_type_converter.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public LocalDate convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        try {
            return LocalDate.parse(source.trim(), FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("날짜는 yyyyMMdd 형식이어야 합니다. 예: 20260319");
        }
    }
}
