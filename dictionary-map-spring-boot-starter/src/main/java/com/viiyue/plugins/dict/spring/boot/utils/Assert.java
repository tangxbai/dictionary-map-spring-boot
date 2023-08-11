package com.viiyue.plugins.dict.spring.boot.utils;

import org.springframework.util.ObjectUtils;

import com.viiyue.plugins.dict.spring.boot.exception.DictionaryException;

public final class Assert {

    public static void isTrue( boolean expression, int code, String message ) {
        if ( !expression ) {
            DictionaryException.throwing( code, message );
        }
    }

    public static void isTrue( boolean expression, int code, String message, Object ... varargs ) {
        if ( !expression ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void isFalse( boolean expression, int code, String message ) {
        if ( expression ) {
            DictionaryException.throwing( code, message );
        }
    }

    public static void isFalse( boolean expression, int code, String message, Object ... varargs ) {
        if ( expression ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void isEmpty( Object value, int code, String message ) {
        if ( !ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message );
        }
    }

    public static void isEmpty( Object value, int code, String message, Object ... varargs ) {
        if ( !ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void notEmpty( Object value, int code, String message ) {
        if ( ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message );
        }
    }

    public static void notEmpty( Object value, int code, String message, Object ... varargs ) {
        if ( ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void isNull( Object value, int code, String message ) {
        if ( value != null ) {
            DictionaryException.throwing( code, message );
        }
    }

    public static void isNull( Object value, int code, String message, Object ... varargs ) {
        if ( value != null ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void notNull( Object value, int code, String message ) {
        if ( value == null ) {
            DictionaryException.throwing( code, message );
        }
    }

    public static void notNull( Object value, int code, String message, Object ... varargs ) {
        if ( value == null ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

}
