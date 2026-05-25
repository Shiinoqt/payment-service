package com.its.gestionepagamentirestclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${order.service.url}")
    private String orderServiceUrl;

    @Bean
    public RestClient orderRestClient() {
        return RestClient.builder()
                .baseUrl(orderServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
