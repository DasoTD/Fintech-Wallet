package com.example.fintechwallet.filter;

import com.example.fintechwallet.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(3)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String key = getRateLimitKey(request, requestPath);

        Bucket bucket = rateLimitConfig.resolveBucket(key + ":" + requestPath, getLimitsForPath(requestPath));

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for key: {}", key);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded\"}");
        }
    }

    private String getRateLimitKey(HttpServletRequest request, String path) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        String ip = getClientIp(request);
        return "ip:" + ip + ":" + path;
    }

    private Bandwidth[] getLimitsForPath(String path) {
        if (path.equals("/auth/login")) {
            return new Bandwidth[]{Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)))};
        } else if (path.equals("/wallet/transfer")) {
            return new Bandwidth[]{Bandwidth.classic(10, Refill.greedy(10, Duration.ofHours(1)))};
        } else if (path.startsWith("/auth/register")) {
            return new Bandwidth[]{Bandwidth.classic(3, Refill.greedy(3, Duration.ofHours(1)))};
        }
        return new Bandwidth[]{Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)))};
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) return xForwardedFor.split(",")[0].trim();
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null) return xRealIp.trim();
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/h2-console") || path.equals("/api/stripe/webhook");
    }
}