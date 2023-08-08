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
package com.viiyue.plugins.dict.spring.boot.config.resolver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.viiyue.plugins.dict.spring.boot.DictContext;
import com.viiyue.plugins.dict.spring.boot.annotation.Dict;
import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;

/**
 * Handles the conversion of multiple values in {@code Jackson} deserialization
 *
 * @author tangxbai
 * @since 1.0.0
 */
@JsonComponent
@ConditionalOnClass( ObjectMapper.class )
@ConditionalOnProperty( prefix = "spring.dict.jackson", name = "enable", matchIfMissing = true, havingValue = "true" )
@EnableConfigurationProperties( DictionaryProperties.class )
public class DictionaryJsonDeserializer extends JsonDeserializer<Dictionary> {

    private static final Map<Class<?>, Map<String, String>> FIELDS = new ConcurrentHashMap<>( 128 );

    private final DictionaryProperties props;
    private BeanDeserializerBase oringinalDeserializer;
    
    public DictionaryJsonDeserializer( DictionaryProperties props ) {
        this.props = props;
    }
    
    @Override
    public Dictionary deserialize( JsonParser parser, DeserializationContext ctx )
            throws IOException, JsonProcessingException {
        final JsonToken token = parser.getCurrentToken();

        // JSON → Dictionary
        if ( JsonToken.START_OBJECT == token ) {
            BeanDeserializerBase deserializer = initOriginalDeserializer( ctx );
            if ( deserializer != null ) {
                deserializer.resolve( ctx );
                if ( props.isLogEnable() ) {
                    props.printLog( "Deserializer JSON string as a dictionary object" );
                }
                return ( Dictionary ) deserializer.deserialize( parser, ctx );
            } else {
                if ( props.isLogEnable() ) {
                    props.printLog( "Cannot deserializer JSON string as a dictionary object" );
                }
            }
        }

        // Jackson parsing context
        JsonStreamContext context = parser.getParsingContext();

        // The object to which the field belongs
        Object bean = context.getCurrentValue();
        if ( bean == null ) {
            return null;
        }

        // The field name of the dictionary object
        String fieldName = context.getCurrentName();
        final String cacheKey = getCacheKey( bean, fieldName );
        if ( cacheKey == null ) {
            throw new IllegalArgumentException( "You must specify the key for the dictionary( "
                    + bean.getClass().getName() + "#" + fieldName + " )" );
        }

        // Integer( code ) → Dictionary
        if ( JsonToken.VALUE_NUMBER_INT == token ) {
            int code = parser.getIntValue();
            if ( props.isLogEnable() ) {
                props.printLog( "Deserializer the code value {} to a dictionary object", code );
            }
            return DictContext.manager().match( cacheKey, code );
        }

        // String( alias ) → Dictionary
        if ( JsonToken.VALUE_STRING == token ) {
            String alias = parser.getText();
            if ( props.isLogEnable() ) {
                props.printLog( "Deserializer the string value \"{}\" to a dictionary object", alias );
            }
            return DictContext.manager().match( cacheKey, alias );
        }
        return null;
    }

    private String getCacheKey( Object bean, String fieldName ) {
        Class<? extends Object> beanType = bean.getClass();
        Map<String, String> dicts = FIELDS.get( beanType );
        if ( dicts == null ) {
            Field [] declares = beanType.getDeclaredFields();
            dicts = new HashMap<>( declares.length );
            for ( Field field : declares ) {
                Dict dict = field.getAnnotation( Dict.class );
                if ( dict != null ) {
                    dicts.put( field.getName(), dict.value() );
                }
            }
            FIELDS.put( beanType, dicts );
        }
        return dicts.get( fieldName );
    }
    
    private BeanDeserializerBase initOriginalDeserializer( DeserializationContext ctx ) throws JsonMappingException {
        if ( oringinalDeserializer == null ) {
            BeanDeserializerFactory bdf = ( BeanDeserializerFactory ) ctx.getFactory();
            JavaType javaType = ctx.constructType( Dictionary.class );
            BeanDescription description = ctx.getConfig().introspect( javaType );
            JsonDeserializer<Object> deserializer = bdf.buildBeanDeserializer( ctx, javaType, description );
            if ( deserializer instanceof BeanDeserializerBase ) {
                this.oringinalDeserializer = ( BeanDeserializerBase ) deserializer;
            }
        }
        return this.oringinalDeserializer;
    }

}
