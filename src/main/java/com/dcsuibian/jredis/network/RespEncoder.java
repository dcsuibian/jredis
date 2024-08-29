package com.dcsuibian.jredis.network;

import com.dcsuibian.jredis.exception.RespCodecException;
import com.dcsuibian.jredis.network.resp2.*;
import com.dcsuibian.jredis.network.resp3.Resp3Object;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RespEncoder extends MessageToByteEncoder<RespObject> {
    private static final byte[] EOL = new byte[]{'\r', '\n'};

    @Override
    protected void encode(ChannelHandlerContext ctx, RespObject msg, ByteBuf out) throws Exception {
        encode(msg, out);
    }

    private void encode(RespObject msg, ByteBuf out) {
        if (!(msg instanceof Resp3Object)) {
            throw new IllegalStateException("Unknown message type");
        }
        if (msg instanceof Resp2Object) {
            if (msg instanceof RespSimpleString) {
                encodeSimpleString((RespSimpleString) msg, out);
            } else if (msg instanceof RespSimpleError) {
                encodeSimpleError((RespSimpleError) msg, out);
            } else if (msg instanceof RespInteger) {
                encodeInteger((RespInteger) msg, out);
            } else if (msg instanceof RespBulkString) {
                encodeBulkString((RespBulkString) msg, out);
            } else if (msg instanceof RespArray) {
                encodeArray((RespArray) msg, out);
            } else {
                throw new RespCodecException("Unknown message type");
            }
        } else {
            throw new RespCodecException("RESP3 is not supported yet");
        }
    }

    private void encodeSimpleString(RespSimpleString msg, ByteBuf out) {
        out.writeByte(RespObjectType.SIMPLE_STRING.getValue());
        out.writeBytes(msg.getValue());
        out.writeBytes(EOL);
    }

    private void encodeSimpleError(RespSimpleError msg, ByteBuf out) {
        out.writeByte(RespObjectType.SIMPLE_ERROR.getValue());
        out.writeBytes(msg.getValue());
        out.writeBytes(EOL);
    }

    private void encodeInteger(RespInteger msg, ByteBuf out) {
        out.writeByte(RespObjectType.INTEGER.getValue());
        String s = String.valueOf(msg.getValue());
        out.writeBytes(s.getBytes());
        out.writeBytes(EOL);
    }

    private void encodeBulkString(RespBulkString msg, ByteBuf out) {
        out.writeByte(RespObjectType.BULK_STRING.getValue());
        byte[] value = msg.getValue();
        if (null == value) {
            out.writeBytes("-1".getBytes());
            out.writeBytes(EOL);
            return;
        }
        out.writeBytes(String.valueOf(value.length).getBytes());
        out.writeBytes(EOL);
        out.writeBytes(value);
        out.writeBytes(EOL);
    }

    private void encodeArray(RespArray msg, ByteBuf out) {
        out.writeByte(RespObjectType.ARRAY.getValue());
        RespObject[] value = msg.getValue();
        if (null == value) {
            out.writeBytes("-1".getBytes());
            out.writeBytes(EOL);
            return;
        }
        out.writeBytes(String.valueOf(value.length).getBytes());
        out.writeBytes(EOL);
        for (RespObject respObject : value) {
            encode(respObject, out);
        }
    }
}
