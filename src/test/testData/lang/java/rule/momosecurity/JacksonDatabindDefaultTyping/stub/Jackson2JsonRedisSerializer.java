package org.springframework.data.redis.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Jackson2JsonRedisSerializer<T> {

    public Jackson2JsonRedisSerializer(Class<T> type) {
    }

    public void setObjectMapper(ObjectMapper objectMapper) {}
}