import com.gome.im.dispatcher.protobuf.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by wangshikai on 16/12/16.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private TcpCallback callback;

    public ClientHandler(TcpCallback callback){
        this.callback = callback;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("client .....");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.callback.callback((Msg.CommonMsg)msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE)
                System.out.println("read idle");
            else if (event.state() == IdleState.WRITER_IDLE)
                System.out.println("write idle");
            else if (event.state() == IdleState.ALL_IDLE)
                System.out.println("all idle");
        }
    }

}
