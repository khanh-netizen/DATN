package com.foxstyle.api.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.foxstyle.api.util.VietnameseTextNormalizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Áp dụng chuẩn hóa tiếng Việt tập trung cho tất cả dữ liệu JSON gửi vào API.
 */
@Configuration
public class VietnameseJacksonConfig {

    @Bean
    Module vietnameseTextModule() {
        SimpleModule module = new SimpleModule("vietnamese-text-normalizer");
        module.addDeserializer(String.class, new JsonDeserializer<>() {
            @Override
            public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                return VietnameseTextNormalizer.normalize(parser.getValueAsString());
            }
        });
        return module;
    }
}
