package io.github.efekurbann.synccommands.messaging.impl.socket;

import io.github.efekurbann.synccommands.objects.Command;
import io.github.efekurbann.synccommands.objects.server.Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
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
                if (socket.isClosed()) continue;

                out.writeUTF(server.getServerName());
                out.writeUTF(command.getCommand());
                out.writeUTF(command.getPublisher().getServerName());
                out.writeUTF(server.getPassword());

            } catch (IOException ex) {
                if (ex instanceof ConnectException) {
                    socket.getLogger().severe(String.format("Tried to send command to %s but could not reach. " +
                            "Is it offline?", server.getServerName()));
                    continue;
                }
                ex.printStackTrace();
            }
        }

        this.socket.printCommandSentMessage(command);
    }

}
