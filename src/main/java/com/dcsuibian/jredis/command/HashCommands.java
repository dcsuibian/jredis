package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.ListPack;
import com.dcsuibian.jredis.datastructure.LongContainer;
import com.dcsuibian.jredis.datastructure.Sds;
import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.RespArray;
import com.dcsuibian.jredis.network.resp2.RespBulkString;
import com.dcsuibian.jredis.server.RedisClient;
import com.dcsuibian.jredis.server.RedisObject;
import com.dcsuibian.jredis.server.RedisServer;
import com.dcsuibian.jredis.server.SharedObjects;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.dcsuibian.jredis.util.DatabaseUtil.*;
import static com.dcsuibian.jredis.util.GenericUtil.equalsIgnoreCase;
import static com.dcsuibian.jredis.util.GenericUtil.match;
import static com.dcsuibian.jredis.util.NetworkUtil.*;
import static com.dcsuibian.jredis.util.ObjectUtil.*;

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

    /**
     * Add a new field, overwrite the old with the new value if it already exists.
     * Return 0 on insert and 1 on update.
     */
    private static int hashTypeSet(RedisObject o, Sds key, Sds value, int flags) {
        int update = 0;
        RedisServer server = RedisServer.get();
        if (RedisObject.Encoding.LIST_PACK == o.getEncoding()) {
            if (key.getLength() > server.getHashMaxListPackValue() || value.getLength() > server.getHashMaxListPackValue()) {
                hashTypeConvert(o, RedisObject.Encoding.DICTIONARY);
            }
        }

        if (RedisObject.Encoding.LIST_PACK == o.getEncoding()) {
            throw new RuntimeException("Not implemented");
        } else if (RedisObject.Encoding.DICTIONARY == o.getEncoding()) {
            Dictionary<Sds, Sds> dict = (Dictionary<Sds, Sds>) o.getValue();
            if (dict.containsKey(key)) {
                update = 1;
            }
            dict.put(key, value);
        } else {
            throw new RuntimeException("Unknown hash encoding");
        }
        return update;
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
        RedisObject o;
        if (null == (o = lookupKeyReadOrReply(c, c.getArgs()[1], null)) || isWrongType(c, o, RedisObject.Type.HASH)) {
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
        Dictionary<Sds, Sds> dict = (Dictionary<Sds, Sds>) o.getValue();
        List<Dictionary.Entry<Sds, Sds>> entries = new ArrayList<>();
        for (Dictionary.Entry<Sds, Sds> entry : dict.entrySet()) {
            Sds key = entry.getKey();
            if (null == pattern || match(pattern, key.getData(), false)) {
                entries.add(new Dictionary.Entry<>(key, entry.getValue()));
            }
        }
        entries.sort(Comparator.comparing(Dictionary.Entry::getKey));
        int begin = (int) cursor.getValue();
        int end;
        if (null == count) {
            end = entries.size();
        } else {
            end = Math.min(begin + (int) count.getValue(), entries.size());
        }
        if (begin >= end) {
            end = 0;
        }
        RespObject[] result = new RespObject[2];
        result[0] = new RespBulkString(String.valueOf(end).getBytes(StandardCharsets.UTF_8));
        List<RespObject> entriesRespObjects = new ArrayList<>();
        for (int i = begin; i < end; i++) {
            entriesRespObjects.add(new RespBulkString(entries.get(i).getKey().getData()));
            entriesRespObjects.add(new RespBulkString(entries.get(i).getValue().getData()));
        }
        result[1] = new RespArray(entriesRespObjects.toArray(new RespObject[0]));
        addArrayReply(c, new RespArray(result));
    }

    public static void hdelCommand(RedisClient c) {
        RedisObject o;
        if (null == (o = lookupKeyWriteOrReply(c, c.getArgs()[1], SharedObjects.ZERO)) || isWrongType(c, o, RedisObject.Type.HASH)) {
            return;
        }
        int deleted = 0;
        boolean keyRemoved = true;
        for (int i = 2; i < c.getArgs().length; i++) {
            if (hashTypeDelete(o, c.getArgs()[i])) {
                deleted++;
                if (0 == hashTypeLength(o)) {
                    dbDelete(c.getDatabase(), c.getArgs()[1]);
                    keyRemoved = true;
                    break;
                }
            }
        }
        RedisServer server = RedisServer.get();
        if (0 != deleted) {
            server.setDirty(server.getDirty() + deleted);
        }
        addLongReply(c, deleted);
    }

    /**
     * Delete an element from a hash.
     * Return true on deleted and false on not found.
     */
    private static boolean hashTypeDelete(RedisObject o, byte[] key) {
        boolean deleted = false;
        if (RedisObject.Encoding.LIST_PACK == o.getEncoding()) {
            throw new RuntimeException("Not implemented");
        } else if (RedisObject.Encoding.DICTIONARY == o.getEncoding()) {
            Dictionary<Sds, Sds> dict = (Dictionary<Sds, Sds>) o.getValue();
            Sds sds = new Sds(key);
            if (dict.containsKey(sds)) {
                dict.remove(sds);
                deleted = true;
            }
            if (dict.needsResize()) {
                dict.resize();
            }
        } else {
            throw new RuntimeException("Unknown hash encoding");
        }
        return deleted;
    }
}
