
package com.example.grocery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Grocery Checkout API")
                        .version("v1")
                        .description("Itemized receipt calculation with promotions; Boot 3.3.4, Java 17, Lombok"));
    }
}
