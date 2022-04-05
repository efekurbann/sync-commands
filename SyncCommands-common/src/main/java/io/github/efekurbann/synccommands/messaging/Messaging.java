package io.github.efekurbann.synccommands.messaging;

import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;

import java.util.logging.Logger;

public abstract class Messaging {

    protected final Logger logger;
    protected final Server server;
    protected final ConsoleExecutor executor;
    protected final Scheduler scheduler;

    public Messaging(Server server, ConsoleExecutor executor, Logger logger, Scheduler scheduler) {
        this.server = server;
        this.executor = executor;
        this.logger = logger;
        this.scheduler = scheduler;
    }

    public abstract void connect(String host, int port, String password, boolean secure);

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
}