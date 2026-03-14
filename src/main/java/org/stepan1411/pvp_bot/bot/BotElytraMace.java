package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.stepan1411.pvp_bot.bot.BotSettings;


public class BotElytraMace {
    
    
    private static class ElytraMaceState {
        int step = 0;
        int cooldownTicks = 0;
        int stuckCounter = 0;
        int lastStep = -1;
        

        boolean elytraEquipped = false;
        int takeoffTicks = 0;
        double startY = 0;
        int waitTicks = 0;
        int retryCount = 0;
        

        int elytraSlot = -1;
        int chestplateSlot = -1;
        int fireworkSlot = -1;
        int maceSlot = -1;
    }
    
    private static final java.util.Map<String, ElytraMaceState> states = new java.util.HashMap<>();
    
    
    private static ElytraMaceState getState(String botName) {
        return states.computeIfAbsent(botName, k -> new ElytraMaceState());
    }
    
    
    public static boolean canUseElytraMace(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        if (!settings.isElytraMaceEnabled()) {
            return false;
        }
        
        double distance = bot.distanceTo(target);
        if (distance > 15.0) {
            return false;
        }
        
        PlayerInventory inventory = bot.getInventory();
        boolean hasAllItems = hasElytra(inventory) && hasMace(inventory) && hasFireworks(inventory) && hasChestplate(inventory);
        
        return hasAllItems;
    }
    
    
    public static boolean doElytraMace(ServerPlayerEntity bot, Entity target, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        ElytraMaceState state = getState(bot.getName().getString());
        World world = bot.getEntityWorld();
        double distance = bot.distanceTo(target);
        

        if (state.step == state.lastStep) {
            state.stuckCounter++;
            if (state.stuckCounter > 100) {
                resetState(state);
                return true;
            }
        } else {
            state.stuckCounter = 0;
            state.lastStep = state.step;
        }
        

        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
            return true;
        }
        
        if (state.waitTicks > 0) {
            state.waitTicks--;
            return true;
        }
        

        if (distance > 20.0) {
            resetState(state);
            return true;
        }
        

        switch (state.step) {
            case 0:
                return stepPrepareElytra(bot, target, state, server, world, settings);
            case 1:
                return stepTakeoff(bot, target, state, server, world, settings);
            case 2:
                return stepWaitAltitude(bot, target, state, server, world, settings);
            case 3:
                return stepRemoveElytraAndWait(bot, target, state, server, world, settings);
            case 4:
                return stepGlideToTarget(bot, target, state, server, world, settings);
            case 5:
                return stepMaceAttack(bot, target, state, server, world, settings);
            default:
                resetState(state);
                return true;
        }
    }
    
    
    private static boolean stepPrepareElytra(ServerPlayerEntity bot, Entity target, ElytraMaceState state, 
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        

        state.elytraSlot = findElytra(inventory);
        state.chestplateSlot = findChestplate(inventory);
        state.fireworkSlot = findFireworks(inventory);
        state.maceSlot = findMace(inventory);
        
        if (state.elytraSlot < 0 || state.fireworkSlot < 0 || state.maceSlot < 0) {
            return false;
        }
        

        if (!selectItem(bot, state.elytraSlot)) {
            return true;
        }
        

        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            state.elytraEquipped = true;
            state.step = 1;
            state.cooldownTicks = 5;
            
        } catch (Exception e) {

        }
        
        return true;
    }
    
    
    private static boolean stepTakeoff(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                     net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        

        if (state.takeoffTicks == 0) {
            state.startY = bot.getY();
        }
        
        state.takeoffTicks++;
        

        if (state.takeoffTicks == 1) {
            bot.setPitch(0.0f);
            bot.jump();
        }
        

        if (state.takeoffTicks == 5) {
            bot.jump();
        }
        

        if (state.takeoffTicks == 8) {
            if (!selectItem(bot, state.fireworkSlot)) {
                return true;
            }
            

            int fireworkCount = settings.getElytraMaceFireworkCount();
            try {
                for (int i = 0; i < fireworkCount; i++) {
                    server.getCommandManager().getDispatcher().execute(
                        "player " + bot.getName().getString() + " use once", 
                        server.getCommandSource()
                    );
                }
            } catch (Exception e) {

            }
        }
        

        if (state.takeoffTicks == 12) {
            bot.setPitch(-90.0f);
            state.step = 2;
            state.cooldownTicks = 3;
        }
        
        return true;
    }
    
    
    private static boolean stepWaitAltitude(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                          net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        state.takeoffTicks++;
        double currentHeight = bot.getY() - state.startY;
        int minAltitude = settings.getElytraMaceMinAltitude();
        

        if (currentHeight >= minAltitude) {
            state.step = 3;
            state.cooldownTicks = 0;
            return true;
        }
        

        if (state.takeoffTicks >= 80 || (state.takeoffTicks > 20 && currentHeight < 2.0)) {
            state.retryCount++;
            int maxRetries = settings.getElytraMaceMaxRetries();
            
            if (state.retryCount < maxRetries) {
                state.step = 0;
                state.takeoffTicks = 0;
                state.elytraEquipped = false;
                state.cooldownTicks = 10;
            } else {
                resetState(state);
                return false;
            }
        }
        
        return true;
    }
    
    
    private static boolean stepRemoveElytraAndWait(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                                 net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        

        if (state.chestplateSlot >= 0) {
            if (state.chestplateSlot != 38) {
                if (!selectItem(bot, state.chestplateSlot)) {
                    return true;
                }
            }
            

            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
            } catch (Exception e) {

            }
        }
        
        state.elytraEquipped = false;
        state.waitTicks = 5;
        state.step = 4;
        
        return true;
    }
    
    
    private static boolean stepGlideToTarget(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        double distance = bot.distanceTo(target);
        

        if (!state.elytraEquipped) {
            if (!selectItem(bot, state.elytraSlot)) {
                return true;
            }
            
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
                state.elytraEquipped = true;
            } catch (Exception e) {

            }
            return true;
        }
        

        lookAtEntity(bot, target);
        

        double deltaX = target.getX() - bot.getX();
        double deltaY = target.getY() - bot.getY();
        double deltaZ = target.getZ() - bot.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        

        float targetPitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
        targetPitch = Math.max(30.0f, Math.min(90.0f, targetPitch));
        bot.setPitch(targetPitch);
        

        double attackDistance = settings.getElytraMaceAttackDistance();
        if (distance <= attackDistance) {
            state.step = 5;
            state.cooldownTicks = 0;
        }
        
        return true;
    }
    
    
    private static boolean stepMaceAttack(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                        net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        

        if (state.elytraEquipped && state.chestplateSlot >= 0) {
            if (state.chestplateSlot != 38) {
                if (!selectItem(bot, state.chestplateSlot)) {
                    return true;
                }
            }
            
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
                state.elytraEquipped = false;
            } catch (Exception e) {

            }
            return true;
        }
        

        if (!selectItem(bot, state.maceSlot)) {
            return true;
        }
        

        lookAtEntity(bot, target);
        

        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " attack once", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            bot.swingHand(Hand.MAIN_HAND);
        }
        

        resetState(state);
        state.cooldownTicks = 20;
        
        return true;
    }
    
    
    private static void resetState(ElytraMaceState state) {
        state.step = 0;
        state.elytraEquipped = false;
        state.takeoffTicks = 0;
        state.startY = 0;
        state.waitTicks = 0;
        state.stuckCounter = 0;
        state.lastStep = -1;
        state.retryCount = 0;
        state.elytraSlot = -1;
        state.chestplateSlot = -1;
        state.fireworkSlot = -1;
        state.maceSlot = -1;
    }
    
    
    private static void lookAtEntity(ServerPlayerEntity bot, Entity target) {
        double dx = target.getX() - bot.getX();
        double dy = target.getY() - bot.getY();
        double dz = target.getZ() - bot.getZ();
        
        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (Math.atan2(dy, distance) * 180.0 / Math.PI);
        
        bot.setYaw(yaw);
        bot.setPitch(-pitch);
    }
    
    
    private static boolean selectItem(ServerPlayerEntity bot, int slot) {
        if (slot < 0 || slot >= 36) return false;
        
        PlayerInventory inventory = bot.getInventory();
        

        if (slot >= 9) {
            ItemStack item = inventory.getStack(slot);
            ItemStack current = inventory.getStack(8);
            inventory.setStack(slot, current);
            inventory.setStack(8, item);
            slot = 8;
        }
        
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        return true;
    }
    
    
    private static int findElytra(PlayerInventory inventory) {

        ItemStack equippedChest = inventory.getStack(38);
        if (!equippedChest.isEmpty() && equippedChest.getItem() == Items.ELYTRA) {
            return 38;
        }
        

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }
    
    
    
    private static int findChestplate(PlayerInventory inventory) {

        ItemStack equippedChest = inventory.getStack(38);
        if (!equippedChest.isEmpty()) {
            String itemName = equippedChest.getItem().toString().toLowerCase();
            if (itemName.contains("chestplate")) {
                return 38;
            }
        }
        

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            String itemName = stack.getItem().toString().toLowerCase();
            if (itemName.contains("chestplate")) {
                return i;
            }
        }
        return -1;
    }
    
    
    private static int findFireworks(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }
    
    
    private static int findMace(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.MACE) {
                return i;
            }
        }
        return -1;
    }
    
    
    private static boolean hasElytra(PlayerInventory inventory) {
        return findElytra(inventory) >= 0;
    }
    
    
    private static boolean hasMace(PlayerInventory inventory) {
        return findMace(inventory) >= 0;
    }
    
    
    private static boolean hasFireworks(PlayerInventory inventory) {
        return findFireworks(inventory) >= 0;
    }
    
    
    private static boolean hasChestplate(PlayerInventory inventory) {
        return findChestplate(inventory) >= 0;
    }
    
    
    public static void reset(String botName) {
        ElytraMaceState state = states.get(botName);
        if (state != null) {
            resetState(state);
        }
    }
}