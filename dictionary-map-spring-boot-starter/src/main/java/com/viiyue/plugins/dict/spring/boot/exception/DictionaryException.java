package com.viiyue.plugins.dict.spring.boot.exception;

import java.text.MessageFormat;

import org.springframework.util.ObjectUtils;

import com.viiyue.plugins.dict.spring.boot.DictContext;

import lombok.Getter;

@Getter
public class DictionaryException extends RuntimeException {

    private static final long serialVersionUID = -2615024500289602783L;

    /** Error code value */
    private int code;
    
    /** Error message description */
    private String message;
    
    /** Format parameters, which may be objects or array objects. */
    private Object varargs;

    public DictionaryException( int code, String message ) {
        super( message );
        this.code = DictContext.settings().getErrorStatusCodeStartingValue() + code;
        this.message = message;
    }
    
    public DictionaryException( int code, String message, Object arg ) {
        this( code, format( message, arg) );
        this.varargs = arg;
    }

    public DictionaryException( int code, String message, Object ... varargs ) {
        this( code, format( message, varargs) );
        this.varargs = varargs;
    }

    public DictionaryException( Throwable cause ) {
        super( cause );
        this.code = 0;
        this.message = cause.getMessage();
    }
    
    public static final void throwing( Throwable cause ) {
        throw new DictionaryException( cause );
    }

    public static final void throwing( int code, String message ) {
        throw new DictionaryException( code, message );
    }

    public static final void throwing( int code, String message, Object arg ) {
        throw new DictionaryException( code, message, arg );
    }
    
    public static final void throwing( int code, String message, Object ... varargs ) {
        throw new DictionaryException( code, message, varargs );
    }

    @Override
    public String toString() {
        return "ERROR(" + code + ": " + message + ")";
    }

    private static String format( String message, Object ... varargs ) {
        if ( !ObjectUtils.isEmpty( varargs ) && message.contains( "{" ) && message.contains( "}" ) ) {
            return MessageFormat.format( message, varargs );
        }
        return message;
    }
    
}
