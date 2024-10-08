package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.datastructure.HyperLogLog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisObject {
    public enum Type {
        STRING, LIST, SET, Z_SET, HASH, HYPER_LOG_LOG, STREAM,
    }

    public enum Encoding {
        SDS, INTEGER, DICTIONARY, LINKED_LIST, INT_SET, STREAM, LIST_PACK, HYPER_LOG_LOG, QUICK_LIST, SKIP_LIST
    }

    private Type type;
    private Encoding encoding;
    private int lru;
    private Object value;

    public RedisObject() {
    }

    public RedisObject(HyperLogLog hyperLogLog) {
        this.type = Type.HYPER_LOG_LOG;
        this.encoding = Encoding.HYPER_LOG_LOG;
        this.value = hyperLogLog;
    }
}
