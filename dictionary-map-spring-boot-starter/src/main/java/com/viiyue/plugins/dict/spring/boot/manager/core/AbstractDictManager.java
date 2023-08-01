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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;
import com.viiyue.plugins.dict.spring.boot.dialect.SqlResolver;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.dict.spring.boot.meta.Language;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;

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
        return cacheable.expandAll( null );
    }

    @Override
    public List<Dictionary> getAll() {
        return filter( cacheable.loadAll( null ) );
    }

    @Override
    public List<Dictionary> getAllAlways() {
        return cacheable.loadAll( null );
    }

    @Override
    public List<Dictionary> get( @NonNull String key ) {
        Assert.notNull( key, "Please specify a dictionary key" );
        return filter( cacheable.loadByKey( null, key ) );
    }

    @Override
    public List<Dictionary> getAlways( @NonNull String key ) {
        Assert.notNull( key, "Please specify a dictionary key" );
        return cacheable.loadByKey( null, key );
    }

    @Override
    public List<Dictionary> get( @NonNull String ... keys ) {
        return filter( getAlways( keys ) );
    }

    @Override
    public boolean add( @Nullable Locale locale, @NonNull Dictionary dict ) {
        Assert.notNull( dict, "Dictionary entry cannot be null" );
        String lang = bridge.toLanguage( locale );
        String table = bridge.props().getDictTable( lang );
        if ( updateable.insert( table, dict ) > 0 ) {
            cacheable.reloadKeyIfNecessary( lang, dict.getKey() );
            cacheable.reloadAllIfNecessary( lang );
            return true;
        }
        return false;
    }

    @Override
    public boolean addBatch( @Nullable Locale locale, @NonNull List<Dictionary> dictionaries ) {
        Assert.notEmpty( dictionaries, "Dictionary entries cannot be null or empty" );
        String lang = bridge.toLanguage( locale );
        String table = bridge.props().getDictTable( lang );
        if ( updateable.insertBatch( table, dictionaries ) > 0 ) {
            reloadKeys( lang, dictionaries );
            cacheable.reloadAllIfNecessary( lang );
            return true;
        }
        return false;
    }

    @Override
    public int update( @Nullable Locale locale, @NonNull Dictionary dict ) {
        Assert.notNull( dict, "Dictionary entry cannot be null" );
        Assert.notNull( dict.getId(), "Dictionary entry id cannot be null" );

        String lang = bridge.toLanguage( locale );
        Dictionary original = queryable.queryById( lang, dict.getId() );
        Assert.notNull( original, "The dictionary dose not exsit ( id: " + dict.getId() + " )" );

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
        }
        return updated;
    }

    @Override
    public int updateBatch( @Nullable Locale locale, @NonNull List<Dictionary> dictionaries ) {
        Assert.notEmpty( dictionaries, "Dictionary entries cannot be null or empty" );

        for ( int i = 0, s = dictionaries.size(); i < s; i ++ ) {
            Dictionary dict = dictionaries.get( i );
            Assert.notNull( dict, "Dictionary entry cannot be null( index: " + i + " )" );
            Assert.notNull( dict.getId(), "Dictionary entry \"id\" cannot be null( index: " + i + " )" );
        }

        String lang = bridge.toLanguage( locale );
        List<Long> ids = dictionaries.stream().map( Dictionary::getId ).distinct().collect( toList() );
        List<Dictionary> originals = queryable.queryByIds( lang, ids );

        String table = bridge.props().getDictTable( lang );
        int updated = updateable.updateBatch( table, dictionaries );
        if ( updated > 0 ) {
            Set<String> updatedKeys = collectKeys( dictionaries, toSet() );
            if ( !isEmpty( originals ) ) {
                updatedKeys.addAll( collectKeys( originals, toSet() ) );
            }
            if ( !isEmpty( updatedKeys ) ) {
                updatedKeys.forEach( key -> cacheable.reloadKeyIfNecessary( lang, key ) );
            }
            cacheable.reloadAllIfNecessary( lang );
        }
        return updated;
    }

    @Override
    public int change( @Nullable Locale locale, @NonNull String key, @Nullable Integer code, boolean enabled ) {
        Assert.notNull( key, "Please specify a dictionary key" );
        return change( bridge.toLanguage( locale ), key, code, enabled );
    }
    
    @Override
    public int changeAll( @NonNull String key, @Nullable Integer code, boolean status ) {
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
        Assert.notNull( key, "Please specify a dictionary key" );
        return remove( bridge.toLanguage( locale ), key, code );
    }
    
    @Override
    public int removeAll( @NonNull String key, @Nullable Integer code ) {
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
        Assert.notNull( language, "Language entry cannot be null" );
        DictionaryProperties props = bridge.props();
        boolean updated = updateable.insert( props.getLanguageTable(), language ) > 0;
        if ( updated ) {
            String source = props.getDictTable( null );
            String target = props.getDictTable( bridge.toLanguage( language.getLang() ) );
            updateable.update( bridge.sql().copyTable( source, target ) );
        }
        return updated;
    }

    @Override
    public int updateLanguage( @NonNull Language language ) {
        Assert.notNull( language, "Language entry cannot be null" );
        Assert.notNull( language.getId(), "Language entry id cannot be null" );
        return updateable.update( bridge.props().getLanguageTable(), language );
    }

    @Override
    public int removeLanguage( @NonNull Locale locale ) {
        Assert.notNull( locale, "Please specify a language" );
        DictionaryProperties props = bridge.props();
        String sql = bridge.sql().delete( props.getLanguageTable(), "lang" );
        int updated = updateable.execute( 0, sql, statement -> {
            statement.setObject( 1, locale.toLanguageTag() );
            return statement.executeUpdate();
        } );
        String lang = bridge.toLanguage( locale );
        sql = bridge.sql().drop( props.getDictTable( lang ) );
        updated += updateable.update( sql );
        cacheable.clearLanguage( lang );
        return updated;
    }

    @Override
    public boolean addSnapshot( @NonNull Locale source, @NonNull Locale target ) {
        Assert.notNull( source, "Please specify source language" );
        Assert.notNull( target, "Please specify the target language" );
        String sourceLang = bridge.toLanguage( source );
        String targetLang = bridge.toLanguage( target );
        Assert.state( !Objects.equals( sourceLang, targetLang ),
                "The target language cannot be the same as the source language" );
        Assert.state( existsLanguage( target ), "You need to add \"" + sourceLang + "\" language first" );
        SqlResolver resolver = bridge.sql();
        String sourceTable = bridge.props().getDictTable( sourceLang );
        String targetTable = bridge.props().getDictTable( targetLang );
        updateable.update( resolver.copyTable( sourceTable, targetTable ) );
        return updateable.update( resolver.copyData( sourceTable, targetTable ) ) > 0;
    }

    @Override
    public boolean existsLanguage( @NonNull Locale source ) {
        Assert.notNull( source, "Please specify a language" );
        String language = bridge.toLanguage( source );
        String langTable = bridge.props().getDictTable( language );
        String sql = bridge.sql().check( langTable );
        return queryable.execute( false, sql, statement -> {
            statement.executeQuery();
            return true;
        } );
    }

    private int change( String language, String key, Integer code, boolean enabled ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().update( table, bridge.props().wrap( "enabled" ) + " = ?",
                code == null ? CONDITIONS_KEY : CONDITIONS_KEY_AND_CODE );
        int updated = updateable.execute( 0, sql, statement -> {
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
        }
        return updated;
    }

    private int remove( String language, String key, Integer code ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().delete( table, code == null ? CONDITIONS_KEY : CONDITIONS_KEY_AND_CODE );
        int updated = updateable.execute( 0, sql, statement -> {
            statement.setObject( 1, key ); // key
            if ( code != null ) {
                statement.setObject( 2, code ); // code
            }
            return statement.executeUpdate();
        } );
        if ( updated > 0 ) {
            cacheable.reloadKeyIfNecessary( language, key );
            cacheable.reloadAllIfNecessary( language );
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
