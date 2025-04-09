package org.study.rpc.constants;

/**
 * @description: 错误处理策略常量
 */
public interface FaultTolerantRules {

    String Failover = "failover";
    String FailFast = "failFast";
    String Failsafe = "failsafe";
}
