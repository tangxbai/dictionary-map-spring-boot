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
package com.viiyue.plugins.dict.spring.boot.config.mybatis;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.function.SqlProvider;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;

/**
 * The dictionary handler for mybatis type convert
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class DictionaryTypeHandler extends BaseTypeHandler<Dictionary> {

    private static final ThreadLocal<Map<String, String>> cachings = new ThreadLocal<>();
    private final DictManager dictManager;
    
    public DictionaryTypeHandler( DictManager dictManager ) {
        this.dictManager = dictManager;
    }

    public void setCacheKeys( Map<String, String> descriptor ) {
        cachings.set( descriptor );
    }

    @Override
    public void setNonNullParameter( PreparedStatement ps, int i, Dictionary parameter, JdbcType jdbcType )
            throws SQLException {
        ps.setInt( i, parameter.getCode() );
    }

    @Override
    public Dictionary getNullableResult( ResultSet rs, String columnName ) throws SQLException {
        return getValue( columnName, rs.wasNull() ? null : rs::getInt );
    }

    @Override
    public Dictionary getNullableResult( ResultSet rs, int columnIndex ) throws SQLException {
        String column = rs.getMetaData().getColumnName( columnIndex );
        return getValue( column, rs.wasNull() ? null : rs::getInt );
    }

    @Override
    public Dictionary getNullableResult( CallableStatement cs, int columnIndex ) throws SQLException {
        String column = cs.getMetaData().getColumnName( columnIndex );
        return getValue( column, cs.wasNull() ? null : cs::getInt );
    }

    private <V> Dictionary getValue( String column, SqlProvider<String, Integer> provider ) throws SQLException {
        if ( provider == null ) {
            return null;
        }
        Map<String, String> cache = cachings.get();
        if ( cache != null ) {
            String cacheKey = cache.get( column );
            if ( cacheKey != null ) {
                Integer codeValue = provider.apply( column );
                return dictManager.match( cacheKey, codeValue );
            }
        }
        return null;
    }

}
