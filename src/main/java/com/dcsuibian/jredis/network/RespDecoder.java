package com.dcsuibian.jredis.network;

import com.dcsuibian.jredis.exception.RespCodecException;
import com.dcsuibian.jredis.network.resp2.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RespDecoder extends ByteToMessageDecoder {
    private static final int EOL_LENGTH = 2;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.isReadable()) {
            RespObjectType type = RespObjectType.valueOf(in.readByte());
            switch (type) {
                case SIMPLE_STRING:
                    RespSimpleString respSimpleString = decodeSimpleString(in);
                    if (null != respSimpleString) {
                        out.add(respSimpleString);
                    }
                    break;
                case SIMPLE_ERROR:
                    RespSimpleError respSimpleError = decodeSimpleError(in);
                    if (null != respSimpleError) {
                        out.add(respSimpleError);
                    }
                    break;
                case INTEGER:
                    RespInteger respInteger = decodeInteger(in);
                    if (null != respInteger) {
                        out.add(respInteger);
                    }
                    break;
                case BULK_STRING:
                    RespBulkString respBulkString = decodeBulkString(in);
                    if (null != respBulkString) {
                        out.add(respBulkString);
                    }
                    break;
                case ARRAY:
                    RespArray respArray = decodeArray(in);
                    if (null != respArray) {
                        out.add(respArray);
                    }
                    break;
                case NULL:
                case BOOLEAN:
                case DOUBLE:
                case BIG_NUMBER:
                case BULK_ERROR:
                case VERBATIM_STRING:
                case MAP:
                case SET:
                case PUSH:
                    throw new UnsupportedOperationException();
                default:
                    throw new RespCodecException("unknown type: " + type);
            }
        }
    }

    //region Check methods
    private boolean invalidSimpleString(ByteBuf in) {
        ByteBuf lineBytes = readLine(in);
        return null == lineBytes;
    }

    private boolean invalidSimpleError(ByteBuf in) {
        ByteBuf lineBytes = readLine(in);
        return null == lineBytes;
    }

    private boolean invalidInteger(ByteBuf in) {
        ByteBuf lineBytes = readLine(in);
        return null == lineBytes;
    }

    private boolean invalidBulkString(ByteBuf in) {
        Integer length = readLength(in);
        if (null == length) {
            return true;
        }
        if (-1 == length) {
            return false;
        }
        if (in.readableBytes() < length + EOL_LENGTH) {
            return true;
        }
        in.skipBytes(length);
        readEndOfLine(in);
        return false;
    }

    private boolean invalidArray(ByteBuf in) {
        Integer length = readLength(in);
        if (null == length) {
            return true;
        }
        if (-1 == length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!in.isReadable()) {
                return true;
            }
            RespObjectType type = RespObjectType.valueOf(in.readByte());
            switch (type) {
                case SIMPLE_STRING:
                    if (invalidSimpleString(in)) {
                        return true;
                    }
                    break;
                case SIMPLE_ERROR:
                    if (invalidSimpleError(in)) {
                        return true;
                    }
                    break;
                case INTEGER:
                    if (invalidInteger(in)) {
                        return true;
                    }
                    break;
                case BULK_STRING:
                    if (invalidBulkString(in)) {
                        return true;
                    }
                    break;
                case ARRAY:
                    if (invalidArray(in)) {
                        return true;
                    }
                    break;
                case NULL:
                case BOOLEAN:
                case DOUBLE:
                case BIG_NUMBER:
                case BULK_ERROR:
                case VERBATIM_STRING:
                case MAP:
                case SET:
                case PUSH:
                    throw new UnsupportedOperationException();
                default:
                    throw new RespCodecException("unknown type: " + type);
            }
        }
        return false;
    }
    //endregion

    //region Decode methods
    private RespSimpleString decodeSimpleString(ByteBuf in) {
        in.markReaderIndex();
        try {
            if (invalidSimpleString(in)) {
                return null;
            }
        } finally {
            in.resetReaderIndex();
        }
        ByteBuf lineBytes = readLine(in);
        assert null != lineBytes;
        byte[] bytes = new byte[lineBytes.readableBytes()];
        lineBytes.readBytes(bytes);
        return new RespSimpleString(bytes);
    }

    private RespSimpleError decodeSimpleError(ByteBuf in) {
        in.markReaderIndex();
        try {
            if (invalidSimpleError(in)) {
                return null;
            }
        } finally {
            in.resetReaderIndex();
        }
        ByteBuf lineBytes = readLine(in);
        assert null != lineBytes;
        byte[] bytes = new byte[lineBytes.readableBytes()];
        lineBytes.readBytes(bytes);
        return new RespSimpleError(bytes);
    }

    private RespInteger decodeInteger(ByteBuf in) {
        in.markReaderIndex();
        try {
            if (invalidInteger(in)) {
                return null;
            }
        } finally {
            in.resetReaderIndex();
        }
        ByteBuf lineBytes = readLine(in);
        assert null != lineBytes;
        byte[] bytes = new byte[lineBytes.readableBytes()];
        lineBytes.readBytes(bytes);
        return new RespInteger(Long.parseLong(new String(bytes)));
    }

    private RespBulkString decodeBulkString(ByteBuf in) {
        in.markReaderIndex();
        try {
            if (invalidBulkString(in)) {
                return null;
            }
        } finally {
            in.resetReaderIndex();
        }
        Integer length = readLength(in);
        assert null != length;
        if (-1 == length) {
            return new RespBulkString(null);
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        readEndOfLine(in);
        return new RespBulkString(bytes);
    }

    private RespArray decodeArray(ByteBuf in) {
        in.markReaderIndex();
        try {
            if (invalidArray(in)) {
                return null;
            }
        } finally {
            in.resetReaderIndex();
        }
        Integer length = readLength(in);
        assert null != length;
        if (-1 == length) {
            return new RespArray(null);
        }
        RespObject[] array = new RespObject[length];
        for (int i = 0; i < length; i++) {
            RespObjectType type = RespObjectType.valueOf(in.readByte());
            switch (type) {
                case SIMPLE_STRING:
                    array[i] = decodeSimpleString(in);
                    break;
                case SIMPLE_ERROR:
                    array[i] = decodeSimpleError(in);
                    break;
                case INTEGER:
                    array[i] = decodeInteger(in);
                    break;
                case BULK_STRING:
                    array[i] = decodeBulkString(in);
                    break;
                case ARRAY:
                    array[i] = decodeArray(in);
                    break;
                case NULL:
                case BOOLEAN:
                case DOUBLE:
                case BIG_NUMBER:
                case BULK_ERROR:
                case VERBATIM_STRING:
                case MAP:
                case SET:
                case PUSH:
                    throw new UnsupportedOperationException();
                default:
                    throw new RespCodecException("unknown type: " + type);
            }
        }
        return new RespArray(array);
    }
    //endregion

    //region Helper methods
    private Integer readLength(ByteBuf in) {
        ByteBuf lengthBytes = readLine(in);
        if (null == lengthBytes) {
            return null;
        }
        byte[] bytes = new byte[lengthBytes.readableBytes()];
        lengthBytes.readBytes(bytes);
        int length = Integer.parseInt(new String(bytes));
        if (length != -1 && length < 0) {
            throw new RespCodecException("invalid length: " + length);
        }
        return length;
    }

    private ByteBuf readLine(ByteBuf in) {
        if (!in.isReadable(EOL_LENGTH)) {
            return null;
        }
        int lfIndex = in.indexOf(in.readerIndex(), in.writerIndex(), (byte) '\n');
        if (lfIndex < 0) {
            return null;
        }
        ByteBuf data = in.readSlice(lfIndex - in.readerIndex() - 1); // `-1` is for CR
        readEndOfLine(in);
        return data;
    }

    private void readEndOfLine(ByteBuf in) {
        byte b0 = in.readByte();
        byte b1 = in.readByte();
        if ('\r' != b0 || '\n' != b1) {
            throw new RespCodecException("delimiter: [" + b0 + "," + b1 + "] (expected: \\r\\n)");
        }
    }
    //endregion
}
