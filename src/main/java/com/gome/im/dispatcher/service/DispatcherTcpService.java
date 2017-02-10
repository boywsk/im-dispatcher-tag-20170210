package com.gome.im.dispatcher.service;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.handler.TcpServerHandler;
import com.gome.im.dispatcher.model.request.ReqReportMsg;
import com.gome.im.dispatcher.model.request.ReqServersMsg;
import com.gome.im.dispatcher.model.response.RspServersMsg;
import com.gome.im.dispatcher.process.GetServersDispatchProcess;
import com.gome.im.dispatcher.process.ReportDispatchProcess;
import com.gome.im.dispatcher.protobuf.Msg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by wangshikai on 16/12/16.
 */
public class DispatcherTcpService {
    private static Logger LOG = LoggerFactory.getLogger(DispatcherTcpService.class);

    //订阅不同类型服务器的服务器列表 <"订阅的服务器类型"，"服务器列表">
    private static ConcurrentHashMap<Integer, ConcurrentSkipListSet<String>> SUBSCRIBE_SERVER_MAP = new ConcurrentHashMap<>();

    public static DispatcherTcpService INSTANCE = new DispatcherTcpService();

    private DispatcherTcpService() {
    }


    public void process(ChannelHandlerContext ctx, Msg.CommonMsg msg) {
        int requestType = msg.getRequestType();
        String ipPort = msg.getIpPort();
        String json = msg.getMsg();
        LOG.info("TCP服务器收到消息:requestType:{},ipPort:{},msg:{}", requestType, ipPort, json);
        String returnMsg = json;
        if (requestType == 1) { // 汇报
            ReqReportMsg reportMsg = JSON.parseObject(json, ReqReportMsg.class);
            ReportDispatchProcess.report(reportMsg.getType(), reportMsg.getCmd(), reportMsg.getIpPort());
            sendMsg(ctx.channel(), createPbMsg(false, returnMsg));
        } else if (requestType == 2) { //tcp 拉取
            ReqServersMsg reqServersMsg = JSON.parseObject(json, ReqServersMsg.class);
            int subscribeServerType = reqServersMsg.getType();
            LOG.info("拉取服务器资源,拉取服务器类型:{},拉取服务器ipPort:{}",subscribeServerType,ipPort);
            ConcurrentSkipListSet<String> set = SUBSCRIBE_SERVER_MAP.get(subscribeServerType);
            if (set == null) {
                set = new ConcurrentSkipListSet<>();
                set.add(ipPort);
                SUBSCRIBE_SERVER_MAP.put(subscribeServerType, set);
            } else {
                set.add(ipPort);
            }
            RspServersMsg rsp = GetServersDispatchProcess.getServersByType(subscribeServerType);
            returnMsg = JSON.toJSONString(rsp);
            sendMsg(ctx.channel(), createPbMsg(true, returnMsg));
        }
        LOG.info("TCP服务器返回内容:{}", returnMsg);
    }

    //检测到变化推送通知
    public static void change2Notify(long isChange,int subscribeServerType) {
        if(isChange > 0){
            ConcurrentSkipListSet<String> set = SUBSCRIBE_SERVER_MAP.get(subscribeServerType);
            if(set != null){
                try {
                    RspServersMsg rsp = GetServersDispatchProcess.getServersByType(subscribeServerType);
                    String returnMsg = JSON.toJSONString(rsp);
                    for(String ipPort : set){
                        Channel channel = TcpServerHandler.CHANNEL_MAP.get(ipPort);
                        sendMsg(channel, createPbMsg(true, returnMsg));
                        LOG.info("TCP检查到变化推送变化内容给ipPort:{},msg:{}",ipPort,returnMsg);
                    }
                } catch (Exception e) {
                    LOG.error("change2Notify error:{}",e);
                }
            }else{
                LOG.error("TCP调度服务订阅的服务器类型为:{}类型的服务列表不存在!",subscribeServerType);
            }
        }
    }

    public static void sendMsg(Channel channel, Msg.CommonMsg msg) {
        try {
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(msg);
            } else {
                LOG.error("channel 已关闭!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Msg.CommonMsg createPbMsg(boolean isChange, String msg) {
        Msg.CommonMsg.Builder builder = Msg.CommonMsg.newBuilder();
        builder.setIsChange(isChange);
        builder.setMsg(msg);
        return builder.build();
    }

}
