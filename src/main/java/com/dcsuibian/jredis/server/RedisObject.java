package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.datastructure.HyperLogLog;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisObject {
    public enum Type {
        SDS, DICTIONARY, INT_SET, SKIP_LIST, QUICK_LIST, LIST_PACK, HyperLogLog
    }

    private Type type;
    private int lru;
    private Object value;

    public RedisObject() {
    }

    public RedisObject(HyperLogLog hyperLogLog) {
        this.type = Type.HyperLogLog;
        this.value = hyperLogLog;
    }
}
