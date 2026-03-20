package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String INTERCEPTOR_START_TIME_ATTRIBUTE = "task09InterceptorStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(INTERCEPTOR_START_TIME_ATTRIBUTE, startTime);

        log.info(
                "[Task09Interceptor][PRE_HANDLE] method={}, uri={}, handler={}",
                request.getMethod(),
                buildRequestPath(request),
                resolveHandlerName(handler)
        );
        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable ModelAndView modelAndView
    ) {
        String viewName = modelAndView == null ? "response-body" : modelAndView.getViewName();
        log.info(
                "[Task09Interceptor][POST_HANDLE] method={}, uri={}, view={}, status={}",
                request.getMethod(),
                buildRequestPath(request),
                viewName,
                response.getStatus()
        );
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex
    ) {
        long startTime = (Long) request.getAttribute(INTERCEPTOR_START_TIME_ATTRIBUTE);
        long durationMs = System.currentTimeMillis() - startTime;
        String exceptionType = ex == null ? "none" : ex.getClass().getSimpleName();

        log.info(
                "[Task09Interceptor][AFTER_COMPLETION] method={}, uri={}, handler={}, status={}, durationMs={}, exception={}",
                request.getMethod(),
                buildRequestPath(request),
                resolveHandlerName(handler),
                response.getStatus(),
                durationMs,
                exceptionType
        );
    }

    private String buildRequestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }

    private String resolveHandlerName(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
        }
        return handler.getClass().getSimpleName();
    }
}
