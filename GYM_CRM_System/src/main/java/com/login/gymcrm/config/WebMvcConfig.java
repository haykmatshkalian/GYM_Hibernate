package com.login.gymcrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RestCallLoggingInterceptor restCallLoggingInterceptor;

    public WebMvcConfig(RestCallLoggingInterceptor restCallLoggingInterceptor) {
        this.restCallLoggingInterceptor = restCallLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(restCallLoggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
