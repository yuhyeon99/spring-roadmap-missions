package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support;

import org.springframework.stereotype.Component;

@Component
public class ExpensivePayloadFormatter {

    public String createSnapshot(String methodLabel, String payload, int rounds) {
        long checksum = 17L;
        int labelLength = methodLabel.length();

        for (int round = 0; round < rounds; round++) {
            for (int index = 0; index < payload.length(); index++) {
                char payloadChar = payload.charAt(index);
                char labelChar = methodLabel.charAt(index % labelLength);
                checksum = (checksum * 131L) ^ (payloadChar + labelChar + round);
                checksum ^= (checksum << 7);
                checksum += payloadChar * (index + 3L);
            }
            checksum ^= (checksum >>> 11);
        }

        return methodLabel + "#" + payload.length() + "#" + Long.toUnsignedString(checksum, 16);
    }
}
