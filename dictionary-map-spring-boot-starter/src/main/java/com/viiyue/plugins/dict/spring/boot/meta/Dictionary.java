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

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.StringJoiner;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.viiyue.plugins.dict.spring.boot.utils.BeanMapper;

import lombok.Getter;
import lombok.Setter;

/**
 * Dictionary bean
 *
 * @author tangxbai
 * @since 1.0.0
 */
@Getter
@Setter
public class Dictionary extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -3550600220934757157L;
    private static final BeanMapper MAPPER = new BeanMapper( Dictionary.class );
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_ENUM = "ENUM";
    
    private Long id;
    private String type;
    private String key;
    private Integer code;
    private String alias;
    private String text;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private transient String token;

    public boolean enabled() {
        return Objects.equals( Boolean.TRUE, enabled );
    }

    public Dictionary enable() {
        this.enabled = Boolean.TRUE;
        return this;
    }

    public Dictionary disable() {
        this.enabled = Boolean.FALSE;
        return this;
    }

    public Dictionary asEnum() {
        this.type = TYPE_ENUM;
        return this;
    }

    public Dictionary asText() {
        this.type = TYPE_TEXT;
        return this;
    }

    public Dictionary forInsert() {
        this.createTime = LocalDateTime.now();
        return this;
    }

    public Dictionary forUpdate() {
        this.updateTime = LocalDateTime.now();
        return this;
    }

    public Object toObject( ParameterBridge bridge ) {
        if ( !enabled() ) {
            return null;
        }
        if ( TYPE_TEXT.equals( type ) ) {
            return text;
        }
        if ( TYPE_ENUM.equals( type ) ) {
            return MAPPER.toValues( this, false, bridge.props().getExpands() );
        }
        return this;
    }
    
    @Override
    public void onCreate() {
        if ( createTime == null ) {
            this.createTime = LocalDateTime.now();
        }
        if ( enabled == null ) {
            this.enabled = Boolean.TRUE;
        }
        if ( StringUtils.isEmpty( type ) ) {
            this.asText();
        }
    }

    @Override
    public void onUpdate() {
        if ( updateTime == null ) {
            this.updateTime = LocalDateTime.now();
        }
    }

    @Override
    public void onConstruct() {
        StringJoiner tokenzier = new StringJoiner( "." );
        tokenzier.add( String.valueOf( id ) );
        tokenzier.add( String.valueOf( type ) );
        tokenzier.add( String.valueOf( key ) );
        tokenzier.add( String.valueOf( code ) );
        tokenzier.add( String.valueOf( alias ) );
        tokenzier.add( String.valueOf( text ) );
        String original = tokenzier.toString();
        byte [] originals = original.getBytes( StandardCharsets.UTF_8 );
        setToken( DigestUtils.md5DigestAsHex( originals ) );
    }

    @Override
    public int hashCode() {
        return token == null ? super.hashCode() : token.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || ! ( obj instanceof Dictionary ) ) {
            return false;
        }
        return Objects.equals( token, ( ( Dictionary ) obj ).token );
    }

    @Override
    public String toString() {
        return String.format( "{key=%s, code=%d, alias=%s, text=%s}", key, code, alias, text );
    }

}
