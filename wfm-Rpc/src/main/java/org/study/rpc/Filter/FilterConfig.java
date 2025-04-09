package org.study.rpc.Filter;


import lombok.Getter;
import lombok.SneakyThrows;
import org.study.rpc.spi.ExtensionLoader;

import java.io.IOException;

/**
 * @description: 拦截器配置类，用于统一管理拦截器
 */
public class FilterConfig {


    @Getter
    private static final FilterChain serviceBeforeFilterChain = new FilterChain();
    @Getter
    private static final FilterChain serviceAfterFilterChain = new FilterChain();
    @Getter
    private static final FilterChain clientBeforeFilterChain = new FilterChain();
    @Getter
    private static final FilterChain clientAfterFilterChain = new FilterChain();

    @SneakyThrows
    public static void initServiceFilter(){
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ServiceAfterFilter.class);
        extensionLoader.loadExtension(ServiceBeforeFilter.class);
        serviceBeforeFilterChain.addFilter(extensionLoader.gets(ServiceBeforeFilter.class));
        serviceAfterFilterChain.addFilter(extensionLoader.gets(ServiceAfterFilter.class));
    }
    public static void initClientFilter() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ClientAfterFilter.class);
        extensionLoader.loadExtension(ClientBeforeFilter.class);
        clientBeforeFilterChain.addFilter(extensionLoader.gets(ClientBeforeFilter.class));
        clientAfterFilterChain.addFilter(extensionLoader.gets(ClientAfterFilter.class));
    }

}
