package com.carrotsearch.hppc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@TestGroup(enabled = false, sysProperty = "large.memory")
public @interface RequiresLargeMemory
{
}
