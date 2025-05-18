package com.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 创建带超时配置的RequestConfig
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000) // 30秒
                .setSocketTimeout(120000) // 2分钟
                .build();

        // 创建HttpClient实例
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // 创建RestTemplate
        return new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(httpClient)
        );
    }
}