package com.gome.im.dispatcher.server;

import com.gome.im.dispatcher.global.Global;
import com.gome.im.dispatcher.handler.HttpServerHandler;
import com.gome.im.dispatcher.handler.TcpServerHandler;
import com.gome.im.dispatcher.handler.UdpServerHandler;
import com.gome.im.dispatcher.protobuf.Msg;
import com.gome.im.dispatcher.service.DispatcherService;
import com.gome.im.dispatcher.utils.ZKClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class DispatchServer {
    private static Logger LOG = LoggerFactory.getLogger(DispatchServer.class);

    private int port;

    private Channel channel;

    public DispatchServer(int port) {
        this.port = port;
    }

    public void initUdpServer() throws Exception {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(NioDatagramChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast("decoder", new JsonObjectDecoder());
                            p.addLast(new UdpServerHandler());
                        }
                    });
            InetAddress address = InetAddress.getLocalHost();
            channel = b.bind(address, port).sync().channel();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        group.shutdownGracefully();
                    }
                }
            });
            LOG.info("UDP服务器启动, host:{},port:{}", address.getHostAddress(), port);
        } catch (Exception e) {
            //e.printStackTrace();
            LOG.error("error:{}",e);
        }
    }


    public static void initTcpServer(int port) {
        ServerBootstrap server = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2 - 1);
        try {
            server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    //.option(ChannelOption.SO_BACKLOG, 128)  //连接数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)  //长连接
                    .childOption(ChannelOption.TCP_NODELAY, true)   //无延迟
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("heartbeat",new IdleStateHandler(120,120,120, TimeUnit.SECONDS));
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(Msg.CommonMsg.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast("handler",new TcpServerHandler());
                        }
                    });
            ChannelFuture f = server.bind("0.0.0.0",port).sync();
            LOG.info("服务器开启TCP,success......,端口号:{}",port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("NettyTCP启动异常：", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void initHttpServer(int port) {
        ServerBootstrap server = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE,false)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("decoder",new HttpRequestDecoder());
                            pipeline.addLast("encoder",new HttpResponseEncoder());
                            pipeline.addLast(new HttpObjectAggregator(1048576));
                            pipeline.addLast("handler",new HttpServerHandler());
                        }
                    });
            ChannelFuture f = server.bind(port).sync();
            LOG.info("服务器开启HTTP,success......,端口号:{}",port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("Netty-HTTP启动异常：", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        //启动服务
        int serverPort = 8866;
        //可以接受端口参数
        if(args.length >= 1 && args[0] != null){
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        //启动服务
        DispatchServer server = new DispatchServer(serverPort);
        server.initUdpServer();


        final int tcpPort = Global.TCP_PORT;
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                initTcpServer(tcpPort);
            }
        });


        final int finalServerPort = serverPort;
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //初始化ZK,并将服务地址发布到ZK根节点 "/im-dispatcher" 的子节点
                ZKClient.getInstance().init(Global.ZK_IP_PORT,Global.UDP_ZK_PATH,finalServerPort,Global.TCP_ZK_PATH,tcpPort);
            }
        });

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int httpPort = Global.HTTP_PORT;
                initHttpServer(httpPort);
                //10.69.3.47:8880
            }
        });

        //初始化客户端服务状态汇报检测
        DispatcherService.getInstance().init();

        LOG.info("服务器开启UDP,success......,端口号:{}",serverPort);
    }

}
