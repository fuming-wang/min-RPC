package org.study.rpc.config;

import lombok.Data;
import org.springframework.core.env.Environment;
import org.study.rpc.annotation.PropertiesField;
import org.study.rpc.annotation.PropertiesPrefix;
import org.study.rpc.constants.RegistryRules;
import org.study.rpc.constants.SerializationRules;

import java.util.HashMap;
import java.util.Map;


@Data
@PropertiesPrefix("rpc")
public class RpcProperties {


    // netty 端口
    @PropertiesField
    private Integer port;

    // 注册中心地址
    @PropertiesField
    private String registerAddr;

    // 注册中心类型
    @PropertiesField
    private String registerType = RegistryRules.ZOOKEEPER;

    // 注册中心密码
    @PropertiesField
    private String registerPsw;


    // 序列化
    @PropertiesField
    private String serialization = SerializationRules.JSON;


    // 服务端额外配置数据
    @PropertiesField("service")
    private Map<String,Object> serviceAttachments = new HashMap<>();

    // 客户端额外配置数据
    @PropertiesField("client")
    private Map<String,Object> clientAttachments = new HashMap<>();

    static RpcProperties rpcProperties;

    public static RpcProperties getInstance(){
            if (rpcProperties == null){
                rpcProperties = new RpcProperties();
            }
        return rpcProperties;
    }
    private RpcProperties(){}


    public void setRegisterType(String registerType) {
        if(registerType == null || registerType.isEmpty()){
            registerType = RegistryRules.ZOOKEEPER;
        }
        this.registerType = registerType;
    }


    public void setSerialization(String serialization) {
        if(serialization == null || serialization.isEmpty()){
            serialization = SerializationRules.JSON;
        }
        this.serialization = serialization;
    }


    /**
     * todo 做一个能够解析任意对象属性的工具类
     */
    public static void init(Environment environment){

    }
}
