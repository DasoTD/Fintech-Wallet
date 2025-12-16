package com.example.fintechwallet.config;

import com.maxmind.geoip2.DatabaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Configuration
public class GeoIpConfig {
    private static final Logger log = LoggerFactory.getLogger(GeoIpConfig.class);

    @Bean
    public DatabaseReader databaseReader() throws IOException {
        ClassPathResource resource = new ClassPathResource("GeoLite2-Country.mmdb");

        if (!resource.exists()) {
            log.warn("GeoIP database not found on classpath ('GeoLite2-Country.mmdb'). Geo restrictions will be disabled.");
            return null;
        }

        // If the resource is available as a File (e.g., during development), use it directly.
        try {
            File file = resource.getFile();
            return new DatabaseReader.Builder(file).build();
        } catch (IOException ex) {
            // Resource is probably inside a jar; copy to a temporary file and use that
            log.debug("GeoIP resource is not a file; copying to temp file to create DatabaseReader");
            try (InputStream in = resource.getInputStream()) {
                File temp = Files.createTempFile("geolite2-country", ".mmdb").toFile();
                temp.deleteOnExit();
                Files.copy(in, temp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return new DatabaseReader.Builder(temp).build();
            }
        }
    }
}