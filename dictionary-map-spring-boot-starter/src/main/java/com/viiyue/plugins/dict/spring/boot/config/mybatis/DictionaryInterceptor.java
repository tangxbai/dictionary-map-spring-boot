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
package com.viiyue.plugins.dict.spring.boot.config.mybatis;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;

import com.viiyue.plugins.dict.spring.boot.annotation.Dict;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;

/**
 * Interceptor for mybatis
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Intercepts( {
    // List<E> query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)
    @Signature( 
        type = Executor.class, 
        method = "query", 
        args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class }
    ),
    // List<E> query(MappedStatement, Object, RowBounds, ResultHandler)
    @Signature( 
        type = Executor.class, 
        method = "query", 
        args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }
    ),
    // Cursor<E> queryCursor(MappedStatement, Object, RowBounds)
    @Signature( 
        type = Executor.class, 
        method = "queryCursor", 
        args = { MappedStatement.class, Object.class, RowBounds.class }
    )
})
public class DictionaryInterceptor implements Interceptor {
    
    private static final Map<Class<?>, Map<String, String>> caches = new ConcurrentHashMap<>( 128 ); 

    @Override
    public Object intercept( Invocation invocation ) throws Throwable {
        MappedStatement statement = ( MappedStatement ) invocation.getArgs()[ 0 ];
        for ( ResultMap resultMap : statement.getResultMaps() ) {
            for ( ResultMapping mapping : resultMap.getPropertyResultMappings() ) {
                if ( Dictionary.class == mapping.getJavaType() ) {
                    TypeHandler<?> typeHandler = mapping.getTypeHandler();
                    if ( typeHandler instanceof DictionaryTypeHandler ) {
                        String column = mapping.getColumn();
                        Class<?> beanType = resultMap.getType();
                        Map<String, String> cache = caches.computeIfAbsent( beanType, type -> new HashMap<>( type.getDeclaredFields().length ) );
                        if ( !cache.containsKey( column ) ) {
                            Field field = beanType.getDeclaredField( mapping.getProperty() );
                            Dict dict = field.getAnnotation( Dict.class );
                            cache.put( column, dict == null ? null : dict.value() );
                        }
                        ( ( DictionaryTypeHandler ) typeHandler ).setCacheKeys( cache );
                    }
                }
            }
        }
        return invocation.proceed();
    }

}
