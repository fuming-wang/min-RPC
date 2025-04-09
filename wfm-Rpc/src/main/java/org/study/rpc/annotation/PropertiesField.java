package org.study.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description: 后缀, 用于读取配置文件
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertiesField {

    /**
     * 默认为属性名 例如: registryType = registry-type  遵守配置文件规则
     */
    String value() default "";
}
