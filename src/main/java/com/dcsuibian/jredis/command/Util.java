package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.resp2.RespSimpleError;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisDatabase;
import com.dcsuibian.jredis.server.RedisObject;

public class Util {
    public static final int LOOKUP_NONE = 0;
    public static final int LOOKUP_NO_TOUCH = 1;
    public static final int LOOKUP_NO_NOTIFY = (1 << 1);
    public static final int LOOKUP_NO_STATS = (1 << 2);
    public static final int LOOKUP_WRITE = (1 << 3);
    public static final int LOOKUP_NO_EXPIRE = (1 << 4);

    public static final int FORCE_DELETE_EXPIRED = 1;
    public static final int AVOID_DELETE_EXPIRED = 2;

    /**
     * Lookup a key for read or write operations, or return NULL if the key is not
     * found in the specified DB.
     */
    public static RedisObject lookupKey(RedisDatabase db, byte[] key, int flags) {
        Sds sds = new Sds(key);
        RedisObject value = null;
        if (db.getDictionary().containsKey(sds)) {
            value = db.getDictionary().get(sds);
            int expiredFlags = 0;
            if ((flags & LOOKUP_NO_EXPIRE) != 0) {
                expiredFlags |= AVOID_DELETE_EXPIRED;
            }
            if (expireIfNeeded(db, key, expiredFlags)) {
                value = null;
            }
        }
        return value;
    }

    public static RedisObject lookupKeyWriteWithFlags(RedisDatabase db, byte[] key, int flags) {
        return lookupKey(db, key, flags | LOOKUP_WRITE);
    }

    public static RedisObject lookupKeyWrite(RedisDatabase db, byte[] key) {
        return lookupKeyWriteWithFlags(db, key, LOOKUP_NONE);
    }

    public static long getExpire(RedisDatabase db, byte[] key) {
        Sds sds = new Sds(key);
        if (0 == db.getExpires().size() || null == db.getExpires().get(sds)) {
            return -1;
        }
        return db.getExpires().get(sds);
    }

    public static boolean isKeyExpired(RedisDatabase db, byte[] key) {
        long when = getExpire(db, key);
        if (when < 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        return now > when;
    }

    public static void deleteExpiredKeyAndPropagate(RedisDatabase db, byte[] key) {
        Sds sds = new Sds(key);
        db.getDictionary().remove(sds);
        db.getExpires().remove(sds);
    }

    /**
     * The return value of the function is false if the key is still valid,
     * otherwise the function returns true if the key is expired.
     */
    public static boolean expireIfNeeded(RedisDatabase db, byte[] key, int flags) {
        if (!isKeyExpired(db, key)) {
            return false;
        }
        if ((flags & AVOID_DELETE_EXPIRED) != 0) {
            return true;
        }
        deleteExpiredKeyAndPropagate(db, key);
        return true;
    }

    public static boolean isWrongType(RedisClient c, RedisObject o, RedisObject.Type type) {
        if (null != o && o.getType() != type) {
            c.getChannelHandlerContext().writeAndFlush(RespSimpleError.WRONG_TYPE);
            return true;
        }
        return false;
    }

}
