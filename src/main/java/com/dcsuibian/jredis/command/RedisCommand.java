package com.dcsuibian.jredis.command;

import com.dcsuibian.jredis.datastructure.Sds;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisCommand {
    @Getter
    @Setter
    public static class Argument {
        public enum Type {
            STRING, INTEGER, DOUBLE
        }

        private String name;
        private Type type;
        private int keySpaceIndex;
        private String token;
        private String summary;
        private String since;
        private int flags;
        private String deprecatedSince;
        private Argument[] subArguments;
        /* runtime populated data */
        private int argumentNumber;
    }

    public static final int COMMAND_DOCUMENT_NONE = 0;
    public static final int COMMAND_DOCUMENT_DEPRECATED = 1; /* Command is deprecated */
    public static final int COMMAND_DOCUMENT_SYSTEM = 1 << 1; /* System (internal) command */

    public static final RedisCommand[] REDIS_COMMANDS;

    static {
        List<RedisCommand> redisCommands = new ArrayList<>();
        REDIS_COMMANDS = redisCommands.toArray(new RedisCommand[0]);
    }


    private Sds declaredName; /* It is a SDS for all commands */
    private String summary; /* Summary of the command (optional). */
    private String complexity; /* Complexity description (optional). */
    private String since; /* Debut version of the command (optional). */
    private int documentFlags;
    private String replacedBy; /* In case the command is deprecated, this is the successor command. */
    private String deprecatedSince; /* In case the command is deprecated, when did it happen? */
    private int arity;
    private long flags;
    private RedisCommand[] subcommands;
    private Sds fullName;
}