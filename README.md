

### Dictionary Map

[![dictionary-map](https://img.shields.io/badge/plugin-dictionary--map--spring--boot--starter-green?style=flat-square)](https://github.com/tangxbai/dictionary-map)  ![size](https://img.shields.io/badge/size-70kB-green?style=flat-square) [![license](https://img.shields.io/badge/license-Apache%202-blue?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)



### 项目简介

基本每个项目或者系统都会有字典数据配置，而这又是一项重复不讨好的工作，就算写好了复制过去也相当麻烦，这些东西基本每个项目都大同小异，不会怎么变化，做国际化自适应也是一项很麻烦的事，所以这里为大家提供一套完整的字典操作Api，支持多语言、支持国际化、支持动态增删改查、支持国际化快速复制、支持字典对象自动转换等操作，这样就无须单独去维护这部分业务逻辑了。

*注意：此项目是一款完全开源的项目，您可以在任何适用的场景使用它，商用或者学习都可以，如果您有任何项目上的疑问，可以在issue上提出您问题，我会在第一时间回复您，如果您觉得它对您有些许帮助，希望能留下一个您的星星（★），谢谢。*

------

此项目遵照 [Apache 2.0 License]( http://www.apache.org/licenses/LICENSE-2.0.txt ) 开源许可 



### 核心亮点

- **支持国际化**：方便快捷的拓展语言种类，并支持从已有语种上拷贝备份；
- **支持语言扩展**：可随意添加或复制新的语种；
- **支持CRUD**：提供对字典表进行增删改查等操作，CRUD 等操作会自动更新缓存信息；
- **支持自动缓存**：数据操作过后自动缓存，并可以指定数据缓存方式（内存或者基于Redis）；                  
- **数据语义化**：通过将字典键进行处理，返回语义化的字典表数据（便于前端处理的格式）；
- **自动参数转换**：支持通过传递 code/alias 转换成字典实体对象，普通传参方式和 JavaBean 均可。
- **支持各种查询**：查询指定键的字典列表，或者精确匹配，亦或是查询所有字典列表等；
- **使用多样化**：提供静态上下文直接访问，或者是使用spring进行对象注入等；
- **自定义国际化方式**：可以通过请求头自动获取，也可以通过请求参数获取，更可以直接定死语言；



### 快速开始

Maven方式（**推荐**）

```xml
<dependency>
	<groupId>com.viiyue.plugins</groupId>
	<artifactId>dictionary-map</artifactId>
	<version>[VERSION]</version>
</dependency>
```

如果你没有使用Maven构建工具，那么可以通过以下途径下载相关jar包，并导入到你的编辑器。

[点击跳转下载页面](https://search.maven.org/search?q=g:com.viiyue.plugins%20AND%20a:dictionary-map&core=gav)

如何获取最新版本？[点击这里获取最新版本](https://search.maven.org/search?q=g:com.viiyue.plugins%20AND%20a:dictionary-map&core=gav)



### 核心注解

<table>
    <thead>
    	<tr>
            <th align="left">注解</th>
        	<th align="left">类型</th>
            <th align="left">描述</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>@Dict( "dict.key" )</td>
        	<td>Parameter( Controller ) | Field( Java bean )</td>
            <td>指定字典的 Key</td>
        </tr>
        <tr>
            <td>@FromHeader</td>
            <td>Parameter( Controller ) </td>
            <td>从 Header 中获取 Locale 对象</td>
        </tr>
        <tr>
            <td>@FromSpring</td>
            <td>Parameter( Controller ) </td>
            <td>通过 Spring 的 LocaleResolver 获取 Locale</td>
        </tr>
    </tbody>
</table>


### 偏好配置

<table>
    <thead>
    	<tr>
            <th width="20%" align="left">属性</th>
            <th width="50%" align="left">描述</th>
            <th width="15%" align="left">类型</th>
            <th width="15%" align="left">默认</th>
        </tr>
    </thead>
    <tbody>
    	<tr>
            <td>spring.dict.jackson.enable</td>
            <td>是否启用基于 JSON 的字典对象转换</td>
            <td>Boolean</td>
            <td>true</td>
        </tr>
        <tr>
            <td>spring.dict.redis-first</td>
            <td>在配置了 Redis 环境的前提下，优先使用 Redis 进行数据缓存</td>
            <td>Boolean</td>
            <td>true</td>
        </tr>
        <tr>
            <td>spring.dict.loaded-default</td>
            <td>是否在程序启动后，自动加载默认字典数据</td>
            <td>Boolean</td>
            <td>true</td>
        </tr>
        <tr>
            <td>spring.dict.locale</td>
            <td>设置默认 Locale，如果你使用了此属性，会优先使用此值作为主要语言</td>
            <td>Boolean</td>
            <td>true</td>
        </tr>
        <tr>
            <td>spring.dict.locale-query</td>
            <td>更改从请求参数中获取 Locale 的参数名</td>
            <td>Boolean</td>
            <td>lang</td>
        </tr>
        <tr>
            <td>spring.dict.locale-argument-resolver</td>
            <td>是否开启对于 Locale 参数的解析，需要配合 @FromHeader/@FromSpring 来使用</td>
            <td>Boolean</td>
            <td>true</td>
        </tr>
        <tr>
            <td>spring.dict.small-batch-size</td>
            <td>执行批量操作时，最小批量操作阈值</td>
            <td>Integer</td>
            <td>500</td>
        </tr>
        <tr>
            <td>spring.dict.big-batch-size</td>
            <td>执行批量操作时，大批量操作的批次处理阈值</td>
            <td>Integer</td>
            <td>1000</td>
        </tr>
        <tr>
            <td>spring.dict.cache-key</td>
            <td>数据缓存的前缀</td>
            <td>String</td>
            <td>cacheable:dict:</td>
        </tr>
        <tr>
            <td>spring.dict.dict-table</td>
            <td>默认字典表名</td>
            <td>String</td>
            <td>global_dictionary</td>
        </tr>
        <tr>
            <td>spring.dict.lang-table</td>
            <td>字典语言种类表</td>
            <td>String</td>
            <td>global_dictionary_lang</td>
        </tr>
        <tr>
            <td>spring.dict.column-wrap-text</td>
            <td>数据库列的包裹字符，用于区分关键字和自定义字符，避免自定义字符被认定为关键字</td>
            <td>String</td>
            <td><code>`</code></td>
        </tr>
    </tbody>
</table>



### 如何使用？

1、在启动类上启用支持

```java
@EnableDictionaryMap
@SpringBootApplication
public class Application {
    
    public static void main( String [] args ) throws Exception {
        SpringApplication.run( Application.class, args );
    }
    
}
```

2、获取引用，以下两种均可

```java
// 1、在 spring bean 中，通过注入的方式获取引用
@Autowired
private DictManager dictManager;
dictManager.xxx();

// 2、在普通类中直接使用静态上下文
DictManager dictManager = DictContext.manager();
dictManager.xxx();
```

2、设计的 API 列表，[ 点击查看接口文档](https://console-docs.apipost.cn/preview/bb2a2baf59e88546/4e611f53b633548e)

```java
// 国际化相关
List<Language> getLanguages(); // 查询所有语种
boolean addLanguage( Language language ); // 添加一个语种
int updateLanguage( Language language ); // 更新已有语种
int removeLanguage( Language language ); // 删除一个语种
boolean existLanguage( Language language ); // 检测是否存在某语种
boolean addSnapshot( Locale source, Locale destination ); // 从指定语言快速拷贝

// 字典相关
Map<String, Object> expandAll(); // 展开语义化的JSON数据格式
List<Dictionary> getAll(); // 查询所有字典列表【仅包含启用的】
List<Dictionary> getAllAlways(); // 查询所有字典列表【忽略状态】
List<Dictionary> get( String key ); // 查询指定键的字典列表【仅包含启用的】
List<Dictionary> getAlways( String key ); // 查询指定键的字典列表【忽略状态】
List<Dictionary> get( String ... key ); // 查询指定键的字典列表【仅包含启用的】
List<Dictionary> getAlways( String ... key ); // 查询指定键的字典列表【忽略状态】
Dictionary match( String key, Integer code ); // 精确匹配指定key和code的字典项【仅包含启用的】
Dictionary matchAlways( String key, Integer code ); // 精确匹配指定key和code的字典项【忽略状态】
Dictionary match( String key, String alias ); // 精确匹配指定key和alias的字典项【仅包含启用的】
Dictionary matchAlways( String key, String alias ); // 精确匹配指定key和alias的字典项【忽略状态】
Dictionary matching( String key, Predicate<Dictionary> predicate ); // 自定义精确匹配筛选
boolean add( Locale locale, Dictionary dict ); // 为某个语种添加一个字典项
boolean addBatch( Locale locale, List<Dictionary dictionaries ); // 为某个语种添加一批字典项
int update( Locale locale, Dictionary dict ); // 更新某个语种下的字典项
int updateBatch( Locale locale, List<Dictionary dictionaries ); // 批量更新某个语种下的字典项
boolean change( Locale locale, String key, Integer code, boolean status ); // 更改字典项状态【locale为空则更新所有】
boolean remove( Locale locale, String key, Integer code ); // 删除字典项【locale为空则更新所有】
```



### 切换国际化

1、通过配置项 `spring.dict.locale` 来指定语言环境，这种属于固定语言种类，无法切换其他语言；

2、通过请求参数中携带 `lang` 参数来确定语言，比如：`?lang=zh-CN`等，此属性可以通过 `spring.dict.locale-query` 配置项来更改；

3、通过请求头携带的语言，自动获取请求头 `Accept-Language` 中的指定的语言种类；

*请注意：决定国际化语言的 **优先级** 按照序号从高到低进行排列*



### 更改默认规则

```java
@Configuration
public class DictionaryConfiguration {
    
    private final DictionaryProperties props;
    
    public DictionaryConfiguration( DictionaryProperties props ) {
        this.props = props;
    }
    
    /**
     * (DEFAULT) 主键生成器
     * 
     * @return the id generator
     * @see com.viiyue.plugins.dict.spring.boot.config.DictionaryAuoConfiguration
     */
    @Bean
    public IdResolver idResolver() {
        return IdGenerator::nextId;
    }
    
    /**
     * (DEFAULT) 如果出现某SQL与数据库平台不符，可以通过此方式进行方法重载。
     * 
     * @return the sql resolver
     * @see com.viiyue.plugins.dict.spring.boot.config.DictionaryAuoConfiguration
     */
    @Bean
    @ConditionalOnMissingBean
    public SqlResolver sqlResolver() {
        return new DefaultSqlResolver( props );
    }
    
    /**
     * (DEFAULT) 定义字典国际化语言获取方式
     * 
     * @return the language resolver instance
     * @see com.viiyue.plugins.dict.spring.boot.config.DictionaryAuoConfiguration
     */
    @Bean
    @ConditionalOnMissingBean
    public LanguageResolver languageResolver() {
        return props.hasDefaultLocale() ? props::getLocale : (() -> {
            // 1. 尝试获取本地 HttpServletRequest（一般都能获取到）
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if ( attributes == null ) {
                return null;
            }
            HttpServletRequest request = ( ( ServletRequestAttributes ) attributes ).getRequest();
            if ( request == null ) {
                return null;
            }

            // 2. 首先尝试从请求参数中获取
            String queryName = props.getLocaleQuery();
            String language = request.getParameter( queryName ); // zh-CN
            if ( !StringUtils.isEmpty( language ) ) {
                language = language.replace( '_', '-' ); // Or zh_CN
                return Locale.forLanguageTag( language );
            }

            // 3. 如果请求中未获取到则尝试通过Spring的国际化解析器获取，还是未果则最后获取请求头中的语言设置。
            return RequestContextUtils.getLocale( request );
        });
    }
    
}
```



### 关于作者

- 邮箱：tangxbai@hotmail.com
- 掘金： https://juejin.im/user/5da5621ce51d4524f007f35f
- 简书： https://www.jianshu.com/u/e62f4302c51f
- Issuse：https://github.com/tangxbai/dictionary-map/issues
