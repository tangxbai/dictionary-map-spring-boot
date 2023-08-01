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
package com.viiyue.plugins.dict.spring.boot.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import com.viiyue.plugins.dict.spring.boot.meta.BaseEntity;
import com.viiyue.plugins.dict.spring.boot.meta.BeanProperty;

/**
 * Property manager for initializing the set of property access for different bean objects
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class BeanMapper {

    private static final String[] EMPTY = null;
    private static final Map<Class<?>, List<BeanProperty>> properties = new HashMap<>( 2 );

    public BeanMapper( Class<?> ... types ) {
        for ( Class<?> beanType : types ) {
            if ( !properties.containsKey( beanType ) ) {
                properties.put( beanType, toProperties( beanType ) );
            }
        }
    }

    private List<BeanProperty> toProperties( Class<?> beanType ) {
        List<BeanProperty> properties = new LinkedList<>();
        for ( Field field : beanType.getDeclaredFields() ) {
            int modifiers = field.getModifiers();
            if ( ! ( Modifier.isStatic( modifiers ) || Modifier.isTransient( modifiers ) ) ) {
                String name = field.getName();
                PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor( beanType, name );
                properties.add( new BeanProperty( field, descriptor ) );
            }
        }
        return properties;
    }

    public List<BeanProperty> properties( Class<?> beanType ) {
        return properties.getOrDefault( beanType, Collections.emptyList() );
    }

    public BeanProperty getProperty( Class<?> beanType, String name ) {
        List<BeanProperty> properties = properties( beanType );
        if ( properties != null ) {
            for ( BeanProperty property : properties ) {
                if ( property.is( name ) ) {
                    return property;
                }
            }
        }
        return null;
    }

    public void eachBean( Class<?> beanType, Consumer<BeanProperty> consumer ) {
        List<BeanProperty> properties = properties( beanType );
        if ( properties != null ) {
            for ( BeanProperty property : properties ) {
                consumer.accept( property );
            }
        }
    }

    public <T extends BaseEntity> Map<String, Object> toValues( T bean ) {
        return toValues( bean, false );
    }

    public <T extends BaseEntity> Map<String, Object> toValues( T bean, boolean nonNull ) {
        return toValues( bean, nonNull, null, EMPTY );
    }
    
    public <T extends BaseEntity> Map<String, Object> toValues( T bean, boolean nonNull, String ... fields ) {
        return toValues( bean, nonNull, null, fields );
    }
    
    public <T extends BaseEntity> Map<String, Object> toValues( T bean, boolean nonNull, Consumer<Map<String, Object>> consumer ) {
        return toValues( bean, nonNull, consumer, EMPTY );
    }
    
    public <T extends BaseEntity> Map<String, Object> toValues( T bean, boolean nonNull, Consumer<Map<String, Object>> consumer, String ... fields ) {
        if ( bean == null ) {
            return Collections.emptyMap();
        }
        List<BeanProperty> properties = properties( bean.getClass() );
        Map<String, Object> values = new LinkedHashMap<>( properties.size() );
        if ( consumer != null ) {
            consumer.accept( values );
        }
        List<String> includes = ObjectUtils.isEmpty( fields ) ? null : Arrays.asList( fields );
        for ( BeanProperty property : properties ) {
            String fieldName = property.getProperty();
            if ( ( includes == null || includes.contains( fieldName ) ) && !values.containsKey( fieldName ) ) {
                Object fieldValue = property.getValue( bean );
                if ( !nonNull || fieldValue != null ) {
                    values.put( property.getColumn(), fieldValue );
                }
            }
        }
        return values;
    }

}
