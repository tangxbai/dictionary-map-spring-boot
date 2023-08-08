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

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import com.viiyue.plugins.dict.spring.boot.DictManager;

/**
 * Provides dictionary type conversion support in the mybatis environment
 *
 * @author tangxbai
 * @since 1.0.0
 */
@ConditionalOnClass( { SqlSession.class, SqlSessionFactory.class } )
public class MybatisAutoConfiguration implements InitializingBean {

    private final DictManager dictManager;
    private final SqlSessionFactory factory;

    public MybatisAutoConfiguration( 
            ObjectProvider<SqlSessionFactory> sqlSessionProvider, 
            ObjectProvider<DictManager> dictManagerProvider ) {
        this.factory = sqlSessionProvider.getIfAvailable();
        this.dictManager = dictManagerProvider.getIfAvailable();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if ( factory != null ) {
            Configuration configuration = factory.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            typeHandlerRegistry.register( new DictionaryTypeHandler( dictManager ) );
            configuration.addInterceptor( new DictionaryInterceptor() );
        }
    }

}
