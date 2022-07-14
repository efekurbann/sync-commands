package io.github.efekurbann.synccommands.messaging;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.logging.Logger;
import io.github.efekurbann.synccommands.messaging.codec.GsonCodec;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.server.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Messaging {

    protected final Logger logger;
    protected final Server server;
    protected final ConsoleExecutor executor;
    protected final Scheduler scheduler;
    protected final GsonCodec<Command> codec = new GsonCodec<>(new Gson(), TypeToken.get(Command.class));

    public Messaging(Server server, ConsoleExecutor executor, Logger logger, Scheduler scheduler) {
        this.server = server;
        this.executor = executor;
        this.logger = logger;
        this.scheduler = scheduler;
    }

    public abstract void connect(String host, int port, String password, boolean secure) throws Exception;

    public abstract void addListeners();

    public abstract void publishCommand(Command command);

    public abstract void close();

    public ConsoleExecutor getExecutor() {
        return executor;
    }

    public Logger getLogger() {
        return logger;
    }

    public Server getServer() {
        return server;
    }

    public void execute(Command command) {
        logger.info(String.format("Successfully executed command: \"%s\" from server %s",
                command.getCommand(), command.getPublisher().getServerName()));
        executor.execute(command.getCommand());
    }

    public void execute(String command, String publisher) {
        logger.info(String.format("Successfully executed command: \"%s\" from server %s", command, publisher));
        executor.execute(command);
    }

    public void printCommandSentMessage(Command command) {
        List<String> list = Arrays.stream(command.getTargetServers()).map(Server::getServerName).collect(Collectors.toList());
        this.logger.info(String.format("Successfully sent command to server(s): %s", String.join(", ", list)));
    }

}
