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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.Objects;

import javax.sql.DataSource;

import com.viiyue.plugins.dict.spring.boot.function.SqlProvider;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;
import com.viiyue.plugins.dict.spring.boot.utils.Assert;
import com.viiyue.plugins.dict.spring.boot.utils.Helper;

import lombok.extern.slf4j.Slf4j;

/**
 * An abstract database manager to provide a unified SQL execution method.
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Slf4j
class AbstractDbManager extends AbstractManager {

    private static final String SQL_STATE_TABLE_OR_VIEW_NOT_FOUND = "42S02"; // For MYSQL

    final DataSource dataSource;

    public AbstractDbManager( ParameterBridge bridge, DataSource dataSource ) {
        super( bridge );
        this.dataSource = dataSource;
    }

    private void printSQL( String sql ) {
        if ( bridge.isLogEnable() ) {
            bridge.printLog( "==> {}", sql );
        }
    }

    private void printSQL( String original, Statement statement ) {
        String sqlString = statement.toString();
        int index = sqlString.indexOf( ':' );
        String sqlText = index >= 0 ? sqlString.substring( index + 2 ) : sqlString;
        if ( !Objects.equals( sqlText, original ) && bridge.isLogEnable() ) {
            bridge.printLog( "==> {}", sqlText );
        }
    }

    private void printError( Exception e ) {
        if ( Helper.LOG.isErrorEnabled() ) {
            log.error( e.getMessage(), e );
        } else {
            e.printStackTrace();
        }
    }

    private boolean handleTableNotExist( Exception e ) {
        if ( e instanceof SQLSyntaxErrorException ) {
            String state = ( ( SQLSyntaxErrorException ) e ).getSQLState();
            if ( Objects.equals( SQL_STATE_TABLE_OR_VIEW_NOT_FOUND, state ) ) {
                if ( Helper.LOG.isWarnEnabled() ) {
                    Helper.LOG.warn( "==> {}", e.getMessage() );
                }
                return true;
            }
        }
        return false;
    }

    protected int update( String sql ) {
        return doExecute( 0, sql, statement -> statement.executeUpdate() );
    }

    protected <R> R doStatement( Connection conn, R defValue, String sql, SqlProvider<PreparedStatement, R> fun )
            throws SQLException {
        try ( PreparedStatement statement = conn.prepareStatement( sql ) ) {
            R result = fun.apply( statement );
            printSQL( sql, statement );
            if ( bridge.isLogEnable() ) {
                bridge.printLog( "The statement executed successfully" );
            }
            return result;
        }
    }

    protected <R> R doExecute( R defValue, String sql, SqlProvider<PreparedStatement, R> fun ) {
        try {
            printSQL( sql );
            try ( Connection conn = dataSource.getConnection() ) {
                return doStatement( conn, defValue, sql, fun );
            }
        } catch ( Exception e ) {
            if ( !handleTableNotExist( e ) ) {
                printError( e );
            }
            return defValue;
        }
    }

    protected <R> R doTransactional( R defValue, SqlProvider<Connection, R> fun ) {
        Assert.notNull( fun, 1, "You must specify a Function interface" );
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit( false );
            R result = fun.apply( connection );
            connection.commit();
            return result;
        } catch ( Exception e ) {
            try {
                connection.rollback();
            } catch ( SQLException e1 ) {
                printError( e );
            }
            if ( !handleTableNotExist( e ) ) {
                printError( e );
            }
            return defValue;
        } finally {
            if ( connection != null ) {
                try {
                    connection.setAutoCommit( true );
                    connection.close();
                } catch ( SQLException e ) {
                    printError( e );
                }
            }
        }
    }
    
}
