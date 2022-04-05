package io.github.efekurbann.synccommands.scheduler;

public interface Scheduler {

    void runAsync(Runnable runnable);

    void runSync(Runnable runnable);

}
