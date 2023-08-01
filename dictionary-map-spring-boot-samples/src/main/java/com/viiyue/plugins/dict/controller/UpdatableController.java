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

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;

/**
 * Dictionary updatable API demos
 *
 * @author tangxbai
 * @since 1.0.0
 */
@RestController
@RequestMapping( "/updatable" )
public class UpdatableController {

    @Autowired
    private DictManager dictManager;

    /**
     * 单条字典数据插入
     * <p>
     * <b>POST</b> - /updatable/dict?lang=zh-CN
     * 
     * @param lang       指定语种
     * @param dictionary 字典数据
     */
    @PostMapping( "/dict" )
    public Boolean insert( @Nullable Locale lang, @NonNull Dictionary dictionary ) {
        return dictManager.add( lang, dictionary );
    }

    /**
     * 批量保存字典数据
     * <p>
     * <b>POST</b> - /updatable/dict/batchs?lang=zh-CN
     * 
     * @param lang         指定语种
     * @param dictionaries 字典列表
     */
    @PostMapping( "/dict/batches" )
    public Boolean addBatch( @Nullable Locale lang, @RequestBody List<Dictionary> dictionaries ) {
        return dictManager.addBatch( lang, dictionaries );
    }

    /**
     * 单个字典修改
     * <p>
     * <b>PUT</b> - /updatable/dict?lang=zh-CN
     * 
     * @param lang       指定语种
     * @param dictionary 字典数据
     */
    @PutMapping( "/dict" )
    public Integer update( @Nullable Locale lang, @NonNull Dictionary dictionary ) {
        return dictManager.update( lang, dictionary );
    }

    /**
     * 批量修改字典
     * <p>
     * <b>PUT</b> - /updatable/dict/batchs?lang=zh-CN
     * 
     * @param lang         指定语种
     * @param dictionaries 字典列表
     */
    @PutMapping( "/dict/batches" )
    public void updateBatch( @Nullable Locale lang, @RequestBody List<Dictionary> dictionaries ) {
        dictManager.updateBatch( lang, dictionaries );
    }

    /**
     * 更改字典启用状态
     * <p>
     * <b>PUT</b> - /updatable/dict/status?lang=zh-CN&key={key}&code={code}&enabled={true|false}
     * 
     * @param lang    语言，可为空，为空则删除所有字典下的指定数据。
     * @param key     字典 {@code key}，不允许为空。
     * @param code    字典 {@code code}，可为空，为空则忽略此条件。
     * @param enabled 启用状态
     */
    @PutMapping( "/dict/enable" )
    public Integer change( @Nullable Locale lang, @NonNull String key, @Nullable Integer code, boolean enabled ) {
        return dictManager.change( lang, key, code, enabled );
    }

    /**
     * 更改字典启用状态
     * <p>
     * <b>PUT</b> - /updatable/dict/status?key={key}&code={code}&enabled={true|false}
     * 
     * @param key     字典 {@code key}，不允许为空。
     * @param code    字典 {@code code}，可为空，为空则忽略此条件。
     * @param enabled 启用状态
     */
    @PutMapping( "/dict/enable/all" )
    public Integer changeAll( @NonNull String key, @Nullable Integer code, boolean enabled ) {
        return dictManager.changeAll( key, code, enabled );
    }
    
    /**
     * 从源字典复制一份到目标语言的字典中
     * <p>
     * <b>POST</b> - /updatable/dict/snapshot?source=zh-CN&target=zh-TW
     * 
     * @param source      源语言
     * @param destination 目标语言
     */
    @PostMapping( "/dict/snapshot" )
    public Boolean snapshot( @NonNull Locale source, @NonNull Locale target ) {
        return dictManager.addSnapshot( source, target );
    }

}
