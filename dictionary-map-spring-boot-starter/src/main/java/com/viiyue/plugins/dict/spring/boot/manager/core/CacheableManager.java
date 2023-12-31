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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.dict.spring.boot.meta.Language;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;

/**
 * An abstract data cache manager for operating data and caching results
 *
 * @author tangxbai
 * @since 1.0.0
 */
class CacheableManager<K> extends AbstractManager {

    private static final Collector<Dictionary, ?, Map<Long, Dictionary>> TO_MAP = Collectors.toMap( Dictionary::getId,
            V -> V );

    private final QueryableManager queryable;
    private final CacheableResolver<K> valueResolver;
    final String delimiter, cacheKey, cacheAll, expandAll, languageListKey;

    public CacheableManager( ParameterBridge bridge, QueryableManager queryable, CacheableResolver<K> valueResolver,
            String delimiter ) {
        super( bridge );
        this.queryable = queryable;
        this.valueResolver = valueResolver;
        this.delimiter = delimiter;
        this.cacheKey = bridge.props().getCacheKey();
        this.cacheAll = bridge.toCacheKey( cacheKey, "all", delimiter );
        this.expandAll = bridge.toCacheKey( cacheKey, "expand", delimiter );
        this.languageListKey = bridge.toCacheKey( cacheKey, "languages", delimiter );
    }

    public String toCacheKey( String key ) {
        return bridge.toCacheKey( cacheKey, key, delimiter );
    }

    public K keyWithLanguage( String key, String language ) {
        return ( K ) ( isEmpty( language ) ? key : bridge.toCacheKey( key, language, delimiter ) );
    }

    public boolean clear( K key ) {
        return valueResolver.clear( key );
    }

    public void clearLanguage( String language ) {
        valueResolver.clearLanguage( language );
    }

    public List<Language> loadLanguages() {
        Object cachedValue = valueResolver.getValue( languageListKey );
        if ( cachedValue == null ) {
            cachedValue = queryable.queryLanguages();
            valueResolver.setValue( ( K ) languageListKey, cachedValue );
        }
        return ( List<Language> ) cachedValue;
    }

    public List<Dictionary> loadAll( String lang ) {
        return bridge.fallbackWithLanguage( lang, language -> {
            K languageKey = keyWithLanguage( cacheAll, language );
            Object cachedValue = loadAllObject( language, languageKey, true );
            if ( cachedValue != null ) {
                makeReferenceIfNecessary( lang, cacheAll, languageKey );
            }
            return theList( cachedValue );
        } );
    }

    public void reloadAllIfNecessary( String lang ) {
        // All list
        K allKey = keyWithLanguage( cacheAll, lang );
        if ( valueResolver.clear( allKey ) ) {
            loadAll( lang );
        }
        // Expanded list
        allKey = keyWithLanguage( expandAll, lang );
        if ( valueResolver.clear( allKey ) ) {
            expandAll( lang );
        }
    }

    public List<Dictionary> loadByKey( String lang, String key ) {
        Assert.notNull( key, "The cache key cannot be null" );
        String cacheKey = bridge.toCacheKey( null, key, delimiter );
        return bridge.fallbackWithLanguage( lang, language -> {
            K languageKey = keyWithLanguage( cacheKey, language );
            Object cachedValue = readObject( language, languageKey, true, () -> {
                List<Dictionary> valueList = queryable.queryByKey( language, key );
                if ( valueList != null ) {
                    valueResolver.setValue( languageKey, valueList );
                    makeReferenceIfNecessary( lang, cacheKey, languageKey );
                    refreshAllIfNecessary( valueList, changes -> {
                        valueResolver.setValue( keyWithLanguage( cacheAll, language ), changes );
                    } );
                }
                return valueList;
            } );
            return theList( cachedValue );
        } );
    }

    public void reloadKeyIfNecessary( String lang, String key ) {
        String cacheKey = toCacheKey( key );
        K keyWithLanguage = keyWithLanguage( cacheKey, lang );
        if ( valueResolver.clear( keyWithLanguage ) ) {
            loadByKey( lang, key );
        }
    }

    public Map<String, Object> expandAll( String lang ) {
        return bridge.fallbackWithLanguage( lang, language -> {
            K languageKey = keyWithLanguage( expandAll, language );
            Object cachedValue = readObject( language, languageKey, true, () -> {
                K allLanguageKey = keyWithLanguage( cacheAll, language );
                Object valueList = loadAllObject( language, allLanguageKey, false );
                if ( valueList != null ) {
                    Map<String, Object> expanded = expandAll( ( List<Dictionary> ) valueList );
                    valueResolver.setValue( languageKey, expanded );
                    return expanded;
                }
                return null;
            } );
            if ( cachedValue != null ) {
                makeReferenceIfNecessary( lang, expandAll, languageKey );
            }
            return theMap( cachedValue );
        } );
    }

    private Object loadAllObject( String language, K languageKey, boolean revert ) {
        return readObject( language, languageKey, revert, () -> {
            Object dbList = queryable.queryAll( language );
            if ( dbList != null ) {
                valueResolver.setValue( languageKey, dbList );
            }
            return dbList;
        } );
    }

    private Object readObject( String language, K languageKey, boolean revert, Supplier<Object> supplier ) {
        Object cachedValue = valueResolver.getValue( languageKey );
        if ( cachedValue == null ) {
            return supplier == null ? null : supplier.get();
        }
        if ( cachedValue instanceof String ) {
            cachedValue = revert ? getRervertedKeyOrValue( cachedValue.toString(), false ) : null;
        }
        return cachedValue;
    }

    private Map<String, Object> expandAll( List<Dictionary> dicts ) {
        if ( isEmpty( dicts ) ) {
            return Collections.emptyMap();
        }
        Map<String, Object> dictionary = new HashMap<>( 128 );
        for ( Dictionary dict : dicts ) {
            String key = dict.getKey();
            Object element = dict.toObject( bridge.props() );
            if ( element == null ) {
                continue;
            }
            if ( !key.contains( "." ) ) {
                dictionary.put( key, element );
                continue;
            }

            String [] keys = key.split( "\\." );
            Map<String, Object> last = dictionary;

            // Create each layer of containers
            int len = keys.length;
            for ( int i = 0, size = len - 1; i < size; i ++ ) {
                String namespace = keys[ i ];
                Object tempMap = last.get( namespace );
                if ( tempMap == null ) {
                    last.put( namespace, last = new HashMap<String, Object>( 6 ) );
                } else {
                    last = ( Map<String, Object> ) tempMap;
                }
            }

            // Put the data into the last container
            String lastNamespace = keys[ len - 1 ];
            Object lastObject = last.get( lastNamespace );
            if ( lastObject == null ) {
                last.put( lastNamespace, element );
            } else if ( lastObject instanceof List ) {
                List<Object> items = ( List<Object> ) lastObject;
                items.add( element );
            } else {
                List<Object> items = new ArrayList<>( 6 );
                items.add( lastObject );
                items.add( element );
                last.put( lastNamespace, items );
            }
        }
        return dictionary;
    }

    private final void refreshAllIfNecessary( List<Dictionary> dicts, Consumer<List<Dictionary>> changed ) {
        K allKey = keyWithLanguage( cacheAll, bridge.getLanguage() );
        if ( valueResolver.existsKey( allKey ) ) {
            List<Dictionary> allList = loadAll( null );
            if ( !isEmpty( allList ) ) {
                Map<Long, Dictionary> map = dicts.stream().collect( TO_MAP );
                boolean isChanged = allList.stream().filter( dict -> {
                    Dictionary item = map.get( dict.getId() );
                    return item != null && !item.equals( dict );
                } ).count() > 0;
                if ( isChanged ) {
                    List<Dictionary> changedList = allList.stream()
                            .map( dict -> map.getOrDefault( dict.getId(), dict ) ).collect( Collectors.toList() );
                    changed.accept( changedList );
                }
            }
        }
    }

    private List<Dictionary> theList( Object cached ) {
        return cached == null ? emptyList() : ( List<Dictionary> ) cached;
    }

    private Map<String, Object> theMap( Object cached ) {
        return cached == null ? emptyMap() : ( Map<String, Object> ) cached;
    }

    private void makeReferenceIfNecessary( String language, String key, K languageKey ) {
        if ( !Objects.equals( key, languageKey ) ) {
            K originalKey = keyWithLanguage( key, language == null ? bridge.getLanguage() : language );
            if ( !Objects.equals( originalKey, languageKey ) ) {
                valueResolver.setValue( originalKey, getRervertedKeyOrValue( languageKey, true ) );
            }
        }
    }

    private Object getRervertedKeyOrValue( Object specifiedKey, boolean returnKey ) {
        Object theKey = null;
        Object theValue = null;
        do {
            theKey = theValue == null ? specifiedKey : theValue;
            theValue = valueResolver.getValue( theKey );
        } while ( isEmpty( theValue ) || theValue instanceof String );
        return returnKey ? theKey : theValue;
    }

}
