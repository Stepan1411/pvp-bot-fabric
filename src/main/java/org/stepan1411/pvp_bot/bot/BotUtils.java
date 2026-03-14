package org.stepan1411.pvp_bot.bot;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.HashMap;
import java.util.Map;

public class BotUtils {
    
    private static final Map<String, BotState> botStates = new HashMap<>();
    
    public static class BotState {
        public int shieldCooldown = 0;
        public int eatCooldown = 0;
        public boolean isBlocking = false;
        public boolean isEating = false;
        public int eatingTicks = 0;
        public int windChargeCooldown = 0;
        public int eatingSlot = -1;
        public int potionCooldown = 0;
        public int buffPotionCooldown = 0;
        public boolean isThrowingPotion = false;
        public int throwingPotionTicks = 0;
        public boolean isMending = false;
        public int mendingCooldown = 0;
        public int xpBottlesThrown = 0;
        public int xpBottlesNeeded = 0;
        public java.util.List<Integer> potionsToThrow = new java.util.ArrayList<>();
        public ItemStack savedOffhandItem = ItemStack.EMPTY;
        
        public boolean isInCobweb = false;
        public boolean isEscapingCobweb = false;
        public int cobwebEscapeTicks = 0;
        public int cobwebEscapeSlot = -1;
        public net.minecraft.util.math.BlockPos waterPosition = null;
        public boolean needsToCollectWater = false;
    }
    
    public static BotState getState(String botName) {
        return botStates.computeIfAbsent(botName, k -> new BotState());
    }
    
    public static void removeState(String botName) {
        botStates.remove(botName);
    }
    
    
    public static void update(ServerPlayerEntity bot, MinecraftServer server) {
        BotSettings settings = BotSettings.get();
        BotState state = getState(bot.getName().getString());
        

        if (state.shieldCooldown > 0) state.shieldCooldown--;
        if (state.eatCooldown > 0) state.eatCooldown--;
        if (state.windChargeCooldown > 0) state.windChargeCooldown--;
        if (state.potionCooldown > 0) state.potionCooldown--;
        if (state.buffPotionCooldown > 0) state.buffPotionCooldown--;
        if (state.mendingCooldown > 0) state.mendingCooldown--;
        

        if (settings.isAutoMendEnabled()) {
            boolean needsMending = handleAutoMend(bot, state, settings, server);
            if (needsMending) {
                return;
            }
        }
        

        if (state.isThrowingPotion) {
            state.throwingPotionTicks++;

            bot.setPitch(90);
            
            if (state.throwingPotionTicks == 2) {

                executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
            }
            if (state.throwingPotionTicks >= 5) {

                if (!state.potionsToThrow.isEmpty()) {
                    int nextSlot = state.potionsToThrow.remove(0);
                    var inventory = bot.getInventory();
                    

                    if (nextSlot >= 9) {
                        ItemStack potion = inventory.getStack(nextSlot);
                        ItemStack current = inventory.getStack(8);
                        inventory.setStack(nextSlot, current);
                        inventory.setStack(8, potion);
                        nextSlot = 8;
                    }
                    
                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, nextSlot);
                    state.throwingPotionTicks = 0;
                } else {

                    state.isThrowingPotion = false;
                    state.throwingPotionTicks = 0;
                }
            }
            return;
        }
        

        handleSwimming(bot);
        

        if (settings.isAutoTotemEnabled()) {
            handleAutoTotem(bot);
        }
        

        if (settings.isAutoPotionEnabled() && !state.isEating) {
            handleAutoBuffPotions(bot, state, server);
        }
        

        if (settings.isAutoEatEnabled() && (state.isEating || !state.isBlocking)) {
            handleAutoEat(bot, state, settings, server);
        }
        
        handleCobwebEscape(bot, state, server);

        if (settings.isAutoShieldEnabled() && !state.isEating) {
            handleAutoShield(bot, state, settings, server);
        }
    }
    
    
    private static void handleSwimming(ServerPlayerEntity bot) {
        if (bot.isTouchingWater() || bot.isSubmergedInWater()) {
            bot.setSwimming(true);
            

            if (bot.isSubmergedInWater()) {
                bot.addVelocity(0, 0.08, 0);
                bot.setSprinting(true);
            } else if (bot.isTouchingWater()) {
                bot.addVelocity(0, 0.04, 0);
            }
            

            if (bot.isOnGround() && bot.isTouchingWater()) {
                bot.jump();
            }
        }
    }
    
    
    private static void handleAutoTotem(ServerPlayerEntity bot) {
        BotState state = getState(bot.getName().getString());
        

        if (state.isBlocking) {
            return;
        }
        
        var inventory = bot.getInventory();
        ItemStack offhand = inventory.getStack(40);
        
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                inventory.setStack(i, offhand.copy());
                inventory.setStack(40, stack.copy());
                return;
            }
        }
    }
    
    
    private static void handleAutoBuffPotions(ServerPlayerEntity bot, BotState state, MinecraftServer server) {

        var combatState = BotCombat.getState(bot.getName().getString());
        if (combatState.target == null) return;
        
        if (state.buffPotionCooldown > 0) return;
        if (state.isThrowingPotion) return;
        
        var inventory = bot.getInventory();
        

        java.util.List<Integer> potionsToUse = new java.util.ArrayList<>();
        

        boolean needStrength = !hasEffect(bot, StatusEffects.STRENGTH, 100);
        boolean needSpeed = !hasEffect(bot, StatusEffects.SPEED, 100);
        boolean needFireResist = !hasEffect(bot, StatusEffects.FIRE_RESISTANCE, 100);
        
        if (needStrength) {
            int slot = findSplashBuffPotion(inventory, "strength");
            if (slot >= 0) potionsToUse.add(slot);
        }
        
        if (needSpeed) {
            int slot = findSplashBuffPotion(inventory, "swiftness");
            if (slot < 0) slot = findSplashBuffPotion(inventory, "speed");
            if (slot >= 0) potionsToUse.add(slot);
        }
        
        if (needFireResist) {
            int slot = findSplashBuffPotion(inventory, "fire_resistance");
            if (slot >= 0) potionsToUse.add(slot);
        }
        

        if (!potionsToUse.isEmpty()) {

            int firstSlot = potionsToUse.remove(0);
            

            if (firstSlot >= 9) {
                ItemStack potion = inventory.getStack(firstSlot);
                ItemStack current = inventory.getStack(8);
                inventory.setStack(firstSlot, current);
                inventory.setStack(8, potion);
                firstSlot = 8;
            }
            
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, firstSlot);
            

            state.potionsToThrow.clear();
            state.potionsToThrow.addAll(potionsToUse);
            

            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.buffPotionCooldown = 100;
        }
    }
    
    
    private static int findSplashBuffPotion(net.minecraft.entity.player.PlayerInventory inventory, String effectName) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();

            if (!(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
                continue;
            }
            
            var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null) continue;
            

            var potion = potionContents.potion();
            if (potion.isPresent()) {
                String potionName = potion.get().getIdAsString().toLowerCase();
                if (potionName.contains(effectName)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    
    private static boolean hasEffect(ServerPlayerEntity bot, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int minDuration) {
        var instance = bot.getStatusEffect(effect);
        if (instance == null) return false;
        return instance.getDuration() > minDuration;
    }
    
    
    private static int findBuffPotion(net.minecraft.entity.player.PlayerInventory inventory, String effectName) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            if (!(item instanceof PotionItem) && !(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
                continue;
            }
            
            var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null) continue;
            

            var potion = potionContents.potion();
            if (potion.isPresent()) {
                String potionName = potion.get().getIdAsString().toLowerCase();
                if (potionName.contains(effectName)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    
    private static boolean useBuffPotion(ServerPlayerEntity bot, BotState state, int slot, MinecraftServer server) {
        var inventory = bot.getInventory();
        ItemStack potionStack = inventory.getStack(slot);
        Item potionItem = potionStack.getItem();
        

        if (slot >= 9) {
            ItemStack current = inventory.getStack(8);
            inventory.setStack(slot, current);
            inventory.setStack(8, potionStack);
            slot = 8;
        }
        

        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        
        if (potionItem instanceof SplashPotionItem || potionItem instanceof LingeringPotionItem) {

            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.buffPotionCooldown = 15;
            return true;
        } else if (potionItem instanceof PotionItem) {

            state.isEating = true;
            state.eatingTicks = 0;
            state.eatingSlot = slot;
            state.buffPotionCooldown = 10;
            bot.setCurrentHand(Hand.MAIN_HAND);
            return true;
        }
        
        return false;
    }
    
    
    private static void handleAutoEat(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {

        if (!settings.isAutoEatEnabled()) {
            if (state.isEating) {
                executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
                state.isEating = false;
                state.eatingTicks = 0;
                state.eatingSlot = -1;
            }
            return;
        }
        
        int hunger = bot.getHungerManager().getFoodLevel();
        float health = bot.getHealth();
        float maxHealth = bot.getMaxHealth();
        
        boolean needFood = hunger <= settings.getMinHungerToEat();
        boolean needHealth = health <= maxHealth * 0.5f;
        boolean criticalHealth = health <= maxHealth * 0.3f;
        

        var combatState = BotCombat.getState(bot.getName().getString());
        boolean isRetreating = combatState.isRetreating;
        
        if (state.isEating) {
            state.eatingTicks++;
            

            if (state.eatingSlot >= 0 && state.eatingSlot < 9) {
                org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(bot.getInventory(), state.eatingSlot);
            }
            

            ItemStack foodStack = bot.getMainHandStack();
            if (foodStack.getItem().getComponents().get(DataComponentTypes.FOOD) != null) {

                bot.setCurrentHand(Hand.MAIN_HAND);
            }
            

            if (state.eatingTicks >= 80) {
                executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
                state.isEating = false;
                state.eatingTicks = 0;
                state.eatingSlot = -1;
                state.eatCooldown = 10;
                

                hunger = bot.getHungerManager().getFoodLevel();
                health = bot.getHealth();
                if (health <= maxHealth * 0.5f || hunger < 18) {
                    state.eatCooldown = 0;
                }
            }
            return;
        }
        



        boolean shouldEat = criticalHealth || needHealth || needFood;
        

        boolean shouldEatGoldenApple = needHealth && hasGoldenApple(bot.getInventory());
        
        if ((shouldEat || shouldEatGoldenApple) && state.eatCooldown <= 0 && !state.isBlocking) {
            int foodSlot = findBestFood(bot.getInventory(), needHealth || criticalHealth);
            if (foodSlot >= 0) {
                var inventory = bot.getInventory();
                

                ItemStack foodStack = inventory.getStack(foodSlot);
                if (!foodStack.isEmpty() && foodStack.getItem().getComponents().get(DataComponentTypes.FOOD) != null) {

                    if (foodSlot >= 9) {
                        ItemStack food = inventory.getStack(foodSlot);
                        ItemStack current = inventory.getStack(8);
                        inventory.setStack(foodSlot, current);
                        inventory.setStack(8, food);
                        foodSlot = 8;
                    }
                    
                    state.eatingSlot = foodSlot;
                    

                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, foodSlot);
                    

                    executeCommand(server, bot, "player " + bot.getName().getString() + " use continuous");
                    state.isEating = true;
                    state.eatingTicks = 0;
                }
            }
        }
    }
    
    
    private static boolean hasGoldenApple(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            Item item = inventory.getStack(i).getItem();
            if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
                return true;
            }
        }
        return false;
    }

    
    private static int findBestFood(net.minecraft.entity.player.PlayerInventory inventory, boolean preferGoldenApple) {
        int bestSlot = -1;
        int bestValue = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            int value = getFoodValue(stack.getItem(), preferGoldenApple);
            if (value > bestValue) {
                bestValue = value;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
    
    private static int getFoodValue(Item item, boolean preferGoldenApple) {
        if (preferGoldenApple) {
            if (item == Items.ENCHANTED_GOLDEN_APPLE) return 100;
            if (item == Items.GOLDEN_APPLE) return 90;
        }
        if (item == Items.GOLDEN_CARROT) return 80;
        if (item == Items.COOKED_BEEF) return 70;
        if (item == Items.COOKED_PORKCHOP) return 70;
        if (item == Items.COOKED_MUTTON) return 65;
        if (item == Items.COOKED_SALMON) return 60;
        if (item == Items.COOKED_COD) return 55;
        if (item == Items.COOKED_CHICKEN) return 50;
        if (item == Items.BREAD) return 45;
        if (item == Items.BAKED_POTATO) return 45;
        if (item == Items.APPLE) return 30;
        if (item == Items.CARROT) return 25;
        
        var foodComponent = item.getComponents().get(DataComponentTypes.FOOD);
        if (foodComponent != null) {
            return foodComponent.nutrition() * 5;
        }
        return 0;
    }
    
    
    private static void handleAutoShield(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        var inventory = bot.getInventory();
        int shieldSlot = findShield(inventory);
        if (shieldSlot < 0) {
            state.isBlocking = false;
            return;
        }
        

        if (settings.isTotemPriority()) {
            ItemStack offhand = inventory.getStack(40);
            if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {

                if (state.isBlocking) {
                    stopBlocking(bot, state, server);
                }
                return;
            }
        }
        
        var combatState = BotCombat.getState(bot.getName().getString());
        

        if (combatState.isMaceDefending) {
            return;
        }
        
        var target = combatState.target;
        
        if (target == null || state.shieldCooldown > 0) {
            if (state.isBlocking) {
                stopBlocking(bot, state, server);
            }
            return;
        }
        
        double distance = bot.distanceTo(target);
        boolean isRetreating = combatState.isRetreating;
        float health = bot.getHealth();
        float maxHealth = bot.getMaxHealth();
        boolean lowHealth = health <= maxHealth * 0.3f;
        



        boolean shouldBlock = false;
        
        if (distance <= 4.0) {

            if (target instanceof PlayerEntity player && player.handSwinging) {
                shouldBlock = true;
            }

            if (isRetreating && lowHealth) {
                shouldBlock = true;
            }
        }
        

        if (state.isEating) {
            shouldBlock = false;
        }
        
        if (shouldBlock && !state.isBlocking) {
            startBlocking(bot, state, shieldSlot, server);
            state.shieldCooldown = 30;
        } else if (!shouldBlock && state.isBlocking) {
            stopBlocking(bot, state, server);
        }
    }
    
    private static void startBlocking(ServerPlayerEntity bot, BotState state, int shieldSlot, MinecraftServer server) {
        var inventory = bot.getInventory();
        
        if (shieldSlot != 40) {
            ItemStack shield = inventory.getStack(shieldSlot);
            ItemStack offhand = inventory.getStack(40);
            

            state.savedOffhandItem = offhand.copy();
            

            inventory.setStack(shieldSlot, offhand);
            inventory.setStack(40, shield);
        }
        

        executeCommand(server, bot, "player " + bot.getName().getString() + " use continuous");
        state.isBlocking = true;
    }
    
    private static void stopBlocking(ServerPlayerEntity bot, BotState state, MinecraftServer server) {
        executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
        state.isBlocking = false;
        
        var inventory = bot.getInventory();
        ItemStack currentOffhand = inventory.getStack(40);
        

        if (currentOffhand.getItem() == Items.SHIELD && !state.savedOffhandItem.isEmpty()) {

            int emptySlot = -1;
            for (int i = 0; i < 36; i++) {
                if (inventory.getStack(i).isEmpty()) {
                    emptySlot = i;
                    break;
                }
            }
            
            if (emptySlot >= 0) {

                inventory.setStack(emptySlot, currentOffhand.copy());

                inventory.setStack(40, state.savedOffhandItem.copy());
            }
            

            state.savedOffhandItem = ItemStack.EMPTY;
        }
    }
    
    private static int findShield(net.minecraft.entity.player.PlayerInventory inventory) {
        if (inventory.getStack(40).getItem() == Items.SHIELD) return 40;
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.SHIELD) return i;
        }
        return -1;
    }
    
    
    public static void useWindCharge(ServerPlayerEntity bot, MinecraftServer server) {
        BotState state = getState(bot.getName().getString());
        if (state.windChargeCooldown > 0) return;
        if (state.isEating) return;
        
        var inventory = bot.getInventory();
        int slot = findWindCharge(inventory);
        if (slot < 0) return;
        

        if (slot >= 9) {
            ItemStack wc = inventory.getStack(slot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(slot, current);
            inventory.setStack(0, wc);
            slot = 0;
        }
        
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        

        bot.setPitch(90);
        

        executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
        
        state.windChargeCooldown = 20;
    }
    
    private static int findWindCharge(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.WIND_CHARGE) return i;
        }
        return -1;
    }
    
    
    public static boolean tryDisableShield(ServerPlayerEntity bot, Entity target) {

        BotState state = getState(bot.getName().getString());
        if (state.isEating) return false;
        
        if (!(target instanceof PlayerEntity player)) return false;
        if (!player.isBlocking()) return false;
        
        var inventory = bot.getInventory();
        int axeSlot = findAxe(inventory);
        if (axeSlot < 0) return false;
        
        if (axeSlot >= 9) {
            ItemStack axe = inventory.getStack(axeSlot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(axeSlot, current);
            inventory.setStack(0, axe);
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, 0);
        } else {
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, axeSlot);
        }
        return true;
    }
    
    private static int findAxe(net.minecraft.entity.player.PlayerInventory inventory) {
        int[] priorities = {-1, -1, -1, -1, -1, -1};
        for (int i = 0; i < 36; i++) {
            Item item = inventory.getStack(i).getItem();
            if (item == Items.NETHERITE_AXE) priorities[0] = i;
            else if (item == Items.DIAMOND_AXE) priorities[1] = i;
            else if (item == Items.IRON_AXE) priorities[2] = i;
            else if (item == Items.STONE_AXE) priorities[3] = i;
            else if (item == Items.GOLDEN_AXE) priorities[4] = i;
            else if (item == Items.WOODEN_AXE) priorities[5] = i;
        }
        for (int slot : priorities) {
            if (slot >= 0) return slot;
        }
        return -1;
    }
    
    
    private static void executeCommand(MinecraftServer server, ServerPlayerEntity bot, String command) {
        try {
            server.getCommandManager().getDispatcher().execute(command, server.getCommandSource());
        } catch (Exception e) {

        }
    }
    
    
    public static boolean hasFood(ServerPlayerEntity bot) {
        var inventory = bot.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                var foodComponent = stack.getItem().getComponents().get(DataComponentTypes.FOOD);
                if (foodComponent != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public static boolean tryUseHealingPotion(ServerPlayerEntity bot, MinecraftServer server) {
        BotState state = getState(bot.getName().getString());
        if (state.isEating) return false;
        if (state.potionCooldown > 0) return false;
        
        var inventory = bot.getInventory();
        

        int potionSlot = findHealingPotion(inventory);
        if (potionSlot < 0) return false;
        
        ItemStack potionStack = inventory.getStack(potionSlot);
        Item potionItem = potionStack.getItem();
        

        if (potionSlot >= 9) {
            ItemStack current = inventory.getStack(8);
            inventory.setStack(potionSlot, current);
            inventory.setStack(8, potionStack);
            potionSlot = 8;
        }
        

        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, potionSlot);
        
        if (potionItem instanceof SplashPotionItem || potionItem instanceof LingeringPotionItem) {

            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.potionCooldown = 10;
            return true;
        } else if (potionItem instanceof PotionItem) {

            state.isEating = true;
            state.eatingTicks = 0;
            state.eatingSlot = potionSlot;
            state.potionCooldown = 5;
            bot.setCurrentHand(Hand.MAIN_HAND);
            return true;
        }
        
        return false;
    }
    
    
    private static int findHealingPotion(net.minecraft.entity.player.PlayerInventory inventory) {

        int splashSlot = -1;
        int normalSlot = -1;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            

            if (isHealingPotion(stack)) {
                if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
                    if (splashSlot < 0) splashSlot = i;
                } else if (item instanceof PotionItem) {
                    if (normalSlot < 0) normalSlot = i;
                }
            }
        }
        

        return splashSlot >= 0 ? splashSlot : normalSlot;
    }
    
    
    private static boolean isHealingPotion(ItemStack stack) {
        var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) return false;
        

        for (var effect : potionContents.getEffects()) {
            var effectType = effect.getEffectType().value();
            String effectName = effectType.toString().toLowerCase();
            if (effectName.contains("healing") || effectName.contains("instant_health")) {
                return true;
            }
        }
        

        var potion = potionContents.potion();
        if (potion.isPresent()) {
            String potionName = potion.get().getIdAsString().toLowerCase();
            if (potionName.contains("healing") || potionName.contains("health")) {
                return true;
            }
        }
        
        return false;
    }
    
    
    private static boolean handleAutoMend(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        var inventory = bot.getInventory();
        

        int xpBottleSlot = findXpBottle(inventory);
        if (xpBottleSlot < 0) {
            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false;
        }
        

        int totalDamageToRepair = 0;
        int itemsNeedingRepair = 0;
        
        for (int armorSlot = 36; armorSlot < 40; armorSlot++) {
            ItemStack armorPiece = inventory.getStack(armorSlot);
            if (armorPiece.isEmpty()) continue;
            

            if (!hasMendingEnchantment(armorPiece)) continue;
            

            int maxDamage = armorPiece.getMaxDamage();
            int currentDamage = armorPiece.getDamage();
            double durabilityPercent = 1.0 - ((double) currentDamage / maxDamage);
            

            if (durabilityPercent < settings.getMendDurabilityThreshold()) {

                int targetDamage = (int) (maxDamage * 0.1);
                int damageToRepair = currentDamage - targetDamage;
                if (damageToRepair > 0) {
                    totalDamageToRepair += damageToRepair;
                    itemsNeedingRepair++;
                }
            }
        }
        
        if (totalDamageToRepair <= 0) {

            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false;
        }
        

        if (!state.isMending) {



            state.xpBottlesNeeded = (totalDamageToRepair / 28) + 2;
            if (state.xpBottlesNeeded < 5) state.xpBottlesNeeded = 5;
            state.xpBottlesThrown = 0;
        }
        

        state.isMending = true;
        

        var combatState = BotCombat.getState(bot.getName().getString());
        Entity target = combatState.target;
        

        if (target != null) {
            BotNavigation.lookAway(bot, target);
            BotNavigation.moveAway(bot, target, 1.3);
        }
        

        if (state.xpBottlesThrown >= state.xpBottlesNeeded) {

            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false;
        }
        

        if (xpBottleSlot >= 9) {
            ItemStack xpBottle = inventory.getStack(xpBottleSlot);
            ItemStack current = inventory.getStack(8);
            inventory.setStack(xpBottleSlot, current);
            inventory.setStack(8, xpBottle);
            xpBottleSlot = 8;
        }
        

        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, xpBottleSlot);
        

        bot.setPitch(90);
        

        executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
        
        state.xpBottlesThrown++;
        
        return true;
    }
    
    
    private static boolean hasMendingEnchantment(ItemStack stack) {
        var enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null) return false;
        

        for (var entry : enchantments.getEnchantments()) {
            String enchantName = entry.getIdAsString().toLowerCase();
            if (enchantName.contains("mending")) {
                return true;
            }
        }
        return false;
    }
    
    
    private static int findXpBottle(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                return i;
            }
        }
        return -1;
    }
    
    
    public static void handleCobwebEscape(ServerPlayerEntity bot, BotState state, MinecraftServer server) {
        boolean inCobweb = bot.getEntityWorld().getBlockState(bot.getBlockPos()).getBlock() == net.minecraft.block.Blocks.COBWEB;
        
        if (state.needsToCollectWater && state.waterPosition != null) {
            net.minecraft.util.math.Vec3d botPos = new net.minecraft.util.math.Vec3d(bot.getX(), bot.getY(), bot.getZ());
            double distToWater = botPos.distanceTo(net.minecraft.util.math.Vec3d.ofCenter(state.waterPosition));
            
            System.out.println("[COBWEB] Returning for water. Distance: " + distToWater + ", Position: " + state.waterPosition);
            
            if (distToWater < 1.5) {
                net.minecraft.block.BlockState blockStateAtWater = bot.getEntityWorld().getBlockState(state.waterPosition);
                net.minecraft.block.Block blockAtWater = blockStateAtWater.getBlock();
                
                System.out.println("[COBWEB] Close to water! Block: " + blockAtWater + ", isWater: " + blockStateAtWater.isOf(net.minecraft.block.Blocks.WATER));
                
                if (blockStateAtWater.isOf(net.minecraft.block.Blocks.WATER)) {
                    System.out.println("[COBWEB] Collecting water at saved position...");
                    
                    if (state.cobwebEscapeSlot >= 0 && state.cobwebEscapeSlot < 9) {
                        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(bot.getInventory(), state.cobwebEscapeSlot);
                    }
                    
                    bot.setPitch(90.0f);
                    executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
                    
                    state.needsToCollectWater = false;
                    state.waterPosition = null;
                    state.cobwebEscapeSlot = -1;
                    System.out.println("[COBWEB] Water collected successfully!");
                    return;
                } else {
                    System.out.println("[COBWEB] Water not found at position, cancelling...");
                    state.needsToCollectWater = false;
                    state.waterPosition = null;
                    return;
                }
            }
            
            net.minecraft.util.math.Vec3d waterPos = net.minecraft.util.math.Vec3d.ofCenter(state.waterPosition);
            BotNavigation.lookAtPosition(bot, waterPos);
            BotNavigation.moveTowardPosition(bot, waterPos, 0.8);
            return;
        }
        
        if (state.isEscapingCobweb) {
            state.cobwebEscapeTicks++;
            

            bot.setPitch(90.0f);
            bot.setVelocity(0, bot.getVelocity().y, 0);
            

            if (state.cobwebEscapeSlot >= 0 && state.cobwebEscapeSlot < 9) {
                org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(bot.getInventory(), state.cobwebEscapeSlot);
            }
            
            if (state.cobwebEscapeTicks == 5) {

                net.minecraft.util.math.BlockPos waterPos = bot.getBlockPos().down();
                state.waterPosition = waterPos;
                
                System.out.println("[COBWEB] Tick 5 - Collecting water at position: " + waterPos);
                executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
                

                ItemStack currentItem = bot.getInventory().getStack(state.cobwebEscapeSlot);
                if (currentItem.getItem() != Items.WATER_BUCKET) {
                    System.out.println("[COBWEB] Water not collected, will return later. Current item: " + currentItem.getItem());
                    state.needsToCollectWater = true;
                } else {
                    System.out.println("[COBWEB] Water collected successfully!");
                    state.needsToCollectWater = false;
                    state.waterPosition = null;
                }
            }
            
            if (state.cobwebEscapeTicks >= 10) {
                System.out.println("[COBWEB] Tick 10 - Finishing escape process...");
                executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
                state.isEscapingCobweb = false;
                state.cobwebEscapeTicks = 0;
                state.isInCobweb = false;
                

                if (!state.needsToCollectWater) {
                    state.cobwebEscapeSlot = -1;
                }
            }
            return;
        }
        
        state.isInCobweb = inCobweb;
        
        if (!inCobweb) {
            return;
        }
        
        int waterSlot = findWaterBucket(bot.getInventory());
        int pearlSlot = findEnderPearl(bot.getInventory());
        
        if (waterSlot < 0 && pearlSlot < 0) {
            System.out.println("[COBWEB] No water bucket or ender pearl found! Bot can attack normally.");
            return;
        }
        

        if (waterSlot >= 0) {

            if (waterSlot >= 9) {
                ItemStack water = bot.getInventory().getStack(waterSlot);
                ItemStack current = bot.getInventory().getStack(8);
                bot.getInventory().setStack(waterSlot, current);
                bot.getInventory().setStack(8, water);
                waterSlot = 8;
            }
            
            System.out.println("[COBWEB] Starting escape process - placing water...");
            
            state.cobwebEscapeSlot = waterSlot;
            state.isEscapingCobweb = true;
            state.cobwebEscapeTicks = 0;
            state.waterPosition = bot.getBlockPos().down();
            state.needsToCollectWater = false;
            

            executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
            bot.setVelocity(0, bot.getVelocity().y, 0);
            

            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(bot.getInventory(), waterSlot);
            bot.setPitch(90.0f);
            

            System.out.println("[COBWEB] Placing water at position: " + state.waterPosition);
            executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
        } else if (pearlSlot >= 0) {

            if (pearlSlot >= 9) {
                ItemStack pearl = bot.getInventory().getStack(pearlSlot);
                ItemStack current = bot.getInventory().getStack(8);
                bot.getInventory().setStack(pearlSlot, current);
                bot.getInventory().setStack(8, pearl);
                pearlSlot = 8;
            }
            
            System.out.println("[COBWEB] Using ender pearl to escape...");
            

            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(bot.getInventory(), pearlSlot);
            

            bot.setPitch(0.0f);
            
            executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
            

            state.isInCobweb = false;
        }
    }
    
    
    private static int findWaterBucket(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                return i;
            }
        }
        return -1;
    }
    
    private static int findEnderPearl(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }
    
    
    public static boolean canAttack(ServerPlayerEntity bot, BotState state) {

        if (state.isEscapingCobweb || state.needsToCollectWater) {
            return false;
        }
        

        if (state.isInCobweb) {
            int waterSlot = findWaterBucket(bot.getInventory());
            int pearlSlot = findEnderPearl(bot.getInventory());
            
            if (waterSlot < 0 && pearlSlot < 0) {
                System.out.println("[COBWEB] " + bot.getName().getString() + " in cobweb but no water/pearl - can attack");
                return true;
            }
            System.out.println("[COBWEB] " + bot.getName().getString() + " in cobweb with escape items - cannot attack");
            return false;
        }
        
        return true;
    }
}
