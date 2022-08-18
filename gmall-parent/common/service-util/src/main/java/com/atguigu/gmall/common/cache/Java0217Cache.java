package com.atguigu.gmall.common.cache;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Java0217Cache {

    @AliasFor("prefix")
    String value() default "cache";

    String prefix() default "cache";
}
