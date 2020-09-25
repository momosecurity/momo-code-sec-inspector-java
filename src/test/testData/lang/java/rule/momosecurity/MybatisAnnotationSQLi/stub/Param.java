package org.apache.ibatis.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
public @interface Param {
    String value();
}