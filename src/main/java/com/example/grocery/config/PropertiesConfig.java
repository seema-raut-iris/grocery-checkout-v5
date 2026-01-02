package com.example.grocery.config;

import com.example.grocery.service.pricing.PriceCatalogProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PriceCatalogProperties.class)
public class PropertiesConfig { }
