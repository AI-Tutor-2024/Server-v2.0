package com.example.ai_tutor;

import com.example.ai_tutor.global.config.YamlPropertySourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AiTutorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiTutorApplication.class, args);
    }
}
