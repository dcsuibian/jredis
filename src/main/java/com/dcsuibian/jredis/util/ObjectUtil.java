package com.dcsuibian.jredis.util;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.IntContainer;
import com.dcsuibian.jredis.datastructure.LongContainer;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespSimpleError;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class ObjectUtil {
    public static boolean isWrongType(RedisClient c, RedisObject o, RedisObject.Type type) {
        if (null != o && o.getType() != type) {
            c.getChannelHandlerContext().writeAndFlush(RespSimpleError.WRONG_TYPE);
            return true;
        }
        return false;
    }

    public static boolean getIntFromBytesOrReply(RedisClient c, byte[] bytes, IntContainer target, String message) {
        try {
            target.setValue(Integer.parseInt(new String(bytes, StandardCharsets.UTF_8)));
            return true;
        } catch (NumberFormatException e) {
            if (null == message) {
                message = "value is not an integer or out of range";
            }
            c.getChannelHandlerContext().writeAndFlush(new RespSimpleError(message.getBytes(StandardCharsets.UTF_8)));
            return false;
        }
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

    public static boolean getLongFromObjectOrReply(RedisClient c, RedisObject o, LongContainer target, String message) {
        if (RedisObject.Type.STRING != o.getType()) {
            c.getChannelHandlerContext().writeAndFlush(new RespSimpleError("value is not an integer or out of range".getBytes(StandardCharsets.UTF_8)));
            return false;
        }
        if (RedisObject.Encoding.INTEGER == o.getEncoding()) {
            target.setValue((Long) o.getValue());
            return true;
        } else {
            Sds sds = (Sds) o.getValue();
            return getLongFromBytesOrReply(c, sds.getData(), target, message);
        }
    }

    private static RedisObject createObject(RedisObject.Type type, Object value) {
        RedisObject o = new RedisObject();
        o.setType(type);
        o.setValue(value);
        return o;
    }

    public static RedisObject createHashObject() {
        Dictionary<Sds, Sds> dict = new Dictionary<>();
        RedisObject o = createObject(RedisObject.Type.HASH, dict);
        o.setEncoding(RedisObject.Encoding.DICTIONARY);
        return o;
    }

    public static RedisObject createStringObject(byte[] value) {
        RedisObject o = createObject(RedisObject.Type.STRING, new Sds(value));
        o.setEncoding(RedisObject.Encoding.SDS);
        return o;
    }

    public static RedisObject createQuickListObject() {
        LinkedList<Sds> list = new LinkedList<>();
        RedisObject o = createObject(RedisObject.Type.LIST, list);
        o.setEncoding(RedisObject.Encoding.QUICK_LIST);
        return o;
    }
}
