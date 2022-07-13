package io.github.efekurbann.synccommands.messaging.impl.socket;

import io.github.efekurbann.synccommands.executor.ConsoleExecutor;
import io.github.efekurbann.synccommands.messaging.Messaging;
import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.server.Server;
import io.github.efekurbann.synccommands.scheduler.Scheduler;

import java.io.IOException;
import java.util.logging.Logger;

public class SocketImpl extends Messaging {

    private SocketServer socketServer;
    private final SocketClient socketClient;

    public SocketImpl(Server server, ConsoleExecutor executor, Logger logger, Scheduler scheduler) {
        super(server, executor, logger, scheduler);
        this.socketClient = new SocketClient(this);
    }

    @Override
    public void connect(String host, int port, String password, boolean secure) throws IOException {
        this.socketServer = new SocketServer(this, secure);
    }

    @Override
    public void addListeners() {
        this.socketServer.start();
    }

    @Override
    public void publishCommand(Command command) {
        scheduler.runAsync(()->this.socketClient.sendCommand(command));
    }

    @Override
    public void close() {
        if (this.socketServer.getServerSocket().isClosed()) return;

        try {
            this.socketServer.getServerSocket().close();
        } catch (IOException ignored) {
            // ik ik ik
        }
    }

}
