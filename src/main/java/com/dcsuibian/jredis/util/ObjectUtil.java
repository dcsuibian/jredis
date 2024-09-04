package com.dcsuibian.jredis.util;

import com.dcsuibian.jredis.datastructure.ListPack;
import com.dcsuibian.jredis.datastructure.LongContainer;
import com.dcsuibian.jredis.network.resp2.RespSimpleError;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;

import java.nio.charset.StandardCharsets;

public class ObjectUtil {
    public static boolean isWrongType(RedisClient c, RedisObject o, RedisObject.Type type) {
        if (null != o && o.getType() != type) {
            c.getChannelHandlerContext().writeAndFlush(RespSimpleError.WRONG_TYPE);
            return true;
        }
        return false;
    }

    public static boolean getLongFromBytesOrReply(RedisClient c, byte[] bytes, LongContainer target, String message) {
        try {
            target.setValue(Long.parseLong(new String(bytes, StandardCharsets.UTF_8)));
            return true;
        } catch (NumberFormatException e) {
            if (null == message) {
                message = "value is not an integer or out of range";
            }
            c.getChannelHandlerContext().writeAndFlush(new RespSimpleError(message.getBytes(StandardCharsets.UTF_8)));
            return false;
        }
    }

    private static RedisObject createObject(RedisObject.Type type, Object value) {
        RedisObject o = new RedisObject();
        o.setType(type);
        o.setValue(value);
        return o;
    }

    public static RedisObject createHashObject() {
        ListPack listPack = new ListPack(0);
        RedisObject o = createObject(RedisObject.Type.HASH, listPack);
        o.setEncoding(RedisObject.Encoding.LIST_PACK);
        return o;
    }
}
