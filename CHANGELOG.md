# Changelog

All notable changes to this project will be documented in this file.

Main version site uses 'x' stand for idea main version. 

## [x.20.1]

### BugFix

- Plugin Icon

## [x.20]

### BugFix

- PlaceholderStringSQLi fix literal placeholder

## [x.19]

### BugFix

- Mybatis XML whereIn contains blank

## [x.18]

### BugFix

- Mybatis XML prefix bug of \${var}

## [x.17]

### Modify

- 1003: Ignore Mybatis wrapper "ew.*" segment
- Optimize sql injection judgement
- Remove unused code and dependencies

## [x.16]

### Modify

- 1011: Change risk comment and description.

## [x.15]

### Modify

- 1004: Fix false positive on mybatis plus

## [x.14]

### Added

- 1025: HardcodedIp

### Modify

- 1020: support jdbc url and except not ASCII text
- vendor change to immomo.com
- add Chinese description

### BugFix

- aspect annotation support 'SetBoolArgQuickFix'

## [x.13]

### Added

- 1021: PublicControllerOnSpring
- 1022: SpringSessionFixProtection
- 1023: ReplacePseudorandomGenerator
- 1024: OpenSAML2IgnoreComment

### Modify

- 1009: support New SecureRandom Expression
- Github issue#5 HardcodedCredentials support Property key
- Replace TrueArgToFalseQuickFix by SetBoolArgQuickFix

## [x.12]

### Modify

- optimize sql sentence regex pattern
- optimize constant String trace method

## [x.11]

### Modify

- Replace entropy algorithm
- Replace deprecated api

## [x.10]

### Added

- 1020: HardcodedCredentials
- scaffold for add/delete Inspection

### Modify

- remove unused test code
- i18n for Inspection Message, QuickFix, DisplayName
- upgrade org.jetbrains.intellij to 0.5.0

## [x.9]

### Modify

- Support more XML Parser to detect XXE vuln.

### BugFix

- Show green screen when use XXE quickfix.
- Github issue#2 XML foreach Error.

## [x.8]

### Modify 

- Replace deprecated API.

## [x.7]

### BugFix

- Order by with limit end, false positive.

### Modify

- Feedback data adds plugin version.
- Optimize the packaging process.

## [x.6]

### BugFix

- Redis could use Jackson as cache, false positive.

## [x.5]

### BugFix

- Some statements see as SQLi, false positive.

## [x.4]

### Added

- Whitelist on vulnerable signs.

### Modify

- Added gain FQName of an Element.
- Make vulnerable sign method to public.
- MOMOBootConfiguration suffix judge.

### BugFix

- SQL joint on INSERT field, false positive.
- SQL joint judge variable type.

## [x.3]

### BugFix

- SQL joint on FIELD, false positive.
- SQL joint on INSERT VALUE(S), false positive.

## [x.2]

### BugFix

- Judge if use MOMOBoot to reduce, false positive.

## [x.1]

### Added

- 1017: LDAPUnserialize
- 1018: BroadCORSAllowOrigin
- 1019: SpringSecurityDebugEnabled

### Modify

- Split MOMOBoot rules.

### BugFix

- Constants InputStream has null point exception.

## [x.0]

### Added

- 1001: PolyadicExpressionSQLi
- 1002: PlaceholderStringSQLi
- 1003: MybatisAnnotationSQLi
- 1004: MybatisXmlSQLi
- 1005: RegexDos
- 1006: JacksonDatabindDefaultTyping
- 1007: FastjsonAutoType
- 1008: NettyResponseSplitting
- 1009: PredictableSeed
- 1010: XxeInspector
- 1011: XStreamUnserialize
- 1014: WeakHashInspector
- 1015: OutdatedEncryptionInspector
- 1016: XMLDecoderUnserialize

