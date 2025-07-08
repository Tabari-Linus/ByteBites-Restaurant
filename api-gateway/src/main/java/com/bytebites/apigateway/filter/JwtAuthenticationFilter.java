package com.bytebites.apigateway.filter;

import com.bytebites.apigateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();
            String method = request.getMethod().toString();

            if (config != null && config.getPublicPaths() != null && isPublicPath(path, method, config)) {
                return chain.filter(exchange);
            }


            if (!containsAuthorizationHeader(request)) {
                logger.warn("Missing Authorization header for request: {}", request.getPath());
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = extractToken(request);

            if (!jwtUtil.validateToken(token)) {
                logger.warn("Invalid JWT token for request: {}", request.getPath());
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }

            try {
                Claims claims = jwtUtil.getClaims(token);
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Email", claims.get("email", String.class))
                        .header("X-User-Roles", String.join(",", (List<String>) claims.get("roles")))
                        .build();

                logger.debug("JWT validated successfully for user: {}", claims.getSubject());
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                logger.error("Error processing JWT token: {}", e.getMessage());
                return onError(exchange, "Error processing JWT token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path, String method, Config config) {
        if (config.getPublicPaths() == null) {
            return false;
        }

        return config.getPublicPaths().stream()
                .anyMatch(publicPath -> {
                    String[] parts = publicPath.split(" ");
                    String pathPattern = parts[0];
                    String allowedMethod = parts.length > 1 ? parts[1] : "*";

                    String normalizedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
                    String normalizedPattern = pathPattern.endsWith("/") ? pathPattern.substring(0, pathPattern.length() - 1) : pathPattern;

                    return normalizedPath.equals(normalizedPattern) &&
                            (allowedMethod.equals("*") || allowedMethod.equals(method));
                });
    }



    public static class Config {
        private final List<String> publicPaths = new ArrayList<>();

        public List<String> getPublicPaths() {
            return publicPaths;
        }

    }


    private boolean containsAuthorizationHeader(ServerHttpRequest request) {
        return request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        logger.error("Gateway authentication error: {}", err);
        return response.setComplete();
    }

}