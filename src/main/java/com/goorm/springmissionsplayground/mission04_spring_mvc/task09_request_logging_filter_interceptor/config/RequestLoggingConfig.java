package com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.config;

import com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.filter.RequestLoggingFilter;
import com.goorm.springmissionsplayground.mission04_spring_mvc.task09_request_logging_filter_interceptor.interceptor.RequestLoggingInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestLoggingConfig implements WebMvcConfigurer {

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration(RequestLoggingFilter requestLoggingFilter) {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestLoggingFilter);
        registrationBean.setName("task09RequestLoggingFilter");
        registrationBean.addUrlPatterns("/mission04/task09/logs/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    @Bean
    public RequestLoggingInterceptor requestLoggingInterceptor() {
        return new RequestLoggingInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor())
                .addPathPatterns("/mission04/task09/logs/**");
    }
}
