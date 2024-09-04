import { fileURLToPath } from 'url'
import fs from 'fs/promises'
import path from 'path'

const SUPPORTED_COMMANDS = [
  'auth',
  'config',
  'scan',
  'info',
  'client',
  'set',
  'type',
  'ttl',
  'get',
  'quit',
  'setnx',
  'lpush',
  'lrange',
  'lrem',
  'rpush',
  'del',
  'sadd',
  'sscan',
  'srem',
  'hset',
  'hscan',
  'hdel',
  'zadd',
  'zrevrange',
  'zrem',
  'setex',
  'exists',
  'expire',
  'ping',
  'select',
  'keys',
  'incr',
  'decr',
  'mset',
  'mget',
  'pfadd',
  'lpushx',
  'rpushx',
  'llen',
]

const GROUPS = {
  generic: 'Generic',
  string: 'String',
  list: 'List',
  set: 'Set',
  sorted_set: 'SortedSet',
  hash: 'Hash',
  pubsub: 'PubSub',
  transactions: 'Transaction',
  connection: 'Connection',
  server: 'Server',
  scripting: 'Script',
  hyperloglog: 'HyperLogLog',
  cluster: 'Cluster',
  sentinel: 'Sentinel',
  geo: 'Geo',
  stream: 'Stream',
  bitmap: 'Bitmap',
}

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const subCommands = {}
const commands = {}

class Command {
  constructor(name, description) {
    this.name = name
    this.description = description
    this.group = this.description['group']
    this.subCommands = []
    this.args = []
    for (const argDescription of this.description['args'] || []) {
    }
  }

  get fullName() {
    return this.name.replaceAll('-', '_').replaceAll(':', '')
  }
}

class SubCommand extends Command {
  constructor(name, description) {
    super(name, description)
    this.containerName = description['container']
  }
}

function createCommand(name, description) {
  if (description['container']) {
    const command = new SubCommand(name, description)
    if (!subCommands[description['container']]) {
      subCommands[description['container']] = {}
    }
    subCommands[description['container']][name] = command
  } else {
    commands[name] = new Command(name, description)
  }
}

async function main() {
  const commandsDir = path.join(__dirname, 'commands')
  const javaCommandPackageDir = path.resolve(__dirname, '../src/main/java/com/dcsuibian/jredis/command')
  console.log('Processing json files...')
  for (const file of await fs.readdir(commandsDir)) {
    const data = await fs.readFile(path.join(commandsDir, file), 'utf-8')
    const json = JSON.parse(data)
    for (const [name, description] of Object.entries(json)) {
      createCommand(name, description)
    }
  }
  console.log('Linking container command to subcommands...')
  for (const command of Object.values(commands)) {
    if (!subCommands[command.name]) {
      continue
    }
    for (const subCommand of Object.values(subCommands[command.name])) {
      subCommand.group = command.group
      command.subCommands.push(subCommand)
    }
  }
  console.log('Generating Commands.java...')
  const javaCommandsFile = path.join(javaCommandPackageDir, 'Commands.java')
  let content = `// Automatically generated by ${path.basename(__filename)}, do not edit.\n`
  content += 'package com.dcsuibian.jredis.command;\n'
  content += '\n'
  content += 'import com.dcsuibian.jredis.datastructure.Sds;\n'
  content += '\n'
  content += 'import java.nio.charset.StandardCharsets;\n'
  content += 'import java.util.ArrayList;\n'
  content += 'import java.util.List;\n'
  content += '\n'
  content += 'public class Commands {\n'
  content += '    public static final RedisCommand[] REDIS_COMMANDS;\n'
  content += '\n'
  content += '    static {\n'
  content += '        RedisCommand command;\n'
  content += '        List<RedisCommand> redisCommands = new ArrayList<>();\n'
  for (const command of Object.values(commands).sort((a, b) => a.name.localeCompare(b.name))) {
    const commandName = command.name.toLowerCase()
    if (!SUPPORTED_COMMANDS.includes(commandName)) {
      continue
    }
    const group = GROUPS[command.group]
    content += '\n'
    content += `        command = new RedisCommand();\n`
    content += `        command.setSummary("${command.description['summary']}");\n`
    content += `        command.setComplexity("${command.description['complexity']}");\n`
    content += `        command.setSince("${command.description['since']}");\n`
    content += `        command.setDeclaredName(new Sds("${commandName}", StandardCharsets.UTF_8));\n`
    content += `        command.setProcessor(${group}Commands::${commandName}Command);\n`
    content += `        command.setFullName(new Sds("${command.fullName}", StandardCharsets.UTF_8));\n`
    content += `        redisCommands.add(command);\n`
  }
  content += '\n'
  content += '        REDIS_COMMANDS = redisCommands.toArray(new RedisCommand[0]);\n'
  content += '    }\n'
  content += '}\n'
  await fs.writeFile(javaCommandsFile, content)
}

main()
  .then(() => console.log('All done, exiting.'))
  .catch(console.error)
