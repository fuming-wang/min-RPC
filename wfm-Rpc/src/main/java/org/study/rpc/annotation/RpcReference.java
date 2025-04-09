package org.study.rpc.annotation;

import org.study.rpc.constants.FaultTolerantRules;
import org.study.rpc.constants.LoadBalancerRules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description: 服务调用方注解
 * Author: wfm
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcReference {

    // 版本
    String serviceVersion() default "1.0";

    // 超时时间
    long timeout() default 5000;

    // 负载均衡:consistentHash,roundRobin...

    String loadBalancer() default LoadBalancerRules.RoundRobin;

    // 可选的容错策略:failover,failFast,failsafe...
    String faultTolerant() default FaultTolerantRules.FailFast;

    // 重试次数
    long retryCount() default 3;
}
