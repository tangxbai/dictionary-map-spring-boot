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
package com.viiyue.plugins.dict.spring.boot.meta;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import com.viiyue.plugins.dict.spring.boot.utils.Helper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The bridge of between java properties and database table columns
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
public class BeanProperty {

    private Field field;
    private String property;
    private Class<?> propertyType;
    private String column;
    private Method getter;
    private Method setter;
    private Class<?> setterParamType;

    public BeanProperty( Field field, PropertyDescriptor pd ) {
        this.field = field;
        this.property = field.getName();
        this.propertyType = field.getType();
        this.column = Helper.toUnderscore( field.getName() );
        this.getter = pd.getReadMethod();
        this.setter = pd.getWriteMethod();
        this.setterParamType = setter.getParameterTypes()[ 0 ];
    }

    public boolean is( String property ) {
        return Objects.equals( this.property, property ) || Objects.equals( this.column, property );
    }

    public final void setValue( Object instance, ResultSet result, int index ) throws SQLException {
        Object param = result.getObject( index, setterParamType );
        if ( param != null ) {
            if ( setterParamType == String.class && ! ( param instanceof String ) ) {
                param = param.toString();
            }
            setValue( instance, param );
        }
    }

    public final void setValue( Object instance, Object value ) {
        try {
            setter.invoke( instance, value );
        } catch ( IllegalAccessException e ) {
            log.error( e.getMessage(), e );
        } catch ( IllegalArgumentException e ) {
            log.error( e.getMessage(), e );
        } catch ( InvocationTargetException e ) {
            log.error( e.getMessage(), e );
        }
    }

    public final Object getValue( Object instance ) {
        try {
            return getter.invoke( instance );
        } catch ( IllegalAccessException e ) {
            log.error( e.getMessage(), e );
        } catch ( IllegalArgumentException e ) {
            log.error( e.getMessage(), e );
        } catch ( InvocationTargetException e ) {
            log.error( e.getMessage(), e );
        }
        return null;
    }

}
