package org.study.rpc.router;

import org.study.rpc.common.ServiceMeta;
import org.study.rpc.config.RpcProperties;
import org.study.rpc.registry.RegistryService;
import org.study.rpc.spi.ExtensionLoader;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 轮询算法
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    private static AtomicInteger roundRobinId = new AtomicInteger(0);

    @Override
    public ServiceMetaRes select(Object[] params, String serviceName) {
        // 获取注册中心
        RegistryService registryService = ExtensionLoader.getInstance().get(RpcProperties.getInstance().getRegisterType());
        List<ServiceMeta> discoveries = registryService.discoveries(serviceName);
        // 1.获取所有服务
        int size = discoveries.size();
        // 2.根据当前轮询ID取余服务长度得到具体服务
        roundRobinId.addAndGet(1);
        if (roundRobinId.get() == Integer.MAX_VALUE){
            roundRobinId.set(0);
        }

        return ServiceMetaRes.build(discoveries.get(roundRobinId.get() % size),discoveries);
    }

}
