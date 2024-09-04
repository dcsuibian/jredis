package com.dcsuibian.jredis.network.resp2;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

@Getter
public class RespSimpleError extends Resp2Object {
    public static final RespSimpleError WRONG_TYPE = new RespSimpleError("WRONGTYPE Operation against a key holding the wrong kind of value".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError ERROR = new RespSimpleError("ERR".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError NO_SUCH_KEY = new RespSimpleError("ERR no such key".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError SYNTAX = new RespSimpleError("ERR syntax error".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError SAME_OBJECT = new RespSimpleError("ERR source and destination objects are the same".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError OUT_OF_RANGE = new RespSimpleError("ERR index out of range".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError NO_SCRIPT = new RespSimpleError("NOSCRIPT No matching script. Please use EVAL.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError LOADING = new RespSimpleError("LOADING Redis is loading the dataset in memory".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError SLOW_EVAL = new RespSimpleError("BUSY Redis is busy running a script. You can only call SCRIPT KILL or SHUTDOWN NOSAVE.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError SLOW_SCRIPT = new RespSimpleError("BUSY Redis is busy running a script. You can only call FUNCTION KILL or SHUTDOWN NOSAVE.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError SLOW_MODULE = new RespSimpleError("BUSY Redis is busy running a module command.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError MASTER_DOWN = new RespSimpleError("MASTERDOWN Link with MASTER is down and replica-serve-stale-data is set to 'no'.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError BG_SAVE = new RespSimpleError("MISCONF Redis is configured to save RDB snapshots, but it's currently unable to persist to disk. Commands that may modify the data set are disabled, because this instance is configured to report errors during writes if RDB snapshotting fails (stop-writes-on-bgsave-error option). Please check the Redis logs for details about the RDB error.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError RO_SLAVE = new RespSimpleError("READONLY You can't write against a read only replica.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError NO_AUTH = new RespSimpleError("NOAUTH Authentication required.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError OOM = new RespSimpleError("OOM command not allowed when used memory > 'maxmemory'.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError EXEC_ABORT = new RespSimpleError("EXECABORT Transaction discarded because of previous errors.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError NO_REPLICAS = new RespSimpleError("NOREPLICAS Not enough good replicas to write.".getBytes(StandardCharsets.UTF_8));
    public static final RespSimpleError BUSY_KEY = new RespSimpleError("BUSYKEY Target key name already exists.".getBytes(StandardCharsets.UTF_8));

    private final byte[] value;

    public RespSimpleError(byte[] value) {
        this.value = value;
    }
}
