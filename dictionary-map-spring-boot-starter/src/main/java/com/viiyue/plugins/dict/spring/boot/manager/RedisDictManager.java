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
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.data.redis.core.RedisTemplate;

import com.viiyue.plugins.dict.spring.boot.manager.core.AbstractDictManager;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;

/**
 * Redis-based dictionary cache manager
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class RedisDictManager<K> extends AbstractDictManager<K> {

    private final RedisTemplate<K, Object> redis;

    public RedisDictManager( ParameterBridge bridge, DataSource dataSource, RedisTemplate<K, Object> redisTemplate ) {
        super( bridge, dataSource );
        this.redis = redisTemplate;
    }

    @Override
    public boolean existsKey( K key ) {
        Boolean hasKey = redis.hasKey( key );
        if ( bridge.isLogEnable() ) {
            if ( Boolean.TRUE.equals( hasKey ) ) {
                bridge.printLog( "Key \"{}\" exists in the Redis cache", key );
            } else {
                bridge.printLog(  "Key \"{}\" does not exist in the redis cache", key );
            }
        }
        return hasKey;
    }

    @Override
    public Object getValue( Object key ) {
        Object cachedValue = redis.opsForValue().get( key );
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Get the cached value of key \"{}\" from the Redis", key );
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
    public void setValue( K key, Object value ) {
        redis.opsForValue().set( key, value );
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Set the cache value of key \"{}\" to Redis", key );
        }
    }

    @Override
    public boolean clear( K key ) {
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Clear the cache value with the cache key of \"{}\"", key );
        }
        Boolean deleted = redis.delete( key );
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
        String cacheKey = bridge.props().getCacheKey();
        Set<K> keys = redis.keys( ( K ) ( cacheKey + "*:" + language ) );
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Clear all redis cache data whose cache keys end in \"{}\"", language );
            bridge.printLog( "The list keys: {}", keys );
        }
        if ( keys != null && keys.size() > 0 ) {
            redis.delete( keys );
        }
    }

}
