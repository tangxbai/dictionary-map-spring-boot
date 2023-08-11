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
import java.util.function.BiFunction;

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
    
    public String getLanguage( String lang ) {
        return lang == null ? getLanguage() : lang;
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
    
    public String splitWithKey( String theKey ) {
        if ( isEmpty( theKey ) ) {
            return theKey;
        }
        String baseKey = props.getCacheKey();
        if ( theKey.startsWith( baseKey ) ) {
            return theKey.substring( baseKey.length() );
        }
        return theKey;
    }
    
    public String resetCacheKey( String theKey, String delimiter ) {
        return isEmpty( theKey ) ? theKey : StringUtils.replace( theKey, delimiter, props.getDemiliter() );
    }

    public String toCacheKey( String cacheKey, String theKey, String delimiter ) {
        if ( cacheKey == null ) {
            cacheKey = props.getCacheKey();
        }
        if ( isEmpty( theKey ) ) {
            return cacheKey;
        }
        if ( theKey.startsWith( cacheKey ) ) {
            return theKey;
        }
        if ( cacheKey.endsWith( theKey ) ) {
            return cacheKey;
        }
        theKey = StringUtils.replace( theKey, props.getDemiliter(), delimiter );
        if ( cacheKey.endsWith( delimiter ) ) {
            return cacheKey.concat( theKey );
        }
        return cacheKey.concat( delimiter ).concat( theKey );
    }

    public <T> T fallbackWithLanguage( String language, BiFunction<String, String, T> fun ) {
        if ( isEmpty( language ) ) {
            if ( isLogEnable() ) {
                printLog( "Query in the default language ..." );
            }
            return fun.apply( null, null );
        }
        if ( isLogEnable() ) {
            printLog( "Query in the given language: \"{}\" ...", language );
        }
        T returnedValue = fun.apply( language, language );
        if ( !isEmpty( returnedValue ) ) {
            return returnedValue;
        }
        String temp = language;
        for ( int index = 0; ( index = temp.lastIndexOf( '_' ) ) > -1; ) {
            temp = temp.substring( 0, index );
            if ( isLogEnable() ) {
                printLog( "Fallback uses \"{}\" to query ...", temp );
            }
            returnedValue = fun.apply( language, temp );
            if ( !isEmpty( returnedValue ) ) {
                return returnedValue;
            }
        }
        if ( isLogEnable() ) {
            printLog( "Finally, try it in the default language ..." );
        }
        return fun.apply( language, null );
    }

}
