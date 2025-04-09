package org.study.rpc.router;

/**
 * @description: 负载均衡,根据负载均衡获取对应的服务节点(负载均衡包装服务节点)
 */
public interface LoadBalancer<T> {

     /**
      * 选择负载均衡策略
      */
     ServiceMetaRes select(Object[] params,String serviceName);

}
