package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.IntSet;
import com.dcsuibian.jredis.datastructure.LongContainer;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.network.resp2.RespInteger;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import com.dcsuibian.jredis.server.SharedObjects;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.dcsuibian.jredis.util.DatabaseUtil.*;
import static com.dcsuibian.jredis.util.GenericUtil.equalsIgnoreCase;
import static com.dcsuibian.jredis.util.GenericUtil.match;
import static com.dcsuibian.jredis.util.NetworkUtil.*;
import static com.dcsuibian.jredis.util.ObjectUtil.getLongFromBytesOrReply;
import static com.dcsuibian.jredis.util.ObjectUtil.isWrongType;

public class SetCommands {
    public static void sscanCommand(RedisClient c) {
        RedisObject o;
        if (null == (o = lookupKeyReadOrReply(c, c.getArgs()[1], null)) || isWrongType(c, o, RedisObject.Type.SET)) {
            return;
        }
        LongContainer cursor = new LongContainer();
        byte[] pattern = null;
        LongContainer count = null;
        if (!parseScanCursorOrReply(c, c.getArgs()[2], cursor)) {
            return;
        }
        if (1 != c.getArgs().length % 2 || c.getArgs().length > 7) {
            addErrorArityReply(c);
            return;
        }
        for (int i = 3; i < c.getArgs().length; i += 2) {
            if (equalsIgnoreCase(c.getArgs()[i], "count")) {
                count = new LongContainer();
                if (!getLongFromBytesOrReply(c, c.getArgs()[i + 1], count, null)) {
                    return;
                }
                if (count.getValue() < 1) {
                    addErrorReply(c, SharedObjects.SYNTAX_ERROR);
                    return;
                }
            } else if (equalsIgnoreCase(c.getArgs()[i], "match")) {
                pattern = c.getArgs()[i + 1];
            } else {
                addErrorReply(c, SharedObjects.SYNTAX_ERROR);
                return;
            }
        }
        if (RedisObject.Encoding.DICTIONARY == o.getEncoding()) {
            List<Sds> keys = new ArrayList<>();
            Dictionary<Sds, Object> dict = (Dictionary<Sds, Object>) o.getValue();
            for (Sds key : dict.keySet()) {
                if (null == pattern || match(pattern, key.getData(), false)) {
                    keys.add(key);
                }
            }
            keys.sort(Sds::compareTo);
            int begin = (int) cursor.getValue();
            int end;
            if (null == count) {
                end = keys.size();
            } else {
                end = Math.min(begin + (int) count.getValue(), keys.size());
            }
            if (begin >= end) {
                end = 0;
            }
            RespObject[] result = new RespObject[2];
            result[0] = new RespBulkString(String.valueOf(end).getBytes(StandardCharsets.UTF_8));
            List<RespObject> keysRespObjects = new ArrayList<>();
            for (int i = begin; i < end; i++) {
                keysRespObjects.add(new RespBulkString(keys.get(i).getData()));
            }
            result[1] = new RespArray(keysRespObjects.toArray(new RespObject[0]));
            addArrayReply(c, new RespArray(result));
        } else if (RedisObject.Encoding.INT_SET == o.getEncoding()) {
            IntSet intSet = (IntSet) o.getValue();
            List<Long> elements = new ArrayList<>();
            for (Long element : intSet) {
                if (null == pattern || match(pattern, String.valueOf(element).getBytes(StandardCharsets.UTF_8), false)) {
                    elements.add(element);
                }
            }
            elements.sort(Long::compareTo);
            int begin = (int) cursor.getValue();
            int end;
            if (null == count) {
                end = elements.size();
            } else {
                end = Math.min(begin + (int) count.getValue(), elements.size());
            }
            if (begin >= end) {
                end = 0;
            }
            RespObject[] result = new RespObject[2];
            result[0] = new RespBulkString(String.valueOf(end).getBytes(StandardCharsets.UTF_8));
            List<RespObject> elementsRespObjects = new ArrayList<>();
            for (int i = begin; i < end; i++) {
                elementsRespObjects.add(new RespBulkString(String.valueOf(elements.get(i)).getBytes(StandardCharsets.UTF_8)));
            }
            result[1] = new RespArray(elementsRespObjects.toArray(new RespObject[0]));
            addArrayReply(c, new RespArray(result));
        } else {
            throw new RuntimeException("Unknown set encoding");
        }
    }

    public static void sremCommand(RedisClient c) {
        RedisObject set;
        if (null == (set = lookupKeyWriteOrReply(c, c.getArgs()[1], RespInteger.ZERO)) || isWrongType(c, set, RedisObject.Type.SET)) {
            return;
        }
        int deleted = 0;
        boolean keyRemoved = false;
        for (int i = 2; i < c.getArgs().length; i++) {
            if (setTypeRemove(set, c.getArgs()[i])) {
                deleted++;
                if (0 == setTypeSize(set)) {
                    Sds key = new Sds(c.getArgs()[1]);
                    c.getDatabase().getDictionary().remove(key);
                    c.getDatabase().getExpires().remove(key);
                    keyRemoved = true;
                    break;
                }
            }
        }
        c.getChannelHandlerContext().writeAndFlush(new RespInteger(deleted));
    }

    private static boolean setTypeRemove(RedisObject setObj, byte[] value) {
        if (RedisObject.Encoding.DICTIONARY == setObj.getEncoding()) {
            Dictionary<Sds, Object> dict = (Dictionary<Sds, Object>) setObj.getValue();
            Sds key = new Sds(value);
            if (dict.containsKey(key)) {
                dict.remove(key);
                if (dict.needsResize()) {
                    dict.resize();
                }
                return true;
            } else {
                return false;
            }
        } else if (RedisObject.Encoding.INT_SET == setObj.getEncoding()) {
            IntSet intSet = (IntSet) setObj.getValue();
            try {
                long longValue = Long.parseLong(new String(value, StandardCharsets.UTF_8));
                return intSet.remove(longValue);
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            throw new RuntimeException("Unknown set encoding");
        }
    }


    private static int setTypeSize(RedisObject set) {
        if (RedisObject.Encoding.DICTIONARY == set.getEncoding()) {
            return ((Dictionary<Sds, Object>) set.getValue()).size();
        } else if (RedisObject.Encoding.INT_SET == set.getEncoding()) {
            return ((IntSet) set.getValue()).size();
        } else {
            throw new RuntimeException("Unknown set encoding");
        }
    }

    public static void saddCommand(RedisClient c) {
        RedisObject set = lookupKeyWrite(c.getDatabase(), c.getArgs()[1]);
        if (isWrongType(c, set, RedisObject.Type.SET)) {
            return;
        }
        if (null == set) {
            set = setTypeCreate(c.getArgs()[2]);
            c.getDatabase().getDictionary().put(new Sds(c.getArgs()[1]), set);
        }
        int added = 0;
        for (int i = 2; i < c.getArgs().length; i++) {
            if (setTypeAdd(set, c.getArgs()[i])) {
                added++;
            }
        }
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
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
