package xyz.efekurbann.synccommands.messaging.impl.socket;

import xyz.efekurbann.synccommands.executor.ConsoleExecutor;
import xyz.efekurbann.synccommands.messaging.Messaging;
import xyz.efekurbann.synccommands.objects.Command;
import xyz.efekurbann.synccommands.objects.Server;
import xyz.efekurbann.synccommands.scheduler.Scheduler;

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
    public void connect(String host, int port, String password, boolean secure) {
        try {
            this.socketServer = new SocketServer(this, secure);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
