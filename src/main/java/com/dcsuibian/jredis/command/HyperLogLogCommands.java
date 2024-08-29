package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.HyperLogLog;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import io.netty.channel.ChannelHandlerContext;

public class HyperLogLogCommands {
    public static void pfaddCommand(RedisClient client) {
        byte[][] args = client.getArgs();
        Sds key = new Sds(args[0]);
        Dictionary<Sds, RedisObject> dictionary = client.getDatabase().getDictionary();
        RedisObject o = dictionary.get(key);
        int update = 0;
        if (null == o) {
            o = new RedisObject(new HyperLogLog());
            dictionary.put(key, o);
            update++;
        }
        HyperLogLog hll = (HyperLogLog) o.getValue();
        for (int i = 2; i < args.length; i++) {
            byte[] element = args[i];
            if (hll.add(element)) {
                update++;
            }
        }
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(new RespInteger(update > 0 ? 1 : 0));
    }
}
