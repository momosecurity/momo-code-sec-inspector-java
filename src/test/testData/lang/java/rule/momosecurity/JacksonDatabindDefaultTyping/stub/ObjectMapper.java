package com.fasterxml.jackson.databind;

public class ObjectMapper {

    public void enableDefaultTyping() {

    }

    public void enableDefaultTyping(ObjectMapper.DefaultTyping dti) {

    }

    public static enum DefaultTyping {
        JAVA_LANG_OBJECT,
        OBJECT_AND_NON_CONCRETE,
        NON_CONCRETE_AND_ARRAYS,
        NON_FINAL;

        private DefaultTyping() {
        }
    }
}