package com.example.testaddon;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.stepan1411.pvp_bot.api.combat.CombatStrategy;
import org.stepan1411.pvp_bot.bot.BotSettings;

/**
 * Test Combat Strategy to verify the fix works
 * This strategy should be called when bot has a target
 */
public class TestCombatStrategy implements CombatStrategy {
    
    private int callCount = 0;
    
    @Override
    public String getName() {
        return "TestCombatStrategy";
    }
    
    @Override
    public int getPriority() {
        return 200; // High priority to execute first
    }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        // Log that canUse was called
        System.out.println("[TEST_STRATEGY] canUse() called for bot: " + bot.getName().getString() + 
                          ", target: " + target.getName().getString());
        
        // Only use this strategy every 5 seconds (100 ticks)
        return bot.age % 100 == 0;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
        callCount++;
        
        // Log that execute was called
        System.out.println("[TEST_STRATEGY] execute() called! Count: " + callCount);
        
        // Send message to bot
        bot.sendMessage(Text.literal("§a[TEST] Strategy executed! Count: " + callCount));
        
        // Don't actually do anything, just test that it's called
        // Return false to allow normal combat to continue
        return false;
    }
    
    @Override
    public int getCooldown() {
        return 20; // 1 second cooldown
    }
    
    public int getCallCount() {
        return callCount;
    }
}
