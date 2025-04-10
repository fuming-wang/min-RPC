package org.study.rpc.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * description: 服务元数据
 * 需要重写hashcode和equals
 */
@Data
public class ServiceMeta implements Serializable {

    private String serviceName;

    private String serviceVersion;

    private String serviceAddr;

    private int servicePort;

    /**
     * 关于redis注册中心的属性
     */
    private long endTime;

    private String UUID;

    /**
     * 故障转移需要移除不可用服务
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMeta that = (ServiceMeta) o;
        return servicePort == that.servicePort &&
                Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(serviceVersion, that.serviceVersion) &&
                Objects.equals(serviceAddr, that.serviceAddr) &&
                Objects.equals(UUID, that.UUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, serviceAddr, servicePort, UUID);
    }
}
