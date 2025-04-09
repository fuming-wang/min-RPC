package org.study.rpc.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: SPI机制
 * @Author: wfm
 */
public class ExtensionLoader{

    private final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    // 系统SPI
    private static final String SYS_EXTENSION_LOADER_DIR_PREFIX = "META-INF/system.rpc/";

    // 用户SPI
    private static final String DIY_EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    private static final String[] prefixs = {SYS_EXTENSION_LOADER_DIR_PREFIX, DIY_EXTENSION_LOADER_DIR_PREFIX};

    /**
     * bean定义信息
     * name:Class的HashMap. key是名称, value是具体的类。
     * 对于定义接口RegisterService有实现RedisRegister和ZooKeeperRegister两个实现类
     * extensionClassCache["redis"] = RedisRegister.class
     * extensionClassCache["zookeeper"] = ZooKeeperRegister.class
     */
    private static final Map<String, Class> extensionClassCache = new ConcurrentHashMap<>();

    /**
     * 和上面有些类似
     * extensionClassCaches["registerService"] = {"redis":RedisRegister.class, "zookeeper": ZooKeeperRegister.class}
     */
    private static final Map<String, Map<String, Class>> extensionClassCaches = new ConcurrentHashMap<>();

    // 上面存储都是类，这里存储实例化的类，这里使用了懒汉模式
    private static final Map<String, Object> singletonsObject = new ConcurrentHashMap<>();

    private static final ExtensionLoader extensionLoader;

    static {
        extensionLoader = new ExtensionLoader();
    }

    public static ExtensionLoader getInstance(){
        return extensionLoader;
    }

    private ExtensionLoader(){

    }
    /**
     * 获取bean, 懒汉模式构造，需要注意线程安全
     */
    public <V> V get(String name) {
        if (!singletonsObject.containsKey(name)) {
            synchronized (ExtensionLoader.class){
                if(!singletonsObject.containsKey(name)){
                    try {
                        singletonsObject.put(name, extensionClassCache.get(name).newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
        return (V) singletonsObject.get(name);
    }

    /**
     * 获取接口下所有的类
     */
    public List<Object> gets(Class clazz) {

        String name = clazz.getName();
        if (!extensionClassCaches.containsKey(name)) {
            try {
                throw new ClassNotFoundException(clazz + "未找到");
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage());
            }
        }
        Map<String, Class> stringClassMap = extensionClassCaches.get(name);
        List<Object> objects = new ArrayList<>();
        if (!stringClassMap.isEmpty()){
            stringClassMap.forEach((k,v)->{
                try {
                    objects.add(singletonsObject.getOrDefault(k, v.newInstance()));
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error(e.getMessage());
                }
            });
        }

        return objects;
    }

    /**
     * 根据spi机制初加载bean的信息放入map
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        if (clazz == null) {
            throw new IllegalArgumentException("class not found");
        }
        ClassLoader classLoader = this.getClass().getClassLoader();
        Map<String, Class> classMap = new HashMap<>();
        // 从系统SPI以及用户SPI中找bean
        for (String prefix : prefixs) {
            String spiFilePath = prefix + clazz.getName();
            Enumeration<URL> enumeration = classLoader.getResources(spiFilePath);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                InputStreamReader inputStreamReader = null;
                inputStreamReader = new InputStreamReader(url.openStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] lineArr = line.split("=");
                    String key = lineArr[0];
                    String name = lineArr[1];
                    Class<?> aClass = Class.forName(name);
                    extensionClassCache.put(key, aClass);
                    classMap.put(key, aClass);
                    logger.info("加载bean key:{} , value:{}",key,name);
                }
            }
        }
        extensionClassCaches.put(clazz.getName(), classMap);
    }

}
