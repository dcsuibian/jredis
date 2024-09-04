package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.*;
import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.network.resp2.RespSimpleString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import com.dcsuibian.jredis.server.SharedObjects;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.dcsuibian.jredis.util.DatabaseUtil.*;
import static com.dcsuibian.jredis.util.NetworkUtil.*;
import static com.dcsuibian.jredis.util.ObjectUtil.getLongFromBytesOrReply;

public class GenericCommands {
    public static final int EXPIRE_NX = 1;
    public static final int EXPIRE_XX = (1 << 1);
    public static final int EXPIRE_GT = (1 << 2);
    public static final int EXPIRE_LT = (1 << 3);

    private static void scanGenericCommandCleanUp() {
        // do nothing
    }

    public static void scanGenericCommand(RedisClient c, RedisObject o, long cursor) {
        LongContainer count = new LongContainer(10);
        Sds typeName = null;
        assert null == o || RedisObject.Type.SET == o.getType() || RedisObject.Type.HASH == o.getType() || RedisObject.Type.Z_SET == o.getType();
        int i = (null == o) ? 2 : 3;
        while (i < c.getArgs().length) {
            int j = c.getArgs().length - i;
            if (new String(c.getArgs()[i], StandardCharsets.UTF_8).equalsIgnoreCase("count") && j >= 2) {
                if (!getLongFromBytesOrReply(c, c.getArgs()[i + 1], count, null)) {
                    scanGenericCommandCleanUp();
                    return;
                }
                if (count.getValue() < 1) {
                    addErrorReply(c, SharedObjects.SYNTAX_ERROR);
                    scanGenericCommandCleanUp();
                    return;
                }
                i += 2;
            } else if (new String(c.getArgs()[i], StandardCharsets.UTF_8).equalsIgnoreCase("match") && j >= 2) {
                // TODO implement
            } else if (new String(c.getArgs()[i], StandardCharsets.UTF_8).equalsIgnoreCase("type") && null == o && j >= 2) {
                typeName = new Sds(c.getArgs()[i + 1]);
                i += 2;
            } else {
                addErrorReply(c, SharedObjects.SYNTAX_ERROR);
                scanGenericCommandCleanUp();
                return;
            }
            Dictionary<?, ?> dict = null;
            if (null == o) {
                dict = c.getDatabase().getDictionary();
            } else if (RedisObject.Type.SET == o.getType() && RedisObject.Encoding.DICTIONARY == o.getEncoding()) {
                dict = (Dictionary<?, ?>) o.getValue();
            } else if (RedisObject.Type.HASH == o.getType() && RedisObject.Encoding.DICTIONARY == o.getEncoding()) {
                dict = (Dictionary<?, ?>) o.getValue();
            } else if (RedisObject.Type.Z_SET == o.getType() && RedisObject.Encoding.SKIP_LIST == o.getEncoding()) {
                ZSet zSet = (ZSet) o.getValue();
                dict = zSet.getDictionary();
                count.setValue(count.getValue() * 2);
            }
            if (null != dict) {

            } else if (RedisObject.Type.SET == o.getType()) {

            } else if (RedisObject.Type.HASH == o.getType() || RedisObject.Type.Z_SET == o.getType()) {

            } else {
                throw new RuntimeException("Not handled encoding in SCAN.");
            }
        }
    }

    public static void scanCommand(RedisClient c) {
        LongContainer cursor = new LongContainer();
        if (!parseScanCursorOrReply(c, c.getArgs()[1], cursor)) {
            return;
        }
        scanGenericCommand(c, null, cursor.getValue());
    }

    public static void ttlCommand(RedisClient c) {
        // TODO implement
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(RespSimpleString.OK);
    }

    public static void typeCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        String type;
        RedisObject o = c.getDatabase().getDictionary().get(new Sds(c.getArgs()[1]));
        if (null == o) {
            type = "none";
        } else {
            switch (o.getType()) {
                case STRING:
                    type = "string";
                    break;
                case LIST:
                    type = "list";
                    break;
                case SET:
                    type = "set";
                    break;
                case Z_SET:
                    type = "zset";
                    break;
                case HASH:
                    type = "hash";
                    break;
                case STREAM:
                    type = "stream";
                    break;
                default:
                    type = "unknown";
            }
        }
        ctx.writeAndFlush(new RespBulkString(type.getBytes(StandardCharsets.UTF_8)));
    }

    public static void keysCommand(RedisClient c) {
        // TODO Return the queried keys, not all keys
        Set<Sds> keySet = c.getDatabase().getDictionary().keySet();
        List<RespObject> list = new ArrayList<>();
        for (Sds key : keySet) {
            list.add(new RespBulkString(key.getData()));
        }
        RespArray respArray = new RespArray(list.toArray(new RespObject[0]));
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        ctx.writeAndFlush(respArray);
    }

    /**
     * Parse additional flags of expire commands
     * Supported flags:
     * - NX: set expiry only when the key has no expiry
     * - XX: set expiry only when the key has an existing expiry
     * - GT: set expiry only when the new expiry is greater than current one
     * - LT: set expiry only when the new expiry is less than current one
     */
    private static boolean parseExtendedExpireArgumentsOrReply(RedisClient c, IntContainer flags) {
        boolean nx = false;
        boolean xx = false;
        boolean gt = false;
        boolean lt = false;
        int i = 3;
        while (i < c.getArgs().length) {
            byte[] option = c.getArgs()[i];
            if ("nx".equalsIgnoreCase(new String(option, StandardCharsets.UTF_8))) {
                flags.setValue(flags.getValue() | EXPIRE_NX);
                nx = true;
            } else if ("xx".equalsIgnoreCase(new String(option, StandardCharsets.UTF_8))) {
                flags.setValue(flags.getValue() | EXPIRE_XX);
                xx = true;
            } else if ("gt".equalsIgnoreCase(new String(option, StandardCharsets.UTF_8))) {
                flags.setValue(flags.getValue() | EXPIRE_GT);
                gt = true;
            } else if ("lt".equalsIgnoreCase(new String(option, StandardCharsets.UTF_8))) {
                flags.setValue(flags.getValue() | EXPIRE_LT);
                lt = true;
            } else {
                addErrorReply(c, "Unsupported option " + new String(option, StandardCharsets.UTF_8));
                return false;
            }
            i++;
        }
        if ((nx && xx) || (nx && gt) || (nx && lt)) {
            addErrorReply(c, "NX and XX, GT or LT options at the same time are not compatible");
            return false;
        }
        if (gt && lt) {
            addErrorReply(c, "GT and LT options at the same time are not compatible");
            return false;
        }
        return true;
    }

    private static boolean checkAlreadyExpired(long when) {
        return when <= System.currentTimeMillis();
    }

    private static void expireGenericCommand(RedisClient c, long baseTime, TimeUnit unit) {
        byte[] key = c.getArgs()[1];
        byte[] param = c.getArgs()[2];
        LongContainer when = new LongContainer();

        IntContainer flag = new IntContainer(0);
        if (parseExtendedExpireArgumentsOrReply(c, flag)) {
            return;
        }
        if (!getLongFromBytesOrReply(c, param, when, null)) {
            return;
        }
        if (TimeUnit.SECONDS == unit) {
            if (when.getValue() > Long.MAX_VALUE / 1000 || when.getValue() < Long.MIN_VALUE / 1000) {
                addErrorExpireTimeReply(c);
                return;
            }
            when.setValue(when.getValue() * 1000);
        }
        if (when.getValue() > Long.MAX_VALUE - baseTime) {
            addErrorExpireTimeReply(c);
            return;
        }
        when.setValue(when.getValue() + baseTime);

        if (null == lookupKeyWrite(c.getDatabase(), key)) {
            addReply(c, SharedObjects.ZERO);
            return;
        }
        if (0 != flag.getValue()) {
            long currentExpire = getExpire(c.getDatabase(), key);
            /* NX option is set, check current expiry */
            if (0 != (flag.getValue() & EXPIRE_NX)) {
                if (-1 != currentExpire) {
                    addReply(c, SharedObjects.ZERO);
                    return;
                }
            }
            /* XX option is set, check current expiry */
            if (0 != (flag.getValue() & EXPIRE_XX)) {
                if (-1 == currentExpire) {
                    addReply(c, SharedObjects.ZERO);
                    return;
                }
            }
            /* GT option is set, check current expiry */
            if (0 != (flag.getValue() & EXPIRE_GT)) {
                if (when.getValue() <= currentExpire || -1 == currentExpire) {
                    addReply(c, SharedObjects.ZERO);
                    return;
                }
            }
            /* LT option is set, check current expiry */
            if (0 != (flag.getValue() & EXPIRE_LT)) {
                if (-1 != currentExpire && when.getValue() >= currentExpire) {
                    addReply(c, SharedObjects.ZERO);
                    return;
                }
            }
        }
        // TODO implement
    }

    /**
     * EXPIRE key seconds [ NX | XX | GT | LT]
     */
    public static void expireCommand(RedisClient c) {
        expireGenericCommand(c, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * EXPIREAT key unix-time-seconds [ NX | XX | GT | LT]
     */
    public static void expireatCommand(RedisClient c) {
        expireGenericCommand(c, 0, TimeUnit.SECONDS);
    }

    /**
     * PEXPIRE key milliseconds [ NX | XX | GT | LT]
     */
    public static void pexpireCommand(RedisClient c) {
        expireGenericCommand(c, System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * PEXPIREAT key unix-time-milliseconds [ NX | XX | GT | LT]
     */
    public static void pexpireatCommand(RedisClient c) {
        expireGenericCommand(c, 0, TimeUnit.MILLISECONDS);
    }

    public static void existsCommand(RedisClient c) {
        long count = 0;
        for (int i = 1; i < c.getArgs().length; i++) {
            if (null != lookupKeyReadWithFlags(c.getDatabase(), c.getArgs()[i], LOOKUP_NO_TOUCH)) {
                count++;
            }
        }
        addLongReply(c, count);
    }

    public static void delCommand(RedisClient c) {
        ChannelHandlerContext ctx = c.getChannelHandlerContext();
        c.getDatabase().getDictionary().remove(new Sds(c.getArgs()[1]));
        ctx.writeAndFlush(RespSimpleString.OK);
    }
}
