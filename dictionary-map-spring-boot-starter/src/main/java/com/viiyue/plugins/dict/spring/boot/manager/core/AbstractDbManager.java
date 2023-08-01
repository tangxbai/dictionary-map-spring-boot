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
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.Objects;

import javax.sql.DataSource;

import com.viiyue.plugins.dict.spring.boot.function.SqlProvider;
import com.viiyue.plugins.dict.spring.boot.meta.ParameterBridge;
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
        String sqlText = sqlString.substring( sqlString.indexOf( ':' ) + 2 );
        if ( !Objects.equals( sqlText, original ) && bridge.isLogEnable() ) {
            bridge.printLog( "==> {}", sqlText );
        }
    }

    protected int update( String sql ) {
        return execute( 0, sql, statement -> statement.executeUpdate() );
    }

    protected <R> R execute( R defValue, String sql, SqlProvider<PreparedStatement, R> fun ) {
        try {
            printSQL( sql );
            try ( Connection connection = dataSource.getConnection() ) {
                try ( PreparedStatement statement = connection.prepareStatement( sql ) ) {
                    R result = fun.apply( statement );
                    printSQL( sql, statement );
                    if ( bridge.isLogEnable() ) {
                        bridge.printLog( "The statement executed successfully" );
                    }
                    return result;
                }
            }
        } catch ( Exception e ) {
            boolean handled = false;
            if ( e instanceof SQLSyntaxErrorException ) {
                String state = ( ( SQLSyntaxErrorException ) e ).getSQLState();
                if ( Objects.equals( SQL_STATE_TABLE_OR_VIEW_NOT_FOUND, state ) ) {
                    handled = true;
                    if ( Helper.LOG.isWarnEnabled() ) {
                        Helper.LOG.warn( "==> {}", e.getMessage() );
                    }
                }
            }
            if ( !handled ) {
                if ( Helper.LOG.isErrorEnabled() ) {
                    log.error( e.getMessage(), e );
                } else {
                    e.printStackTrace();
                }
            }
            return defValue;
        }
    }

}
