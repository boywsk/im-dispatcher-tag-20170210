package com.gome.im.dispatcher.process;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.ServerModel;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.ReqReportMsg;
import com.gome.im.dispatcher.service.DispatcherService;
import com.gome.im.dispatcher.service.DispatcherTcpService;
import com.gome.im.dispatcher.utils.JedisClusterClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import java.util.Set;

/**
 *
 * IM服务汇报服务状态（逻辑层和接入层）
 * Created by wangshikai on 2016/7/27.
 */
public class ReportDispatchProcess extends DispatchProcess {
    private static Logger LOG = LoggerFactory.getLogger(ReportDispatchProcess.class);

    /**
     * 初始化处理的请求类型
     */
    public ReportDispatchProcess(){
        this.requestType = Global.REQUEST_TYPE.REPORT.value;
    }

    @Override
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        ReqReportMsg reportMsg = msg.getReqReportMsg();
        report(reportMsg.getType(), reportMsg.getCmd(), reportMsg.getIpPort());
        String rspReportJson = JSON.toJSONString(reportMsg);
        try {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(rspReportJson, CharsetUtil.UTF_8), packet.sender()));
            LOG.info("服务端返回数据信息:{}", rspReportJson);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("服务端返回数据信息失败:{}", rspReportJson);
        }
        LOG.info("DispatchProcess ReqReportMsg success");
    }

    /**
     * 客户端每5分钟一定要汇报一次服务，否则服务器会作为服务宕掉处理
     *
     * @param type
     * @param cmd
     * @param ipPort
     */
    public static void report(int type, Set<Long> cmd, String ipPort) {
        if (StringUtils.isEmpty(ipPort)) {
            LOG.error("错误数据:客户端汇报资源的ipPort值为空:{}", ipPort);
            return;
        }
        long nowTime = System.currentTimeMillis();
        ServerModel server = new ServerModel();
        server.setStatus(Global.SERVER_STATUS.OK.value);
        server.setType(type);
        server.setCmd(cmd);
        server.setIpPort(ipPort);
        server.setServerEnv(Global.ENV);
        server.setUpdateTime(nowTime);
        try {
            //存入redis
            JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
            long isChange = cluster.hset(Global.REDIS_SERVERS_KEY, server.getIpPort(), JSON.toJSONString(server));
            //如果有变化推送通知TCP方式订阅者
            DispatcherTcpService.change2Notify(isChange,server.getType());
            //存入mongodb
            DispatcherService.SERVER_DAO.saveOrUpdateServer(server);
            LOG.info("report success,ipPort:{},type:{}", server.getIpPort(), server.getType());
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("dispatcher report error:{},type:{},ipPort:{}", e, server.getType(), server.getIpPort());
        }
    }


}
