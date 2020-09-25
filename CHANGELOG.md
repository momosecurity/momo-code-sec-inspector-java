# Changelog

All notable changes to this project will be documented in this file.

Main version site uses 'x' stand for idea main version. 

## [x.7]

### BugFix

- 解决order by with limit end 修复错误

### Modify

- 上报内容增加插件版本信息
- 优化打包流程

## [x.6]

### BugFix

- 解决Jackson用于Redis缓存时的误报

## [x.5]

### BugFix

- 解决拼接语句误报为SQL语句问题 

## [x.4]

### Added

- 漏洞签名白名单功能

### Modify

- 增加获取Element的FQName方法
- 外提计算漏洞签名算法
- MomoBootConfiguration后缀调整

### BugFix

- 解决SQL拼接Insert Field误报
- SQL拼接判断时，增加对拼接变量类型的判断

## [x.3]

### BugFix

- 解决SQL拼接注入Field嵌套误报
- 解决SQL拼接检查insert value/values误报

## [x.2]

### BugFix

- 细化对MOMOBoot使用判断，降低MOMOBoot规则误报

## [x.1]

### Added

- 1017: LDAP反序列化风险
- 1018: 宽泛的CORS Allowed Origin设置
- 1019: SpringSecurity关闭Debug模式

### Modify

- 拆分MOMOBoot规则包名

### BugFix

- Constants Inputstream null判断

## [x.0]

### Added

- 1001: 多项式拼接型SQL注入漏洞
- 1002: 占位符拼接型SQL注入漏洞
- 1003: Mybatis注解SQL注入漏洞
- 1004: Mybatis XML SQL注入漏洞
- 1005: RegexDos风险
- 1006: Jackson反序列化风险
- 1007: Fastjson反序列化风险
- 1008: Netty响应拆分攻击
- 1009: 固定的随机数种子风险
- 1010: XXE漏洞
- 1011: XStream反序列化风险
- 1012: MomoBoot配置类命名不规范
- 1013: MomoBoot属性配置类命名不规范
- 1014: 脆弱的消息摘要算法
- 1015: 过时的加密标准
- 1016: XMLDecoder反序列化风险

