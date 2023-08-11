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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
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
public class DictionaryJsonSerializer extends JsonSerializer<Dictionary> {

    private final DictionaryProperties props;
    private JsonSerializer<Object> originalSerializer;

    public DictionaryJsonSerializer( DictionaryProperties props ) {
        this.props = props;
    }
    
    @Override
    public void serializeWithType( Dictionary value, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSerializer ) throws IOException {
        WritableTypeId typeId = typeSerializer.typeId( value, JsonToken.VALUE_NUMBER_INT );
        WritableTypeId typeIdDef = typeSerializer.writeTypePrefix( gen, typeId );
        doSerialize( value, gen, provider, true );
        typeSerializer.writeTypeSuffix( gen, typeIdDef );
    }

    @Override
    public void serialize( Dictionary value, JsonGenerator gen, SerializerProvider serializers ) throws IOException {
        JsonStreamContext context = gen.getOutputContext();
        doSerialize( value, gen, serializers, context.inRoot() || context.inArray() );
    }
    
    private void doSerialize( Dictionary value, JsonGenerator gen, SerializerProvider serializers, boolean internal )
            throws IOException {
        if ( value == null ) {
            gen.writeNull();
        } else if ( internal ) {
           JsonSerializer<Object> serializer = initOringinalSerializer( gen, serializers );
           if ( serializer != null ) {
               serializer.serialize( value, gen, serializers );
           } else {
               if ( props.isLogEnable() ) {
                   props.printLog( "Unable to serialize dictionary object to string" );
               }
           }
        } else {
            gen.writeObject( value.toObject( props ) );
        }
    }
    
    private JsonSerializer<Object> initOringinalSerializer( JsonGenerator gen, SerializerProvider serializers )
            throws JsonMappingException {
        if ( originalSerializer == null ) {
            ObjectMapper om = ( ObjectMapper ) gen.getCodec();
            SerializerFactory factory = om.getSerializerFactory();
            SerializationConfig config = serializers.getConfig();
            JavaType javaType = config.constructType( Dictionary.class );
            BeanDescription description = config.introspect( javaType );
            if ( factory instanceof BeanSerializerFactory ) {
                BeanSerializerFactory bsf = ( BeanSerializerFactory ) factory;
                this.originalSerializer = bsf.findBeanOrAddOnSerializer( serializers, javaType, description, false );
            }
        }
        return originalSerializer;
    }

}
