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
package com.viiyue.plugins.dict.spring.boot.manager;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import com.viiyue.plugins.dict.spring.boot.manager.core.AbstractDictManager;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;

/**
 * Memory-based dictionary cache manager
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class MemoryDictManager extends AbstractDictManager<String> {

    private static final Map<String, Object> caches = new ConcurrentHashMap<>( 128 );

    public MemoryDictManager( ParameterBridge bridge, DataSource datasource ) {
        super( bridge, datasource );
    }

    @Override
    public boolean existsKey( String key ) {
        Boolean hasKey = caches.containsKey( key );
        if ( bridge.isLogEnable() ) {
            if ( Boolean.TRUE.equals( hasKey ) ) {
                bridge.printLog( "Key \"{}\" exists in the Memory cache", key );
            } else {
                bridge.printLog( "Key \"{}\" does not exist in the Memory cache", key );
            }
        }
        return hasKey;
    }

    @Override
    public Object getValue( Object key ) {
        Object cachedValue = caches.get( key );
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Get the cached value of key \"{}\" from the Memory", key );
            if ( cachedValue != null ) {
                int size = cachedValue instanceof Collection ? ( ( Collection<?> ) cachedValue ).size() : 1;
                if ( size > 1 ) {
                    bridge.printLog( "{} pieces of data were found in Memory.", size );
                } else {
                    bridge.printLog( "The target data was found in Memory." );
                }
            }
        }
        return cachedValue;
    }

    @Override
    public void setValue( String key, Object value ) {
        caches.put( key, value );
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Set the cache value of key \"{}\" to Memory", key );
        }
    }

    @Override
    public boolean clear( String key ) {
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Clear the cache value with the cache key of \"{}\"", key );
        }
        boolean deleted = caches.remove( key ) != null;
        if ( bridge.isLogEnable() ) {
            if ( deleted ) {
                bridge.printLog( "Cleanup succeeded" );
            } else {
                bridge.printLog( "Invalid cleanup" );
            }
        }
        return deleted;
    }

    @Override
    public void clearLanguage( String language ) {
        Set<String> keys = caches.keySet();
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Clear all Memory cache data whose cache keys end in \"{}\"", language );
            bridge.printLog( "The list keys: {}", keys );
        }
        for ( String key : keys ) {
            if ( key.endsWith( language ) ) {
                caches.remove( key );
            }
        }
    }

}
