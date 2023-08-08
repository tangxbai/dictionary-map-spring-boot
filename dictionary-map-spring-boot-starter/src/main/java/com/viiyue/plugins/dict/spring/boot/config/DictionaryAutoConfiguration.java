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
package com.viiyue.plugins.dict.spring.boot.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.viiyue.plugins.dict.spring.boot.DictContext;
import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.config.resolver.DictionaryArgumentResolver;
import com.viiyue.plugins.dict.spring.boot.config.resolver.DictionaryConverter;
import com.viiyue.plugins.dict.spring.boot.config.resolver.LocaleArgumentResolver;
import com.viiyue.plugins.dict.spring.boot.dialect.DefaultSqlResolver;
import com.viiyue.plugins.dict.spring.boot.dialect.SqlResolver;
import com.viiyue.plugins.dict.spring.boot.function.IdResolver;
import com.viiyue.plugins.dict.spring.boot.function.LanguageResolver;
import com.viiyue.plugins.dict.spring.boot.manager.MemoryDictManager;
import com.viiyue.plugins.dict.spring.boot.manager.RedisDictManager;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;
import com.viiyue.plugins.dict.spring.boot.utils.IdGenerator;

/**
 * Dictionary auto configuration in the spring-boot framework
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Order( Integer.MAX_VALUE )
@ConditionalOnClass( DataSource.class )
@EnableConfigurationProperties( DictionaryProperties.class )
class DictionaryAutoConfiguration implements ApplicationListener<ApplicationStartedEvent> {

    private final DataSource dataSource;
    private final DictionaryProperties props;
    private final RedisTemplate<String, Object> stringRedis;
    private final RedisTemplate<Object, Object> objectRedis;

    public DictionaryAutoConfiguration( 
        DictionaryProperties props, 
        ObjectProvider<RedisTemplate<String, Object>> stringRedisProvider,
        ObjectProvider<RedisTemplate<Object, Object>> objectRedisProvider, 
        ObjectProvider<DataSource> dataSourceProvider ) {
        this.props = props;
        this.dataSource = dataSourceProvider.getIfAvailable();
        this.stringRedis = stringRedisProvider.getIfAvailable();
        this.objectRedis = objectRedisProvider.getIfAvailable();
        Assert.notNull( dataSource, "No data source could be found in the Spring context" );
    }
    
    @Bean
    @ConditionalOnMissingBean
    public IdResolver idResolver() {
        return IdGenerator::nextId;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlResolver sqlResolver() {
        return new DefaultSqlResolver( props );
    }
    
    @Bean
    @ConditionalOnMissingBean
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

    @Bean
    @Primary
    public DictManager dictManager( 
        ObjectProvider<IdResolver> idResolver, 
        ObjectProvider<SqlResolver> sqlResolver,
        ObjectProvider<LanguageResolver> languageResolver ) {
        
        DictManager manager = null;
        ParameterBridge bridge = new ParameterBridge( props, idResolver.getIfAvailable(), sqlResolver.getIfAvailable(),
                languageResolver.getIfAvailable() );
        
        // Cache in redis
        if ( props.isRedisFirst() ) {
            if ( stringRedis != null ) {
                manager = new RedisDictManager<String>( bridge, dataSource, stringRedis );
            } else if ( objectRedis != null ) {
                manager = new RedisDictManager<Object>( bridge, dataSource, objectRedis );
            }
        }
        
        // Cache in memory
        if ( manager == null ) {
            manager = new MemoryDictManager( bridge, dataSource );
        }
        
        // Initialize the dictionary data
        if ( props.isLoadedDefault() ) {
            if ( props.isLogEnable() ) {
                props.printLog(  "Initialize loading default dictionary data" );
            }
            manager.getAllAlways();
        }
        DictContext.settingConfigurer( props );
        DictContext.managerConfigurer( manager );
        return manager;
    }

    @Override
    public void onApplicationEvent( ApplicationStartedEvent event ) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        
        DictManager dictManager = context.getBean( DictManager.class );
        RequestMappingHandlerAdapter handlerAdapter = context.getBean( RequestMappingHandlerAdapter.class );
        List<HandlerMethodArgumentResolver> defaults = handlerAdapter.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>( defaults.size() + 1 );
        
        // Locale argument resolver for spring
        if ( props.isLocaleArgumentResolver() ) {
            argumentResolvers.add( new LocaleArgumentResolver( props ) );
        }
        
        // Query parameter resolver for spring
        argumentResolvers.add( new DictionaryArgumentResolver( dictManager, props ) );
        argumentResolvers.addAll( defaults );
        handlerAdapter.setArgumentResolvers( Collections.unmodifiableList( argumentResolvers ) );
        
        // Object dictionary converter for spring
        ConverterRegistry converterRegistry = context.getBean( ConverterRegistry.class );
        converterRegistry.addConverter( new DictionaryConverter( dictManager ) );
    }
    
}
