package com.gome.im.dispatcher.handler;


import com.gome.im.dispatcher.service.DispatcherHttpService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by wangshikai on 17/1/3.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger LOG = LoggerFactory.getLogger(HttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest msg) throws Exception {
        try {
            //TODO 未来可以加参数签名校验请求可靠性
            Map<String, String> parmMap = new HttpRequestParser(msg).parse(); // 将GET, POST所有请求参数转换成Map对象
            String uri = msg.getUri();
            LOG.info("HTTP URI,uri:{}", uri);
            if (uri.startsWith("/sendSMS")) {
                //发短信
                DispatcherHttpService.INSTANCE.smsReport(channelHandlerContext, msg, parmMap);
            } else if (uri.startsWith("/serverStatus")) {
                //汇报服务器性能状态
                DispatcherHttpService.INSTANCE.reportServerStatus(channelHandlerContext, msg, parmMap);
            } else {
                //
                LOG.error("未知URI");
            }
            LOG.info("HTTP-SERVER 收到数据参数:{}", parmMap);
        } catch (Exception e) {
            LOG.error("HTTP-SERVER 解析参数错误:{}", e);
        }

    }

}
