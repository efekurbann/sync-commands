package io.github.efekurbann.synccommands.messaging.impl.socket;

import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SocketClient {

    private final SocketImpl socket;

    public SocketClient(SocketImpl socket) {
        this.socket = socket;
    }

    public void sendCommand(Command command) {
        for (Server server : command.getTargetServers()) {
            try (Socket socket = new Socket(InetAddress.getByName(server.getHost()), server.getPort());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                if (socket.isClosed()) return;

                out.close();
                out.writeUTF(server.getServerName());
                out.writeUTF(command.getCommand());
                out.writeUTF(command.getPublisher().getServerName());
                out.writeUTF(server.getPassword());

                this.socket.getLogger().info(String.format("Successfully sent command to server: %s:%d (%s)",
                        server.getHost(), server.getPort(), server.getServerName()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
