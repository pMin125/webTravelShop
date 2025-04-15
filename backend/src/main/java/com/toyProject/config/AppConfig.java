package com.toyProject.config;

import com.siot.IamportRestClient.IamportClient;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    String apiKey = "5545788037464428";
    String secretKey = "gqtWnK6jPl9ZgDzhVH3gti7yKhAFefVkfYl1aIXE0aPRyhlHsJGYzW1L4hoXKnATlWA8Bp1BSUdVdSIy";

    @Bean
    public IamportClient iamportClient() {
        return new IamportClient(apiKey, secretKey);
    }
}

