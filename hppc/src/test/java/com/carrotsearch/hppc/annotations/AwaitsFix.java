package com.carrotsearch.hppc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;

/**
 * An annotation for bug tracking issues that await resolution.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@TestGroup(enabled = false)
public @interface AwaitsFix {
  String value();
}
