package org.study.rpc.protocol.handler.consumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.study.rpc.common.RpcFuture;
import org.study.rpc.common.RpcRequestHolder;
import org.study.rpc.common.RpcResponse;
import org.study.rpc.protocol.RpcProtocol;


public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) {
        long requestId = msg.getHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
        future.getPromise().setSuccess(msg.getBody());
    }

}
