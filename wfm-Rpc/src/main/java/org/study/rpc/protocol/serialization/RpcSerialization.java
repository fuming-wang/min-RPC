package org.study.rpc.protocol.serialization;

import java.io.IOException;

/**
 * @description: 序列化接口
 */
public interface RpcSerialization {

    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}
