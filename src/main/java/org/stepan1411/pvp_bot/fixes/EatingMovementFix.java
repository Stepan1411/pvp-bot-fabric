package org.stepan1411.pvp_bot.fixes;

import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.bot.BotUtils;

public class EatingMovementFix {

    public static void apply(ServerPlayerEntity bot) {
        var state = BotUtils.getState(bot.getName().getString());
        if (!state.isEating) return;

        bot.setSprinting(false);

        bot.forwardSpeed *= 0.2f;
        bot.sidewaysSpeed *= 0.2f;

        var vel = bot.getVelocity();
        bot.setVelocity(vel.x * 0.8, vel.y, vel.z * 0.8);
    }
}
