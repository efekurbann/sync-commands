package io.github.efekurbann.synccommands.messaging.impl.rabbitmq;

import com.rabbitmq.client.*;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.logging.Logger;
import io.github.efekurbann.synccommands.messaging.Messaging;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.server.MQServer;
import io.github.efekurbann.synccommands.objects.server.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class RabbitMQ extends Messaging {

    private static final String EXCHANGE = "synccommands";
    private static final String ROUTING_KEY = "synccommands:command";

    private final String username;
    private final String virtualHost;
    private Connection connection;
    private Channel channel;
    private RabbitMQListener listener;

    public RabbitMQ(Server server, ConsoleExecutor executor, Logger logger, Scheduler scheduler) {
        super(server, executor, logger, scheduler);

        MQServer mqServer = (MQServer) server;
        this.username = mqServer.getUsername();
        this.virtualHost = mqServer.getVirtualHost();
    }

    @Override
    public void connect(String host, int port, String password, boolean secure) throws Exception { // secure is not used in RabbitMQ

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(30_000);

        addListeners(); // we are calling it in here because consuming requires a listener so it has to be initialized


        this.connection = connectionFactory.newConnection();
        this.channel = this.connection.createChannel();

        String queue = this.channel.queueDeclare("", false, true, true, null).getQueue();
        this.channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC, false, true, null);
        this.channel.queueBind(queue, EXCHANGE, ROUTING_KEY);
        this.channel.basicConsume(queue, true, this.listener, tag -> {});

    }

    @Override
    public void addListeners() {
        this.listener = new RabbitMQListener();

        listener.setName("SyncCommands - RabbitMQ Thread");
        listener.start();
    }

    @Override
    public void publishCommand(Command command) {
        scheduler.runAsync(() -> {
            try {
                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, codec.encode(command));

                printCommandSentMessage(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void close() {
        try {
            this.channel.close();
            this.connection.close();
            this.listener.interrupt();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    // i am not sure if the extending Thread is required but i'll use it anyways.
    // this need to be checked since i do not have a rabbitmq server, i can't.
    // this class extends thread just to be sure, it may not be required for rabbitmq.
    class RabbitMQListener extends Thread implements DeliverCallback {

        @Override
        public void handle(String message, Delivery delivery) {
            Command command = codec.decode(delivery.getBody());

            if (!command.getTargetServers()[0].getServerName().equals("all") && Arrays.stream(command.getTargetServers())
                    .noneMatch(s -> s.getServerName().equalsIgnoreCase(RabbitMQ.this.server.getServerName()))) return;

            execute(command);
        }

    }


}