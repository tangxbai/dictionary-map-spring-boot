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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

/**
 * JSON 预配置
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass( ObjectMapper.class )
@AutoConfigureBefore( JacksonAutoConfiguration.class )
public class JacksonConfiguration {

	private static final String HH_MM_SS = "HH:mm:ss";
	private static final String YYYY_MM_DD = "yyyy-MM-dd";
	private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

	@Bean
	public JavaTimeModule javaTimeModule() {
	    
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern( HH_MM_SS );
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( YYYY_MM_DD );
		DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern( YYYY_MM_DD_HH_MM_SS );

		JavaTimeModule module = new JavaTimeModule();
		
		// Serializer
		module.addSerializer( LocalTime.class, new LocalTimeSerializer( timeFormatter ) );
		module.addSerializer( LocalDate.class, new LocalDateSerializer( dateFormatter ) );
		module.addSerializer( LocalDateTime.class, new LocalDateTimeSerializer( datetimeFormatter ) );
		
		// Deserializer
		module.addDeserializer( LocalTime.class, new LocalTimeDeserializer( timeFormatter ) );
		module.addDeserializer( LocalDate.class, new LocalDateDeserializer( dateFormatter ) );
		module.addDeserializer( LocalDateTime.class, new LocalDateTimeDeserializer( datetimeFormatter ) );
		return module;
	}

}
