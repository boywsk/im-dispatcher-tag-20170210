package com.gome.im.dispatcher.handler;

import com.gome.im.dispatcher.model.ServerChannelModel;
import com.gome.im.dispatcher.protobuf.Msg;
import com.gome.im.dispatcher.service.DispatcherTcpService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangshikai on 16/12/15.
 */
public class TcpServerHandler extends ChannelInboundHandlerAdapter{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static AttributeKey<ServerChannelModel> SERVER_CHANNEL_KEY = AttributeKey.valueOf("serverChannel");

    public static ConcurrentHashMap<String,Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    private void setChannel(String ipPort,Channel channel){
        if (StringUtils.isNotEmpty(ipPort)){
            CHANNEL_MAP.put(ipPort,channel);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) { logger.info("服务端收到新连接!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Msg.CommonMsg commonMsg = (Msg.CommonMsg)msg;
            Attribute<ServerChannelModel> attr = ctx.channel().attr(SERVER_CHANNEL_KEY);
            ServerChannelModel serverChannelModel = attr.get();
            if(serverChannelModel == null){
                serverChannelModel = new ServerChannelModel();
                serverChannelModel.setChannel(ctx.channel());
                serverChannelModel.setIpPort(commonMsg.getIpPort());
                attr.setIfAbsent(serverChannelModel);
            }
            setChannel(commonMsg.getIpPort(),ctx.channel());
            DispatcherTcpService.INSTANCE.process(ctx,commonMsg);
        } catch (Exception e) {
            logger.error("TCP服务器,error:{}",e);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE){
                try {
                    logger.info("read idle");
                    ctx.channel().close();
                } catch (Exception e) {
                    logger.error("readTimeOut error:{}",e);
                }
            }
            else if (event.state() == IdleState.WRITER_IDLE){
                logger.info("write idle");
            }
            else if (event.state() == IdleState.ALL_IDLE){
                logger.info("all idle");
            }
        }
    }
}
