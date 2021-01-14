package org.wens.toolkit.query.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wens
 */
@Condition
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsLike {

    String value() default "";

    String pattern() default "%${value}%" ;

    String[] or();


}
