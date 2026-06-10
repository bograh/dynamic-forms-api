package dev.ograh.dynamicforms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DynamicFormsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicFormsApplication.class, args);
    }

}