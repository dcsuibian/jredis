package com.dcsuibian.jredis.server;

import com.dcsuibian.jredis.asynchronous.EventLoop;
import com.dcsuibian.jredis.version.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Setter
@Getter
public class RedisServer {
    //region General
    private long mainThreadId; /* Main thread id */
    private Path configFile; /* Absolute config file path, or null */
    private RedisDatabase database;
    private AtomicInteger lruClock; /* Clock for LRU eviction */
    private boolean sentinelMode; /* True if this instance is a Sentinel. */
    //endregion
    //region Network
    private int port; /* TCP listening port */
    //endregion
    //region Statistics
    private Instant statStartTime; /* Server start time */
    private long statCommands; /* Number of processed commands */
    private long statConnections; /* Number of connections received */
    private long statExpiredKeys; /* Number of expired keys */
    private long statEvictedKeys;
    private long statEvictedClients; /* Number of evicted clients */
    private long statKeySpaceHits; /* Number of successful lookups of keys */
    private long statKeySpaceMisses; /* Number of failed lookups of keys */
    private long statAofRewrites; /* Number of aof file rewrites performed */
    private long statRdbSaves; /* Number of rdb saves performed */
    private long statRejectedConnections;
    //endregion
    //region RDB persistence
    private long dirty; /* Changes to DB from the last save */
    private Instant lastSave;
    //endregion
    //region Shutdown
    private Duration shutdownTimeout; /* Graceful shutdown time limit */
    //endregion
    //region Time cache
    private TimeZone timeZone;
    //endregion
    //region Cluster
    private boolean clusterEnabled; /* Is cluster enabled? */
    private int clusterPort;
    private Duration clusterNodeTimeout; /* Cluster node timeout */
    //endregion
    //region Scripting
    RedisClient scriptCaller;  /* The client running script right now, or NULL */
    //endregion
    //region ACLs
    private Path aclFile;  /* ACL Users file. NULL if not configured. */

    //endregion
    public static void main(String[] args) {
        try {
            log.warn("oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo");
            log.warn("Redis version={} just started", Version.REDIS_VERSION);
            if (0 == args.length) {
                log.warn("Warning: no config file specified, using the default config");
            } else {
                log.warn("Configuration loaded");
            }
            log.warn("Server initialized");
            EventLoop eventLoop = new EventLoop();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(6379));
            eventLoop.registerChannel(serverSocketChannel, SelectionKey.OP_ACCEPT, RedisServer::handleAccept);
            eventLoop.run();
            serverSocketChannel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleAccept(EventLoop loop, SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            loop.registerChannel(clientChannel, SelectionKey.OP_READ, RedisServer::handleRead);
            log.debug("Accepted connection from {}", clientChannel.getRemoteAddress());
        } catch (IOException e) {
            log.debug(e.toString());
        }
    }

    private static void handleRead(EventLoop loop, SelectionKey key) {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                clientChannel.close();
            } else {
                buffer.flip();
                clientChannel.write(buffer);
                buffer.clear();
            }
            log.warn("Read and echoed data");
        } catch (IOException e) {
            log.debug(e.toString());
        }
    }
}
