package com.gome.im.dispatcher.model.request;

import java.io.Serializable;

/**
 *
 * 汇报服务器 cpu  内存  等硬件资源使用状态
 * Created by wangshikai on 17/1/3.
 */
public class ReqServerStat implements Serializable{
    private String ip;

    private double cpuInfo;
    private double memInfo;

    private int connections;  //网络连接数

    public double getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(double cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public double getMemInfo() {
        return memInfo;
    }

    public void setMemInfo(double memInfo) {
        this.memInfo = memInfo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }
}
