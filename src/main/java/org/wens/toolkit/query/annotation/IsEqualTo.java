package org.wens.toolkit.query.annotation;


import org.mybatis.dynamic.sql.SqlColumn;

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
public @interface IsEqualTo {

    String value() default "";

    String[] or() default {};

}
