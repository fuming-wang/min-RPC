package org.study.rpc.demo.service;

import org.study.rpc.annotation.RpcService;
import org.study.rpc.demo.Test3Service;

@RpcService
public class Test3ServiceImpl implements Test3Service {

    @Override
    public String test(String a, String b) {
        return a + b;
    }
}
