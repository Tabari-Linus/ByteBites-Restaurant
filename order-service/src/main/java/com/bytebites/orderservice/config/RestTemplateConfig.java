package com.bytebites.orderservice.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateHeaderModifierInterceptor()));
        return restTemplate;
    }

    public static class RestTemplateHeaderModifierInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                ClientHttpRequestExecution execution) throws IOException {
            
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest currentRequest = 
                    ((ServletRequestAttributes) requestAttributes).getRequest();
                
                String userId = currentRequest.getHeader("X-User-Id");
                String userRoles = currentRequest.getHeader("X-User-Roles");
                
                if (userId != null) {
                    request.getHeaders().set("X-User-Id", userId);
                }
                if (userRoles != null) {
                    request.getHeaders().set("X-User-Roles", userRoles);
                }
            }
            
            return execution.execute(request, body);
        }
    }
}