// Automatically generated by generate-command-code.js, do not edit.
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
        command.setSummary("Authenticate to the server");
        command.setComplexity("O(N) where N is the number of passwords defined for the user");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("auth", StandardCharsets.UTF_8));
        command.setProcessor(ConnectionCommands::authCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("A container for client connection commands");
        command.setComplexity("Depends on subcommand.");
        command.setSince("2.4.0");
        command.setDeclaredName(new Sds("client", StandardCharsets.UTF_8));
        command.setProcessor(ConnectionCommands::clientCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("A container for server configuration commands");
        command.setComplexity("Depends on subcommand.");
        command.setSince("2.0.0");
        command.setDeclaredName(new Sds("config", StandardCharsets.UTF_8));
        command.setProcessor(ServerCommands::configCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Decrement the integer value of a key by one");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("decr", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::decrCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Delete a key");
        command.setComplexity("O(N) where N is the number of keys that will be removed. When a key to remove holds a value other than a string, the individual complexity for this key is O(M) where M is the number of elements in the list, set, sorted set or hash. Removing a single key that holds a string value is O(1).");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("del", StandardCharsets.UTF_8));
        command.setProcessor(GenericCommands::delCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Determine if a key exists");
        command.setComplexity("O(N) where N is the number of keys to check.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("exists", StandardCharsets.UTF_8));
        command.setProcessor(GenericCommands::existsCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Set a key's time to live in seconds");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("expire", StandardCharsets.UTF_8));
        command.setProcessor(GenericCommands::expireCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Get the value of a key");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("get", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::getCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Delete one or more hash fields");
        command.setComplexity("O(N) where N is the number of fields to be removed.");
        command.setSince("2.0.0");
        command.setDeclaredName(new Sds("hdel", StandardCharsets.UTF_8));
        command.setProcessor(HashCommands::hdelCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Incrementally iterate hash fields and associated values");
        command.setComplexity("O(1) for every call. O(N) for a complete iteration, including enough command calls for the cursor to return back to 0. N is the number of elements inside the collection..");
        command.setSince("2.8.0");
        command.setDeclaredName(new Sds("hscan", StandardCharsets.UTF_8));
        command.setProcessor(HashCommands::hscanCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Set the string value of a hash field");
        command.setComplexity("O(1) for each field/value pair added, so O(N) to add N field/value pairs when the command is called with multiple field/value pairs.");
        command.setSince("2.0.0");
        command.setDeclaredName(new Sds("hset", StandardCharsets.UTF_8));
        command.setProcessor(HashCommands::hsetCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Increment the integer value of a key by one");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("incr", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::incrCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Get information and statistics about the server");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("info", StandardCharsets.UTF_8));
        command.setProcessor(ServerCommands::infoCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Find all keys matching the given pattern");
        command.setComplexity("O(N) with N being the number of keys in the database, under the assumption that the key names in the database and the given pattern have limited length.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("keys", StandardCharsets.UTF_8));
        command.setProcessor(GenericCommands::keysCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Get the length of a list");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("llen", StandardCharsets.UTF_8));
        command.setProcessor(ListCommands::llenCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Prepend one or multiple elements to a list");
        command.setComplexity("O(1) for each element added, so O(N) to add N elements when the command is called with multiple arguments.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("lpush", StandardCharsets.UTF_8));
        command.setProcessor(ListCommands::lpushCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Prepend an element to a list, only if the list exists");
        command.setComplexity("O(1) for each element added, so O(N) to add N elements when the command is called with multiple arguments.");
        command.setSince("2.2.0");
        command.setDeclaredName(new Sds("lpushx", StandardCharsets.UTF_8));
        command.setProcessor(ListCommands::lpushxCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Get a range of elements from a list");
        command.setComplexity("O(S+N) where S is the distance of start offset from HEAD for small lists, from nearest end (HEAD or TAIL) for large lists; and N is the number of elements in the specified range.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("lrange", StandardCharsets.UTF_8));
        command.setProcessor(ListCommands::lrangeCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Remove elements from a list");
        command.setComplexity("O(N+M) where N is the length of the list and M is the number of elements removed.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("lrem", StandardCharsets.UTF_8));
        command.setProcessor(ListCommands::lremCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Get the values of all the given keys");
        command.setComplexity("O(N) where N is the number of keys to retrieve.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("mget", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::mgetCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Set multiple keys to multiple values");
        command.setComplexity("O(N) where N is the number of keys to set.");
        command.setSince("1.0.1");
        command.setDeclaredName(new Sds("mset", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::msetCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Adds the specified elements to the specified HyperLogLog.");
        command.setComplexity("O(1) to add every element.");
        command.setSince("2.8.9");
        command.setDeclaredName(new Sds("pfadd", StandardCharsets.UTF_8));
        command.setProcessor(HyperLogLogCommands::pfaddCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Ping the server");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("ping", StandardCharsets.UTF_8));
        command.setProcessor(ConnectionCommands::pingCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Close the connection");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("quit", StandardCharsets.UTF_8));
        command.setProcessor(ConnectionCommands::quitCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Append one or multiple elements to a list");
        command.setComplexity("O(1) for each element added, so O(N) to add N elements when the command is called with multiple arguments.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("rpush", StandardCharsets.UTF_8));
        command.setProcessor(ListCommands::rpushCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Append an element to a list, only if the list exists");
        command.setComplexity("O(1) for each element added, so O(N) to add N elements when the command is called with multiple arguments.");
        command.setSince("2.2.0");
        command.setDeclaredName(new Sds("rpushx", StandardCharsets.UTF_8));
        command.setProcessor(ListCommands::rpushxCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Add one or more members to a set");
        command.setComplexity("O(1) for each element added, so O(N) to add N elements when the command is called with multiple arguments.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("sadd", StandardCharsets.UTF_8));
        command.setProcessor(SetCommands::saddCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Incrementally iterate the keys space");
        command.setComplexity("O(1) for every call. O(N) for a complete iteration, including enough command calls for the cursor to return back to 0. N is the number of elements inside the collection.");
        command.setSince("2.8.0");
        command.setDeclaredName(new Sds("scan", StandardCharsets.UTF_8));
        command.setProcessor(GenericCommands::scanCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Change the selected database for the current connection");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("select", StandardCharsets.UTF_8));
        command.setProcessor(ConnectionCommands::selectCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Set the string value of a key");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("set", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::setCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Set the value and expiration of a key");
        command.setComplexity("O(1)");
        command.setSince("2.0.0");
        command.setDeclaredName(new Sds("setex", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::setexCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Set the value of a key, only if the key does not exist");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("setnx", StandardCharsets.UTF_8));
        command.setProcessor(StringCommands::setnxCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Remove one or more members from a set");
        command.setComplexity("O(N) where N is the number of members to be removed.");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("srem", StandardCharsets.UTF_8));
        command.setProcessor(SetCommands::sremCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Incrementally iterate Set elements");
        command.setComplexity("O(1) for every call. O(N) for a complete iteration, including enough command calls for the cursor to return back to 0. N is the number of elements inside the collection..");
        command.setSince("2.8.0");
        command.setDeclaredName(new Sds("sscan", StandardCharsets.UTF_8));
        command.setProcessor(SetCommands::sscanCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Get the time to live for a key in seconds");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("ttl", StandardCharsets.UTF_8));
        command.setProcessor(GenericCommands::ttlCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Determine the type stored at key");
        command.setComplexity("O(1)");
        command.setSince("1.0.0");
        command.setDeclaredName(new Sds("type", StandardCharsets.UTF_8));
        command.setProcessor(GenericCommands::typeCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Add one or more members to a sorted set, or update its score if it already exists");
        command.setComplexity("O(log(N)) for each item added, where N is the number of elements in the sorted set.");
        command.setSince("1.2.0");
        command.setDeclaredName(new Sds("zadd", StandardCharsets.UTF_8));
        command.setProcessor(SortedSetCommands::zaddCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Remove one or more members from a sorted set");
        command.setComplexity("O(M*log(N)) with N being the number of elements in the sorted set and M the number of elements to be removed.");
        command.setSince("1.2.0");
        command.setDeclaredName(new Sds("zrem", StandardCharsets.UTF_8));
        command.setProcessor(SortedSetCommands::zremCommand);
        redisCommands.add(command);

        command = new RedisCommand();
        command.setSummary("Return a range of members in a sorted set, by index, with scores ordered from high to low");
        command.setComplexity("O(log(N)+M) with N being the number of elements in the sorted set and M the number of elements returned.");
        command.setSince("1.2.0");
        command.setDeclaredName(new Sds("zrevrange", StandardCharsets.UTF_8));
        command.setProcessor(SortedSetCommands::zrevrangeCommand);
        redisCommands.add(command);

        REDIS_COMMANDS = redisCommands.toArray(new RedisCommand[0]);
    }
}
