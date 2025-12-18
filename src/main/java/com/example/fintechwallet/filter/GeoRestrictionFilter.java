package com.example.fintechwallet.filter;

import com.example.fintechwallet.config.GeoRestrictionProperties;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
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
import java.net.InetAddress;
import java.util.List;

@Component
@Order(5)
public class GeoRestrictionFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(GeoRestrictionFilter.class);
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Autowired
    private GeoRestrictionProperties properties;

    @Autowired(required = false)
    private DatabaseReader databaseReader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        if (databaseReader == null) {
            log.warn("GeoRestrictionFilter is enabled but no GeoIP database was found; skipping geo checks");
            chain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();

        if (isPrivateIp(clientIp)) {
            chain.doFilter(request, response);
            return;
        }

        String countryCode = getCountryCode(clientIp);
        if (countryCode == null) {
            log.warn("GeoIP lookup failed for IP: {}", clientIp);
            chain.doFilter(request, response);
            return;
        }

        log.debug("GeoIP: IP {} → Country {}", clientIp, countryCode);

        boolean blocked = false;
        boolean pathMatched = false;
        for (var entry : properties.getPaths().entrySet()) {
            if (matcher.match(entry.getKey(), requestPath)) {
                pathMatched = true;
                List<String> pathBlocked = (List<String>) entry.getValue(); // Adjust if nested
                blocked = pathBlocked.contains(countryCode);
                break;
            }
        }

        if (!pathMatched) {
            blocked = properties.getBlockedCountries().contains(countryCode);
        }

        if (blocked) {
            log.warn("Geo blocked: IP {} (Country: {}) on path {}", clientIp, countryCode, requestPath);
            if (properties.isLogOnly()) {
                chain.doFilter(request, response);
                return;
            }
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Region restricted\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getCountryCode(String ip) {
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CountryResponse response = databaseReader.country(ipAddress);
            return response.getCountry().getIsoCode();
        } catch (Exception e) {
            log.error("GeoIP error for IP: {}", ip, e);
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) return xForwardedFor.split(",")[0].trim();
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null) return xRealIp.trim();
        return request.getRemoteAddr();
    }

    private boolean isPrivateIp(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127.") || ip.equals("0:0:0:0:0:0:0:1");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") || path.startsWith("/actuator") || path.startsWith("/h2-console") || path.equals("/api/stripe/webhook");
    }
}