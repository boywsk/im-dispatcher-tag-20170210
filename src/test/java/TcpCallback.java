import com.gome.im.dispatcher.protobuf.Msg;

/**
 * Created by wangshikai on 16/12/16.
 */
public interface TcpCallback {
    public void callback(Msg.CommonMsg msg);
}
