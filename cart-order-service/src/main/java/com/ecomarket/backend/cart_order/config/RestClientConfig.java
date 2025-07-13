package com.ecomarket.backend.cart_order.config;

import com.ecomarket.backend.cart_order.client.AuthClient;
import com.ecomarket.backend.cart_order.client.ProductClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.apache.hc.core5.util.Timeout;


@Configuration
public class RestClientConfig {

    @Value("${client.auth.base-url}")
    private String authBaseUrl;

    @Value("${client.product.base-url}")
    private String productBaseUrl;

    @Value("${client.http.connect-timeout}")
    private int connectTimeoutSeconds;

    @Value("${client.http.read-timeout}")
    private int readTimeoutSeconds;

    @Value("${client.http.connection-request-timeout}")
    private int connectionRequestTimeoutSeconds;



    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(connectionRequestTimeoutSeconds))
                .setConnectTimeout(Timeout.ofSeconds(connectTimeoutSeconds))
                .setResponseTimeout(Timeout.ofSeconds(readTimeoutSeconds))
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory httpRequestFactory(CloseableHttpClient httpClient) {
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }


    @Bean
    public ProductClient productClient(HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        // 1. Crear el RestClient específico para productos
        RestClient productSpecificClient = RestClient.builder()
                .baseUrl(productBaseUrl)
                .requestFactory(httpRequestFactory)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(productSpecificClient);
        HttpServiceProxyFactory productFactory = HttpServiceProxyFactory.builderFor(adapter).build();

        return productFactory.createClient(ProductClient.class);
    }

    @Bean
    public AuthClient authClient(HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        RestClient authSpecificClient = RestClient.builder()
                .baseUrl(authBaseUrl)
                .requestFactory(httpRequestFactory)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(authSpecificClient);
        HttpServiceProxyFactory authFactory = HttpServiceProxyFactory.builderFor(adapter).build();

        return authFactory.createClient(AuthClient.class);
    }

}