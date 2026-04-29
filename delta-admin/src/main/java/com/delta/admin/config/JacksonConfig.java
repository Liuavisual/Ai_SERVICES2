package com.delta.admin.config;

import com.delta.common.annotation.ObfuscatedId;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer obfuscatedIdCustomizer() {
        return builder -> builder.annotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Object findSerializer(Annotated a) {
                if (a.hasAnnotation(ObfuscatedId.class)) {
                    return com.delta.common.serializer.ObfuscatedIdSerializer.class;
                }
                return super.findSerializer(a);
            }

            @Override
            public Object findDeserializer(Annotated a) {
                if (a.hasAnnotation(ObfuscatedId.class)) {
                    return com.delta.common.serializer.ObfuscatedIdDeserializer.class;
                }
                return super.findDeserializer(a);
            }
        });
    }
}
