package org.study.rpc.protocol.serialization;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;
import org.study.rpc.common.RpcResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class HessianSerialization implements RpcSerialization {


    @Override
    public <T> byte[] serialize(T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        byte[] results;

        HessianSerializerOutput hessianOutput;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            hessianOutput = new HessianSerializerOutput(os);
            hessianOutput.writeObject(object);
            hessianOutput.flush();
            results = os.toByteArray();
        } catch (Exception e) {
            throw new SerializationException(e);
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) {
        if (bytes == null) {
            throw new NullPointerException();
        }
        T result;

        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
            HessianSerializerInput hessianInput = new HessianSerializerInput(is);
            result = (T) hessianInput.readObject(clz);

            /*
             * FIXME 丑陋的解决当前的问题
             * 由于序列化具体实现的问题，导致了Hessian和JSON序列化接受的数据格式不统一
             * 当前以JSON的为主（先实现测试），提供方返回的数据为List，第一个元素是实际的结果
             * 在Response中，我们需要将其取出来，否则在消费者将出现无法将List转换为目标对象
             */
            if(clz.equals(RpcResponse.class)){
                RpcResponse response = (RpcResponse) result;
                if(response.getData() != null){
                    List dataList = (ArrayList) response.getData();
                    if(dataList.size() > 0){
                        response.setData(dataList.get(0));
                    }
                    result = (T) response;
                }

            }
        } catch (Exception e) {
            throw new SerializationException(e);
        }

        return result;
    }
}

