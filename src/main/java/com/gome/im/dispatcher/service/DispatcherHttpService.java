package com.gome.im.dispatcher.service;

import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.process.ReportStatProcess;
import com.gome.im.dispatcher.utils.SMSUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.*;

/**
 * Created by wangshikai on 17/1/6.
 */
public class DispatcherHttpService {
    private static Logger LOG = LoggerFactory.getLogger(DispatcherHttpService.class);
    public static DispatcherHttpService INSTANCE = new DispatcherHttpService();
    private static ConcurrentHashMap<String, Long> SERVER_MAP = new ConcurrentHashMap();

    private static long VALID_TIME = 2 * 60 * 1000; //请求频度时间

    private DispatcherHttpService() {
    }

    private boolean checkValid(ChannelHandlerContext channelHandlerContext, FullHttpRequest msg, String serverKey){
        try {
            String remoteIpPort = channelHandlerContext.channel().remoteAddress().toString();
            if (remoteIpPort != null && !remoteIpPort.isEmpty()) {
                serverKey = remoteIpPort;
            }
        } catch (Exception e) {
            LOG.error("error:{}", e);
        }

        if (serverKey == null || serverKey.isEmpty()) {
            LOG.error("必填参数serverName不存在,serverName:{}", serverKey);
            writeResponse(channelHandlerContext.channel(), "{\"code\":204,\"msg\":\"参数错误\"}", msg);
            return false;
        }
        Long time = SERVER_MAP.get(serverKey);
        if (time != null) {
            if (System.currentTimeMillis() - time.longValue() < VALID_TIME) {
                LOG.error("请求过于频繁!");
                writeResponse(channelHandlerContext.channel(), "{\"code\":205,\"msg\":\"请求过于频繁\"}", msg);
                return false;
            }else{
                SERVER_MAP.put(serverKey, System.currentTimeMillis());
                return true;
            }
        }else{
            SERVER_MAP.put(serverKey, System.currentTimeMillis());
            return true;
        }
    }

    public void smsReport(ChannelHandlerContext channelHandlerContext, FullHttpRequest msg, Map<String, String> parmMap) {
        String ipPort = parmMap.get("serverName");
        boolean isValid = checkValid(channelHandlerContext,msg,ipPort);
        if(!isValid){
            return;
        }
        sendSMS(parmMap.get("serverName"), parmMap.get("status"), parmMap.get("extra"));
        writeResponse(channelHandlerContext.channel(), "{\"code\":200}", msg);
    }

    public void reportServerStatus(ChannelHandlerContext channelHandlerContext, FullHttpRequest msg, Map<String, String> parmMap){
        String ip = parmMap.get("ip");
        boolean isValid = checkValid(channelHandlerContext,msg,ip);
        if(!isValid){
            return;
        }
        ReportStatProcess.reportStat(parmMap.get("ip"), Double.parseDouble(parmMap.get("cpuInfo")), Double.parseDouble(parmMap.get("memInfo")), Integer.parseInt(parmMap.get("connections")));
        writeResponse(channelHandlerContext.channel(), "{\"code\":200}", msg);
    }


    private void sendSMS(String serverName, String status, String extra) {
        String serverStatus = "";
        if (status.equals("0")) {
            serverStatus = "正常";
        } else if (status.equals("1")) {
            serverStatus = "异常";
        } else {
            serverStatus = status;
        }
        String content = "状态:" + serverStatus + ",服务器:" + serverName + ",内容:" + extra;
        SMSUtil.SendSMS("【美信报警】", Global.SMS_USERNAME, Global.SMS_PWD, content, Global.MOBILES);
    }

    /**
     * http返回响应数据
     *
     * @param channel
     */
    private void writeResponse(Channel channel, String responseContent, FullHttpRequest fullHttpRequest) {
        // Convert the response content to a ChannelBuffer.
        ByteBuf buf = copiedBuffer(responseContent, CharsetUtil.UTF_8);

        // Decide whether to close the connection or not.
        boolean close = fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || fullHttpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);

        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());
        }

        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
