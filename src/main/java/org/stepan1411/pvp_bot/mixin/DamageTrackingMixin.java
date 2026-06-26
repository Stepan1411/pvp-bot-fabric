package org.stepan1411.pvp_bot.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class DamageTrackingMixin {

    private static final String BOT_PLAYER_CLASS = "hero.bane.herobot.bot.BotPlayer";

    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void onDamageTrack(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        if (!Float.isFinite(amount) || amount > 1000 || amount < 0) return;

        // Damage RECEIVED by bot
        if (this.getClass().getName().equals(BOT_PLAYER_CLASS)) {
            org.stepan1411.pvp_bot.stats.StatsReporter.addDamageReceived(amount);
        }

        // Damage DEALT by bot (bot is the attacker)
        Entity attacker = source.getAttacker();
        if (attacker != null && attacker.getClass().getName().equals(BOT_PLAYER_CLASS)) {
            org.stepan1411.pvp_bot.stats.StatsReporter.addDamageDealt(amount);
        }
    }
}
