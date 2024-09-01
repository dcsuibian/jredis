package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.IntSet;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

import static com.dcsuibian.jredis.command.Util.isWrongType;
import static com.dcsuibian.jredis.command.Util.lookupKeyWrite;

public class SetCommands {
    public static void sscanCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void sremCommand(RedisClient client) {
        // TODO implement
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void saddCommand(RedisClient client) {
        RedisObject set = lookupKeyWrite(client.getDatabase(), client.getArgs()[1]);
        if (isWrongType(client, set, RedisObject.Type.SET)) {
            return;
        }
        if (null == set) {
            set = setTypeCreate(client.getArgs()[2]);
            client.getDatabase().getDictionary().put(new Sds(client.getArgs()[1]), set);
        }
        int added = 0;
        for (int i = 2; i < client.getArgs().length; i++) {
            if (setTypeAdd(set, client.getArgs()[i])) {
                added++;
            }
        }
        ChannelHandlerContext ctx = client.getChannelHandlerContext();
        ctx.writeAndFlush(new RespInteger(added));
    }

    private static boolean setTypeAdd(RedisObject subject, byte[] value) {
        if (RedisObject.Encoding.DICTIONARY == subject.getEncoding()) {
            Dictionary<Sds, Object> dict = (Dictionary<Sds, Object>) subject.getValue();
            if (dict.containsKey(new Sds(value))) {
                return false;
            }
            dict.put(new Sds(value), null);
            return true;
        } else if (RedisObject.Encoding.INT_SET == subject.getEncoding()) {
            IntSet intSet = (IntSet) subject.getValue();
            try {
                long longValue = Long.parseLong(new String(value, StandardCharsets.UTF_8));
                boolean success = intSet.add(longValue);
                if (success) {
                    int maxEntries = Math.min(512, 1 << 30);
                    if (intSet.getLength() > maxEntries) {
                        setTypeConvert(subject, RedisObject.Encoding.DICTIONARY);
                    }
                }
                return success;
            } catch (NumberFormatException e) {
                setTypeConvert(subject, RedisObject.Encoding.DICTIONARY);
                Dictionary<Sds, Object> dict = (Dictionary<Sds, Object>) subject.getValue();
                if (dict.containsKey(new Sds(value))) {
                    return false;
                }
                dict.put(new Sds(value), null);
                return true;
            }
        } else {
            throw new RuntimeException("Unknown set encoding");
        }
    }

    private static void setTypeConvert(RedisObject setObj, RedisObject.Encoding encoding) {
        if (RedisObject.Encoding.DICTIONARY == encoding) {
            IntSet intSet = (IntSet) setObj.getValue();
            Dictionary<Sds, Object> d = new Dictionary<>();
            d.expand(intSet.getLength());
            for (long value : intSet) {
                Sds sds = new Sds(String.valueOf(value), StandardCharsets.UTF_8);
                d.put(sds, null);
            }
        } else {
            throw new RuntimeException("Unsupported set conversion");
        }
    }

    private static RedisObject setTypeCreate(byte[] value) {
        RedisObject result = new RedisObject();
        result.setType(RedisObject.Type.SET);
        try {
            Long.parseLong(new String(value, StandardCharsets.UTF_8));
            result.setEncoding(RedisObject.Encoding.INT_SET);
            result.setValue(new IntSet());
        } catch (NumberFormatException e) {
            result.setEncoding(RedisObject.Encoding.DICTIONARY);
            result.setValue(new Dictionary<Sds, Object>());
        }
        return result;
    }
}
