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

import java.util.Map;

import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;

/**
 * An abstract SQL resolver for serving SQL statements for different platforms.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public abstract class SqlResolver {

    protected static final String[] EMPTY_STRING_ARRAY = {};
    /** SQL: {@value}, in order to detect the presence of a table. */
    protected static final String CHECK = "SELECT id FROM {0} LIMIT 1";
    /** SQL: {@value} */
    protected static final String QUERY = "SELECT * FROM {0}";
    /** SQL: {@value} */
    protected static final String QUERY_AND_ORDER_BY = "SELECT * FROM {0} ORDER BY {1}";
    /** SQL: {@value} */
    protected static final String INSERT = "INSERT INTO {0} VALUES {1}";
    /** SQL: {@value} */
    protected static final String UPDATE = "UPDATE {0} SET {1}";
    /** SQL: {@value} */
    protected static final String DELETE = "DELETE FROM {0}";
    /** SQL: {@value} */
    protected static final String DROP = "DROP TABLE {0}";

    protected final DictionaryProperties props;

    public SqlResolver( DictionaryProperties props ) {
        this.props = props;
    }

    public abstract String check( String table );

    public abstract String queryWithin( String table, String column, Object ... values );

    public abstract String query( String table );
    
    public abstract String queryAndOrderBy( String table, String orderBy );

    public abstract String query( String table, String ... conditions );
    
    public abstract String insert( String table, String values );

    public abstract String insert( String table, Object ... values );

    public abstract String insert( String table, Map<String, Object> map );

    public abstract String update( String table, String expression );

    public abstract String update( String table, Map<String, Object> map );

    public abstract String update( String table, String expression, String ... conditions );

    public abstract String update( String table, Map<String, Object> map, String ... conditions );

    public abstract String delete( String table );

    public abstract String delete( String table, String ... conditions );

    public abstract String copyTable( String target, String source );

    public abstract String copyData( String target, String source );

    public abstract String drop( String table );

}
