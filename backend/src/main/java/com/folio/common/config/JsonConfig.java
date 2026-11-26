package com.folio.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.deser.jdk.StringDeserializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Trims leading/trailing whitespace from every JSON string field before validation, so {@code " Priya "} and
 * {@code "Priya"} are the same name and {@code @Email}/{@code @NotBlank} see clean input. Fields that must be
 * preserved byte-for-byte (passwords) opt out with {@code @JsonDeserialize(using = StringDeserializer.class)}.
 */
@Configuration(proxyBeanMethods = false)
public class JsonConfig {

    @Bean
    JacksonModule trimStringsModule() {
        return new SimpleModule("folio-trim-strings").addDeserializer(String.class, new TrimmingStringDeserializer());
    }

    static final class TrimmingStringDeserializer extends StringDeserializer {

        @Override
        public String deserialize(JsonParser parser, DeserializationContext context) {
            String value = super.deserialize(parser, context);
            return value == null ? null : value.strip();
        }
    }
}
