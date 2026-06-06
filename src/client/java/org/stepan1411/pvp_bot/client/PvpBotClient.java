package org.stepan1411.pvp_bot.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PvpBotClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("PVP_BOT_CLIENT");

    @Override
    public void onInitializeClient() {
        LOGGER.info("PVP Bot client initialized");
    }
}
