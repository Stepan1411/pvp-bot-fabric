package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BotDamageHandler {
    
    public static void register() {
        // Регистрируем обработчик урона через Fabric API
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // Проверяем, является ли это ServerPlayerEntity
            if (entity instanceof ServerPlayerEntity player) {
                String playerName = player.getName().getString();
                
                // Проверяем, является ли этот игрок нашим ботом
                if (BotManager.getAllBots().contains(playerName)) {
                    // Вызываем обработчик боя
                    BotCombat.onBotDamaged(player, source);
                }
            }
            
            // Возвращаем true чтобы урон прошёл
            return true;
        });
    }
}
