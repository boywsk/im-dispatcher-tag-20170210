package com.gome.im.dispatcher.model;

import io.netty.channel.Channel;

/**
 * Created by wangshikai on 16/12/16.
 */
public class ServerChannelModel {
    private String ipPort;
    private Channel channel;

    public String getIpPort() {
        return ipPort;
    }

    public void setIpPort(String ipPort) {
        this.ipPort = ipPort;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
