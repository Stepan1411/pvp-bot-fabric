package org.stepan1411.pvp_bot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.bot.BotDamageHandler;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotNameGenerator;
import org.stepan1411.pvp_bot.bot.BotPath;
import org.stepan1411.pvp_bot.bot.BotTicker;
import org.stepan1411.pvp_bot.command.BotCommand;
import org.stepan1411.pvp_bot.config.WorldConfigHelper;
import org.stepan1411.pvp_bot.network.BotPayloads;
import org.stepan1411.pvp_bot.network.BotSettingsReader;
import org.stepan1411.pvp_bot.network.BotSettingsUpdater;
import org.stepan1411.pvp_bot.network.SettingsPayloads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Pvp_bot implements ModInitializer {

    public static final String MOD_ID = "pvp_bot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("PVP Bot mod loaded!");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BotCommand.register(dispatcher);
        });


        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            WorldConfigHelper.init(server);


            WorldConfigHelper.setOnWorldChangeCallback(() -> {
                BotManager.switchWorld(server);
                BotPath.init();
            });

            BotManager.init(server);
            BotKits.init(server);
            BotPath.init();
        });


        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            BotManager.reset(server);
        });

        BotTicker.register();
        BotDamageHandler.register();

        PayloadTypeRegistry.playC2S().register(SettingsPayloads.SettingsRequestPayload.ID, SettingsPayloads.SettingsRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SettingsPayloads.SettingsResponsePayload.ID, SettingsPayloads.SettingsResponsePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SettingsPayloads.SettingsUpdatePayload.ID, SettingsPayloads.SettingsUpdatePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(BotPayloads.BotListRequestPayload.ID, BotPayloads.BotListRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BotPayloads.BotListResponsePayload.ID, BotPayloads.BotListResponsePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BotPayloads.BotActionPayload.ID, BotPayloads.BotActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SettingsPayloads.SettingsRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var settings = BotSettingsReader.readAll();
                context.responseSender().sendPacket(new SettingsPayloads.SettingsResponsePayload(settings));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SettingsPayloads.SettingsUpdatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                BotSettingsUpdater.update(payload.key(), payload.value());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BotPayloads.BotListRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var names = BotManager.getAllBots();
                context.responseSender().sendPacket(new BotPayloads.BotListResponsePayload(List.copyOf(names)));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(BotPayloads.BotActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity bot = context.server().getPlayerManager().getPlayer(payload.botName());
                if (bot == null) return;
                var server = context.server();
                try {
                    switch (payload.action()) {
                        case "KILL" -> server.getCommandManager().getDispatcher().execute("player " + payload.botName() + " kill", server.getCommandSource());
                        case "HEAL" -> bot.setHealth(20f);
                        case "CLEAR_INVENTORY" -> bot.getInventory().clear();
                        case "GIVE_KIT" -> BotKits.giveKit("default", bot);
                        case "TP_TO_ME" -> {
                            ServerPlayerEntity player = context.player();
                            if (player != null) {
                                server.getCommandManager().getDispatcher().execute("execute as " + payload.botName() + " at @s run tp @s " + player.getName().getString(), server.getCommandSource());
                            }
                        }
                    }
                } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                    LOGGER.warn("Failed to execute bot action: {}", e.getMessage());
                }
            });
        });
    }
}
