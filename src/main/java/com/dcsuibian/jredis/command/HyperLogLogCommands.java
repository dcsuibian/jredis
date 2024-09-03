package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.HyperLogLog;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import com.dcsuibian.jredis.server.SharedObjects;

import static com.dcsuibian.jredis.util.DatabaseUtil.dbAdd;
import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyWrite;
import static com.dcsuibian.jredis.util.NetworkUtil.addReply;

public class HyperLogLogCommands {
    public static void pfaddCommand(RedisClient c) {
        RedisObject o = lookupKeyWrite(c.getDatabase(), c.getArgs()[1]);
        int updated = 0;
        if (null == o) {
            o = new RedisObject(new HyperLogLog());
            dbAdd(c.getDatabase(), c.getArgs()[1], o);
            updated++;
        }
        HyperLogLog hll = (HyperLogLog) o.getValue();
        for (int i = 2; i < c.getArgs().length; i++) {
            byte[] element = c.getArgs()[i];
            if (hll.add(element)) {
                updated++;
            }
        }
        addReply(c, updated != 0 ? SharedObjects.ONE : SharedObjects.ZERO);
    }
}
