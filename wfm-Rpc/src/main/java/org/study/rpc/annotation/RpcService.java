package org.study.rpc.annotation;

import org.springframework.stereotype.Component;
import java.lang.annotation.*;


/**
 * description: 服务提供方
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {

    // 指定实现方,默认为实现接口中第一个
    Class<?> serviceInterface() default void.class;

    // 版本
    String serviceVersion() default "1.0";
}
