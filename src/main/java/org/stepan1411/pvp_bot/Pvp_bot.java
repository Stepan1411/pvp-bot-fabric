package org.stepan1411.pvp_bot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stepan1411.pvp_bot.bot.BotCombat;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotPath;
import org.stepan1411.pvp_bot.bot.BotSettings;
import org.stepan1411.pvp_bot.bot.BotTicker;
import org.stepan1411.pvp_bot.command.BotCommand;
import org.stepan1411.pvp_bot.config.WorldConfigHelper;
import org.stepan1411.pvp_bot.network.BotPayloads;
import org.stepan1411.pvp_bot.network.BotSettingsReader;
import org.stepan1411.pvp_bot.network.BotSettingsUpdater;
import org.stepan1411.pvp_bot.network.SettingsPayloads;
import org.stepan1411.pvp_bot.stats.StatsCollector;
import org.stepan1411.pvp_bot.stats.StatsConfig;
import org.stepan1411.pvp_bot.stats.StatsSender;

import java.util.ArrayList;
import java.util.List;

public class Pvp_bot implements ModInitializer {
    public static final String MOD_ID = "pvp_bot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing PvP Bot");

        registerPayloads();
        registerCommands();
        registerServerEvents();
        registerTickEvents();

        LOGGER.info("PvP Bot initialized");
    }

    private void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(SettingsPayloads.SettingsRequestPayload.ID, SettingsPayloads.SettingsRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SettingsPayloads.SettingsUpdatePayload.ID, SettingsPayloads.SettingsUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BotPayloads.BotListRequestPayload.ID, BotPayloads.BotListRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BotPayloads.BotActionPayload.ID, BotPayloads.BotActionPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(SettingsPayloads.SettingsResponsePayload.ID, SettingsPayloads.SettingsResponsePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BotPayloads.BotListResponsePayload.ID, BotPayloads.BotListResponsePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SettingsPayloads.SettingsRequestPayload.ID, (payload, context) -> {
            var player = context.player();
            var response = new SettingsPayloads.SettingsResponsePayload(BotSettingsReader.readAll());
            ServerPlayNetworking.send(player, response);
        });

        ServerPlayNetworking.registerGlobalReceiver(SettingsPayloads.SettingsUpdatePayload.ID, (payload, context) -> {
            BotSettingsUpdater.update(payload.key(), payload.value());
        });

        ServerPlayNetworking.registerGlobalReceiver(BotPayloads.BotListRequestPayload.ID, (payload, context) -> {
            var player = context.player();
            var bots = new ArrayList<>(BotManager.getAllBots());
            var response = new BotPayloads.BotListResponsePayload(bots);
            ServerPlayNetworking.send(player, response);
        });

        ServerPlayNetworking.registerGlobalReceiver(BotPayloads.BotActionPayload.ID, (payload, context) -> {
            var server = context.server();
            var source = server.getCommandSource();
            var dispatcher = server.getCommandManager().getDispatcher();
            String botName = payload.botName();
            String action = payload.action();
            try {
                switch (action) {
                    case "remove" -> dispatcher.execute("pvpbot remove " + botName, source);
                    case "attack" -> {
                        String target = payload.botName();
                        dispatcher.execute("pvpbot bot-management attack " + botName + " " + target, source);
                    }
                    default -> context.player().sendMessage(Text.literal("Unknown action: " + action));
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to execute bot action '{}' for '{}': {}", action, botName, e.getMessage());
            }
        });
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BotCommand.register(dispatcher);
        });
    }

    private void registerServerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WorldConfigHelper.init(server);
            BotKits.init(server);
            BotPath.init();
            BotManager.init(server);
            StatsConfig.load();
            StatsCollector.load();
            StatsSender.getInstance().start(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            StatsSender.getInstance().stop();
            StatsCollector.save();
            BotManager.updateBotData(server);
            BotManager.saveBots();
            BotManager.reset(server);
        });
    }

    private void registerTickEvents() {
        BotTicker.register();
    }
}
