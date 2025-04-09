package org.study.rpc.demo.service;


import org.study.rpc.annotation.RpcService;
import org.study.rpc.demo.Test2Service;

/**
 * description: 接口实现类
 * Author: wangfuming
 */
@RpcService
public class Test2ServiceImpl implements Test2Service {

    @Override
    public String test(String key) {
        System.out.println("服务提供1 test2 测试成功 :" + key);
        return key;
    }
}
