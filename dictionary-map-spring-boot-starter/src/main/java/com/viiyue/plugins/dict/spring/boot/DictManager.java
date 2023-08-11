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
package com.viiyue.plugins.dict.spring.boot;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.dict.spring.boot.meta.Language;
import com.viiyue.plugins.dict.spring.boot.utils.Assert;

/**
 * Dictionary Core Manager, which is used to centrally maintain various APIs for dictionaries.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public interface DictManager {

    /**
     * Query the list of all existing languages
     * 
     * @return the languages
     */
    List<Language> getLanguages();

    // Core output

    /**
     * <p>
     * Expand all dictionary values and transform the data presentation
     * </p>
     * 
     * <strong>From</strong>:
     * <table border=1 width="100%">
     * <tr>
     * <th width="20%" >Column</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td align="center">test.abc.ddd
     * <td>Value</td>
     * </tr>
     * <tr>
     * <td align="center">test.abc.def
     * <td>A</td>
     * </tr>
     * <tr>
     * <td align="center">test.abc.def
     * <td>B</td>
     * </tr>
     * <tr>
     * <td align="center">test.abc.def
     * <td>C</td>
     * </tr>
     * </table>
     * 
     * <p>
     * <strong>TO</strong>:
     * 
     * <pre style="background: #f5f5f5">
     * 
     * {
     *     "test": {
     *         "abc": {
     *             "ddd": "Value",
     *             "def" : [ "A", "B", "C" ]
     *         }
     *     }
     * }
     * </pre>
     * 
     * @return the expanded values
     */
    Map<String, Object> expandAll();

    // Queries

    /**
     * Query all dictionaries
     * 
     * @return the dictionaries
     */
    List<Dictionary> getAll();

    /**
     * Query all dictionaries and <b>ignores status</b>
     * 
     * @return the dictionaries
     */
    List<Dictionary> getAllAlways();

    /**
     * Queries the dictionary value of the specified key
     * 
     * @param key the dictionary key
     * @return the dictionaries
     */
    List<Dictionary> get( @NonNull String key );

    /**
     * Queries the dictionary value of the specified key and <b>ignores status</b>
     * 
     * @param key the dictionary key
     * @return the dictionaries
     */
    List<Dictionary> getAlways( @NonNull String key );

    /**
     * Query dictionary values for multiple specified keys
     * 
     * @param keys the dictionary keys
     * @return the dictionaries
     */
    List<Dictionary> get( @NonNull String ... keys );

    /**
     * Query dictionary values for multiple specified keys and <b>ignores status</b>
     * 
     * @param keys the dictionary keys
     * @return the dictionaries
     */
    default List<Dictionary> getAlways( @NonNull String ... keys ) {
        Assert.notEmpty( keys, 5, "The cache keys cannot be null or empty" );
        List<Dictionary> dicts = new ArrayList<>( 16 );
        Arrays.asList( keys ).forEach( key -> dicts.addAll( getAlways( key ) ) );
        return dicts;
    }

    // Precise matching

    /**
     * Exactly match the target dictionary by {@code key} and {@code code}
     * 
     * @param key  the dictionary key
     * @param code the dictionary code value
     * @return the final matching dictionary value
     */
    default Dictionary match( @NonNull String key, Integer code ) {
        return matching( key, dict -> dict.enabled() && Objects.equals( dict.getCode(), code ) );
    }

    /**
     * Exactly match the target dictionary by {@code key} and {@code code} and <b>ignores status</b>
     * 
     * @param key  the dictionary key
     * @param code the dictionary code value
     * @return the final matching dictionary value
     */
    default Dictionary matchAlways( @NonNull String key, Integer code ) {
        return matching( key, dict -> Objects.equals( dict.getCode(), code ) );
    }

    /**
     * Exactly match the target dictionary by {@code key} and {@code text}
     * 
     * @param key   the dictionary key
     * @param alias the dictionary alias
     * @return the final matching dictionary value
     */
    default Dictionary match( @NonNull String key, String alias ) {
        return matching( key, dict -> dict.enabled() && Objects.equals( dict.getAlias(), alias ) );
    }

    /**
     * Exactly match the target dictionary by {@code key} and {@code text} and <b>ignores status</b>
     * 
     * @param key   the dictionary key
     * @param alias the dictionary alias
     * @return the final matching dictionary value
     */
    default Dictionary matchAlways( @NonNull String key, String alias ) {
        return matching( key, dict -> Objects.equals( dict.getAlias(), alias ) );
    }

    /**
     * Filter the dictionary by specifying keys and predicate functions
     * 
     * @param key       the dictionary key
     * @param predicate the filter function
     * @return the final matching dictionary value
     */
    default Dictionary matching( @NonNull String key, Predicate<Dictionary> predicate ) {
        Assert.notNull( key, 2, "Please specify a dictionary key" );
        if ( predicate == null ) {
            return null;
        }
        List<Dictionary> dicts = getAlways( key );
        if ( !isEmpty( dicts ) ) {
            for ( Dictionary dict : dicts ) {
                if ( predicate.test( dict ) ) {
                    return dict;
                }
            }
        }
        return null;
    }

    // CUD - Dictionary

    /**
     * Adds a dictionary entry to the dictionary table for the specified language
     * 
     * @param locale the language locale, which can be {@code null}. If null, means default.
     * @param dict   the dictionary entry
     * @return the result of the operation
     */
    boolean add( @Nullable Locale locale, @NonNull Dictionary dict );

    /**
     * Batch add dictionary entries to the dictionary table of the specified language
     * 
     * @param locale       the language locale, which can be {@code null}. If null, means default.
     * @param dictionaries the dictionary entries
     * @return the result of the operation
     */
    boolean addBatch( @Nullable Locale locale, @NonNull List<Dictionary> dictionaries );

    /**
     * Updates the dictionary information in the specified language
     * 
     * @param locale the language locale, which can be {@code null}. If null, means default.
     * @param dict   the dictionary entry
     * @return the number of rows affected
     */
    int update( @Nullable Locale locale, @NonNull Dictionary dict );

    /**
     * Batch update the dictionary information in the specified language
     * 
     * @param locale       the language locale, which can be {@code null}. If null, means default.
     * @param dictionaries the dictionary entries
     * @return the number of rows affected
     */
    int updateBatch( @Nullable Locale locale, @NonNull List<Dictionary> dictionaries );

    /**
     * Change the status of a dictionary group
     * 
     * @param locale  the language locale, which can be {@code null}. If null, means default.
     * @param key     the dictionary key, which cannot be {@code null}.
     * @param code    the dictionary code value, which can be {@code null}.
     * @param enabled the enabled status
     * @return the number of rows affected
     */
    int change( @Nullable Locale locale, @NonNull String key, @Nullable Integer code, boolean enabled );

    /**
     * Change the status of all dictionary dictionary groups
     * 
     * @param key    the dictionary key, which cannot be {@code null}.
     * @param code   the dictionary code value, which can be {@code null}.
     * @param status the enabled status
     * @return the number of rows affected
     */
    int changeAll( @NonNull String key, @Nullable Integer code, boolean status );

    /**
     * Remove the dictionary item of the specified language
     * 
     * @param locale the language locale, which can be {@code null}. If null, means default.
     * @param key    the dictionary key, which cannot be {@code null}.
     * @param code   the dictionary code value, which can be {@code null}.
     * @return the number of rows affected
     */
    int remove( @Nullable Locale locale, @NonNull String key, @Nullable Integer code );

    /**
     * Removes the data of the specified key from all dictionary tables
     * 
     * @param key  the dictionary key, which cannot be {@code null}.
     * @param code the dictionary code value, which can be {@code null}.
     * @return the number of rows affected
     */
    int removeAll( @NonNull String key, @Nullable Integer code );

    // CUD - Language

    /**
     * Add an internationalized language
     * 
     * @param language the language entry information
     * @return the result of the operation
     */
    boolean addLanguage( @NonNull Language language );

    /**
     * Update the internationalized language
     * 
     * @param language the language entry information, and the {@code id} cannot be null.
     * @return the number of rows affected
     */
    int updateLanguage( @NonNull Language language );

    /**
     * Remove an internationalized language
     * 
     * @param locale the language locale
     * @return the number of rows affected
     */
    int removeLanguage( @NonNull Locale locale );

    /**
     * Copy a snapshot from an existing dictionary
     * 
     * @param source the source language
     * @param target the target language
     * @return the result of the operation
     */
    boolean addSnapshot( @NonNull Locale source, @NonNull Locale target );

    /**
     * Check if a language exists
     * 
     * @param source the source language
     * @return {@code true} means that the language exists, otherwise it does not.
     */
    boolean existsLanguage( @NonNull Locale source );

}
