package org.study.rpc.consumer;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.study.rpc.Filter.FilterConfig;
import org.study.rpc.Filter.FilterData;
import org.study.rpc.common.*;
import org.study.rpc.constants.MsgType;
import org.study.rpc.constants.ProtocolConstants;
import org.study.rpc.config.RpcProperties;
import org.study.rpc.protocol.MsgHeader;
import org.study.rpc.protocol.RpcProtocol;
import org.study.rpc.router.LoadBalancer;
import org.study.rpc.router.LoadBalancerFactory;
import org.study.rpc.router.ServiceMetaRes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.study.rpc.constants.FaultTolerantRules.*;

/**
 * @description: 消费者代理层
 */
@Slf4j
public class RpcInvokerProxy implements InvocationHandler {

    private final String serviceVersion;
    private final long timeout;
    private final String loadBalancerType;
    private final String faultTolerantType;
    private final long retryCount;


    public RpcInvokerProxy(String serviceVersion, long timeout, String faultTolerantType, String loadBalancerType, long retryCount) throws Exception {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.loadBalancerType = loadBalancerType;
        this.faultTolerantType = faultTolerantType;
        this.retryCount = retryCount;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        // 构建消息头
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        final byte[] serialization = RpcProperties.getInstance().getSerialization().getBytes();
        header.setSerializationLen(serialization.length);
        header.setSerializations(serialization);
        header.setMsgType((byte) MsgType.REQUEST.ordinal());
        header.setStatus((byte) 0x1);
        protocol.setHeader(header);

        // 构建请求体
        RpcRequest request = new RpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setData(ObjectUtils.isEmpty(args) ? new Object[0] : args);
        request.setDataClass(ObjectUtils.isEmpty(args) ? null : args[0].getClass());
        request.setServiceAttachments(RpcProperties.getInstance().getServiceAttachments());
        request.setClientAttachments(RpcProperties.getInstance().getClientAttachments());

        // 拦截器的上下文
        FilterData filterData = new FilterData(request);
        FilterConfig.getClientBeforeFilterChain().doFilter(filterData);

        protocol.setBody(request);

        RpcConsumer rpcConsumer = new RpcConsumer();

        String serviceName = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object[] params = {request.getData()};
        // 1. 获取负载均衡策略
        LoadBalancer loadBalancer = LoadBalancerFactory.get(loadBalancerType);

        // 2. 根据策略获取对应服务
        ServiceMetaRes serviceMetaRes = loadBalancer.select(params, serviceName);

        ServiceMeta curServiceMeta = serviceMetaRes.getCurServiceMeta();
        Collection<ServiceMeta> otherServiceMeta = serviceMetaRes.getOtherServiceMeta();
        long count = 1;
        long retryCount = this.retryCount;
        RpcResponse rpcResponse = null;
        // 重试机制
        while (count <= retryCount ){
            // 处理返回数据
            RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
            // XXXHolder
            RpcRequestHolder.REQUEST_MAP.put(requestId, future);
            try {
                // 发送消息
                rpcConsumer.sendRequest(protocol, curServiceMeta);
                // 等待响应数据返回
                rpcResponse = future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS);
                // 如果有异常并且没有其他服务
                if(rpcResponse.getException() != null && otherServiceMeta.isEmpty()){
                    throw rpcResponse.getException();
                }
                if (rpcResponse.getException() != null){
                    throw rpcResponse.getException();
                }
                log.info("rpc 调用成功, serviceName: {}", serviceName);

                FilterConfig.getClientAfterFilterChain().doFilter(filterData);

                return rpcResponse.getData();

            }catch (Throwable e){

                String errorMsg = e.toString();
                // todo 看其他人实现的RPC框架，继续增加自己的扩展策略
                switch (faultTolerantType){
                    // 快速失败: 不在进行尝试, 直接返回信息
                    case FailFast:
                        log.warn("rpc 调用失败, 触发 FailFast 策略,异常信息: {}", errorMsg);
                        // 理论上不会为null, 但是idea让我在这里加上assert
                        assert rpcResponse != null;
                        return rpcResponse.getException();
                    // 故障转移, 找其他的节点进行尝试
                    case Failover:
                        log.warn("rpc 调用失败,第{}次重试,异常信息:{}", count, errorMsg);
                        count++;
                        if (!ObjectUtils.isEmpty(otherServiceMeta)){
                            ServiceMeta next = otherServiceMeta.iterator().next();
                            curServiceMeta = next;
                            otherServiceMeta.remove(next);
                        }else {
                            String msg = String.format("rpc 调用失败,无服务可用 serviceName: {%s}, 异常信息: {%s}", serviceName, errorMsg);
                            log.warn(msg);
                            throw new RuntimeException(msg);
                        }
                        break;
                    // 忽视这次错误
                    case Failsafe:
                        return null;
                }
            }
        }

        throw new RuntimeException("rpc 调用失败，超过最大重试次数: {}" + retryCount);
    }
}
