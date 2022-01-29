package xyz.efekurbann.synccommands.objects;

public class Server {

    private final String serverName;
    private final String host;
    private final int port;
    private final String password;
    private final boolean secure;

    public Server(String serverName, String host, int port, String password, boolean secure) {
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.password = password;
        this.secure = secure;
    }

    public String getServerName() {
        return serverName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSecure() {
        return secure;
    }
}
