/**
 * Copyright (C) 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viiyue.plugins.dict.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 * Redis configuration
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties( CacheProperties.class )
@AutoConfigureBefore( RedisAutoConfiguration.class )
public class RedisConfiguration {

    @Bean
	public RedisTemplate<String, Object> redisTemplate( ObjectMapper om, RedisConnectionFactory factory ) {
		ObjectMapper objectMapper = om.copy();
		PolymorphicTypeValidator typeValidator = objectMapper.getPolymorphicTypeValidator();
		objectMapper.activateDefaultTyping( typeValidator, DefaultTyping.NON_FINAL );

		GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer( objectMapper );
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory( factory );
		template.setKeySerializer( RedisSerializer.string() );
		template.setValueSerializer( jacksonSerializer );
		template.setHashKeySerializer( RedisSerializer.string() );
		template.setHashValueSerializer( jacksonSerializer );
		template.afterPropertiesSet();
		return template;
	}


}
