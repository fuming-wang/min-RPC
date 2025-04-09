package org.study.rpc.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.study.rpc.Filter.FilterConfig;
import org.study.rpc.annotation.RpcReference;
import org.study.rpc.config.RpcProperties;
import org.study.rpc.protocol.serialization.SerializationFactory;
import org.study.rpc.registry.RegistryFactory;
import org.study.rpc.router.LoadBalancerFactory;
import org.study.rpc.utils.PropertiesUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @description: 消费者后置处理器
 */
@Configuration
public class ConsumerPostProcessor implements BeanPostProcessor, EnvironmentAware, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(ConsumerPostProcessor.class);

    RpcProperties rpcProperties;

    /**
     * 从配置文件中读取配置
     */
    @Override
    public void setEnvironment(Environment environment) {
        RpcProperties properties = RpcProperties.getInstance();
        PropertiesUtils.init(properties, environment);
        rpcProperties = properties;
        logger.info("读取配置文件成功");

    }

    /**
     * 初始化一些bean
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        SerializationFactory.init();
        RegistryFactory.init();
        LoadBalancerFactory.init();
        FilterConfig.initClientFilter();
    }

    /**
     * 代理层注入
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取所有字段
        final Field[] fields = bean.getClass().getDeclaredFields();
        // 遍历所有字段找到 RpcReference 注解的字段
        for (Field field : fields) {
            if(field.isAnnotationPresent(RpcReference.class)){
                final RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                final Class<?> aClass = field.getType();
                field.setAccessible(true);
                Object object = null;
                try {
                    // 创建代理对象
                    object = Proxy.newProxyInstance(
                            aClass.getClassLoader(),
                            new Class<?>[]{aClass},
                            new RpcInvokerProxy(rpcReference.serviceVersion(),
                                    rpcReference.timeout(),
                                    rpcReference.faultTolerant(),
                                    rpcReference.loadBalancer(),
                                    rpcReference.retryCount()));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                try {
                    // 将代理对象设置给字段
                    field.set(bean, object);
                    field.setAccessible(false);
                    logger.info("{} field: {} 注入成功", beanName, field.getName());
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage());
                    logger.info("{} field: {} 注入失败", beanName, field.getName());
                }
            }
        }
        return bean;
    }
}
