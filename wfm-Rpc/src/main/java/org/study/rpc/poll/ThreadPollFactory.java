package org.study.rpc.poll;

import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.study.rpc.common.RpcRequest;
import org.study.rpc.common.RpcResponse;
import org.study.rpc.common.RpcServiceNameBuilder;
import org.study.rpc.constants.MsgStatus;
import org.study.rpc.constants.MsgType;
import org.study.rpc.protocol.MsgHeader;
import org.study.rpc.protocol.RpcProtocol;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreadPollFactory {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPollFactory.class);


    private static final ThreadPoolExecutor slowPoll;

    private static final ThreadPoolExecutor fastPoll;

    private static volatile ConcurrentHashMap<String, AtomicInteger> slowTaskMap = new ConcurrentHashMap<>();

    private static int corSize = Runtime.getRuntime().availableProcessors();

    // 缓存服务 该缓存放这里不太好,应该作一个统一 Config 进行管理
    @Setter
    private static Map<String, Object> rpcServiceMap;

//    public static void setRpcServiceMap(Map<String, Object> rpcMap){
//        rpcServiceMap = rpcMap;
//    }

    static {
        slowPoll = new ThreadPoolExecutor(corSize / 2, corSize, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(2000),
                r->{
                    Thread thread = new Thread(r);
                    thread.setName("slow poll-"+r.hashCode());
                    thread.setDaemon(true);
                    return thread;
                });

        fastPoll = new ThreadPoolExecutor(corSize, corSize * 2, 60L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000),
                r->{
                    Thread thread = new Thread(r);
                    thread.setName("fast poll-"+r.hashCode());
                    thread.setDaemon(true);
                    return thread;
                });
        startClearMonitor();
    }

    private ThreadPollFactory(){}




    /**
     * 清理慢请求
     */
    private static void startClearMonitor(){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()->{
            slowTaskMap.clear();
        },5,5,TimeUnit.MINUTES);
    }

    public static void submitRequest(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol){

        final RpcRequest request = protocol.getBody();
        String key = request.getClassName() + request.getMethodName() + request.getServiceVersion();
        ThreadPoolExecutor poll = fastPoll;
        if (slowTaskMap.containsKey(key) && slowTaskMap.get(key).intValue() >= 10){
            poll = slowPoll;
        }
        poll.submit(()->{
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            final MsgHeader header = protocol.getHeader();
            RpcResponse response = new RpcResponse();
            long startTime = System.currentTimeMillis();

            try {
                final Object result = submit(ctx, protocol);
                //  TODO Java的返回值为空或者只有一个，这和输入可以多个参数不同。由于序列化与反序列化同一，需要变成一个List
                ArrayList<Object> results = new ArrayList<>();
                results.add(result);
                response.setData(result == null ? null: results);
                response.setDataClass(result == null ? null : result.getClass());
                header.setStatus((byte) MsgStatus.SUCCESS.ordinal());
            } catch (Exception e) {
                // 执行业务失败则将异常返回
                header.setStatus((byte) MsgStatus.FAILED.ordinal());
                response.setException(e);
                logger.error("process request {} error", header.getRequestId(), e);
            }finally {
                long cost = System.currentTimeMillis() - startTime;
                System.out.println("cost time:" + cost);
                if(cost > 1000){
                    final AtomicInteger timeOutCount = slowTaskMap.putIfAbsent(key, new AtomicInteger(1));
                    if (timeOutCount!=null){
                        timeOutCount.incrementAndGet();
                    }
                }
            }
            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            logger.info("执行成功: {},{},{},{}",Thread.currentThread().getName(),request.getClassName(),request.getMethodName(),request.getServiceVersion());
            ctx.fireChannelRead(resProtocol);
        });
    }

    private static Object submit(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception{
        RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
        MsgHeader header = protocol.getHeader();
        header.setMsgType((byte) MsgType.RESPONSE.ordinal());
        final RpcRequest request = protocol.getBody();
        // 执行具体业务
        return handle(request);
    }

    // 调用方法
    private static Object handle(RpcRequest request) throws Exception {
        String serviceKey = RpcServiceNameBuilder.buildServiceKey(request.getClassName(), request.getServiceVersion());
        // 获取服务信息
        Object serviceBean = rpcServiceMap.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        // 获取服务提供方信息并且创建
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();

        // FIXME 需要优化一下代理层，让两个序列化传输一致，不然会出现object[], object[][]
//        Object[] parameters = {request.getData()};
        Object[] parameters = null;
        if(request.getData().getClass().isArray()){
            parameters = (Object[]) request.getData();
        }else {
            parameters = new Object[]{request.getData()};
        }

        FastClass fastClass = FastClass.create(serviceClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);

        // 调用方法并返回结果
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }

}
