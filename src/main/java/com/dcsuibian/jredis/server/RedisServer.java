package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.network.RespDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Getter
public class RedisServer {
    private final int port;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private EventLoopGroup main;

    public RedisServer() {
        this.port = 6379;
    }

    public RedisServer(int port) {
        this.port = port;
    }

    public void start() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        main = new NioEventLoopGroup(1);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.group(boss, worker);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                ch.pipeline().addLast(new RespDecoder());
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.info("Received: {}", msg);
                        super.channelRead(ctx, msg);
                    }
                });

            }
        });
        serverBootstrap.bind(port).syncUninterruptibly();
    }

    public void stop() {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
        main.shutdownGracefully();
    }
}
