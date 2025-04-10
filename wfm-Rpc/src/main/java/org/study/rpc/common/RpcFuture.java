package org.study.rpc.common;

import io.netty.util.concurrent.Promise;
import lombok.Data;


@Data
public class RpcFuture<T> {

    private Promise<T> promise;
    private long timeout;

    public RpcFuture() {
    }

    public RpcFuture(Promise<T> promise, long timeout) {
        this.promise = promise;
        this.timeout = timeout;
    }
}
