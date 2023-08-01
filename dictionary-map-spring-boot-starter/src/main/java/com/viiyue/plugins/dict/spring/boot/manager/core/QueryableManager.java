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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.sql.DataSource;

import com.viiyue.plugins.dict.spring.boot.function.SqlConsumer;
import com.viiyue.plugins.dict.spring.boot.meta.BaseEntity;
import com.viiyue.plugins.dict.spring.boot.meta.BeanProperty;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.dict.spring.boot.meta.Language;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;

/**
 * Abstract data query manager, mainly provides some general query methods.
 *
 * @author tangxbai
 * @since 1.0.0
 */
class QueryableManager extends AbstractDbManager {

    public QueryableManager( ParameterBridge bridge, DataSource dataSource ) {
        super( bridge, dataSource );
    }

    public final List<Language> queryLanguages() {
        String table = bridge.props().getLanguageTable();
        return queryList( Language.class, "language", bridge.sql().query( table ), Language::new, null );
    }

    public Dictionary queryById( String language, Long id ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().query( table, "id" );
        List<Dictionary> dictionaries = queryList( Dictionary.class, language, sql, Dictionary::new, statement -> {
            statement.setObject( 1, id );
        } );
        return dictionaries == null || dictionaries.size() >= 1 ? dictionaries.get( 0 ) : null;
    }

    public List<Dictionary> queryByIds( String language, List<Long> ids ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().queryWithin( table, "id", ids.toArray() );
        return queryList( Dictionary.class, language, sql, Dictionary::new, statement -> {
            for ( int i = 0, s = ids.size(); i < s; i ++ ) {
                statement.setObject( i + 1, ids.get( i ) );
            }
        } );
    }

    public final List<Dictionary> queryAll( String language ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().query( table );
        return queryList( Dictionary.class, language, sql, Dictionary::new, null );
    }

    public final List<Dictionary> queryByKey( String language, String key ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().query( table, "key" );
        return queryList( Dictionary.class, language, sql, Dictionary::new, statement -> {
            statement.setObject( 1, key );
        } );
    }

    public final List<Dictionary> queryIn( String language, Object ... keys ) {
        String table = bridge.props().getDictTable( language );
        String sql = bridge.sql().queryWithin( table, "key", keys );
        return queryList( Dictionary.class, language, sql, Dictionary::new, statement -> {
            for ( int i = 0, len = keys.length; i < len; i ++ ) {
                statement.setObject( i + 1, keys[ i ] );
            }
        } );
    }

    public final <T extends BaseEntity> List<T> queryList( Class<?> beanType, String language, String sql, Supplier<T> supplier,
            SqlConsumer<PreparedStatement> consumer ) {
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Try Querying \"{}\" data from the database ...", language );
        }
        return execute( null, sql, statement -> {
            if ( consumer != null ) {
                consumer.apply( statement );
            }
            ResultSet result = statement.executeQuery();
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();
            List<T> dicts = new ArrayList<>( columnCount );
            while ( result.next() ) {
                T instance = supplier.get();
                for ( int i = 1; i <= columnCount; i ++ ) {
                    String column = metadata.getColumnLabel( i );
                    BeanProperty property = getProperty( beanType, column );
                    if ( property != null ) {
                        property.setValue( instance, result, i );
                    }
                }
                instance.onConstruct();
                dicts.add( instance );
            }
            if ( bridge.isLogEnable() ) {
                int size = dicts.size();
                if ( dicts.size() == 1 ) {
                    bridge.printLog( "One piece of data was queried in the database." );
                } else {
                    bridge.printLog( "{} pieces of data were queried in the database.", size );
                }
            }
            return dicts;
        } );
    }

}
