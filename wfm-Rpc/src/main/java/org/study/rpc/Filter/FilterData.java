package org.study.rpc.Filter;

import lombok.Data;
import org.study.rpc.common.RpcRequest;
import org.study.rpc.common.RpcResponse;

import java.util.Map;

/**
 * @description: 上下文数据

 */
@Data
public class FilterData {

    private String serviceVersion;
    private long timeout;
    private long retryCount;
    private String className;
    private String methodName;
    private Object args;
    private Map<String,Object> serviceAttachments;
    private Map<String,Object> clientAttachments;
    private RpcResponse data; // 执行业务逻辑后的数据

    public FilterData(RpcRequest request) {
        this.args = request.getData();
        this.className = request.getClassName();
        this.methodName = request.getMethodName();
        this.serviceVersion = request.getServiceVersion();
        this.serviceAttachments = request.getServiceAttachments();
        this.clientAttachments = request.getClientAttachments();
    }
    public FilterData(){

    }

    @Override
    public String toString() {
        return "调用: Class: " + className + " Method: " + methodName + " args: " + args +" Version: " + serviceVersion
                +" Timeout: " + timeout +" ServiceAttachments: " + serviceAttachments +
                " ClientAttachments: " + clientAttachments;
    }

}
