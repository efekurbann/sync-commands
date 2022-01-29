package xyz.efekurbann.synccommands.objects;

public class Command {

    private final String command;
    private final Server[] targetServers;
    private final Server publisher;

    public Command(String command, Server publisher, Server... targetServers) {
        this.command = command;
        this.publisher = publisher;

        this.targetServers = targetServers;
    }

    public String getCommand() {
        return command;
    }

    public Server[] getTargetServers() {
        return targetServers;
    }

    public Server getPublisher() {
        return publisher;
    }
}
