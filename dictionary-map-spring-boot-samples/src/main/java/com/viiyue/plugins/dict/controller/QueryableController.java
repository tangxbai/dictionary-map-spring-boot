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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viiyue.plugins.dict.mapper.UserMapper;
import com.viiyue.plugins.dict.model.User;
import com.viiyue.plugins.dict.spring.boot.DictManager;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;

/**
 * Dictionary queryable API demos
 *
 * @author tangxbai
 * @since 1.0.0
 */
@RestController
@RequestMapping( "/queryable" )
public class QueryableController {
    
    @Autowired
    private DictManager dictManager;
    
    @Autowired
    private UserMapper userMapper;
    
    @GetMapping("/user")
    public List<User> queryUserAll() {
        return userMapper.selectAll();
    }

    /**
     * 以JSON视图化的方式平铺展开字典数据
     * <pre style="background-color: #F5F5F5">
     * 
     * {
     *    "settings": {
     *        "copyright": "@2023",
     *        "title": "Dictionary map",
     *        "...": [ 1, 2, 3, 4, 5, ... ]
     *    },
     *    "user": {
     *        "gender": [
     *            {
     *                "code": 1,
     *                "text": "Male"
     *            },
     *            {
     *                "code": 2,
     *                "text": "Female"
     *            }
     *        ]
     *    }
     * }
     * </pre>
     * 
     * @return the json object
     */
    @GetMapping( "/expands" )
    public Map<String, Object> expandAll() {
        return dictManager.expandAll();
    }

    /**
     * 查询所有字典列表，忽略被禁用的数据。
     * 
     * @return
     */
    @GetMapping( "/all" )
    public List<Dictionary> getAll() {
        return dictManager.getAll();
    }

    /**
     * 查询所有字典列表，包括被禁用的数据。
     * 
     * @return
     */
    @GetMapping( "/all/ignore-status" )
    public List<Dictionary> getAllAlways() {
        return dictManager.getAllAlways();
    }
    
    /**
     * 查找指定 {@code key} 的字典项，被忽略的将无法查出出来。
     * 
     * @param key 指定字典{@code key}
     * @return
     */
    @GetMapping( "/filter" )
    public List<Dictionary> get( @NonNull String key ) {
        return dictManager.get( key );
    }

    /**
     * 查找指定 {@code key} 的字典项，被禁用的也会被查出来。
     * 
     * @param key 指定字典{@code key}
     * @return
     */
    @GetMapping( "/filter/ignore-status" )
    public List<Dictionary> getAlways( @NonNull String key ) {
        return dictManager.getAlways( key );
    }
    
    /**
     * 查询所有在 {@code key} 数组中的字典项，不包含被禁用的项。
     * 
     * @param key 指定字典{@code key}
     * @return
     */
    @GetMapping( "/filters" )
    public List<Dictionary> get( @NonNull String[] key ) {
        return dictManager.get( key );
    }
    
    /**
     * 查询所有在 {@code key} 数组中的字典项，包含被禁用的项。
     * 
     * @param key 指定字典{@code key}
     * @return
     */
    @GetMapping( "/filters/ignore-status" )
    public List<Dictionary> getAlways( @NonNull String[] key ) {
        return dictManager.getAlways( key );
    }

    /**
     * 精确匹配指定 {@code key} 和 {@code code} 的字典项，忽略被禁用的，并返回第一个符合的目标匹配项。
     * 
     * @param key  指定字典{@code key}
     * @param code 指定字典{@code code}
     * @return
     */
    @GetMapping( "/match/key-and-code" )
    public Dictionary match( @NonNull String key, @NonNull Integer code ) {
        return dictManager.match( key, code );
    }
    
    /**
     * 精确匹配指定 {@code key} 和 {@code code} 的字典项，包含被禁用的，并返回第一个符合的目标匹配项。
     * 
     * @param key  指定字典{@code key}
     * @param code 指定字典{@code code}
     * @return
     */
    @GetMapping( "/match/key-and-code/ignore-status" )
    public Dictionary matchAlways( @NonNull String key, @NonNull Integer code ) {
        return dictManager.matchAlways( key, code );
    }

    /**
     * 精确匹配指定 {@code key} 和 {@code alias} 的字典项，包含被禁用的，并返回第一个符合的目标匹配项。
     * 
     * @param key  指定字典{@code key}
     * @param code 指定字典{@code alias}
     * @return
     */
    @GetMapping( "/match/key-and-text" )
    public Dictionary match( @NonNull String key, @NonNull String alias ) {
        return dictManager.match( key, alias );
    }
    
    /**
     * 精确匹配指定 {@code key} 和 {@code alias} 的字典项，忽略被禁用的，并返回第一个符合的目标匹配项。
     * 
     * @param key  指定字典{@code key}
     * @param code 指定字典{@code alias}
     * @return
     */
    @GetMapping( "/match/key-and-text/ignore-status" )
    public Dictionary matchAlways( @NonNull String key, @NonNull String alias ) {
        return dictManager.matchAlways( key, alias );
    }
    
}
