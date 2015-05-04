package com.carrotsearch.hppc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Suppresses forbidden-API checks.
 */
@Retention(RetentionPolicy.CLASS)
@Target({
  ElementType.CONSTRUCTOR, 
  ElementType.FIELD, 
  ElementType.METHOD, 
  ElementType.TYPE })
public @interface SuppressForbidden {
}
