package io.github.efekurbann.synccommands.util;

import io.github.efekurbann.synccommands.scheduler.Scheduler;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

public class UpdateChecker {

    private static final String RESOURCE_ID = "99596";
    private static final String URL = "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID;

    private final Logger logger;
    private final Scheduler scheduler;
    private final String version;
    private boolean upToDate;

    public UpdateChecker(String version, Logger logger, Scheduler scheduler) {
        this.logger = logger;
        this.scheduler = scheduler;
        this.version = version;
    }

    public void checkUpdates() {
        this.scheduler.runAsync(()-> {
            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL(URL).openConnection();
                InputStreamReader reader = new InputStreamReader(con.getInputStream());
                String latestVersion = (new BufferedReader(reader)).readLine();
                this.upToDate = latestVersion.equals(version);

                if (!this.upToDate) {
                    logger.info("An update was found for SyncCommands!");
                    logger.info("Download from: https://www.spigotmc.org/resources/" + RESOURCE_ID);
                } else
                    logger.info("Plugin is up to date, no update found.");
            } catch (IOException exception) {
                this.logger.info("Could not check for updates: " + exception.getMessage());
            }
        });
    }

    public boolean isUpToDate() {
        return upToDate;
    }
}
