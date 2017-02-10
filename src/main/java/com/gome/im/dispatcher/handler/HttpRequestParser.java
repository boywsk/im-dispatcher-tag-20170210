package com.gome.im.dispatcher.handler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangshikai on 17/1/3.
 */
public class HttpRequestParser {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private FullHttpRequest fullReq;

    /**
     * 构造一个解析器
     *
     * @param req
     */
    public HttpRequestParser(FullHttpRequest req) {
        this.fullReq = req;
    }

    /**
     * 解析请求参数
     *
     * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
     */
    public Map<String, String> parse() {
        HttpMethod method = fullReq.method();

        Map<String, String> parmMap = new HashMap<>();

        try {
            if (HttpMethod.GET == method) {
                // 是GET请求
                QueryStringDecoder decoder = new QueryStringDecoder(fullReq.uri());
                for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                    parmMap.put(entry.getKey(), entry.getValue().get(0));
                }
            } else if (HttpMethod.POST == method) {
                // 是POST请求
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
                decoder.offer(fullReq);
                List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
                for (InterfaceHttpData parm : parmList) {
                    Attribute data = (Attribute) parm;
                    parmMap.put(data.getName(), data.getValue());
                }
            } else {
                // 不支持其它方法
            }
        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("http parse error:{}",e);
        }

        return parmMap;
    }
}
