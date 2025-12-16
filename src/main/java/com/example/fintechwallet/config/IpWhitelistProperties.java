package com.example.fintechwallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "security.ip-whitelist")
public class IpWhitelistProperties {
    private boolean enabled = true;
    private List<String> globalAllow = new ArrayList<>();
    private Map<String, List<String>> paths = new HashMap<>();
    private boolean defaultDeny = false;

    // Getters and Setters...
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public List<String> getGlobalAllow() { return globalAllow; }
    public void setGlobalAllow(List<String> globalAllow) { this.globalAllow = globalAllow; }
    public Map<String, List<String>> getPaths() { return paths; }
    public void setPaths(Map<String, List<String>> paths) { this.paths = paths; }
    public boolean isDefaultDeny() { return defaultDeny; }
    public void setDefaultDeny(boolean defaultDeny) { this.defaultDeny = defaultDeny; }
}