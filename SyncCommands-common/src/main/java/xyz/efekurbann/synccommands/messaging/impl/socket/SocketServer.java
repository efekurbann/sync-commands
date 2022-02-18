package xyz.efekurbann.synccommands.messaging.impl.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer extends Thread {

    private final String password;
    private final boolean secure;
    private final ServerSocket serverSocket;
    private final SocketImpl socket;

    public SocketServer(SocketImpl socket, boolean secure) throws IOException {
        this.socket = socket;
        this.password = socket.getServer().getPassword();
        this.secure = secure;
        this.serverSocket = new ServerSocket(socket.getServer().getPort());

        setName("SyncCommands Socket Thread");
    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            if (serverSocket.isClosed()) break;
            try (Socket socket = serverSocket.accept()) {
                DataInputStream input = new DataInputStream(socket.getInputStream());

                String targetServer = input.readUTF();
                if (!targetServer.equals("all") && !targetServer.equalsIgnoreCase(this.socket.getServer().getServerName())) return;

                String command = input.readUTF();
                String publisher = input.readUTF();

                if (secure) {
                    String pass = input.readUTF();
                    if (!pass.equals(password)) {
                        this.socket.getLogger().severe("Someone tried to execute command without permission!");
                        this.socket.getLogger().severe("Command: " + command);
                        this.socket.getLogger().severe("Publisher: " + publisher);
                        this.socket.getLogger().severe("IP: " + socket.getInetAddress().getHostAddress());
                        this.socket.getLogger().severe("Hostname: " + socket.getInetAddress().getHostName());
                        this.socket.getLogger().severe("This is a really important warning, do not ignore this!");
                        continue;
                    }
                }

                this.socket.getLogger().info(String.format("Successfully executed command: %s from %s", command, publisher));
                this.socket.getExecutor().execute(command);
            } catch (IOException e) {
                if (e.getMessage().equalsIgnoreCase("socket closed")) break;

                e.printStackTrace();
            }
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

}
