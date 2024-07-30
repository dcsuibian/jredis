package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.datastructure.Sds;

import java.time.Instant;

public class RedisClient {
    private long id;
    private long flags;
    private RedisDatabase database;
    private RedisObject<Sds> name;
    private Sds queryBuffer;
    private User user;
    private Instant createTime;
    private long duration;
    private boolean authenticated;
}
