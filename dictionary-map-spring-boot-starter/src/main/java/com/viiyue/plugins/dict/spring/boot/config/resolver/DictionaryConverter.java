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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.annotation.Dict;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.dict.spring.boot.utils.Helper;

/**
 * Convert alias/code values to dictionary object
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class DictionaryConverter implements ConditionalGenericConverter {

    private final DictManager dictManager;
    
    public DictionaryConverter( DictManager dictManager ) {
        this.dictManager = dictManager;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertiblePairs = Collections.newSetFromMap( new ConcurrentHashMap<>() );
        ConvertiblePair string2Dictionary = new ConvertiblePair( String.class, Dictionary.class );
        ConvertiblePair integer2Dictionary = new ConvertiblePair( Integer.class, Dictionary.class );
        convertiblePairs.add( string2Dictionary );
        convertiblePairs.add( integer2Dictionary );
        return convertiblePairs;
    }

    @Override
    public boolean matches( TypeDescriptor sourceType, TypeDescriptor targetType ) {
        return targetType.getType() == Dictionary.class;
    }
    
    @Override
    public Object convert( Object source, TypeDescriptor sourceType, TypeDescriptor targetType ) {
        Dict dict = targetType.getAnnotation( Dict.class );
        if ( dict != null ) {
            Class<?> varType = sourceType.getType();
            if ( varType == String.class ) {
                String codeOrAlias = source.toString();
                Integer codeValue = Helper.toInt( codeOrAlias );
                if ( codeValue != null ) {
                    return dictManager.match( dict.value(), codeValue ); // Code value
                }
                return dictManager.match( dict.value(), codeOrAlias ); // String value
            }
            if ( varType == Integer.class ) {
                return dictManager.match( dict.value(), ( Integer ) source ); // Code value
            }
        }
        return null;
    }


}
