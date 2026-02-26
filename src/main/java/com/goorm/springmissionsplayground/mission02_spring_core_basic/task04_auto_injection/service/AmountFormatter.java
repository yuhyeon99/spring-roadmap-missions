package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class AmountFormatter {

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);

    public String format(int amount) {
        return numberFormat.format(amount) + "원";
    }
}
