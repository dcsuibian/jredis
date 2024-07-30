package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.Sds;

public class RedisDatabase {
    private int id;
    private Dictionary<Sds, RedisObject<Object>> dictionary;
    private Dictionary<Sds, Long> expires;
}
