package org.study.rpc.demo.service;


import org.study.rpc.annotation.RpcService;
import org.study.rpc.demo.TestService;

/**
 * description: 接口实现类
 * Author: wangfuming
 */
@RpcService
public class TestServiceImpl implements TestService {

    @Override
    public void test(String key) {
        System.out.println(1/0);
        System.out.println("服务提供1 test 测试成功  :" + key);
    }

    @Override
    public void test2(String key) {
        System.out.println("服务提供1 test2 测试成功  :" + key);
    }


}
