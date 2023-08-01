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

import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.annotation.Dict;
import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.dict.spring.boot.utils.Helper;

/**
 * Convert alias/code values to dictionary object
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class DictionaryArgumentResolver implements HandlerMethodArgumentResolver {

    private final DictManager dictManager;
    private final DictionaryProperties props;

    public DictionaryArgumentResolver( DictManager dictManager, DictionaryProperties props ) {
        this.dictManager = dictManager;
        this.props = props;
    }

    @Override
    public boolean supportsParameter( MethodParameter parameter ) {
        return Dictionary.class.equals( parameter.getParameterType() )
                && parameter.hasParameterAnnotation( Dict.class );
    }

    @Override
    public Object resolveArgument( 
        MethodParameter parameter, 
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, 
        WebDataBinderFactory binderFactory ) throws Exception {
        
        String paramName = parameter.getParameterName();
        String paramValue = webRequest.getParameter( paramName );
        if ( StringUtils.isEmpty( paramValue ) ) {
            return null;
        }
        
        Integer code = Helper.toInt( paramValue );
        String cacheKey = parameter.getParameterAnnotation( Dict.class ).value();
        
        if ( code == null ) {
            if ( props.isLogEnable() ) {
                props.printLog( "Convert the string value \"{}\" to a dictionary object", paramValue );
            }
            return dictManager.match( cacheKey, paramValue ); // Dictionary alias
        }
        
        if ( props.isLogEnable() ) {
            props.printLog( "Convert the code value {} to a dictionary object", code );
        }
        return dictManager.match( cacheKey, code ); // Dictionary code
    }

}
