package io.github.efekurbann.synccommands.logging.impl;

import io.github.efekurbann.synccommands.logging.Logger;

public class VelocityLogger implements Logger {

    private final org.slf4j.Logger logger;

    public VelocityLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void severe(String message) {
        logger.error(message);
    }
}
