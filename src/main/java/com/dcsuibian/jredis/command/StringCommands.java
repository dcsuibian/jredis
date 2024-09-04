package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.*;
import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.server.*;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.dcsuibian.jredis.util.CompareUtil.equalsIgnoreCase;
import static com.dcsuibian.jredis.util.DatabaseUtil.*;
import static com.dcsuibian.jredis.util.NetworkUtil.*;
import static com.dcsuibian.jredis.util.ObjectUtil.*;

public class StringCommands {
    private static final int SET_KEY_KEEP_TTL = 1;
    private static final int SET_KEY_NO_SIGNAL = 2;
    private static final int SET_KEY_ALREADY_EXIST = 4;
    private static final int SET_KEY_DOESNT_EXIST = 8;

    private static final int COMMAND_GET = 0;
    private static final int COMMAND_SET = 1;

    private static final int OBJ_NO_FLAGS = 0;
    private static final int OBJ_SET_NX = 1;
    private static final int OBJ_SET_XX = 1 << 1;
    private static final int OBJ_EX = 1 << 2;
    private static final int OBJ_PX = 1 << 3;
    private static final int OBJ_KEEP_TTL = 1 << 4;
    private static final int OBJ_SET_GET = 1 << 5;
    private static final int OBJ_EX_AT = 1 << 6;
    private static final int OBJ_PX_AT = 1 << 7;
    private static final int OBJ_PERSIST = 1 << 8;

    private static boolean getGenericCommand(RedisClient c) {
        RedisObject o;
        if (null == (o = lookupKeyReadOrReply(c, c.getArgs()[1], SharedObjects.NULL[c.getRespVersion()]))) {
            return true;
        }
        if (isWrongType(c, o, RedisObject.Type.STRING)) {
            return false;
        }
        if (RedisObject.Encoding.SDS == o.getEncoding()) {
            addBulkStringReply(c, (Sds) o.getValue());
        } else if (RedisObject.Encoding.INTEGER == o.getEncoding()) {
            addBulkStringReply(c, ((Long) o.getValue()).toString().getBytes(StandardCharsets.UTF_8));
        } else {
            throw new RuntimeException("Unknown string encoding");
        }
        return true;
    }

    public static void getCommand(RedisClient c) {
        getGenericCommand(c);
    }

    private static boolean parseExtendedStringArgumentsOrReply(RedisClient c, IntContainer flags, TimeUnitContainer unit, BytesContainer expire, int commandType) {
        int i = commandType == COMMAND_GET ? 2 : 3;
        for (; i < c.getArgs().length; i++) {
            byte[] option = c.getArgs()[i];
            byte[] next = i == c.getArgs().length - 1 ? null : c.getArgs()[i + 1];
            if (('n' == option[0] || 'N' == option[0]) && ('x' == option[1] || 'X' == option[1]) && 2 == option.length && 0 == (flags.getValue() & OBJ_SET_XX) && (commandType == COMMAND_SET)) {
                flags.setValue(flags.getValue() | OBJ_SET_NX);
            } else if (('x' == option[0] || 'X' == option[0]) && ('x' == option[1] || 'X' == option[1]) && 2 == option.length && 0 == (flags.getValue() & OBJ_SET_NX) && (commandType == COMMAND_SET)) {
                flags.setValue(flags.getValue() | OBJ_SET_XX);
            } else if (('g' == option[0] || 'G' == option[0]) && ('e' == option[1] || 'E' == option[1]) && ('t' == option[2] || 'T' == option[2]) && 3 == option.length && (commandType == COMMAND_SET)) {
                flags.setValue(flags.getValue() | OBJ_SET_GET);
            } else if (equalsIgnoreCase(option, "KEEPTTL") && 0 == (flags.getValue() & OBJ_PERSIST) && 0 == (flags.getValue() & OBJ_EX) && 0 == (flags.getValue() & OBJ_EX_AT) && 0 == (flags.getValue() & OBJ_PX) && 0 == (flags.getValue() & OBJ_PX_AT) && (commandType == COMMAND_SET)) {
                flags.setValue(flags.getValue() | OBJ_KEEP_TTL);
            } else if (equalsIgnoreCase(option, "PERSIST") && (commandType == COMMAND_GET) && 0 == (flags.getValue() & OBJ_EX) && 0 == (flags.getValue() & OBJ_EX_AT) && 0 == (flags.getValue() & OBJ_PX) && 0 == (flags.getValue() & OBJ_PX_AT) && 0 == (flags.getValue() & OBJ_KEEP_TTL)) {
                flags.setValue(flags.getValue() | OBJ_PERSIST);
            } else if (('e' == option[0] || 'E' == option[0]) && ('x' == option[1] || 'X' == option[1]) && 2 == option.length && 0 == (flags.getValue() & OBJ_KEEP_TTL) && 0 == (flags.getValue() & OBJ_PERSIST) && 0 == (flags.getValue() & OBJ_EX_AT) && 0 == (flags.getValue() & OBJ_PX) && 0 == (flags.getValue() & OBJ_PX_AT) && null != next) {
                flags.setValue(flags.getValue() | OBJ_EX);
                expire.setValue(next);
                i++;
            } else if (('p' == option[0] || 'P' == option[0]) && ('x' == option[1] || 'X' == option[1]) && 2 == option.length && 0 == (flags.getValue() & OBJ_KEEP_TTL) && 0 == (flags.getValue() & OBJ_PERSIST) && 0 == (flags.getValue() & OBJ_EX_AT) && 0 == (flags.getValue() & OBJ_EX) && 0 == (flags.getValue() & OBJ_PX_AT) && null != next) {
                flags.setValue(flags.getValue() | OBJ_PX);
                unit.setValue(TimeUnit.MILLISECONDS);
                expire.setValue(next);
                i++;
            } else if (('e' == option[0] || 'E' == option[0]) && ('x' == option[1] || 'X' == option[1]) && ('a' == option[2] || 'A' == option[2]) && ('t' == option[3] || 'T' == option[3]) && 4 == option.length && 0 == (flags.getValue() & OBJ_KEEP_TTL) && 0 == (flags.getValue() & OBJ_PERSIST) && 0 == (flags.getValue() & OBJ_EX) && 0 == (flags.getValue() & OBJ_PX) && 0 == (flags.getValue() & OBJ_PX_AT) && null != next) {
                flags.setValue(flags.getValue() | OBJ_EX_AT);
                expire.setValue(next);
                i++;
            } else if (('p' == option[0] || 'P' == option[0]) && ('x' == option[1] || 'X' == option[1]) && ('a' == option[2] || 'A' == option[2]) && ('t' == option[3] || 'T' == option[3]) && 4 == option.length && 0 == (flags.getValue() & OBJ_KEEP_TTL) && 0 == (flags.getValue() & OBJ_PERSIST) && 0 == (flags.getValue() & OBJ_EX) && 0 == (flags.getValue() & OBJ_PX) && 0 == (flags.getValue() & OBJ_EX_AT) && null != next) {
                flags.setValue(flags.getValue() | OBJ_PX_AT);
                unit.setValue(TimeUnit.MILLISECONDS);
                expire.setValue(next);
                i++;
            } else {
                addErrorReply(c, SharedObjects.SYNTAX_ERROR);
                return false;
            }
        }
        return true;
    }

    private static boolean getExpireMillisecondsOrReply(RedisClient c, byte[] expire, int flags, TimeUnit unit, LongContainer milliseconds) {
        if (!getLongFromBytesOrReply(c, expire, milliseconds, null)) {
            return false;
        }
        if (milliseconds.getValue() <= 0 || (unit == TimeUnit.SECONDS && milliseconds.getValue() > Long.MAX_VALUE / 1000)) {
            addErrorExpireTimeReply(c);
            return false;
        }
        if (unit == TimeUnit.SECONDS) {
            milliseconds.setValue(milliseconds.getValue() * 1000);
        }
        if ((flags & OBJ_PX) != 0 || (flags & OBJ_EX) != 0) {
            milliseconds.setValue(milliseconds.getValue() + System.currentTimeMillis());
        }
        if (milliseconds.getValue() <= 0) {
            addErrorExpireTimeReply(c);
            return false;
        }
        return true;
    }

    private static void setGenericCommand(RedisClient c, int flags, byte[] key, byte[] value, byte[] expire, TimeUnit unit, RespObject okReply, RespObject abortReply) {
        LongContainer milliseconds = new LongContainer(0);
        if (null != expire && getExpireMillisecondsOrReply(c, expire, flags, unit, milliseconds)) {
            return;
        }
        if ((flags & OBJ_SET_GET) != 0) {
            if (!getGenericCommand(c)) {
                return;
            }
        }
        boolean found = lookupKeyWrite(c.getDatabase(), key) != null;
        if ((flags & OBJ_SET_NX) != 0 && found || (flags & OBJ_SET_XX) != 0 && !found) {
            if ((flags & OBJ_SET_GET) == 0) {
                addReply(c, abortReply == null ? SharedObjects.NULL[c.getRespVersion()] : abortReply);
            }
            return;
        }
        int setKeyFlags = 0;
        setKeyFlags |= (flags & OBJ_KEEP_TTL) != 0 ? SET_KEY_KEEP_TTL : 0;
        setKeyFlags |= found ? SET_KEY_ALREADY_EXIST : SET_KEY_DOESNT_EXIST;
        setKey(c, c.getDatabase(), key, value, setKeyFlags);
        RedisServer server = RedisServer.get();

        server.setDirty(server.getDirty() + 1);

        if (null != expire) {
            setExpire(c, c.getDatabase(), key, milliseconds.getValue());
        }
        if (0 == (flags & OBJ_SET_GET)) {
            addReply(c, okReply == null ? SharedObjects.OK : okReply);
        }
    }

    private static void setKey(RedisClient c, RedisDatabase db, byte[] key, byte[] value, int flags) {
        boolean keyFound = false;
        if (0 != (flags & SET_KEY_ALREADY_EXIST)) {
            keyFound = true;
        } else if (0 == (flags & SET_KEY_DOESNT_EXIST)) {
            keyFound = lookupKeyWrite(db, key) != null;
        }
        if (keyFound) {
            dbAdd(db, key, createStringObject(value));
        } else {
            dbOverwrite(db, key, createStringObject(value));
        }
        if (0 == (flags & SET_KEY_KEEP_TTL)) {
            deleteExpire(db, key);
        }
    }

    public static void setCommand(RedisClient c) {
        BytesContainer expire = new BytesContainer(null);
        TimeUnitContainer unit = new TimeUnitContainer(TimeUnit.SECONDS);
        IntContainer flags = new IntContainer(OBJ_NO_FLAGS);
        if (!parseExtendedStringArgumentsOrReply(c, flags, unit, expire, COMMAND_SET)) {
            return;
        }
        setGenericCommand(c, flags.getValue(), c.getArgs()[1], c.getArgs()[2], expire.getValue(), unit.getValue(), null, null);
    }

    public static void setnxCommand(RedisClient c) {
        setGenericCommand(c, OBJ_SET_NX, c.getArgs()[1], c.getArgs()[2], null, TimeUnit.SECONDS, SharedObjects.ONE, SharedObjects.ZERO);
    }

    public static void setexCommand(RedisClient c) {
        setGenericCommand(c, OBJ_EX, c.getArgs()[1], c.getArgs()[3], c.getArgs()[2], TimeUnit.SECONDS, null, null);
    }

    public static void psetexCommand(RedisClient c) {
        setGenericCommand(c, OBJ_PX, c.getArgs()[1], c.getArgs()[3], c.getArgs()[2], TimeUnit.MILLISECONDS, null, null);
    }

    private static void msetGenericCommand(RedisClient c, boolean nx) {
        if (0 == c.getArgs().length % 2) {
            addErrorArityReply(c);
            return;
        }
        if (nx) {
            for (int i = 1; i < c.getArgs().length; i += 2) {
                if (lookupKeyWrite(c.getDatabase(), c.getArgs()[i]) != null) {
                    addReply(c, SharedObjects.ZERO);
                    return;
                }
            }
        }
        for (int i = 1; i < c.getArgs().length; i += 2) {
            setKey(c, c.getDatabase(), c.getArgs()[i], c.getArgs()[i + 1], 0);
        }
        RedisServer server = RedisServer.get();
        server.setDirty(server.getDirty() + (c.getArgs().length - 1) / 2);
        addReply(c, nx ? SharedObjects.ONE : SharedObjects.OK);
    }

    public static void msetCommand(RedisClient c) {
        msetGenericCommand(c, false);
    }

    public static void msetnxCommand(RedisClient c) {
        msetGenericCommand(c, true);
    }

    public static void mgetCommand(RedisClient c) {
        RespObject[] array = new RespObject[c.getArgs().length - 1];
        for (int i = 1; i < c.getArgs().length; i++) {
            RedisObject o = lookupKeyRead(c.getDatabase(), c.getArgs()[i]);
            if (null == o) {
                array[i - 1] = SharedObjects.NULL[c.getRespVersion()];
            } else {
                if (RedisObject.Type.STRING != o.getType()) {
                    array[i - 1] = SharedObjects.NULL[c.getRespVersion()];
                } else {
                    array[i - 1] = new RespBulkString(((Sds) o.getValue()).getData());
                }
            }
        }
        addReply(c, new RespArray(array));
    }

    private static void incrDecrCommand(RedisClient c, long increment) {
        RedisObject o = lookupKeyWrite(c.getDatabase(), c.getArgs()[1]);
        if (isWrongType(c, o, RedisObject.Type.STRING)) {
            return;
        }
        LongContainer valueContainer = new LongContainer();
        if (!getLongFromObjectOrReply(c, o, valueContainer, null)) {
            return;
        }
        long oldValue = valueContainer.getValue();
        if ((increment < 0 && oldValue < 0 && increment < (Long.MIN_VALUE - oldValue)) || (increment > 0 && oldValue > 0 && increment > (Long.MAX_VALUE - oldValue))) {
            addErrorReply(c, "increment or decrement would overflow");
            return;
        }
        long value = oldValue + increment;
        if (RedisObject.Encoding.INTEGER == o.getEncoding()) {
            o.setValue(value);
        } else {
            o.setValue(new Sds(Long.toString(value).getBytes(StandardCharsets.UTF_8)));
        }
        RedisServer server = RedisServer.get();
        server.setDirty(server.getDirty() + 1);
        addLongReply(c, value);
    }

    public static void incrCommand(RedisClient c) {
        incrDecrCommand(c, 1);
    }

    public static void decrCommand(RedisClient c) {
        incrDecrCommand(c, -1);
    }
}