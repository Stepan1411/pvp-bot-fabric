package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class BotDamageHandler {
    
    public static void register() {

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {

            if (entity instanceof ServerPlayerEntity player) {
                String playerName = player.getName().getString();
                

                if (BotManager.getAllBots().contains(playerName)) {

                    BotCombat.onBotDamaged(player, source);
                }
            }
            

            return true;
        });
    }
}
