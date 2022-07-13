package io.github.efekurbann.synccommands.objects.server;

public class MQServer extends Server {

    private final String username;
    private final String vhost;

    public MQServer(String serverName, String host, int port, String password, boolean secure, String username, String vhost) {
        super(serverName, host, port, password, secure);
        this.username = username;
        this.vhost = vhost;
    }

    public String getVirtualHost() {
        return vhost;
    }

    public String getUsername() {
        return username;
    }
}
