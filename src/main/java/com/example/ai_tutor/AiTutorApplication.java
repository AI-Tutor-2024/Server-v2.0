package com.example.ai_tutor;

import com.example.ai_tutor.global.config.YamlPropertySourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@PropertySource(value = { "classpath:oauth2/application-oauth2.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:database/application-database.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:s3/application-s3.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:webclient/application-webclient.yml" }, factory = YamlPropertySourceFactory.class)
@PropertySource(value = { "classpath:chatgpt/application-chatgpt.yml" }, factory = YamlPropertySourceFactory.class)
public class AiTutorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTutorApplication.class, args);
    }

}
