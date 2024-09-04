package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.network.RespObject;
import com.dcsuibian.jredis.network.resp2.*;
import com.dcsuibian.jredis.network.resp3.RespNull;

public class SharedObjects {
    public static final RespSimpleString OK = RespSimpleString.OK;
    public static final RespBulkString EMPTY_BULK_STRING = RespBulkString.EMPTY;
    public static final RespInteger ZERO = RespInteger.ZERO;
    public static final RespInteger ONE = RespInteger.ONE;
    public static final RespArray EMPTY_ARRAY = RespArray.EMPTY;
    public static final RespSimpleString PONG = RespSimpleString.PONG;
    public static final RespSimpleString QUEUED = RespSimpleString.QUEUED;

    public static final RespSimpleError WRONG_TYPE_ERROR = RespSimpleError.WRONG_TYPE;
    public static final RespSimpleError ERROR = RespSimpleError.ERROR;
    public static final RespSimpleError NO_SUCH_KEY_ERROR = RespSimpleError.NO_SUCH_KEY;
    public static final RespSimpleError SYNTAX_ERROR = RespSimpleError.SYNTAX;
    public static final RespSimpleError SAME_OBJECT_ERROR = RespSimpleError.SAME_OBJECT;
    public static final RespSimpleError OUT_OF_RANGE_ERROR = RespSimpleError.OUT_OF_RANGE;
    public static final RespSimpleError NO_SCRIPT_ERROR = RespSimpleError.NO_SCRIPT;
    public static final RespSimpleError LOADING_ERROR = RespSimpleError.LOADING;
    public static final RespSimpleError SLOW_EVAL_ERROR = RespSimpleError.SLOW_EVAL;
    public static final RespSimpleError SLOW_SCRIPT_ERROR = RespSimpleError.SLOW_SCRIPT;
    public static final RespSimpleError SLOW_MODULE_ERROR = RespSimpleError.SLOW_MODULE;
    public static final RespSimpleError MASTER_DOWN_ERROR = RespSimpleError.MASTER_DOWN;
    public static final RespSimpleError BG_SAVE_ERROR = RespSimpleError.BG_SAVE;
    public static final RespSimpleError RO_SLAVE_ERROR = RespSimpleError.RO_SLAVE;
    public static final RespSimpleError NO_AUTH_ERROR = RespSimpleError.NO_AUTH;
    public static final RespSimpleError OOM_ERROR = RespSimpleError.OOM;
    public static final RespSimpleError EXEC_ABORT_ERROR = RespSimpleError.EXEC_ABORT;
    public static final RespSimpleError NO_REPLICAS_ERROR = RespSimpleError.NO_REPLICAS;
    public static final RespSimpleError BUSY_KEY_ERROR = RespSimpleError.BUSY_KEY;

    public static final RespObject[] NULL = new RespObject[4];
    public static final RespObject[] NULL_ARRAY = new RespObject[4];

    static {
        NULL[2] = RespBulkString.NULL;
        NULL[3] = RespNull.NULL;

        NULL_ARRAY[2] = RespArray.NULL;
        NULL_ARRAY[3] = RespNull.NULL;
    }
}
