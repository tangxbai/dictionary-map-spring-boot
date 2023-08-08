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
package com.viiyue.plugins.dict.model;

import com.viiyue.plugins.dict.spring.boot.annotation.Dict;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;
import com.viiyue.plugins.mybatis.annotation.bean.DefaultOrderBy;
import com.viiyue.plugins.mybatis.annotation.bean.Table;
import com.viiyue.plugins.mybatis.annotation.member.Id;
import com.viiyue.plugins.mybatis.annotation.rule.ExpressionRule;
import com.viiyue.plugins.mybatis.annotation.rule.NamingRule;
import com.viiyue.plugins.mybatis.annotation.rule.ValueRule;
import com.viiyue.plugins.mybatis.enums.ExpressionStyle;
import com.viiyue.plugins.mybatis.enums.NameStyle;
import com.viiyue.plugins.mybatis.enums.ValueStyle;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table( prefix = "t_" ) // BaseModel -> t_base_model
@NamingRule( NameStyle.UNDERLINE ) // filedName -> filed_name
@ValueRule( ValueStyle.JDBC_TYPE ) // #{filedName, jdbcType=TYPE}
@ExpressionRule( ExpressionStyle.DB_TYPE ) // fieldName = #{fieldName, jdbcType=TYPE}
@DefaultOrderBy( "#pk" ) // order by id desc
public class User {

    @Id
    private Long id;
    private String username;
    
    @Dict( "user.gender" )
    private Dictionary gender;
    
    @Dict( "settings.title" )
    private Dictionary title;
    
}
