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

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viiyue.plugins.dict.spring.boot.DictManager;

/**
 * Dictionary removable API demos
 *
 * @author tangxbai
 * @since 1.0.0
 */
@RestController
@RequestMapping( "/removable" )
public class RemovableController {
    
    @Autowired
    private DictManager dictManager;

    /**
     * 删除指定语言的字典项
     * <p>
     * <b>DELETE</b> - /updatable/dict?lang=zh-CN&key={key}&code={code}
     * 
     * @param lang 语言，可为空，为空则删除所有字典下的指定数据。
     * @param key  字典 {@code key}，不允许为空。
     * @param code 字典 {@code code}，可为空，为空则忽略此条件。
     */
    @DeleteMapping( "/dict" )
    public Integer remove( @Nullable Locale lang, @NonNull String key, @Nullable Integer code ) {
        return dictManager.remove( lang, key, code );
    }

    /**
     * 在所有字典中删除符合指定条件的条目
     * <p>
     * <b>DELETE</b> - /updatable/dict/all?key={key}&code={code}
     * 
     * @param key  字典 {@code key}，不允许为空。
     * @param code 字典 {@code code}，可为空，为空则忽略此条件。
     */
    @DeleteMapping( "/dict/all" )
    public Integer removeAll( @NonNull String key, @Nullable Integer code ) {
        return dictManager.removeAll( key, code );
    }

}
