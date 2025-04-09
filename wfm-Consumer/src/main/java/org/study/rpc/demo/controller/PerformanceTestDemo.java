package org.study.rpc.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.study.rpc.annotation.RpcReference;
import org.study.rpc.constants.LoadBalancerRules;
import org.study.rpc.demo.TestService;


@RestController
public class PerformanceTestDemo {

    @RpcReference(loadBalancer = LoadBalancerRules.ConsistentHash)
    TestService testService;

    @GetMapping("pt/test/{count}/{key}")
    public String test(@PathVariable Integer count, @PathVariable String key){
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < count; i++){
            testService.test2(key);
        }
        long endTime = System.currentTimeMillis();

        return ((endTime - startTime)/1000) + "s";
    }
}
