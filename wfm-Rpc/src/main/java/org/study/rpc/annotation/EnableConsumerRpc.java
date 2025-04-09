package org.study.rpc.annotation;

import org.springframework.context.annotation.Import;
import org.study.rpc.consumer.ConsumerPostProcessor;

import java.lang.annotation.*;

/**
 * description: 开启调用方自动装配
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ConsumerPostProcessor.class)
public @interface EnableConsumerRpc {

}
