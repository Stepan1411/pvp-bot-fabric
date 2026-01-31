package org.stepan1411.pvp_bot.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class PvpBotClient implements ClientModInitializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PVP_BOT_CLIENT");
    private static final String WARNING_FILE = "no_singleplayer_warning.txt";
    private static boolean warningShown = false;
    
    @Override
    public void onInitializeClient() {
        LOGGER.info("PVP Bot client initializing...");
        
        // Показываем предупреждение когда открывается экран выбора мира
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (warningShown) return;
            
            // Проверяем что это экран выбора синглплеер мира
            if (!(screen instanceof SelectWorldScreen)) return;
            
            LOGGER.info("SelectWorldScreen opened, checking warning...");
            
            // Проверяем файл настройки
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
            Path warningFile = configDir.resolve(WARNING_FILE);
            
            if (Files.exists(warningFile)) {
                LOGGER.info("Warning disabled by user");
                warningShown = true;
                return;
            }
            
            warningShown = true;
            LOGGER.info("Showing warning screen");
            
            // Показываем экран предупреждения
            client.setScreen(new WarningScreen(screen));
        });
        
        LOGGER.info("PVP Bot client initialized");
    }
}
