/**
 * Copyright (C) 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.viiyue.plugins.dict.spring.boot.meta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Dictionary language bean
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Getter
@Setter
public class Language extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -7673341517954340202L;

    private Long id; // COLUMN
    @Getter( AccessLevel.NONE )
    @Setter( AccessLevel.NONE )
    private Locale lang; // COLUMN
    private String label; // COLUMN
    private LocalDateTime createTime; // COLUMN

    @Getter( AccessLevel.NONE )
    @Setter( AccessLevel.NONE )
    private transient String language;

    @Setter( AccessLevel.NONE )
    private transient String displayLabel;

    public String getLang() {
        return language;
    }

    public void setLang( Locale locale ) {
        this.lang = locale;
        this.language = locale.toLanguageTag();
    }

    public void setLang( String lang ) {
        if ( !StringUtils.isEmpty( lang ) ) {
            this.lang = Locale.forLanguageTag( lang );
            this.language = lang;
        }
    }

    public void setDisplayLabel( Locale locale ) {
        if ( locale != null && lang != null ) {
            this.displayLabel = lang.getDisplayName( locale );
        }
    }

    @Override
    public void onCreate() {
        if ( createTime == null ) {
            this.createTime = LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return language == null ? label : "\"" + language + "\" " + label;
    }

}
