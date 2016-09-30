package mobilecloud.lib;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method as remotely executable
 *
 */
@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Remote {}
