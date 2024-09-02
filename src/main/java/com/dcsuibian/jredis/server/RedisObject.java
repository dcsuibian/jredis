package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.datastructure.HyperLogLog;
import com.dcsuibian.jredis.datastructure.QuickList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisObject {
    public enum Type {
        STRING, LIST, SET, Z_SET, HASH, MODULE, STREAM
    }

    public enum Encoding {
        SDS, INTEGER, DICTIONARY, LINKED_LIST, INT_SET, STREAM, LIST_PACK, HYPER_LOG_LOG, QUICK_LIST
    }

    private Type type;
    private Encoding encoding;
    private int lru;
    private Object value;

    public RedisObject() {
    }

    public RedisObject(HyperLogLog hyperLogLog) {
        this.type = Type.STRING;
        this.encoding = Encoding.HYPER_LOG_LOG;
        this.value = hyperLogLog;
    }

    public RedisObject(QuickList quickList) {
        this.type = Type.LIST;
        this.encoding = Encoding.QUICK_LIST;
        this.value = quickList;
    }
}
