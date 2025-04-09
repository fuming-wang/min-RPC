package org.study.rpc.protocol.handler.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.rpc.Filter.FilterConfig;
import org.study.rpc.Filter.FilterData;
import org.study.rpc.common.RpcRequest;
import org.study.rpc.common.RpcResponse;
import org.study.rpc.constants.MsgStatus;
import org.study.rpc.protocol.MsgHeader;
import org.study.rpc.protocol.RpcProtocol;

/**
 * @description: 前置拦截器
 */
public class ServiceBeforeFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Logger logger = LoggerFactory.getLogger(ServiceBeforeFilterHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        final RpcRequest request = protocol.getBody();
        final FilterData filterData = new FilterData(request);
        RpcResponse response = new RpcResponse();
        MsgHeader header = protocol.getHeader();

        try {
            FilterConfig.getServiceBeforeFilterChain().doFilter(filterData);
        } catch (Exception e) {
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setException(e);
            logger.error("before process request {} error", header.getRequestId(), e);
            resProtocol.setHeader(header);
            resProtocol.setBody(response);
            ctx.writeAndFlush(resProtocol);
            return;
        }
        ctx.fireChannelRead(protocol);
    }
}
