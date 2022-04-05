package io.github.efekurbann.synccommands.messaging.impl.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.efekurbann.synccommands.messaging.Messaging;
import redis.clients.jedis.*;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.messaging.codec.GsonCodec;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Redis extends Messaging {

    private JedisPool jedisPool;
    private RedisPubSub listener;
    private final GsonCodec<Command> codec = new GsonCodec<>(new Gson(), TypeToken.get(Command.class));

    public Redis(Server server, ConsoleExecutor executor, Logger logger, Scheduler scheduler) {
        super(server, executor, logger, scheduler);
    }

    @Override
    public void connect(String host, int port, String password, boolean secure) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(16);
        jedisPool = new JedisPool(config, host, port, 2000, password, secure);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
        }

    }

    @Override
    public void addListeners() {
        Thread thread = new Thread(() -> {
            try (Jedis jedis = Redis.this.jedisPool.getResource()) {
                Redis.this.listener = new RedisPubSub();
                jedis.subscribe(listener, "synccommands".getBytes(StandardCharsets.UTF_8));
            }
        });

        thread.setName("SyncCommands Redis Thread");
        thread.start();
    }

    @Override
    public void publishCommand(Command command) {
        scheduler.runAsync(() -> {
            try (Jedis jedis = this.jedisPool.getResource()) {
                jedis.publish("synccommands".getBytes(StandardCharsets.UTF_8), codec.encode(command));

                List<String> list = Arrays.stream(command.getTargetServers()).map(Server::getServerName).collect(Collectors.toList());
                this.logger.info(String.format("Successfully sent command to server(s): %s", String.join(", ", list)));
            }
        });
    }

    @Override
    public void close() {
        listener.unsubscribe();
        jedisPool.close();
    }

    class RedisPubSub extends BinaryJedisPubSub {

        private final ReentrantLock lock = new ReentrantLock();

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            String channelName = new String(channel, StandardCharsets.UTF_8);
            if (!channelName.equalsIgnoreCase("synccommands")) return;

            Command command = codec.decode(message);

            if (!command.getTargetServers()[0].getServerName().equals("all") && Arrays.stream(command.getTargetServers())
                    .noneMatch(s -> s.getServerName().equalsIgnoreCase(Redis.this.server.getServerName()))) return;

            logger.info("Successfully executed command: " + command.getCommand() + " from " + command.getPublisher().getServerName());
            executor.execute(command.getCommand());
        }

        @Override
        public void unsubscribe(byte[]... channels) {
            this.lock.lock();
            try {
                super.unsubscribe(channels);
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        public void subscribe(byte[]... channels) {
            this.lock.lock();
            try {
                for (byte[] channel : channels) {
                    super.subscribe(channel);
                }
            } finally {
                this.lock.unlock();
            }
        }
    }


}
