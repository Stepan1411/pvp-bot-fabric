package org.stepan1411.pvp_bot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.stepan1411.pvp_bot.bot.BotDamageHandler;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotTicker;
import org.stepan1411.pvp_bot.command.BotCommand;
import org.stepan1411.pvp_bot.stats.StatsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pvp_bot implements ModInitializer {

    public static final String MOD_ID = "pvp_bot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("PVP Bot mod loaded!");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BotCommand.register(dispatcher);
        });

        // Инициализация при старте сервера - восстановление ботов
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            BotManager.init(server);
            BotKits.init(server);
            StatsReporter.start(server); // Запускаем отправку статистики
        });
        
        // Сохранение при остановке сервера
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            StatsReporter.stop(); // Останавливаем отправку статистики
            BotManager.reset(server);
        });

        BotTicker.register();
        BotDamageHandler.register();
    }
}
