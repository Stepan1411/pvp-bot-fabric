package com.example.testaddon;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.stepan1411.pvp_bot.api.PvpBotAPI;
import org.stepan1411.pvp_bot.api.combat.CombatStrategyRegistry;
import org.stepan1411.pvp_bot.api.event.BotEventManager;

/**
 * Test addon to verify all bug fixes work correctly
 * Tests:
 * 1. Combat Strategy Integration (Fix #1)
 * 2. Attack Handler Cancellation (Fix #2)
 * 3. Proper API usage without getServer() (Fix #3)
 */
public class TestAddonWithFixes implements ModInitializer {
    
    private TestCombatStrategy testStrategy;
    
    @Override
    public void onInitialize() {
        System.out.println("[TEST_ADDON] Initializing test addon...");
        
        BotEventManager events = PvpBotAPI.getEventManager();
        
        // Test 1: Combat Strategy Integration
        testStrategy = new TestCombatStrategy();
        CombatStrategyRegistry.getInstance().register(testStrategy);
        System.out.println("[TEST_ADDON] ✓ Registered TestCombatStrategy");
        
        // Test 2: Attack Handler Cancellation
        events.registerAttackHandler((bot, target) -> {
            // Cancel attacks against players whose name starts with "Friend"
            if (target instanceof ServerPlayerEntity player) {
                String targetName = player.getName().getString();
                if (targetName.startsWith("Friend")) {
                    System.out.println("[TEST_ADDON] ✓ Cancelled attack against: " + targetName);
                    bot.sendMessage(Text.literal("§c[TEST] Attack cancelled (friendly target)"));
                    return true; // Cancel attack
                }
            }
            return false; // Allow attack
        });
        System.out.println("[TEST_ADDON] ✓ Registered attack handler");
        
        // Test 3: Tick Handler (for monitoring)
        events.registerTickHandler(bot -> {
            // Every 10 seconds, report strategy call count
            if (bot.age % 200 == 0) {
                int count = testStrategy.getCallCount();
                if (count > 0) {
                    System.out.println("[TEST_ADDON] ✓ Strategy has been called " + count + " times");
                } else {
                    System.out.println("[TEST_ADDON] ⚠ Strategy has NOT been called yet");
                }
            }
        });
        System.out.println("[TEST_ADDON] ✓ Registered tick handler");
        
        // Test 4: Spawn Handler (for logging)
        events.registerSpawnHandler(bot -> {
            System.out.println("[TEST_ADDON] ✓ Bot spawned: " + bot.getName().getString());
            bot.sendMessage(Text.literal("§e[TEST] Test addon is active!"));
        });
        System.out.println("[TEST_ADDON] ✓ Registered spawn handler");
        
        System.out.println("[TEST_ADDON] ========================================");
        System.out.println("[TEST_ADDON] Test addon initialized successfully!");
        System.out.println("[TEST_ADDON] ");
        System.out.println("[TEST_ADDON] To test:");
        System.out.println("[TEST_ADDON] 1. Spawn a bot: /pvpbot spawn TestBot");
        System.out.println("[TEST_ADDON] 2. Give bot a target");
        System.out.println("[TEST_ADDON] 3. Watch console for strategy calls");
        System.out.println("[TEST_ADDON] 4. Spawn player named 'FriendlyPlayer'");
        System.out.println("[TEST_ADDON] 5. Bot should not attack FriendlyPlayer");
        System.out.println("[TEST_ADDON] ========================================");
    }
}
