## MOMO CODE SEC INSPECTOR

![Downloads](https://img.shields.io/jetbrains/plugin/d/15120-momo-code-sec-inspector-java-)
![Rating](https://img.shields.io/jetbrains/plugin/r/stars/15120-momo-code-sec-inspector-java-)
[![JetBrains IntelliJ Platform SDK Docs](https://jb.gg/badges/docs.svg)](http://www.jetbrains.org/intellij/sdk/docs)

本插件作为Java项目静态代码安全审计工具，侧重于在编码过程中发现项目潜在的安全风险，并提供一键修复能力。

本插件利用IDEA原生Inspection机制检查项目，自动检查当前活跃窗口的活跃文件，检查速度快，占用资源少。

插件提供的规则名称均以"<b>Momo</b>"开头。

### 目录

1. [版本支持](#版本支持)
2. [安装使用](#安装使用)
3. [效果展示](#效果展示)
4. [插件规则](#插件规则)
5. [贡献代码](#贡献代码)
6. [注意事项](#注意事项)
7. [关于我们](#关于我们)



### 版本支持

Intellij IDEA ( Community / Ultimate )  \>= 2017.3



### 安装使用

IDEA插件市场搜索"**immomo**"安装。

<img src="static/install.jpg" height="400">



### 效果展示

**演示一： XXE漏洞发现与一键修复**

<img src="static/show1.gif" height="400">

**演示二： Mybatis XML Mapper SQL注入漏洞发现与一键修复**

<img src="static/show2.gif" height="400">



### 插件规则

|编号|规则名称|修复建议|一键修复|
|-|-|-|-|
|1001|多项式拼接型SQL注入漏洞|<font color="#6abe83">T</font>||
|1002|占位符拼接型SQL注入漏洞|<font color="#6abe83">T</font>||
|1003|Mybatis注解SQL注入漏洞|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1004|Mybatis XML SQL注入漏洞|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1005|RegexDos风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1006|Jackson反序列化风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1007|Fastjson反序列化风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1008|Netty响应拆分攻击|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1009|固定的随机数种子风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1010|XXE漏洞|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1011|XStream反序列化风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1014|脆弱的消息摘要算法|<font color="#6abe83">T</font>||
|1015|过时的加密标准|<font color="#6abe83">T</font>||
|1016|XMLDecoder反序列化风险|<font color="#6abe83">T</font>||
|1017|LDAP反序列化风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1018|宽泛的CORS Allowed Origin设置|<font color="#6abe83">T</font>||
|1019|SpringSecurity关闭Debug模式|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1020|硬编码凭证风险|<font color="#6abe83">T</font>||
|1021|"@RequestMapping" 方法应当为 "public"|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1022|Spring 会话固定攻击风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1023|不安全的伪随机数生成器|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|
|1024|OpenSAML2 认证绕过风险|<font color="#6abe83">T</font>|<font color="#6abe83">T</font>|


### 贡献代码

#### 项目结构

```
src
├── main
│   ├── java
│   │   └── com
│   │       └── immomo
│   │           └── momosec
│   │               ├── aspect
│   │               ├── entity
│   │               ├── fix
│   │               ├── lang
│   │               │   ├── java
│   │               │   │   ├── rule
│   │               │   │   │   └── momosecurity
│   │               │   │   │       └── {InspectionName}.java
│   │               │   │   └── utils
│   │               │   └── xml
│   │               │       └── rule
│   │               │           └── momosecurity
│   │               │               └── {InspectionName}.java
│   │               └── utils
│   └── resources
│       ├── META-INF
│       │   ├── description.html
│       │   ├── pluginIcon.svg
│       │   └── plugin.xml
│       └── inspectionDescriptions
│           └── {InspectionName}.html
└── test
    ├── java
    │   └── com
    │       └── immomo
    │           └── momosec
    │               └── lang
    │                   ├── java
    │                   │   ├── fix
    │                   │   └── rule
    │                   │       └── momosecurity
    │                   │           └── {InspectionName}Test.java
    │                   └── xml
    │                       └── rule
    │                           └── momosecurity
    │                               └── {InspectionName}Test.java
    ├── resources
    └── testData
        └── lang
            ├── java
            │   └── rule
            │       └── momosecurity
            │           └── {InspectionName}
            │               └──...
            └── xml
                └── rule
                    └── momosecurity
                        └── {InspectionName}
                            └──...
```

#### 脚手架

```shell script
# 新增检查规则
> python script/addInspection.py

# 删除检查规则
> python script/deleteInspection.py
```

#### 单元测试

```shell script
> ./gradlew :test
```

#### 预发布打包

1. PLUGIN_BAN_CONST=true ./gradlew --no-daemon clean build -PMOMO_CODE_SEC_INSPECTOR_ENV=pre
2. build/distributions/*.zip 为待发布插件

预发布情况下，插件上报地址写于`src/main/resources/properties/pre.properties`

#### 发布打包

1. PLUGIN_BAN_CONST=true ./gradlew --no-daemon clean build -PMOMO_CODE_SEC_INSPECTOR_ENV=prod
2. build/distributions/*.zip 为待发布插件

正式发布情况下，插件上报地址写于`src/main/resources/properties/prod.properties`



### 注意事项

- 分支命名规则：

以版本号命名的分支，原则上代表支持的idea版本下限。

如branch为2018.3代表当前分支支持版本范围是>=2018.3 (或说from 183.* to *)。

插件具体支持idea版本范围见`gradle.properties`中`idea_since_build`与`idea_until_build`部分。

- 插件版本号命名规则：

原则上，插件版本号以支持的idea版本下限为大版本编号。

如插件当前版本为`x.1`，`x`为开发时所用IDEA版本编号，`.1`为插件发布版本。

需要注意的是，因IDEA更新机制问题，插件新版本号只能**向上增长**。

具体见`gradle.properties`的`plugin_version`字段。

- 版本号对应关系

|分支名|插件版本|IDEA版本|
|---|---|---|
|2018.3|193|2018.3.* <= x|
|2017.3|173|2017.3.* <= x <= 2018.2.*|

- JetBrains Plugins Marketplace 版本

发布到插件市场的版本不支持漏洞上报功能。

发布到插件市场的版本不支持白名单签名下发功能。



### 关于我们


> 陌陌安全致力于以务实的工作保障陌陌旗下所有产品及亿万用户的信息安全，以开放的心态拥抱信息安全机构、团队与个人之间的共赢协作，以自由的氛围和丰富的资源支撑优秀同学的个人发展与职业成长。


Website：https://security.immomo.com

WeChat:

<img src="https://momo-mmsrc.oss-cn-hangzhou.aliyuncs.com/img-1c96a083-7392-3b72-8aec-bad201a6abab.jpeg" width="200" hegiht="200" align="center" /><br>
