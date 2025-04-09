package org.study.rpc.protocol.handler.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.study.rpc.Filter.FilterConfig;
import org.study.rpc.Filter.FilterData;
import org.study.rpc.Filter.client.ClientLogFilter;
import org.study.rpc.common.RpcResponse;
import org.study.rpc.constants.MsgStatus;
import org.study.rpc.protocol.MsgHeader;
import org.study.rpc.protocol.RpcProtocol;


public class ServiceAfterFilterHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    private final Logger logger = LoggerFactory.getLogger(ServiceAfterFilterHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> protocol) {
        FilterData filterData = new FilterData();
        filterData.setData(protocol.getBody());
        RpcResponse response = new RpcResponse();
        MsgHeader header = protocol.getHeader();
        try {
            FilterConfig.getServiceAfterFilterChain().doFilter(filterData);
        } catch (Exception e) {
            header.setStatus((byte) MsgStatus.FAILED.ordinal());
            response.setException(e);
            logger.error("after process request {} error", header.getRequestId(), e);
        }
        ctx.writeAndFlush(protocol);
    }
}
