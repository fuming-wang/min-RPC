package org.study.rpc.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.study.rpc.annotation.EnableProviderRpc;


@SpringBootApplication
@EnableProviderRpc
public class RpcProviderDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcProviderDemoApplication.class, args);
    }
}
