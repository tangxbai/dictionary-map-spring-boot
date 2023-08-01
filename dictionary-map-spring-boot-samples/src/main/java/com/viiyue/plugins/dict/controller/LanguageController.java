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
package com.viiyue.plugins.dict.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.meta.Language;

/**
 * Dictionary queryable API demos
 *
 * @author tangxbai
 * @since 1.0.0
 */
@RestController
@RequestMapping( "/language" )
public class LanguageController {
    
    @Autowired
    private DictManager dictManager;
    
    /**
     * 检测是否存在某个给定语种
     * 
     * @param lang 待查询语言
     * @return 查询结果
     */
    @GetMapping( "/exists" )
    public Boolean existsLanguage( Locale lang ) {
        return dictManager.existsLanguage( lang );
    }
    
    /**
     * 查询所有已有的语言列表
     * 
     * @return 所有语言列表数据
     */
    @GetMapping( "/list" )
    public List<Language> getLanguages() {
        return dictManager.getLanguages();
    }

    /**
     * 添加一个语种
     * 
     * @param language 语言数据，其中 {@code id} 不能为空。
     * @return 操作结果
     */
    @PostMapping
    public Boolean addLanguage( Language language ) {
        language.setCreateTime( LocalDateTime.now() );
        return dictManager.addLanguage( language );
    }
    
    /**
     * 更新语种基础信息
     * 
     * @param language 语言数据，其中 {@code id} 不能为空。
     * @return 受影响的行数
     */
    @PutMapping
    public Integer updateLanguage( Language language ) {
        return dictManager.updateLanguage( language );
    }
    
    /**
     * 删除一个语种
     * 
     * @param lang 待删除的语种
     * @return 受影响的行数
     */
    @DeleteMapping
    public Integer removeLanguage( Locale lang ) {
        return dictManager.removeLanguage( lang );
    }

}
