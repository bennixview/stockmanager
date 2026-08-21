package com.bewi.stockmanager.marketdata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Lets the other self-contained systems' browsers read this system's API.
 *
 * <p>UI integration in SCS happens in the browser, so the portfolio pages fetch quotes and open an
 * event stream against this origin directly.
 */
@Configuration(proxyBeanMethods = false)
class WebConfiguration implements WebMvcConfigurer {

    private final MarketDataProperties properties;

    WebConfiguration(MarketDataProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET")
                .maxAge(3600);
        registry.addMapping("/js/**")
                .allowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET");
    }
}
