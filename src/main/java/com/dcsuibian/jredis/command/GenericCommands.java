package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.IntContainer;
import com.dcsuibian.jredis.datastructure.LongContainer;
import com.dcsuibian.jredis.datastructure.Sds;
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
import java.util.concurrent.TimeUnit;

import static com.dcsuibian.jredis.util.DatabaseUtil.*;
import static com.dcsuibian.jredis.util.GenericUtil.equalsIgnoreCase;
import static com.dcsuibian.jredis.util.GenericUtil.match;
import static com.dcsuibian.jredis.util.NetworkUtil.*;
import static com.dcsuibian.jredis.util.ObjectUtil.getLongFromBytesOrReply;

public class GenericCommands {
    public static final int EXPIRE_NX = 1;
    public static final int EXPIRE_XX = (1 << 1);
    public static final int EXPIRE_GT = (1 << 2);
    public static final int EXPIRE_LT = (1 << 3);

    public static void scanCommand(RedisClient c) {
        LongContainer cursor = new LongContainer();
        byte[] pattern = null;
        LongContainer count = null;
        if (!parseScanCursorOrReply(c, c.getArgs()[1], cursor)) {
            return;
        }
        if (0 != c.getArgs().length % 2 || c.getArgs().length > 6) {
            addErrorArityReply(c);
            return;
        }
        for (int i = 2; i < c.getArgs().length; i += 2) {
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
        List<Sds> keys = new ArrayList<>();
        for (Sds key : c.getDatabase().getDictionary().keySet()) {
            if (null == pattern || match(pattern, key.getData(), false) && !expireIfNeeded(c.getDatabase(), key.getData(), 0)) {
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
        RespObject[] result = new RespObject[2];
        result[0] = new RespBulkString(String.valueOf(end).getBytes(StandardCharsets.UTF_8));
        List<RespObject> keysRespObjects = new ArrayList<>();
        for (int i = begin; i < end; i++) {
            keysRespObjects.add(new RespBulkString(keys.get(i).getData()));
        }
        result[1] = new RespArray(keysRespObjects.toArray(new RespObject[0]));
        addArrayReply(c, new RespArray(result));
    }

    /**
     * Implements TTL, PTTL, EXPIRETIME and PEXPIRETIME
     */
    private static void ttlGenericCommand(RedisClient c, boolean outputMs, boolean outputAbs) {
        if (null == lookupKeyReadWithFlags(c.getDatabase(), c.getArgs()[1], LOOKUP_NO_TOUCH)) {
            addLongReply(c, -2);
            return;
        }
        long expire = getExpire(c.getDatabase(), c.getArgs()[1]);
        long ttl = -1;
        if (-1 != expire) {
            ttl = outputAbs ? expire : expire - System.currentTimeMillis();
            if (ttl < 0) {
                ttl = 0;
            }
        }
        if (-1 == ttl) {
            addLongReply(c, -1);
        } else {
            addLongReply(c, outputMs ? ttl : ((ttl + 500) / 1000));
        }
    }

    /**
     * TTL key
     */
    public static void ttlCommand(RedisClient c) {
        ttlGenericCommand(c, false, false);
    }

    /**
     * PTTL key
     */
    public static void pttlCommand(RedisClient c) {
        ttlGenericCommand(c, true, false);
    }

    /**
     * EXPIRETIME key
     */
    public static void expiretimeCommand(RedisClient c) {
        ttlGenericCommand(c, false, true);
    }

    /**
     * PEXPIRETIME key
     */
    public static void pexpiretimeCommand(RedisClient c) {
        ttlGenericCommand(c, true, true);
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
                case HYPER_LOG_LOG:
                    type = "hyperloglog";
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
        byte[] pattern = c.getArgs()[1];
        boolean allKeys = '*' == pattern[0] && 1 == pattern.length;
        List<RespObject> respObjects = new ArrayList<>();
        for (Dictionary.Entry<Sds, RedisObject> entry : c.getDatabase().getDictionary().entrySet()) {
            Sds key = entry.getKey();
            if (allKeys || match(pattern, key.getData(), false)) {
                respObjects.add(new RespBulkString(key.getData()));
            }
        }
        addArrayReply(c, new RespArray(respObjects.toArray(new RespObject[0])));
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
