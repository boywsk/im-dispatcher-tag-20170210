import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.model.request.ReqServersMsg;
import com.gome.im.dispatcher.protobuf.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshikai on 16/12/16.
 */
public class TcpClient {
    private static Logger LOG = LoggerFactory.getLogger(TcpClient.class);
    private static Channel channel;

    public void init(String ip, int port, final TcpCallback callback) {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)  //长连接
                .option(ChannelOption.TCP_NODELAY, true)   //无延迟)
//                .option(ChannelOption.SO_TIMEOUT, 3000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(100, 100, 120, TimeUnit.SECONDS));
                        pipeline.addLast(new ProtobufVarint32FrameDecoder());
                        pipeline.addLast(new ProtobufDecoder(Msg.CommonMsg.getDefaultInstance()));
                        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new ClientHandler(callback));
                    }
                });
        ChannelFuture f = null;
        try {
            f = bootstrap.connect(ip, port).await();
            if (f.isSuccess()) {
                LOG.info("客户端连接服务器成功!");
                channel = f.channel();


                ReqServersMsg reqServersMsg = new ReqServersMsg();
                reqServersMsg.setType(-1);
                //注册拉取
                Msg.CommonMsg msg = CreateCommonMsg(2, "127.0.0.1:8880", JSON.toJSONString(reqServersMsg));
                channel.writeAndFlush(msg);
                /*for (int i = 0; i < 2; i++) {
                    Thread.sleep(1000);
                    //汇报
                    Msg.CommonMsg msg2 = CreateCommonMsg(1, "127.0.0.1:8880", "{\n" +
                            "    \"type\": -1,\n" +
                            "    \"ipPort\": \"127.0.0.1:8880\",\n" +
                            "    \"cmd\": [\n" +
                            "        123,\n" +
                            "        456\n" +
                            "    ]\n" +
                            "}");
                    channel.writeAndFlush(msg2);
                    LOG.info("客户端发送数字 i=" + i);
                }*/
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            f.cause();
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("连接到服务器 port:" + port);
    }


    public static Msg.CommonMsg CreateCommonMsg(int requestType, String ipPort, String jsonMsg) {
        Msg.CommonMsg.Builder builder = Msg.CommonMsg.newBuilder();
        builder.setRequestType(requestType);
        builder.setIpPort(ipPort);
        builder.setMsg(jsonMsg);
        return builder.build();
    }

    public void connect(TcpClient client) {
        String ip = "10.125.3.51";
        int port = 9966;
        client.init(ip, port, new TcpCallback() {
            @Override
            public void callback(Msg.CommonMsg msg) {
                System.out.println("客户端收到服务端消息:" + msg.getMsg() + "/t" + msg.getIsChange());
            }
        });
    }

    public static void main(String[] args) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                TcpClient client2 = new TcpClient();
                client2.connect(client2);
            }
        });
        t.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                TcpClient client3 = new TcpClient();
                client3.connect(client3);
            }
        });
        t2.start();
        TcpClient client1 = new TcpClient();
        client1.connect(client1);
    }

}
