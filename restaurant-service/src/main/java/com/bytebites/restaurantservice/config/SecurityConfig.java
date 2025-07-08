package com.bytebites.restaurantservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants").permitAll()
                        .requestMatchers("/api/restaurants/*/menu").permitAll() 
                        .requestMatchers("/api/restaurants/*/menu/*").permitAll() 
                        .anyRequest().authenticated()
                )
                .addFilter(requestHeaderAuthenticationFilter())
                .authenticationProvider(preAuthenticatedAuthenticationProvider());

        return http.build();
    }

    @Bean
    public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() {
        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader("X-User-Id");
        filter.setCredentialsRequestHeader("X-User-Roles");
        filter.setExceptionIfHeaderMissing(false);
        filter.setAuthenticationManager(authentication -> {
            String userId = (String) authentication.getPrincipal();
            String roles = (String) authentication.getCredentials();

            List<SimpleGrantedAuthority> authorities = roles != null ?
                    Arrays.stream(roles.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .toList() :
                    List.of();

            return new PreAuthenticatedAuthenticationToken(userId, roles, authorities);
        });
        return filter;
    }

    @Bean
    public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
        provider.setPreAuthenticatedUserDetailsService(token -> {
            String userId = (String) token.getPrincipal();
            String roles = (String) token.getCredentials();

            List<SimpleGrantedAuthority> authorities = roles != null ?
                    Arrays.stream(roles.split(","))
                            .map(SimpleGrantedAuthority::new)
                            .toList() :
                    List.of();

            return new org.springframework.security.core.userdetails.User(userId, "", authorities);
        });
        return provider;
    }
}