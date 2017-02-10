package com.gome.im.dispatcher.mongo;

import com.gome.im.dispatcher.global.Global;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Created by wangshikai on 17/1/6.
 */
public class ServerStatDao extends BaseDao {
    private final static String COLL_NAME = "t_server_stat";

    public void saveOrUpdateServerStat(String ip, double cpuInfo, double memInfo, int connections) {
        try {
            if (ip == null || ip.isEmpty()) {
                log.error("mongodb saveOrUpdateServerStat error,ip为空:{}", ip);
                return;
            }
            MongoCollection<Document> coll = this.getCollection(dbName, COLL_NAME);
            Document doc = new Document();
            if (cpuInfo >= 0) {
                doc.put("cpuInfo", cpuInfo);
            }
            if (memInfo >= 0) {
                doc.put("memInfo", memInfo);
            }
            doc.put("connections", connections);
            doc.put("serverEnv", Global.ENV);
            Bson filter = Filters.eq("ip", ip);
            coll.findOneAndUpdate(filter, new Document("$set", doc), new FindOneAndUpdateOptions().upsert(true));
        } catch (Exception e) {
            log.error("mongodb saveOrUpdateServerStat error:{},server ipPort:{}", e, ip);
        }
    }
}
