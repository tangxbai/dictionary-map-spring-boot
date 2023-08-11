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
package com.viiyue.plugins.dict.spring.boot.dialect;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;

/**
 * The default SQL resolver, provided according to the {@code MySQL} database.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class DefaultSqlResolver extends SqlResolver {

    private static final String COPY_TABLE = "CREATE TABLE IF NOT EXISTS {0} LIKE {1}";
    private static final String COPY_DATA = "INSERT INTO {0} SELECT * FROM {1}";

    private final String listDelimiter;
    private final String listPrefix, listSuffix;

    public DefaultSqlResolver( DictionaryProperties props ) {
        super( props );
        String wrapText = props.getColumnWrapText();
        this.listDelimiter = props.wrap( ", " );
        this.listPrefix = "(" + wrapText;
        this.listSuffix = wrapText + ")";
    }

    @Override
    public String check( String table ) {
        return MessageFormat.format( CHECK, props.wrap( table ) );
    }

    @Override
    public String query( String table ) {
        return query( table, EMPTY_STRING_ARRAY );
    }

    @Override
    public String query( String table, String ... conditions ) {
        String statement = MessageFormat.format( QUERY, props.wrap( table ) );
        if ( !isEmpty( conditions ) ) {
            statement += toWhere( conditions );
        }
        return statement;
    }
    
    @Override
    public String queryAndOrderBy( String table, String orderBy ) {
        return MessageFormat.format( QUERY_AND_ORDER_BY, props.wrap( table ), props.wrap( orderBy ) );
    }

    @Override
    public String queryWithin( String table, String column, Object ... values ) {
        StringJoiner joiner = new StringJoiner( ", ", "(", ")" );
        Arrays.stream( values ).forEach( v -> joiner.add( "?" ) );
        return query( table ) + " WHERE " + props.wrap( column ) + " IN " + joiner;
    }

    @Override
    public String insert( String table, String values ) {
        return MessageFormat.format( INSERT, props.wrap( table ), values );
    }

    @Override
    public String insert( String table, Object ... values ) {
        StringJoiner joiner = new StringJoiner( ", ", "(", ")" );
        Arrays.stream( values ).forEach( v -> joiner.add( "?" ) );
        return insert( table, joiner.toString() );
    }

    @Override
    public String insert( String table, Map<String, Object> map ) {
        StringJoiner columns = new StringJoiner( listDelimiter, listPrefix, listSuffix );
        StringJoiner values = new StringJoiner( ", ", "(", ")" );
        map.forEach( ( olumn, value ) -> {
            columns.add( olumn );
            values.add( "?" );
        } );
        return MessageFormat.format( INSERT, props.wrap( table ) + " " + columns, values );
    }

    @Override
    public String update( String table, String expression ) {
        return MessageFormat.format( UPDATE, props.wrap( table ), expression );
    }

    @Override
    public String update( String table, Map<String, Object> map ) {
        StringJoiner joiner = new StringJoiner( ", " );
        map.forEach( ( name, value ) -> joiner.add( props.wrap( name ) + " = ?" ) );
        return update( table, joiner.toString() );
    }

    @Override
    public String update( String table, String expression, String ... conditions ) {
        String statement = update( table, expression );
        if ( !isEmpty( conditions ) ) {
            statement += toWhere( conditions );
        }
        return statement;
    }

    @Override
    public String update( String table, Map<String, Object> map, String ... conditions ) {
        String statement = update( table, map );
        if ( !isEmpty( conditions ) ) {
            statement += toWhere( conditions );
        }
        return statement;
    }

    @Override
    public String delete( String table ) {
        return delete( table, EMPTY_STRING_ARRAY );
    }

    @Override
    public String delete( String table, String ... conditions ) {
        String statement = MessageFormat.format( DELETE, props.wrap( table ) );
        if ( !isEmpty( conditions ) ) {
            statement += toWhere( conditions );
        }
        return statement;
    }

    @Override
    public String copyTable( String source, String target ) {
        return MessageFormat.format( COPY_TABLE, props.wrap( target ), props.wrap( source ) );
    }

    @Override
    public String copyData( String source, String target ) {
        return MessageFormat.format( COPY_DATA, props.wrap( target ), props.wrap( source ) );
    }

    @Override
    public String drop( String table ) {
        return MessageFormat.format( DROP, props.wrap( table ) );
    }

    private String toWhere( String ... conditions ) {
        StringJoiner joiner = new StringJoiner( " AND " );
        Arrays.stream( conditions ).forEach( column -> joiner.add( props.wrap( column ) + " = ?" ) );
        return " WHERE " + joiner;
    }

}
