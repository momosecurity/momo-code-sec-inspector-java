package com.alibaba.fastjson.parser;

public class ParserConfig {
    public static ParserConfig global;

    public static ParserConfig getGlobalInstance() {
        return global;
    }

    public ParserConfig() {

    }

    public void setAutoTypeSupport(boolean autoTypeSupport) {
        return ;
    }

    static {
        global = new ParserConfig();
    }
}