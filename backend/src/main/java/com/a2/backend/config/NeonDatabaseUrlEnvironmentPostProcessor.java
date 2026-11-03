package com.a2.backend.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Maps Neon's DATABASE_URL (pooled, used by the app) and DATABASE_URL_UNPOOLED (direct, used by Flyway)
 * from the libpq format {@code postgresql://user:pass@host/db?...} to Spring's JDBC properties.
 * When DATABASE_URL is absent, the DB_URL / DB_USERNAME / DB_PASSWORD settings in application.yml apply.
 */
public class NeonDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "neonDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String pooled = environment.getProperty("DATABASE_URL");
        if (pooled == null || pooled.isBlank()) {
            return;
        }

        String direct = environment.getProperty("DATABASE_URL_UNPOOLED", pooled);
        Map<String, Object> properties = new HashMap<>();
        putConnection(properties, "spring.datasource.url", "spring.datasource.username", "spring.datasource.password", pooled);
        putConnection(properties, "spring.flyway.url", "spring.flyway.user", "spring.flyway.password", direct);

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    private static void putConnection(Map<String, Object> properties, String urlKey, String userKey,
            String passwordKey, String databaseUrl) {
        URI uri = URI.create(databaseUrl.trim());
        String userInfo = uri.getRawUserInfo();
        if (userInfo == null || !userInfo.contains(":")) {
            throw new IllegalStateException("DATABASE_URL must include user and password");
        }

        int separator = userInfo.indexOf(':');
        properties.put(userKey, decode(userInfo.substring(0, separator)));
        properties.put(passwordKey, decode(userInfo.substring(separator + 1)));

        String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
        String query = toJdbcQuery(uri.getRawQuery());
        properties.put(urlKey, "jdbc:postgresql://" + uri.getHost() + port + uri.getRawPath() + query);
    }

    private static String toJdbcQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return "?sslmode=require";
        }
        // libpq spells it channel_binding; pgjdbc expects channelBinding.
        String query = Stream.of(rawQuery.split("&"))
                .map(param -> param.startsWith("channel_binding=") ? "channelBinding=" + param.substring(16) : param)
                .collect(Collectors.joining("&"));
        return "?" + query;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
