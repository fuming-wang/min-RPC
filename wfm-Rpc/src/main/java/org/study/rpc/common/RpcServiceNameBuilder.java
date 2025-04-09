package org.study.rpc.common;


public class RpcServiceNameBuilder {


    // key: 服务名 value: 服务提供方s
    public static String buildServiceKey(String serviceName, String serviceVersion) {
        return String.join("$", serviceName, serviceVersion);
    }

}
