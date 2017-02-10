import com.alibaba.fastjson.JSON;
import com.gome.im.dispatcher.model.request.ReqServersMsg;

/**
 * Created by wangshikai on 16/12/16.
 */
public class TestJson {
    public static void main(String[] args) {
        ReqServersMsg reqServersMsg = new ReqServersMsg();
        reqServersMsg.setType(-1);
        System.out.println(JSON.toJSONString(reqServersMsg));
    }
}
