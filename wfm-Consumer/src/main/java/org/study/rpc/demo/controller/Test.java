package org.study.rpc.demo.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.study.rpc.annotation.RpcReference;
import org.study.rpc.constants.FaultTolerantRules;
import org.study.rpc.constants.LoadBalancerRules;
import org.study.rpc.demo.Test2Service;
import org.study.rpc.demo.Test3Service;
import org.study.rpc.demo.TestService;


/**
 * description: 用于测试RPC服务的消费者Controller接口
 * Author: wangfuming
 */
@RestController
public class Test {

    @RpcReference(timeout = 10000L,faultTolerant = FaultTolerantRules.Failover, loadBalancer = LoadBalancerRules.RoundRobin)
    TestService testService;

    @RpcReference(loadBalancer = LoadBalancerRules.ConsistentHash)
    Test2Service test2Service;

    @RpcReference(loadBalancer = LoadBalancerRules.ConsistentHash)
    Test3Service test3Service;

    /**
     * 轮询
     * 会触发故障转移,提供方模拟异常
     */
    @RequestMapping("test/{key}")
    public String test(@PathVariable String key){
        testService.test(key);
        return "test1 ok";
    }

    /**
     * 一致性哈希
     */
    @RequestMapping("test2/{key}")
    public String test2(@PathVariable String key){

        return test2Service.test(key);
    }

    /**
     * 轮询,无如何异常
     */
    @RequestMapping("test3/{key}")
    public String test3(@PathVariable String key){
        testService.test2(key);
        return "test2 ok";
    }

    @RequestMapping("test4/{key}/{key2}")
    public String test4(@PathVariable String key, @PathVariable String key2){
        return test3Service.test(key, key2);
    }

}
