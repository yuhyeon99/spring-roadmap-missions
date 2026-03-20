package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.controller;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.dto.RequestLoggingResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission04/task09/logs")
public class RequestLoggingController {

    @GetMapping("/requests")
    public RequestLoggingResponse inspectRequest(
            @RequestParam(defaultValue = "filter-interceptor") String topic,
            HttpServletRequest request
    ) {
        return new RequestLoggingResponse(
                request.getMethod(),
                request.getRequestURI(),
                topic,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "필터와 인터셉터 로그를 함께 확인할 수 있습니다."
        );
    }
}
