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
package com.viiyue.plugins.dict.spring.boot.meta;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.Locale;
import java.util.function.Function;

import org.springframework.util.StringUtils;

import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;
import com.viiyue.plugins.dict.spring.boot.dialect.SqlResolver;
import com.viiyue.plugins.dict.spring.boot.function.IdResolver;
import com.viiyue.plugins.dict.spring.boot.function.LanguageResolver;

/**
 * Composite parametric bridges for the integration of complex parameters
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class ParameterBridge {

    private final DictionaryProperties props;
    private final IdResolver idResolver;
    private final SqlResolver sqlResolver;
    private final LanguageResolver languageResolver;

    public ParameterBridge( DictionaryProperties props, IdResolver idResolver, SqlResolver sqlResolver,
            LanguageResolver languageResolver ) {
        this.props = props;
        this.idResolver = idResolver;
        this.sqlResolver = sqlResolver;
        this.languageResolver = languageResolver;
    }

    public DictionaryProperties props() {
        return this.props;
    }

    public Long nextId() {
        return idResolver.nextId();
    }

    public boolean hasIdResolver() {
        return idResolver != null;
    }

    public SqlResolver sql() {
        return this.sqlResolver;
    }

    public boolean isLogEnable() {
        return props.isLogEnable();
    }
    
    public void printLog( String message ) {
        props.printLog( message );
    }
    
    public void printLog( String message, Object arg ) {
        props.printLog( message, arg );
    }
    
    public void printLog( String message, Object arg1, Object arg2 ) {
        props.printLog( message, arg1, arg2 );
    }
    
    public void printLog( String message, Object ... varargs ) {
        props.printLog( message, varargs );
    }
    
    public Locale locale() {
        return languageResolver == null ? null : languageResolver.getLocale();
    }

    public String getLanguage() {
        return toLanguage( locale() );
    }

    public String getLanguage( Locale locale ) {
        return toLanguage( locale == null ? locale() : locale );
    }

    public String toLanguage( Locale locale ) {
        if ( locale == null ) {
            return null;
        }
        String languageTag = locale.toLanguageTag();
        return isEmpty( languageTag ) ? null : toLanguage( languageTag );
    }

    public String toLanguage( String language ) {
        return language == null ? null : language.toLowerCase( Locale.ENGLISH ).replace( '-', '_' );
    }

    public String toCacheKey( String cacheKey, String other, String delimiter ) {
        if ( cacheKey == null ) {
            cacheKey = props.getCacheKey();
        }
        if ( isEmpty( other ) ) {
            return cacheKey;
        }
        other = StringUtils.replace( other, ".", delimiter );
        if ( cacheKey.endsWith( delimiter ) ) {
            return cacheKey.concat( other );
        }
        return cacheKey.concat( delimiter ).concat( other );
    }

    public <T> T fallbackWithLanguage( String language, Function<String, T> fun ) {
        if ( language == null ) {
            language = getLanguage();
        }
        if ( isEmpty( language ) ) {
            if ( isLogEnable() ) {
                printLog( "Query in the default language ..." );
            }
            return fun.apply( null );
        }
        
        if ( isLogEnable() ) {
            printLog( "Query in the given language: \"{}\" ...", language );
        }
        T returnedValue = fun.apply( language );
        if ( !isEmpty( returnedValue ) ) {
            return returnedValue;
        }
        for ( int index = 0; ( index = language.lastIndexOf( '_' ) ) > -1; ) {
            language = language.substring( 0, index );
            if ( isLogEnable() ) {
                printLog( "Fallback uses \"{}\" to query ...", language );
            }
            returnedValue = fun.apply( language );
            if ( !isEmpty( returnedValue ) ) {
                return returnedValue;
            }
        }
        
        if ( isLogEnable() ) {
            printLog( "Finally, try it in the default language ..." );
        }
        return fun.apply( null );
    }

}
