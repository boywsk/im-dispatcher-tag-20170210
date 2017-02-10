package com.gome.im.dispatcher.global;


import com.gome.im.dispatcher.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by wangshikai on 2016/7/18.
 */
public class Global {
    private static Logger LOG = LoggerFactory.getLogger(Global.class);

    public static String CONFIG_FILE = "config.properties";

    /**
     * redis 中存储的服务器(接入层和逻辑层服务)
     */
    public static String REDIS_SERVERS_KEY = "dispatcher_servers_key";

    /**
     * redis 中存储的服务器(RPC服务)
     */
    public static String REDIS_RPC_SERVERS_KEY = "rpc_dispatcher_servers_key";

    /**
     * redis 记录被降级的im－server Set<ipPort>
     */
    public static String DELETE_SERVER_KEY = "dispatcher_delete_key";

    /**
     * redis 中存储的服务器cpu，mem计算的权重信息
     */
    public static String REDIS_SERVER_STAT_KEY = "servers_stat_key";

    /**
     * redis 短信开关配置
     */
    public static final String IS_SMS_KEY = "is_sms_key";

    //zookeeper
    public static String ZK_IP_PORT;
    public static String UDP_ZK_PATH;

    public static String TCP_ZK_PATH;

    public static String ENV;

    //mongo dbName
    public static String MONGODB_DBNAME;

    //短信平台配置
    public static String SMS_USERNAME;
    public static String SMS_PWD;
    public static String MOBILES;

    //TCP开启端口
    public static int TCP_PORT;

    //http开启端口
    public static int HTTP_PORT = 8880;


    static {
        Properties conf = PropertiesUtils.LoadProperties(CONFIG_FILE);
        //重新加载配置文件
        CONFIG_FILE = conf.getProperty("config-file");
        LOG.info("全局配置文件路径:{}",CONFIG_FILE);
        REDIS_SERVERS_KEY = conf.getProperty("dispatcher-key");
        REDIS_RPC_SERVERS_KEY = conf.getProperty("dispatcher-rpc-key");
        LOG.info("服务集群资源redis key:{}",REDIS_SERVERS_KEY);
        UDP_ZK_PATH = conf.getProperty("udp-zookeeper-path");
        TCP_ZK_PATH = conf.getProperty("tcp-zookeeper-path");
        LOG.info("服务集群资源zk udp路径:{},tcp路径:{}", UDP_ZK_PATH,TCP_ZK_PATH);
        //短信平台配置
        SMS_USERNAME = conf.getProperty("sms-username");
        SMS_PWD = conf.getProperty("sms-pwd");
        MOBILES = conf.getProperty("mobiles");
        ENV = conf.getProperty("env");
        TCP_PORT = Integer.parseInt(conf.getProperty("tcp-port"));
        HTTP_PORT = Integer.parseInt(conf.getProperty("http-port"));

        Properties properties = PropertiesUtils.LoadProperties(CONFIG_FILE);
        ZK_IP_PORT = properties.getProperty("zookeeperAddress");
        MONGODB_DBNAME = properties.getProperty("mongodb.dbName");

    }

    /**
     * 服务器状态类型
     */
    public static enum SERVER_STATUS {
        NONE(0),
        OK(1);
        public int value;
        private SERVER_STATUS(int value){
            this.value = value;
        }
    }

    /**
     * 请求类型
     */
    public static enum REQUEST_TYPE {

        REPORT(1),          // 汇报IM服务资源
        GET_RESOURCES(2),   // 获取IM服务资源

        RPC_REPORT(3),      //汇报RPC服务资源
        RPC_PULL(4),        //拉取RPC服务资源

        REPORT_STAT(5);     //汇报服务器cpu,内存等性能状态类型

        public int value;
        private REQUEST_TYPE(int value) {
            this.value = value;
        }
    }


    /**
     * 汇报的服务器类型
     */
    public static enum SERVER_TYPE {

        GATEWAY(1), // 接入
        LOGIC(2), // 逻辑
        API(3), // api
        FILE(4), // 文件
        ALL(5),  //全部
        RPC(6);  //RPC服务类型

        public int value;

        private SERVER_TYPE(int value) {
            this.value = value;
        }
    }

}
