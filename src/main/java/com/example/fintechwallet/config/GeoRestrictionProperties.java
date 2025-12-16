package com.example.fintechwallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "security.geo-restriction")
public class GeoRestrictionProperties {
    private boolean enabled = true;
    private boolean defaultAllow = true;
    private List<String> blockedCountries = new ArrayList<>();
    private Map<String, List<String>> paths = new HashMap<>();
    private boolean logOnly = false;

    // Getters and Setters...
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isDefaultAllow() { return defaultAllow; }
    public void setDefaultAllow(boolean defaultAllow) { this.defaultAllow = defaultAllow; }
    public List<String> getBlockedCountries() { return blockedCountries; }
    public void setBlockedCountries(List<String> blockedCountries) { this.blockedCountries = blockedCountries; }
    public Map<String, List<String>> getPaths() { return paths; }
    public void setPaths(Map<String, List<String>> paths) { this.paths = paths; }
    public boolean isLogOnly() { return logOnly; }
    public void setLogOnly(boolean logOnly) { this.logOnly = logOnly; }
}