package com.gome.im.dispatcher.model.request;

import com.gome.im.dispatcher.model.request.rpc.ReqRpcReportMsg;
import com.gome.im.dispatcher.model.request.rpc.ReqRpcServersMsg;

import java.io.Serializable;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class ClientMsg implements Serializable {
    private int requestType;  // 请求类型: 1 : 上报服务器资源地址  2:拉取匹配资源服务地址

    private ReqReportMsg reqReportMsg;  //汇报IM服务器ipPort cmd 资源

    private ReqServersMsg reqServersMsg;   //拉取IM服务器ipPort cmd资源

    private ReqRpcReportMsg reqRpcReportMsg;   //汇报 rpc服务器 ipPort cmd 资源

    private ReqRpcServersMsg reqRpcServersMsg;  //拉取 rpc服务器 ipPort cmd 资源

    private ReqServerStat reqServerStat;  //汇报RPC服务器 cpu 内存使用状态

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public ReqServersMsg getReqServersMsg() {
        return reqServersMsg;
    }

    public void setReqServersMsg(ReqServersMsg reqServersMsg) {
        this.reqServersMsg = reqServersMsg;
    }

    public ReqReportMsg getReqReportMsg() {
        return reqReportMsg;
    }

    public void setReqReportMsg(ReqReportMsg reqReportMsg) {
        this.reqReportMsg = reqReportMsg;
    }

    public ReqRpcReportMsg getReqRpcReportMsg() {
        return reqRpcReportMsg;
    }

    public void setReqRpcReportMsg(ReqRpcReportMsg reqRpcReportMsg) {
        this.reqRpcReportMsg = reqRpcReportMsg;
    }

    public ReqRpcServersMsg getReqRpcServersMsg() {
        return reqRpcServersMsg;
    }

    public void setReqRpcServersMsg(ReqRpcServersMsg reqRpcServersMsg) {
        this.reqRpcServersMsg = reqRpcServersMsg;
    }

    public ReqServerStat getReqServerStat() {
        return reqServerStat;
    }

    public void setReqServerStat(ReqServerStat reqServerStat) {
        this.reqServerStat = reqServerStat;
    }
}
