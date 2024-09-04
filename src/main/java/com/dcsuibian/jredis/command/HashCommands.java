package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.ListPack;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import com.dcsuibian.jredis.server.SharedObjects;
import io.netty.channel.ChannelHandlerContext;

import static com.dcsuibian.jredis.util.DatabaseUtil.dbAdd;
import static com.dcsuibian.jredis.util.DatabaseUtil.lookupKeyWrite;
import static com.dcsuibian.jredis.util.NetworkUtil.*;
import static com.dcsuibian.jredis.util.ObjectUtil.createHashObject;
import static com.dcsuibian.jredis.util.ObjectUtil.isWrongType;

public class HashCommands {
    private static final int HASH_SET_COPY = 0;

    private static RedisObject hashTypeLookupWriteOrCreate(RedisClient c, byte[] key) {
        RedisObject o = lookupKeyWrite(c.getDatabase(), key);
        if (isWrongType(c, o, RedisObject.Type.HASH)) {
            return null;
        }
        if (null == o) {
            o = createHashObject();
            dbAdd(c.getDatabase(), key, o);
        }
        return o;
    }

    private static int hashTypeSet(RedisObject o, Sds key, Sds value, int flags) {
        int update = 0;
        // TODO implement
        throw new RuntimeException("Not implemented");
    }

    private static void hashTypeTryConvert(RedisObject o, byte[][] args, int begin, int end) {
        if (RedisObject.Encoding.LIST_PACK != o.getEncoding()) {
            return;
        }
        int sum = 0;
        for (int i = begin; i <= end; i++) {
            sum += args[i].length;
        }
        if (!((ListPack) o.getValue()).isSafeToAdd(sum)) {
            hashTypeConvert(o, RedisObject.Encoding.DICTIONARY);
        }
    }

    private static void hashTypeConvert(RedisObject o, RedisObject.Encoding encoding) {
        if (RedisObject.Encoding.LIST_PACK == o.getEncoding()) {
            hashTypeConvertListPack(o, encoding);
        } else if (RedisObject.Encoding.DICTIONARY == o.getEncoding()) {
            throw new RuntimeException("Not implemented");
        } else {
            throw new RuntimeException("Unknown hash encoding");
        }
    }

    private static void hashTypeConvertListPack(RedisObject o, RedisObject.Encoding encoding) {
        assert RedisObject.Encoding.LIST_PACK == o.getEncoding();
        if (encoding == RedisObject.Encoding.LIST_PACK) {
            return;
        } else if (encoding == RedisObject.Encoding.DICTIONARY) {
            ListPack listPack = (ListPack) o.getValue();
            Dictionary<Sds, Sds> dict = new Dictionary<>();
            dict.expand(hashTypeLength(o));
            for (int i = 0; i < listPack.getLength(); i += 2) {
                dict.put(new Sds(listPack.getStringValue(i)), new Sds(listPack.getStringValue(i + 1)));
            }
            o.setEncoding(RedisObject.Encoding.DICTIONARY);
            o.setValue(dict);
        } else {
            throw new RuntimeException("Unknown hash encoding");
        }
    }

    private static int hashTypeLength(RedisObject o) {
        if (RedisObject.Encoding.LIST_PACK == o.getEncoding()) {
            return ((ListPack) o.getValue()).getLength() / 2;
        } else if (RedisObject.Encoding.DICTIONARY == o.getEncoding()) {
            return ((Dictionary<Sds, Sds>) o.getValue()).size();
        } else {
            throw new RuntimeException("Unknown hash encoding");
        }
    }

    public static void hsetCommand(RedisClient c) {
        if (1 == c.getArgs().length % 2) {
            addErrorArityReply(c);
            return;
        }
        RedisObject o;
        if (null == (o = hashTypeLookupWriteOrCreate(c, c.getArgs()[1]))) {
            return;
        }
        hashTypeTryConvert(o, c.getArgs(), 2, c.getArgs().length - 1);
        int created = 0;
        for (int i = 2; i < c.getArgs().length; i += 2) {
            created += 1 - hashTypeSet(o, new Sds(c.getArgs()[i]), new Sds(c.getArgs()[i + 1]), HASH_SET_COPY);
        }
        byte[] commandName = c.getArgs()[0];
        if ('s' == commandName[1] || 'S' == commandName[1]) {
            addLongReply(c, created);
        } else {
            addReply(c, SharedObjects.OK);
        }
    }

    public static void hscanCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void hdelCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
