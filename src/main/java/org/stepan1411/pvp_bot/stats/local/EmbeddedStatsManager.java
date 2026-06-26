package org.stepan1411.pvp_bot.stats.local;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class EmbeddedStatsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("pvp_bot_stats_embedded");
    private static final int LOCAL_PORT = 32221;
    private static LocalStatsStore store;
    private static LocalStatsServer server;
    private static boolean running = false;

    public static boolean isRunning() {
        return running;
    }

    public static void start(Path gameDir) {
        if (running) return;
        try {
            Path staticDir = gameDir.resolve("pvpbotstats");
            if (!Files.exists(staticDir)) {
                extractStaticFiles(staticDir);
            }

            Path dataFile = gameDir.resolve("pvpbot_stats_data.json");
            store = new LocalStatsStore(dataFile);
            if (Files.exists(dataFile)) {
                store.load();
                LOGGER.info("Loaded existing stats from {}", dataFile);
            }

            server = new LocalStatsServer(LOCAL_PORT, store, staticDir);
            running = true;
            LOGGER.info("Local stats server started on port {} (dashboard: http://localhost:{}/)", LOCAL_PORT, LOCAL_PORT);
        } catch (Exception e) {
            LOGGER.error("Failed to start local stats server", e);
        }
    }

    public static void stop() {
        if (!running) return;
        try {
            if (store != null) {
                store.save();
                LOGGER.info("Stats saved");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save stats", e);
        }
        if (server != null) {
            server.stop();
        }
        running = false;
        LOGGER.info("Local stats server stopped");
    }

    public static String getLocalUrl() {
        return "http://localhost:" + LOCAL_PORT;
    }

    private static void extractStaticFiles(Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        Files.createDirectories(targetDir.resolve("css"));
        Files.createDirectories(targetDir.resolve("js"));

        String[] files = {
            "pvpbotstats/index.html",
            "pvpbotstats/favicon.ico",
            "pvpbotstats/logo.png",
            "pvpbotstats/css/style.css",
            "pvpbotstats/js/dashboard.js"
        };

        ClassLoader cl = EmbeddedStatsManager.class.getClassLoader();
        for (String resource : files) {
            Path dest = targetDir.resolve(resource.substring("pvpbotstats/".length()));
            try (InputStream in = cl.getResourceAsStream(resource)) {
                if (in != null) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.debug("Extracted {} to {}", resource, dest);
                } else {
                    LOGGER.warn("Resource not found: {}", resource);
                }
            }
        }

        LOGGER.info("Extracted frontend files to {}", targetDir);
    }
}
