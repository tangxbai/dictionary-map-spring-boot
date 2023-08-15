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
package com.viiyue.plugins.dict.spring.boot.manager.core;

import static com.viiyue.plugins.dict.spring.boot.utils.Assert.ALREADY_EXISTS;
import static com.viiyue.plugins.dict.spring.boot.utils.Assert.CANNOT_BE_NULL_OR_EMPTY;
import static com.viiyue.plugins.dict.spring.boot.utils.Assert.CANNOT_BE_THE_SAME;
import static com.viiyue.plugins.dict.spring.boot.utils.Assert.DOSE_NOT_EXIST;
import static com.viiyue.plugins.dict.spring.boot.utils.Assert.ITEM_CANNOT_BE_NULL_OR_EMPTY;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;
import com.viiyue.plugins.dict.spring.boot.dialect.SqlResolver;
import com.viiyue.plugins.dict.spring.boot.exception.DictionaryException;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.dict.spring.boot.meta.Language;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;
import com.viiyue.plugins.dict.spring.boot.utils.Assert;

/**
 * An abstract dictionary manager, primarily used to implement interface methods in common parts.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public abstract class AbstractDictManager<K> extends CacheableResolver<K> implements DictManager {

    private static final String [] CONDITIONS_KEY = { "key" };
    private static final String [] CONDITIONS_KEY_AND_CODE = { "key", "code" };

    protected final ParameterBridge bridge;
    protected final QueryableManager queryable;
    protected final UpdatableManager updateable;
    protected final CacheableManager<K> cacheable;

    public AbstractDictManager( ParameterBridge bridge, DataSource datasource ) {
        this.bridge = bridge;
        this.queryable = new QueryableManager( bridge, datasource );
        this.updateable = new UpdatableManager( bridge, datasource );
        this.cacheable = new CacheableManager<K>( bridge, queryable, this, ":" );
    }

    @Override
    public List<Language> getLanguages() {
        List<Language> languages = cacheable.loadLanguages();
        if ( !isEmpty( languages ) ) {
            Locale locale = bridge.locale();
            languages.forEach( language -> language.setDisplayLabel( locale ) );
            if ( bridge.isLogEnable() ) {
                bridge.printLog( "Language list: {}", languages );
            }
        }
        return languages;
    }

    @Override
    public Map<String, Object> expandAll() {
        return cacheable.expandAll( bridge.getLanguage() );
    }

    @Override
    public List<Dictionary> getAll() {
        return filter( getAllAlways() );
    }

    @Override
    public List<Dictionary> getAllAlways() {
        return cacheable.loadAll( bridge.getLanguage() );
    }

    @Override
    public List<Dictionary> get( @NonNull String key ) {
        return filter( getAlways( key ) );
    }

    @Override
    public List<Dictionary> getAlways( @NonNull String key ) {
        Assert.notEmpty( CANNOT_BE_NULL_OR_EMPTY, key, "Please specify a dictionary key", "key" );
        return cacheable.loadByKey( bridge.getLanguage(), key );
    }

    @Override
    public List<Dictionary> get( @NonNull String ... keys ) {
        return filter( getAlways( keys ) );
    }

    @Override
    public boolean add( @Nullable Locale locale, @NonNull Dictionary dict ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, dict, "Dictionary entry cannot be null", "dict" );
        String lang = bridge.toLanguage( locale );
        String table = bridge.props().getDictTable( lang );
        if ( updateable.insert( table, dict ) > 0 ) {
            cacheable.reloadKeyIfNecessary( lang, dict.getKey() );
            cacheable.reloadAllIfNecessary( lang );
            cacheable.reloadExpandAllIfNecessary( lang );
            return true;
        }
        return false;
    }

    @Override
    public boolean addBatch( @Nullable Locale locale, @NonNull List<Dictionary> dictionaries ) {
        Assert.notEmpty( CANNOT_BE_NULL_OR_EMPTY, dictionaries, "Dictionaries cannot be null or empty", "dictionaries" );
        String lang = bridge.toLanguage( locale );
        String table = bridge.props().getDictTable( lang );
        if ( updateable.insertBatch( table, dictionaries ) > 0 ) {
            reloadKeys( lang, dictionaries );
            cacheable.reloadAllIfNecessary( lang );
            cacheable.reloadExpandAllIfNecessary( lang );
            return true;
        }
        return false;
    }

    @Override
    public int update( @Nullable Locale locale, @NonNull Dictionary dict ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, dict, "Dictionary entry cannot be null", "dict" );
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, dict.getId(), "Dictionary id cannot be null", "dict<id>" );

        String lang = bridge.toLanguage( locale );
        Dictionary original = queryable.queryById( lang, dict.getId() );
        Assert.notNull( DOSE_NOT_EXIST, original, "The dictionary dose not exsit ( id: {0} )", dict.getId() );

        String table = bridge.props().getDictTable( lang );
        int updated = updateable.update( table, dict );
        if ( updated > 0 ) {
            String updatedKey = dict.getKey();
            String originalKey = original.getKey();
            cacheable.reloadKeyIfNecessary( lang, updatedKey );
            if ( !Objects.equals( updatedKey, originalKey ) ) {
                cacheable.reloadKeyIfNecessary( lang, originalKey );
            }
            cacheable.reloadAllIfNecessary( lang );
            cacheable.reloadExpandAllIfNecessary( lang );
        }
        return updated;
    }

    @Override
    public int updateBatch( @Nullable Locale locale, @NonNull List<Dictionary> dictionaries ) {
        Assert.notEmpty( CANNOT_BE_NULL_OR_EMPTY, dictionaries, "Dictionaries cannot be null or empty", "dictionaries" );

        // Invalid check
        for ( int i = 0, s = dictionaries.size(); i < s; i ++ ) {
            Dictionary dict = dictionaries.get( i );
            Assert.notNull( ITEM_CANNOT_BE_NULL_OR_EMPTY, dict, "Entry cannot be null( index: {0} )", i );
            Assert.notNull( ITEM_CANNOT_BE_NULL_OR_EMPTY, dict.getId(), "The entry \"id\" cannot be null( index: {0} )", i );
        }
        
        // Query all keys before modification
        String lang = bridge.toLanguage( locale );
        List<Long> ids = dictionaries.stream().map( Dictionary::getId ).distinct().collect( toList() );
        List<Dictionary> originals = queryable.queryByIds( lang, ids );

        // Then perform the modify operation 
        String table = bridge.props().getDictTable( lang );
        int updated = updateable.updateBatch( table, dictionaries );
        if ( updated > 0 ) {
            
            // Refresh the cache
            Set<String> updatedKeys = collectKeys( dictionaries, toSet() );
            if ( !isEmpty( originals ) ) {
                updatedKeys.addAll( collectKeys( originals, toSet() ) );
            }
            if ( !isEmpty( updatedKeys ) ) {
                updatedKeys.forEach( key -> cacheable.reloadKeyIfNecessary( lang, key ) );
            }
            cacheable.reloadAllIfNecessary( lang );
            cacheable.reloadExpandAllIfNecessary( lang );
        }
        return updated;
    }

    @Override
    public int change( @Nullable Locale locale, @NonNull String key, @Nullable Integer code, boolean enabled ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, key, "Please specify a dictionary key", "key" );
        return change( bridge.toLanguage( locale ), key, code, enabled );
    }
    
    @Override
    public int changeAll( @NonNull String key, @Nullable Integer code, boolean status ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, key, "Please specify a dictionary key", "key" );
        int updated = 0;
        List<Language> languages = cacheable.loadLanguages();
        if ( !isEmpty( languages ) ) {
            for ( Language language : languages ) {
                updated += change( bridge.toLanguage( language.getLang() ), key, code, status );
            }
        }
        return updated;
    }

    @Override
    public int remove( @Nullable Locale locale, @NonNull String key, @Nullable Integer code ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, key, "Please specify a dictionary key", "key" );
        return remove( bridge.toLanguage( locale ), key, code );
    }
    
    @Override
    public int removeAll( @NonNull String key, @Nullable Integer code ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, key, "Please specify a dictionary key", "key" );
        int result = 0;
        List<Language> languages = cacheable.loadLanguages();
        if ( !isEmpty( languages ) ) {
            for ( Language language : languages ) {
                result += remove( bridge.toLanguage( language.getLang() ), key, code );
            }
        }
        return result;
    }

    @Override
    public boolean addLanguage( @NonNull Language language ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, language, "Language entry cannot be null", "language" );
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, language.getLang(), "Entry id cannot be null", "language<lang>" );
        String lang = language.getLang();
        SqlResolver sqlResolver = bridge.sql();
        DictionaryProperties props = bridge.props();
        String langTable = props.getLanguageTable();
        
        // Data configuration and check
        List<Language> languages = getLanguages();
        if ( !isEmpty( languages ) ) {
            // Fix the order index value
            if ( language.getOrderIndex() == null ) {
                OptionalInt optional = languages.stream().mapToInt( Language::getOrderIndex ).max();
                if ( optional.isPresent() ) {
                    language.setOrderIndex( optional.getAsInt() + 1 );
                }
            }
            // Detect if it has been added
            for ( Language ln : languages ) {
                if ( ln.sameWith( lang ) ) {
                    DictionaryException.throwing( ALREADY_EXISTS, "Language \"{0}\" already exists", lang );
                }
            }
        }
        
        String languageTag = bridge.toLanguage( lang );
        boolean updated = updateable.insert( langTable, language ) > 0;
        if ( updated ) {
            String source = props.getDictTable( null );
            String target = props.getDictTable( languageTag );
            updateable.update( sqlResolver.copyTable( source, target ) );
            cacheable.reloadLanguagesIfNecessary();
        }
        return updated;
    }

    @Override
    public int updateLanguage( @NonNull Language language ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, language, "Language entry cannot be null" );
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, language.getId(), "Entry id cannot be null", "language<lang>" );
        int row = updateable.update( bridge.props().getLanguageTable(), language );
        if ( row > 0 ) {
            cacheable.reloadLanguagesIfNecessary();
        }
        return row;
    }

    @Override
    public int removeLanguage( @NonNull Locale locale ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, locale, "Please specify a locale", "locale" );
        return updateable.doTransactional( 0, conn -> {
            DictionaryProperties props = bridge.props();
            String lang = bridge.toLanguage( locale );
            
            // First, Remove the language entry
            String sql = bridge.sql().delete( props.getLanguageTable(), "lang" );
            int row = updateable.doStatement( conn, 0, sql, statement -> {
                statement.setObject( 1, locale.toLanguageTag() );
                return statement.executeUpdate();
            });
            
            // Then delete the dictionary table for the language
            if ( row > 0 ) {
                sql = bridge.sql().drop( props.getDictTable( lang ) );
                row += updateable.doStatement( conn, 0, sql, statement -> statement.executeUpdate() );
            }
            
            // Finally, clean up the cache data.
            clearLanguage( lang );
            cacheable.reloadLanguagesIfNecessary();
            return row;
        });
    }

    @Override
    public boolean addSnapshot( @Nullable Locale source, @NonNull Locale target ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, target, "Please specify the target language", "target" );
        String sourceLang = bridge.toLanguage( source );
        String targetLang = bridge.toLanguage( target );
        Assert.isFalse( CANNOT_BE_THE_SAME, Objects.equals( sourceLang, targetLang ), 
                "The target language cannot be the same as the source", source, target );
        Assert.isFalse( DOSE_NOT_EXIST, existsLanguage( target ), "You need to add \"{0}\" language first", target );
        return updateable.doTransactional( false, conn -> {
            SqlResolver resolver = bridge.sql();
            String sourceTable = bridge.props().getDictTable( sourceLang );
            String targetTable = bridge.props().getDictTable( targetLang );
            updateable.update( resolver.copyTable( sourceTable, targetTable ) );
            boolean updated = updateable.update( resolver.copyData( sourceTable, targetTable ) ) > 0;
            if ( updated ) {
                reloadAllWhenKeyEndsWithLanguage( targetLang );
            }
            return updated;
        });
    }

    @Override
    public boolean existsLanguage( @NonNull Locale source ) {
        Assert.notNull( CANNOT_BE_NULL_OR_EMPTY, source, "Please specify a language", "source" );
        List<Language> languages = getLanguages();
        if ( !isEmpty( languages ) ) {
            for ( Language language : languages ) {
                if ( language.sameWith( source ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private void reloadAllWhenKeyEndsWithLanguage( String language ) {
        Set<K> keys = getKeys( language );
        if ( !isEmpty( keys ) ) {
            for ( K key : keys ) {
                String theKey = key.toString();
                if ( theKey.endsWith( language ) ) {
                    theKey = theKey.substring( 0, theKey.length() - language.length() - cacheable.delimiter.length() );
                }
                if ( Objects.equals( cacheable.cacheAll, theKey ) ) {
                    cacheable.reloadAllIfNecessary( language );
                } else if ( Objects.equals( cacheable.expandAll, theKey ) ) {
                    cacheable.reloadExpandAllIfNecessary( language );
                } else {
                    theKey = bridge.splitWithKey( theKey );
                    theKey = bridge.resetCacheKey( theKey, cacheable.delimiter );
                    cacheable.reloadKeyIfNecessary( language, theKey );
                }
            }
        }
    }

    private int change( String language, String key, Integer code, boolean enabled ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().update( table, bridge.props().wrap( "enabled" ) + " = ?",
                code == null ? CONDITIONS_KEY : CONDITIONS_KEY_AND_CODE );
        int updated = updateable.doExecute( 0, sql, statement -> {
            statement.setObject( 1, enabled ? 1 : 0 ); // enabled
            statement.setObject( 2, key ); // key
            if ( code != null ) {
                statement.setObject( 3, code ); // code
            }
            return statement.executeUpdate();
        } );
        if ( updated > 0 ) {
            cacheable.reloadKeyIfNecessary( language, key );
            cacheable.reloadAllIfNecessary( language );
            cacheable.reloadExpandAllIfNecessary( language );
        }
        return updated;
    }

    private int remove( String language, String key, Integer code ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().delete( table, code == null ? CONDITIONS_KEY : CONDITIONS_KEY_AND_CODE );
        int updated = updateable.doExecute( 0, sql, statement -> {
            statement.setObject( 1, key ); // key
            if ( code != null ) {
                statement.setObject( 2, code ); // code
            }
            return statement.executeUpdate();
        } );
        if ( updated > 0 ) {
            cacheable.reloadKeyIfNecessary( language, key );
            cacheable.reloadAllIfNecessary( language );
            cacheable.reloadExpandAllIfNecessary( language );
        }
        return updated;
    }

    private List<Dictionary> filter( List<Dictionary> dicts ) {
        if ( !isEmpty( dicts ) ) {
            return dicts.stream().filter( Dictionary::enabled ).collect( Collectors.toList() );
        }
        return dicts;
    }

    private <R> R collectKeys( List<Dictionary> dictionaries, Collector<? super String, ?, R> collector ) {
        return dictionaries.stream().map( Dictionary::getKey ).filter( Objects::nonNull ).distinct()
                .collect( collector );
    }

    private void reloadKeys( String language, List<Dictionary> dictionaries ) {
        dictionaries.stream().map( Dictionary::getKey ).distinct().forEach( key -> {
            cacheable.reloadKeyIfNecessary( language, key );
        } );
    }

}
