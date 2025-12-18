package com.example.fintechwallet.filter;

import com.example.fintechwallet.config.IpWhitelistProperties;
import com.example.fintechwallet.util.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(4)
public class IpWhitelistFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(IpWhitelistFilter.class);
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Autowired
    private IpWhitelistProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();

        log.debug("IP Whitelist check for IP: {} on path: {}", clientIp, requestPath);

        if (IpUtils.isIpAllowed(clientIp, properties.getGlobalAllow())) {
            chain.doFilter(request, response);
            return;
        }

        List<String> allowedIps = null;
        for (var entry : properties.getPaths().entrySet()) {
            if (matcher.match(entry.getKey(), requestPath)) {
                allowedIps = entry.getValue();
                break;
            }
        }

        if (allowedIps == null) {
            if (properties.isDefaultDeny()) {
                blockRequest(response, clientIp, requestPath);
                return;
            } else {
                chain.doFilter(request, response);
                return;
            }
        }

        if (IpUtils.isIpAllowed(clientIp, allowedIps)) {
            chain.doFilter(request, response);
        } else {
            blockRequest(response, clientIp, requestPath);
        }
    }

    private void blockRequest(HttpServletResponse response, String clientIp, String path) throws IOException {
        log.warn("IP {} blocked on path {}", clientIp, path);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"IP not whitelisted\"}");
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
        return path.startsWith("/auth/") || path.startsWith("/h2-console") || path.equals("/api/stripe/webhook");
    }
}