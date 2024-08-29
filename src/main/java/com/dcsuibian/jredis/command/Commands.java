package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Sds;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static final RedisCommand[] REDIS_COMMANDS;

    static {
        RedisCommand command;
        List<RedisCommand> redisCommands = new ArrayList<>();

        command = new RedisCommand();
        command.setDeclaredName(new Sds("ping", StandardCharsets.UTF_8));
        command.setProcessor(ConnectionCommands::pingCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setDeclaredName(new Sds("quit", StandardCharsets.UTF_8));
        command.setProcessor(ConnectionCommands::quitCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setDeclaredName(new Sds("pfadd", StandardCharsets.UTF_8));
        command.setProcessor(HyperLogLogCommands::pfaddCommand);
        redisCommands.add(command);

        REDIS_COMMANDS = redisCommands.toArray(new RedisCommand[0]);
    }
}
