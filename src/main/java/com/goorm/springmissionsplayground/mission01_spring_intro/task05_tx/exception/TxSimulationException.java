package com.goorm.springmissionsplayground.mission01_spring_intro.task05_tx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TxSimulationException extends RuntimeException {
    public TxSimulationException(String message) {
        super(message);
    }
}
