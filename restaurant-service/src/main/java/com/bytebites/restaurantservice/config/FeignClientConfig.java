package com.bytebites.restaurantservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(FeignClientConfig.class);

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-User-Id");
                String userRoles = request.getHeader("X-User-Roles");

                
                logger.info("Feign Interceptor: Preparing to forward headers. User-Id: [{}], Roles: [{}]", userId, userRoles);

                if (userId != null) {
                    template.header("X-User-Id", userId);
                }
                if (userRoles != null) {
                    template.header("X-User-Roles", userRoles);
                }
            } else {
                logger.warn("Feign Interceptor: Could not get request attributes. Headers will not be forwarded.");
            }
        };
    }
}