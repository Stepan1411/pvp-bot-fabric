package ru.stepan1411.pvp_bot_gui.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import ru.stepan1411.pvp_bot_gui.client.network.BotPayloads;
import ru.stepan1411.pvp_bot_gui.client.network.SettingsPayloads;

public class Pvp_bot_guiClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(SettingsPayloads.SettingsRequestPayload.ID, SettingsPayloads.SettingsRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SettingsPayloads.SettingsUpdatePayload.ID, SettingsPayloads.SettingsUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SettingsPayloads.SettingsResponsePayload.ID, SettingsPayloads.SettingsResponsePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(BotPayloads.BotListRequestPayload.ID, BotPayloads.BotListRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BotPayloads.BotListResponsePayload.ID, BotPayloads.BotListResponsePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BotPayloads.BotActionPayload.ID, BotPayloads.BotActionPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(SettingsPayloads.SettingsResponsePayload.ID, (payload, context) -> {
            PvPGuiScreen.onSettingsReceived(payload.settings());
        });

        ClientPlayNetworking.registerGlobalReceiver(BotPayloads.BotListResponsePayload.ID, (payload, context) -> {
            PvPGuiScreen.onBotListReceived(payload.botNames());
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("pvpgui")
                .executes(context -> {
                    MinecraftClient.getInstance().send(PvPGuiScreen::open);
                    return 1;
                })
            );
        });
    }
}
