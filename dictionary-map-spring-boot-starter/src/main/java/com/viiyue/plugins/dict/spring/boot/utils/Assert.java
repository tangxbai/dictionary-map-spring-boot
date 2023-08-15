package com.viiyue.plugins.dict.spring.boot.utils;

import org.springframework.util.ObjectUtils;

import com.viiyue.plugins.dict.spring.boot.exception.DictionaryException;

public final class Assert {
    
    public static final int CANNOT_BE_NULL_OR_EMPTY = 2;
    public static final int ITEM_CANNOT_BE_NULL_OR_EMPTY = 3;
    public static final int CANNOT_BE_THE_SAME = 4;
    public static final int DOSE_NOT_EXIST = 5;
    public static final int ALREADY_EXISTS = 6;

    public static void isTrue( int code, boolean expression, String message ) {
        if ( !expression ) {
            DictionaryException.throwing( code, message );
        }
    }
    
    // Avoid using array objects
    public static void isTrue( int code, boolean expression, String message, Object arg ) {
        if ( !expression ) {
            DictionaryException.throwing( code, message, arg );
        }
    }
    
    public static void isTrue( int code, boolean expression, String message, Object ... varargs ) {
        if ( !expression ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void isFalse( int code, boolean expression, String message ) {
        if ( expression ) {
            DictionaryException.throwing( code, message );
        }
    }
    
    // Avoid using array objects
    public static void isFalse( int code, boolean expression, String message, Object arg ) {
        if ( expression ) {
            DictionaryException.throwing( code, message, arg );
        }
    }
    
    public static void isFalse( int code, boolean expression, String message, Object ... varargs ) {
        if ( expression ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void isEmpty( int code, Object value, String message ) {
        if ( !ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message );
        }
    } 
    
    // Avoid using array objects
    public static void isEmpty( int code, Object value, String message, Object arg ) {
        if ( !ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message, arg );
        }
    }

    public static void isEmpty( int code, Object value, String message, Object ... varargs ) {
        if ( !ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void notEmpty( int code, Object value, String message ) {
        if ( ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message );
        }
    }
    
    // Avoid using array objects
    public static void notEmpty( int code, Object value, String message, Object arg ) {
        if ( ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message, arg );
        }
    }
    
    public static void notEmpty( int code, Object value, String message, Object ... varargs ) {
        if ( ObjectUtils.isEmpty( value ) ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void isNull( int code, Object value, String message ) {
        if ( value != null ) {
            DictionaryException.throwing( code, message );
        }
    }
    
    // Avoid using array objects
    public static void isNull( int code, Object value, String message, Object arg ) {
        if ( value != null ) {
            DictionaryException.throwing( code, message, arg );
        }
    }

    public static void isNull( int code, Object value, String message, Object ... varargs ) {
        if ( value != null ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

    public static void notNull( int code, Object value, String message ) {
        if ( value == null ) {
            DictionaryException.throwing( code, message );
        }
    }

    // Avoid using array objects 
    public static void notNull( int code, Object value, String message, Object arg ) {
        if ( value == null ) {
            DictionaryException.throwing( code, message, arg );
        }
    }
    
    public static void notNull( int code, Object value, String message, Object ... varargs ) {
        if ( value == null ) {
            DictionaryException.throwing( code, message, varargs );
        }
    }

}
