/**
 * Copyright (C) 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.viiyue.plugins.dict.spring.boot.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.viiyue.plugins.dict.spring.boot.DictManager;

/**
 * Common helper
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class Helper {

    private static final String EMPTY = "";
    public static final Logger LOG = LoggerFactory.getLogger( DictManager.class.getName() );
    
    public static final String getLogPrefix() {
        StackTraceElement [] stacks = Thread.currentThread().getStackTrace();
        for ( int i = 0, len = stacks.length; i < len; i ++ ) {
            StackTraceElement stack = stacks[ i ];
            if ( stack.getMethodName().equals( "printLog" ) ) {
                StackTraceElement next = stacks[ i + 1 ];
                String className = next.getClassName();
                return className.substring( className.lastIndexOf( '.' ) + 1 ) + "[" + next.getLineNumber() + "] - ";
            }
        }
        return EMPTY;
    }

    public static final <T> T fristNonNull( final List<T> items ) {
        for ( T item : items ) {
            if ( item != null ) {
                return item;
            }
        }
        return null;
    }

    public static Integer toInt( final String str ) {
        if ( str == null ) {
            return null;
        }
        try {
            return Integer.parseInt( str );
        } catch ( final NumberFormatException nfe ) {
            return null;
        }
    }

    public static final String toUnderscore( final String text ) {
        char [] chars = text.toCharArray();
        int len = chars.length;
        StringBuilder content = new StringBuilder( len );
        for ( int i = 0; i < len; i ++ ) {
            char c = chars[ i ];
            if ( Character.isUpperCase( c ) ) {
                if ( i > 0 && chars[ i - 1 ] != '_' ) {
                    content.append( '_' );
                }
                content.append( Character.toLowerCase( c ) );
            } else {
                content.append( c );
            }
        }
        return content.toString();
    }

}
