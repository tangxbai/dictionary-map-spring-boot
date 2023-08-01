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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;
import com.viiyue.plugins.dict.spring.boot.dialect.DefaultSqlResolver;
import com.viiyue.plugins.dict.spring.boot.dialect.SqlResolver;
import com.viiyue.plugins.dict.spring.boot.function.IdResolver;
import com.viiyue.plugins.dict.spring.boot.function.LanguageResolver;
import com.viiyue.plugins.dict.spring.boot.utils.IdGenerator;

/**
 * Dictionary related configuration 
 *
 * @author tangxbai
 * @since 1.0.0
 * @se com.viiyue.plugins.dict.spring.boot.config.DictionaryAuoConfiguration
 */
//@Configuration
//@EnableConfigurationProperties( DictionaryProperties.class )
public class DictionaryConfiguration {
    
    private final DictionaryProperties props;
    
    public DictionaryConfiguration( DictionaryProperties props ) {
        this.props = props;
    }
    
    /**
     * (DEFAULT) Dictionary id generator
     * 
     * @return the id generator
     * @see com.viiyue.plugins.dict.spring.boot.config.DictionaryAuoConfiguration
     */
    @Bean
    public IdResolver idResolver() {
        return IdGenerator::nextId;
    }
    
    
    /**
     * (DEFAULT) Different SQL cross-platform resolver
     * 
     * @return the sql resolver
     * @see com.viiyue.plugins.dict.spring.boot.config.DictionaryAuoConfiguration
     */
    @Bean
    public SqlResolver sqlResolver( ) {
        return new DefaultSqlResolver( props );
    }

    /**
     * (DEFAULT) Provides a dictionary language resolver
     * 
     * @return the language resolver instance
     * @see com.viiyue.plugins.dict.spring.boot.config.DictionaryAuoConfiguration
     */
    @Bean
    public LanguageResolver languageResolver() {
        return props.hasDefaultLocale() ? props::getLocale : (() -> {
            // 1. Try to get HttpServletRequest from the local context
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if ( attributes == null ) {
                return null;
            }
            HttpServletRequest request = ( ( ServletRequestAttributes ) attributes ).getRequest();
            if ( request == null ) {
                return null;
            }

            // 2. Get it first from query parameters
            String queryName = props.getLocaleQuery();
            String language = request.getParameter( queryName ); // zh-CN
            if ( !StringUtils.isEmpty( language ) ) {
                language = language.replace( '_', '-' ); // Or zh_CN
                return Locale.forLanguageTag( language );
            }

            // 3. Get from spring locale resolver or HTTP header( "Accept-Language" )
            return RequestContextUtils.getLocale( request );
        });
    }
    
}
