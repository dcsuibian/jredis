package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.config.Version;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.stream.Collectors;

@Slf4j
@Setter
@Getter
public class RedisServer {
    //region
    private boolean clusterEnabled = false;
    private boolean lazyFreeLazyServerDelete = false;
    private int dirty = 0;
    private RedisDatabase[] databases;
    private int port;
    //endregion

    //region ThreadLocal
    public static final ThreadLocal<RedisServer> THREAD_LOCAL = new ThreadLocal<>();
    //endregion

    private volatile boolean running = false;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private EventLoopGroup main;

    public RedisServer(String configFileContent) {
        if (null == configFileContent) {
            port = 6379;
        } else {
            Scanner scanner = new Scanner(configFileContent);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("port")) {
                    port = Integer.parseInt(line.split(" ")[1]);
                }
            }
        }
        databases = new RedisDatabase[16];
        for (int i = 0; i < databases.length; i++) {
            databases[i] = new RedisDatabase();
        }
    }

    public RedisServer() {
        this((String) null);
    }

    public RedisServer(InputStream inputStream) {
        // This solution converts different line breaks (like \r\n) to \n.
        this(new BufferedReader(new InputStreamReader(inputStream)).lines().parallel().collect(Collectors.joining("\n")));
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
            log.warn("oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo");
            log.warn("Redis version={}", Version.REDIS_VERSION);
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

    public static RedisServer get() {
        return THREAD_LOCAL.get();
    }
}
