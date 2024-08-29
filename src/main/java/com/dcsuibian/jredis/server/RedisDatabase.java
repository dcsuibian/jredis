package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.datastructure.Dictionary;
import com.dcsuibian.jredis.datastructure.Sds;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisDatabase {
    private int id;
    private Dictionary<Sds, RedisObject> dictionary;
    private Dictionary<Sds, Long> expires;

    public RedisDatabase() {
        this.dictionary = new Dictionary<>();
        this.expires = new Dictionary<>();
    }
}
