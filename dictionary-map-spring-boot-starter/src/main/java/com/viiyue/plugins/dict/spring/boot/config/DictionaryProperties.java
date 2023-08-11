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
package com.viiyue.plugins.dict.spring.boot.config;

import static org.springframework.util.StringUtils.isEmpty;

import java.util.Locale;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.viiyue.plugins.dict.spring.boot.utils.Helper;

import lombok.Setter;

/**
 * Dictionary configuration properties
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Setter
@ConfigurationProperties( prefix = "spring.dict" )
public class DictionaryProperties {

    /**
     * Whether to use Redis cache first
     */
    private boolean redisFirst = true;
    
    /**
     * Whether to load default dictionary data automatically
     */
    private boolean loadedDefault = true;
    
    /**
     * Whether to enable parsing of the locale parameter
     */
    private boolean localeArgumentResolver = true;
    
    /**
     * Set the minimum number of batch executions
     */
    private int smallBatchSize = 500;
    
    /**
     * Set the maximum number of batch executions
     */
    private int bigBatchSize = 1000;

    /**
     * Set the log level
     */
    private LogLevel logLevel = LogLevel.DEBUG;

    /**
     * Set the default fixed locale
     */
    private Locale locale;
    
    /**
     * The delimiter of the key
     */
    private String demiliter = ".";
    
    /**
     * Locale query name
     */
    private String localeQuery = "lang";
    
    /**
     * Data cache key
     */
    private String cacheKey = "cacheable:dict:";
    
    /**
     * The dictionary table name
     */
    private String dictTable = "global_dictionary";
    
    /**
     * The dictionary language table name
     */
    private String langTable = "global_dictionary_lang";
    
    /**
     * Exported fields
     */
    private String[] expands = { "code", "text" };

    /**
     * The wrapper text for the database keyword, the default is: {@code `}.
     */
    private String columnWrapText = "`"; // `table` or `column`
    
    /**
     * Error status code starting value
     */
    private int errorStatusCodeStartingValue = 9999;

    public boolean isRedisFirst() {
        return redisFirst;
    }

    public boolean isLoadedDefault() {
        return loadedDefault;
    }

    public final boolean isLocaleArgumentResolver() {
        return localeArgumentResolver;
    }

    public int getSmallBatchSize() {
        return smallBatchSize;
    }

    public int getBigBatchSize() {
        return bigBatchSize;
    }

    public String getCacheKey() {
        return cacheKey;
    }
    
    public final Locale getLocale() {
        return locale;
    }
    
    public final String getDemiliter() {
        return demiliter;
    }
    
    public final String getLocaleQuery() {
        return localeQuery;
    }
    
    public boolean hasDefaultLocale() {
        return locale != null;
    }

    public String getColumnWrapText() {
        return columnWrapText;
    }

    public String wrap( String content ) {
        return isEmpty( columnWrapText ) ? content : columnWrapText + content + columnWrapText;
    }

    public String getDictTable( String language ) {
        return isEmpty( language ) ? dictTable : dictTable + "_" + language;
    }

    public String getLanguageTable() {
        return this.langTable;
    }
    
    public final String [] getExpands() {
        return expands;
    }
    
    public final int getErrorStatusCodeStartingValue() {
        return errorStatusCodeStartingValue;
    }

    public final LogLevel getLogLevel() {
        return logLevel;
    }

    public final boolean isLogEnable() {
        return logLevel.isEnable();
    }

    public final void printLog( String message ) {
        logLevel.print( message );
    }
    
    public final void printLog( String message, Object arg ) {
        logLevel.print( message, arg );
    }
    
    public final void printLog( String message, Object arg1, Object arg2 ) {
        logLevel.print( message, arg1, arg2 );
    }
    
    public final void printLog( String message, Object ... varargs ) {
        logLevel.print( message, varargs );
    }

    public static enum LogLevel {

        NONE, // Do nothing

        INFO {
            @Override
            public boolean isEnable() {
                return Helper.LOG.isInfoEnabled();
            }
            
            @Override
            public void print( String message ) {
                Helper.LOG.info( msg( message ) );
            }

            @Override
            public void print( String message, Object arg ) {
                Helper.LOG.info( msg( message ), arg );
            }

            @Override
            public void print( String message, Object arg1, Object arg2 ) {
                Helper.LOG.info( msg( message ), arg1, arg2 );
            }

            @Override
            public void print( String message, Object ... varargs ) {
                Helper.LOG.info( msg( message ), varargs );
            }
        },

        ERROR {
            @Override
            public boolean isEnable() {
                return Helper.LOG.isErrorEnabled();
            }
            
            @Override
            public void print( String message ) {
                Helper.LOG.error( msg( message ) );
            }

            @Override
            public void print( String message, Object arg ) {
                Helper.LOG.error( msg( message ), arg );
            }

            @Override
            public void print( String message, Object arg1, Object arg2 ) {
                Helper.LOG.error( msg( message ), arg1, arg2 );
            }

            @Override
            public void print( String message, Object ... varargs ) {
                Helper.LOG.error( msg( message ), varargs );
            }
        },

        DEBUG {
            @Override
            public boolean isEnable() {
                return Helper.LOG.isDebugEnabled();
            }
            
            @Override
            public void print( String message ) {
                Helper.LOG.debug( msg( message ) );
            }

            @Override
            public void print( String message, Object arg ) {
                Helper.LOG.debug( msg( message ), arg );
            }

            @Override
            public void print( String message, Object arg1, Object arg2 ) {
                Helper.LOG.debug( msg( message ), arg1, arg2 );
            }

            @Override
            public void print( String message, Object ... varargs ) {
                Helper.LOG.debug( msg( message ), varargs );
            }
        },

        WARN {
            @Override
            public boolean isEnable() {
                return Helper.LOG.isWarnEnabled();
            }

            @Override
            public void print( String message ) {
                Helper.LOG.warn( msg( message ) );
            }

            @Override
            public void print( String message, Object arg ) {
                Helper.LOG.warn( msg( message ), arg );
            }

            @Override
            public void print( String message, Object arg1, Object arg2 ) {
                Helper.LOG.warn( msg( message ), arg1, arg2 );
            }

            @Override
            public void print( String message, Object ... varargs ) {
                Helper.LOG.warn( msg( message ), varargs );
            }
        };
        
        protected String msg( String message ) {
            return Helper.getLogPrefix() + message;
        }

        public boolean isEnable() {
            return false;
        }
        
        public void print( String message ) {
            // Do nothing...
        }
        
        public void print( String message, Object arg ) {
            // Do nothing...
        }

        public void print( String message, Object arg1, Object arg2 ) {
            // Do nothing...
        }
        
        public void print( String message, Object ... varargs ) {
            // Do nothing...
        }

    }

}
