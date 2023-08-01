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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.viiyue.plugins.dict.spring.boot.annotation.FromHeader;
import com.viiyue.plugins.dict.spring.boot.annotation.FromSpring;
import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;

/**
 * Compatible with spring's parsing of locale parameters
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class LocaleArgumentResolver implements HandlerMethodArgumentResolver {

    private final DictionaryProperties props;
    
    public LocaleArgumentResolver( DictionaryProperties props ) {
        this.props = props;
    }

    @Override
    public boolean supportsParameter( MethodParameter parameter ) {
        return Locale.class.equals( parameter.getParameterType() );
    }

    @Override
    public Object resolveArgument( 
        MethodParameter parameter, 
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, 
        WebDataBinderFactory binderFactory ) throws Exception {
        
        // Query string
        String parameterName = parameter.getParameterName();
        String paramValue = webRequest.getParameter( parameterName );
        if ( !StringUtils.isEmpty( paramValue ) ) {
            paramValue = paramValue.replace( '_', '-' );
            return Locale.forLanguageTag( paramValue );
        }
        
        // @FromHeader
        if ( parameter.hasParameterAnnotation( FromHeader.class ) ) {
            if ( props.isLogEnable() ) {
                props.printLog( "Convert the \"{}\" in the request header to a Locale object", paramValue );
            }
            return webRequest.getLocale();
        }
        
        // @FromSpring
        if ( parameter.hasParameterAnnotation( FromSpring.class ) ) {
            Locale locale = RequestContextUtils.getLocale( toHttpRequest( webRequest ) );
            if ( props.isLogEnable() ) {
                props.printLog( "Get the Locale object from the Spring Framework( {} )", paramValue, locale );
            }
            return locale;
        }
        return null;
    }

    private HttpServletRequest toHttpRequest( NativeWebRequest webRequest ) {
        HttpServletRequest request = webRequest.getNativeRequest( HttpServletRequest.class );
        if ( request == null ) {
            throw new IllegalStateException(
                    "Current request is not of type [javax.servlet.http.HttpServletRequest]: " + webRequest );
        }
        return request;
    }

}
