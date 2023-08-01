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

import static com.viiyue.plugins.dict.spring.boot.utils.Helper.fristNonNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.IntUnaryOperator;

import javax.sql.DataSource;

import com.viiyue.plugins.dict.spring.boot.meta.BaseEntity;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;

/**
 * The abstract data update manager mainly provides some general CRUD and other methods.
 *
 * @author tangxbai
 * @since 1.0.0
 */
class UpdatableManager extends AbstractDbManager {
    
    private static final IntUnaryOperator operator = v -> v == Statement.SUCCESS_NO_INFO ? 1 : v < 0 ? 0 : v;

    private final int smallBatchSize;
    private final int bigBatchSize;

    public UpdatableManager( ParameterBridge bridge, DataSource dataSource ) {
        super( bridge, dataSource );
        this.smallBatchSize = bridge.props().getSmallBatchSize();
        this.bigBatchSize = bridge.props().getBigBatchSize();
    }

    /**
     * INSERT INTO {TABLE} ({COLUMN}, {COLUMN}, ...) VALUES (?, ?, ...)
     * 
     * @param <T>       the bean of {@link BaseEntity}
     * @param table     the target table name
     * @param creatable the auto create table sql statement, it can be {@code null}.
     * @param bean      the target bean object
     * @return the number of rows affected
     */
    public final <T extends BaseEntity> int insert( String table, T bean ) {
        bean.onCreate();
        Map<String, Object> mapping = toValues( bean, true, map -> {
            if ( bean.getId() == null && bridge.hasIdResolver() ) {
                map.put( "id", bridge.nextId() );
            }
        });
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Ready to insert data into the \"{}\" table ...", table );
            bridge.printLog( "The data map is: {}", mapping );
        }
        String sql = bridge.sql().insert( table, mapping );
        if ( bean.getId() == null && bridge.hasIdResolver() ) {
            bean.setId( bridge.nextId() );
            if ( bridge.isLogEnable() ) {
                bridge.printLog( "Generate the ID primary key value( {} )", bean.getId() );
            }
        }
        return execute( 0, sql, statement -> {
            int index = 1;
            for ( Map.Entry<String, Object> entry : mapping.entrySet() ) {
                statement.setObject( index ++, entry.getValue() );
            }
            return statement.executeUpdate();
        } );
    }

    /**
     * Batch insert
     * 
     * @param <T>   the bean of {@link BaseEntity}
     * @param table the target table name
     * @param beans the target data list
     * @return the number of rows affected
     */
    public final <T extends BaseEntity> int insertBatch( String table, List<T> beans ) {
        T firstItem = null;
        if ( isEmpty( beans ) || ( firstItem = fristNonNull( beans ) ) == null ) {
            return 0;
        }
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Ready to batch insert data into the \"{}\" table ...", table );
        }
        Class<? extends BaseEntity> beanType = firstItem.getClass();

        // OUTPUT -> (?, ?, ?, ...)
        StringJoiner value = new StringJoiner( ", ", "(", ")" );
        eachBean( beanType, property -> value.add( "?" ) );

        // Id resolver
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Prepare to generate ID primary key values in batches ..." );
        }
        for ( T bean : beans ) {
            if ( bean.getId() == null && bridge.hasIdResolver() ) {
                bean.setId( bridge.nextId() );
                bean.onCreate();
            }
        }
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Initializing the id value is complete" );
        }

        // Bean -> Map<String, Object>
        List<Map<String, Object>> mapValues = beans.stream().map( this::toValues ).collect( toList() );

        int rows, size = beans.size();
        if ( size <= smallBatchSize ) {
            if ( bridge.isLogEnable() ) {
                bridge.printLog( "The data size is {}, which is inserted using the minibatch scheme.", size );
            }

            // OUTPUT -> (?, ?, ...), (?, ?, ...), ...
            StringJoiner values = new StringJoiner( ", " );
            for ( int i = 0; i < size; i ++ ) {
                if ( beans.get( i ) != null ) {
                    values.add( value.toString() );
                }
            }

            // OUTPUT -> INSERT INTO {TABLE} VALUES (?, ?, ...), (?, ?, ...), ...
            String sql = bridge.sql().insert( table, values.toString() );
            rows = execute( 0, sql, statement -> {
                int index = 1;
                for ( Map<String, Object> mapping : mapValues ) {
                    if ( !isEmpty( mapping ) ) {
                        for ( Map.Entry<String, Object> entry : mapping.entrySet() ) {
                            statement.setObject( index ++, entry.getValue() );
                        }
                    }
                }
                return statement.executeUpdate();
            } );
        } else {
            if ( bridge.isLogEnable() ) {
                bridge.printLog( "Now that the data size exceeds {}, use the high-volume scheme.", size );
            }
            // INSERT INTO {TABLE} VALUES (?, ?, ...)
            String sql = bridge.sql().insert( table, value.toString() );
            rows = executeBatch( sql, mapValues, false );
        }
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Total: {}, Success: {}, Failure: {}", size, rows, size - rows );
        }
        return rows;
    }

    /**
     * UPDATE {TABLE} SET {COLUMN} = ?, {COLUMN} = ?, ... WHERE id = ?
     * 
     * @param <T>       the bean of {@link BaseEntity}
     * @param table     the target table name
     * @param creatable the auto create table sql statement, it can be {@code null}.
     * @param bean      the target bean object
     * @return the number of rows affected
     */
    public final <T extends BaseEntity> int update( String table, T bean ) {
        bean.onUpdate();
        Map<String, Object> mapping = toValues( bean, true );
        Object id = mapping.remove( "id" ); // Filter id
        String sql = bridge.sql().update( table, mapping, "id" );
        mapping.put( "id", id ); // Last value
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Ready to update table \"{}\" ...", table );
            bridge.printLog( "The data map is: {}", mapping );
        }
        return execute( 0, sql, statement -> {
            int index = 1;
            for ( Map.Entry<String, Object> entry : mapping.entrySet() ) {
                statement.setObject( index ++, entry.getValue() );
            }
            return statement.executeUpdate();
        } );
    }

    /**
     * Update batch
     * 
     * @param <T>   the bean of {@link BaseEntity}
     * @param table the target table name
     * @param beans the target data list
     * @return the number of rows affected
     */
    public final <T extends BaseEntity> int updateBatch( String table, List<T> beans ) {
        T firstItem = null;
        if ( isEmpty( beans ) || ( firstItem = fristNonNull( beans ) ) == null ) {
            return 0;
        }
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "Prepare to perform batch update of \"{}\" ...", table );
        }
        beans.stream().forEach( BaseEntity::onUpdate );
        Class<? extends BaseEntity> beanType = firstItem.getClass();

        // OUTPUT -> column = ?, column = ?, column = ?, ...
        StringJoiner expressions = new StringJoiner( ", " );
        eachBean( beanType, property -> {
            if ( !property.is( "id" ) ) {
                expressions.add( bridge.props().wrap( property.getColumn() ) + " = ?" );
            }
        } );

        // Bean -> Map<String, Object>
        List<Map<String, Object>> values = beans.stream().map( this::toValues ).collect( toList() );

        // UPDATE {TABLE} SET column = ?, column = ?, column = ?, ... WHERE id = ?
        String sql = bridge.sql().update( table, expressions.toString(), "id" );
        int rows = executeBatch( sql, values, true );
        if ( bridge.isLogEnable() ) {
            int size = beans.size();
            bridge.printLog( "Total: {}, Success: {}, Failure: {}", size, rows, size - rows );
        }
        return rows;
    }

    private <T extends BaseEntity> int executeBatch( String sql, List<Map<String, Object>> beans, boolean withId ) {
        return execute( 0, sql, statement -> {
            int row = 0;
            int size = beans.size();
            for ( int i = 0; i < size; i ++ ) {
                int index = 1;
                Map<String, Object> mapping = beans.get( i );
                if ( !isEmpty( mapping ) ) {
                    for ( Map.Entry<String, Object> entry : mapping.entrySet() ) {
                        if ( !withId || !Objects.equals( entry.getKey(), "id" ) ) {
                            statement.setObject( index ++, entry.getValue() );
                        }
                    }
                    if ( withId ) {
                        statement.setObject( index ++, mapping.get( "id" ) ); // WHERE id = ?
                    }
                }
                statement.addBatch();
                if ( ( i + 1 ) % bigBatchSize == 0 ) {
                    row += Arrays.stream( statement.executeBatch() ).map( operator ).sum();
                    statement.clearBatch();
                }
            }
            if ( size % bigBatchSize != 0 ) {
                row += Arrays.stream( statement.executeBatch() ).map( operator ).sum();
                statement.clearBatch();
            }
            return row;
        } );
    }

}
