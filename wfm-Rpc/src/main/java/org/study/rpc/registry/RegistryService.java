package org.study.rpc.registry;

import org.study.rpc.common.ServiceMeta;

import java.io.IOException;
import java.util.List;

/**
 * @description: 注册中心接口
 */
public interface RegistryService {

    /**
     * @description: 服务注册
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * @description: 服务注销

     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;


    /**
     * @description: 获取 serviceName 下的所有服务
     */
    List<ServiceMeta> discoveries(String serviceName);

    /**
     * @description: 关闭
     */
    void destroy() throws IOException;

}
