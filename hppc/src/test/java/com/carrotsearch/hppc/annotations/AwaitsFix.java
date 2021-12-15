/*
 * HPPC
 *
 * Copyright (C) 2010-2022 Carrot Search s.c.
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package com.carrotsearch.hppc.annotations;

import com.carrotsearch.randomizedtesting.annotations.TestGroup;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** An annotation for bug tracking issues that await resolution. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@TestGroup(enabled = false)
public @interface AwaitsFix {
  String value();
}
