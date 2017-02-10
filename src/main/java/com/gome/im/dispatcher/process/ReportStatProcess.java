package com.gome.im.dispatcher.process;

import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.model.request.ClientMsg;
import com.gome.im.dispatcher.model.request.ReqServerStat;
import com.gome.im.dispatcher.service.DispatcherService;
import com.gome.im.dispatcher.utils.JedisClusterClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

/**
 * Created by wangshikai on 17/1/3.
 */
public class ReportStatProcess extends DispatchProcess{
    private static Logger LOG = LoggerFactory.getLogger(ReportStatProcess.class);

    /**
     * 初始化处理的请求类型
     */
    public ReportStatProcess(){
        this.requestType = Global.REQUEST_TYPE.REPORT_STAT.value;
    }

    @Override
    public void process(ChannelHandlerContext ctx, DatagramPacket packet, ClientMsg msg) {
        ReqServerStat reqServerStat = msg.getReqServerStat();
        String statJson = JSON.toJSONString(reqServerStat);
        try {
            reportStat(reqServerStat.getIp(),reqServerStat.getCpuInfo(),reqServerStat.getMemInfo(),reqServerStat.getConnections());
            LOG.info("reportStat success,stat:{}", statJson);
        } catch (Exception e) {
            LOG.error("处理服务器CPU,内存等信息失败error:{},stat:{}", e,statJson);
        }
        LOG.info("DispatchProcess ReportStatProcess success");
    }

    public static void reportStat(String ip,double cupInfo,double memInfo,int connections){
        Double weight = 1 - cupInfo * memInfo;
        if(weight <= 0){
            LOG.error("DispatchProcess ReportStatProcess error,计算的权重结果小于零,请查看参数是否汇报正确!");
            return;
        }
        try {
            //存入redis
            JedisCluster cluster = JedisClusterClient.INSTANCE.getJedisCluster();
            cluster.hset(Global.REDIS_SERVER_STAT_KEY, ip, weight.toString());
            //存入mongodb
            DispatcherService.SERVER_STAT_DAO.saveOrUpdateServerStat(ip,cupInfo,memInfo,connections);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("reportStat error:{}", e);
        }

    }

}
