package mobilecloud.lib;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used to indicate that a class field is ignored when
 * sending to the server. Please use this annotation instead of "transient"
 * keyword if you want to ignore a field. Due to design consideration we do not
 * actually ignore "transient" fields when doing migrations.
 */
@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignore {

}
