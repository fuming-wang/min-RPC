package org.study.rpc.protocol.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.study.rpc.common.RpcRequest;
import org.study.rpc.common.RpcResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @description: JSON序列化
 */
public class JsonSerialization implements RpcSerialization {


    private static final ObjectMapper MAPPER;

    static {
        MAPPER = generateMapper(JsonInclude.Include.ALWAYS);
    }

    private static ObjectMapper generateMapper(JsonInclude.Include include) {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setSerializationInclusion(include);
        customMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        customMapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true);
        customMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return customMapper;
    }

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        return obj instanceof String ? ((String) obj).getBytes() : MAPPER.writeValueAsString(obj).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 解决jackson在反序列化对象时为LinkedHashMap
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        final T t = MAPPER.readValue(data, clz);
        if (clz.equals(RpcRequest.class)) {
            RpcRequest rpcRequest = ((RpcRequest) t);

            rpcRequest.setData(convertReq(rpcRequest.getData(), rpcRequest.getDataClass()));
            return (T) rpcRequest;
        }else{
            RpcResponse rpcResponse = ((RpcResponse)t);
            rpcResponse.setData(convertRes(rpcResponse.getData(), rpcResponse.getDataClass()));
            return (T) rpcResponse;
        }
    }

    public Object convertReq(Object data, Class clazz) {
        if (ObjectUtils.isEmpty(data)){
            return null;
        }
        final Object o = ((ArrayList) data).get(0);
        if (BeanUtils.isSimpleProperty(o.getClass())){
            return o;
        }
        final LinkedHashMap map = (LinkedHashMap) o;
        return convert(clazz,map);
    }
    public Object convertRes(Object data,Class clazz)  {
        if (ObjectUtils.isEmpty(data)){
            return null;
        }
        Object o = ((ArrayList) data).get(0);
        if (BeanUtils.isSimpleProperty(o.getClass())){
            return o;
        }
        LinkedHashMap map = (LinkedHashMap) data;
        return convert(clazz,map);
    }

    public Object convert(Class clazz,LinkedHashMap map) {
        // 额外处理对象
        final Class dataClass = clazz;

        try {
            Object o = dataClass.newInstance();
            map.forEach((k,v)->{

                try {
                    final Field field = dataClass.getDeclaredField(String.valueOf(k));
                    if (v!=null && v.getClass().equals(LinkedHashMap.class)){
                        v = convert(field.getType(),(LinkedHashMap) v);
                    }
                    field.setAccessible(true);
                    field.set(o, v);
                    field.setAccessible(false);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            });
            return o;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}

