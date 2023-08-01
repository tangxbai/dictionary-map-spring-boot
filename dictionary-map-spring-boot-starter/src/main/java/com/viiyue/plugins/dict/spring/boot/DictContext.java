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
package com.viiyue.plugins.dict.spring.boot;

import java.util.concurrent.atomic.AtomicReference;

import com.viiyue.plugins.dict.spring.boot.config.DictionaryProperties;

/**
 * Dictionary manager context
 *
 * @author tangxbai
 * @since 1.0.0
 */
public final class DictContext {

    private static final AtomicReference<DictManager> INSTANCE = new AtomicReference<>();
    private static final AtomicReference<DictionaryProperties> SETTINGS = new AtomicReference<>();

    private DictContext() {}

    public static final DictManager manager() {
        return INSTANCE.get();
    }
    
    public static final void managerConfigurer( DictManager manager ) {
        if ( INSTANCE.get() == null ) {
            synchronized ( INSTANCE ) {
                if ( INSTANCE.get() == null ) {
                    INSTANCE.set( manager );
                }
            }
        }
    }
    
    public static final DictionaryProperties settings() {
        return SETTINGS.get();
    }
    
    public static final void settingConfigurer( DictionaryProperties settings ) {
        if ( SETTINGS.get() == null ) {
            synchronized ( SETTINGS ) {
                if ( SETTINGS.get() == null ) {
                    SETTINGS.set( settings );
                }
            }
        }
    }

}
