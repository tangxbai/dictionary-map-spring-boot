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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viiyue.plugins.dict.spring.boot.annotation.Dict;
import com.viiyue.plugins.dict.spring.boot.annotation.FromHeader;
import com.viiyue.plugins.dict.spring.boot.annotation.FromSpring;
import com.viiyue.plugins.dict.spring.boot.meta.Dictionary;

import lombok.Getter;
import lombok.Setter;

/**
 * Example of automatic dictionary conversion
 *
 * @author tangxbai
 * @since 1.0.0
 */
@RestController
@RequestMapping( "/transfer" )
public class TransferController {
    
    /**
     * {@link Locale} 参数解析测试，可用多种姿势获取该参数。
     * 
     * @param source 从请求参数中获取
     * @param target 从请求参数中获取
     * @param accept 从请求头中获取
     * @param spring 从spring国际化中获取
     * @return 封装后的集合对象
     */
    @GetMapping( "/locale" )
    public Map<String, Locale> localeAutoConvert( 
        Locale source, // Query parameter( source=zh-CN )
        Locale target, // Query parameter( target=zh-CN )
        @FromHeader Locale accept, // Http header( "Accepte-Language": "zh-CN" )
        @FromSpring Locale spring  // org.springframework.web.servlet.LocaleResolver
    ) {
        Map<String, Locale> locales = new HashMap<String, Locale>( 4 );
        locales.put( "(query) source", source );
        locales.put( "(query) target", target );
        locales.put( "(header) Accept-Language", accept );
        locales.put( "(spring) LocaleResolver", spring );
        return locales;
    }
    
    /**
     * 字典 {@code code}/ {@code alias} 值自动转换为字典对象，参数必须使用 {@link @Dict} 标注才能正确找到字典数据。
     * <pre>
     * URL: /transfer/query/auto-wrapper?gender=1
     * URL: /transfer/query/auto-wrapper?gender=male
     * 
     * TO: "gender" = &lt;Dictionary&gt; // Bean
     * </pre>
     * 
     * @param gender 字典对象
     * @return 字典对象
     */
    @GetMapping( "/query/param/auto-wrapper" )
    public Dictionary paramAutoWrapper( @Dict( "user.gender" ) Dictionary gender ) {
        return gender;
    }
    
    /**
     * 自动封装到对象属性中
     * 
     * <pre>
     * URL: /transfer/query/auto-wrapper?gender=1
     * URL: /transfer/query/auto-wrapper?gender=male
     * 
     * TO: "gender" = &lt;Dictionary&gt; // Bean
     * </pre>
     * 
     * @param user 实体Bean
     * @return 实体Bean
     */
    @GetMapping( "/query/object/auto-wrapper" )
    public User formAutoWrapper( User user ) {
        return user;
    }
    
    /**
     * JSON参数中包含字典 {@code code}/ {@code alias} 值也可以自动转换为字典对象
     * <pre>
     * FROM: <hr>{
     *    "name": "tangxbai",
     *    "title": 0, // code or alias of dictionary
     *    "gender": 1 // code or alias of dictionary
     * }<hr>
     * 
     * TO: <hr>{
     *    "name": "tangxbai",
     *    "title": &lt;Dictionary&gt;, // Bean
     *    "gender": &lt;Dictionary&gt; // Bean
     * }<hr>
     * </pre>
     * 
     * @param user 实体Bean
     * @return 实体Bean
     */
    @GetMapping( "/body/auto-wrapper" )
    public User jsonAutoWrapper( @RequestBody User user ) {
        return user;
    }
    
    @Getter
    @Setter
    public static class User {
        private String name;

        // 自动转换的数据必须用 @Dict 指定字典 key
        @Dict( "settings.title" )
        private Dictionary title;

        // 自动转换的数据必须用 @Dict 指定字典 key
        @Dict( "user.gender" )
        private Dictionary gender;
    }
    
}
