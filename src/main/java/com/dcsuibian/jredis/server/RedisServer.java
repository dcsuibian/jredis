package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.network.CommandHandler;
import com.dcsuibian.jredis.network.RespDecoder;
import com.dcsuibian.jredis.network.RespEncoder;
import io.netty.bootstrap.ServerBootstrap;
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
    private volatile boolean running = false;
    private final int port;
    private RedisDatabase[] databases;

    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private EventLoopGroup main;

    public RedisServer() {
        this(6379);
    }

    public RedisServer(int port) {
        this.port = port;
        databases = new RedisDatabase[16];
        for (int i = 0; i < databases.length; i++) {
            databases[i] = new RedisDatabase();
        }
    }

    public void start() {
        if (running) {
            return;
        }
        synchronized (this) {
            if (running) {
                return;
            }
            running = true;
            boss = new NioEventLoopGroup();
            worker = new NioEventLoopGroup();
            main = new NioEventLoopGroup(1);
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            final RedisServer redisServer = this;
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    ch.pipeline().addLast(new RespEncoder());
                    ch.pipeline().addLast(new RespDecoder());
                    ch.pipeline().addLast(main, new CommandHandler(redisServer));
                }
            });
            serverBootstrap.bind(port).syncUninterruptibly();
        }
    }

    public void stop() {
        if (!running) {
            return;
        }
        synchronized (this) {
            if (!running) {
                return;
            }
            running = false;
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            main.shutdownGracefully();
        }
    }
}
