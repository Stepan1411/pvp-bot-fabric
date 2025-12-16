package org.stepan1411.pvp_bot.bot;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
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
        public int eatingSlot = -1; // Слот с едой которую едим
    }
    
    public static BotState getState(String botName) {
        return botStates.computeIfAbsent(botName, k -> new BotState());
    }
    
    public static void removeState(String botName) {
        botStates.remove(botName);
    }
    
    /**
     * Обновление утилит бота
     */
    public static void update(ServerPlayerEntity bot, MinecraftServer server) {
        BotSettings settings = BotSettings.get();
        BotState state = getState(bot.getName().getString());
        
        // Уменьшаем кулдауны
        if (state.shieldCooldown > 0) state.shieldCooldown--;
        if (state.eatCooldown > 0) state.eatCooldown--;
        if (state.windChargeCooldown > 0) state.windChargeCooldown--;
        
        // Плавание
        handleSwimming(bot);
        
        // Авто-тотем
        if (settings.isAutoTotemEnabled()) {
            handleAutoTotem(bot);
        }
        
        // Авто-еда (приоритет над щитом)
        // Всегда обрабатываем если уже едим, или если не блокируем
        if (settings.isAutoEatEnabled() && (state.isEating || !state.isBlocking)) {
            handleAutoEat(bot, state, settings, server);
        }
        
        // Авто-щит (только если не едим)
        if (settings.isAutoShieldEnabled() && !state.isEating) {
            handleAutoShield(bot, state, settings, server);
        }
    }
    
    /**
     * Плавание - бот плывёт вверх когда в воде
     */
    private static void handleSwimming(ServerPlayerEntity bot) {
        if (bot.isTouchingWater() || bot.isSubmergedInWater()) {
            bot.setSwimming(true);
            
            // Плывём вверх сильнее
            if (bot.isSubmergedInWater()) {
                bot.addVelocity(0, 0.08, 0); // Сильнее плывём вверх
                bot.setSprinting(true); // Спринт в воде = быстрое плавание
            } else if (bot.isTouchingWater()) {
                bot.addVelocity(0, 0.04, 0); // Держимся на поверхности
            }
            
            // Прыгаем если на поверхности воды
            if (bot.isOnGround() && bot.isTouchingWater()) {
                bot.jump();
            }
        }
    }
    
    /**
     * Авто-тотем
     */
    private static void handleAutoTotem(ServerPlayerEntity bot) {
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
    
    /**
     * Авто-еда с использованием команд Carpet
     */
    private static void handleAutoEat(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        int hunger = bot.getHungerManager().getFoodLevel();
        float health = bot.getHealth();
        float maxHealth = bot.getMaxHealth();
        
        boolean needFood = hunger <= settings.getMinHungerToEat();
        boolean needHealth = health <= maxHealth * 0.5f; // Меньше 50% HP
        boolean criticalHealth = health <= maxHealth * 0.3f; // Меньше 30% HP - срочно есть!
        
        // Проверяем отступает ли бот (из BotCombat)
        var combatState = BotCombat.getState(bot.getName().getString());
        boolean isRetreating = combatState.isRetreating;
        
        if (state.isEating) {
            state.eatingTicks++;
            
            // ПРИНУДИТЕЛЬНО держим слот с едой
            if (state.eatingSlot >= 0 && state.eatingSlot < 9) {
                ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) bot.getInventory()).setSelectedSlot(state.eatingSlot);
            }
            
            // Держим ПКМ нажатым напрямую (не через Carpet - это не сбрасывает прогресс)
            ItemStack foodStack = bot.getMainHandStack();
            if (foodStack.getItem().getComponents().get(DataComponentTypes.FOOD) != null) {
                // Используем предмет напрямую каждый тик
                bot.setCurrentHand(Hand.MAIN_HAND);
            }
            
            // Еда занимает ~32 тика, но с учётом задержек ждём 80 тиков (4 сек)
            if (state.eatingTicks >= 80) {
                bot.stopUsingItem();
                state.isEating = false;
                state.eatingTicks = 0;
                state.eatingSlot = -1;
                state.eatCooldown = 10;
                
                // Если всё ещё нужно есть - продолжаем сразу
                hunger = bot.getHungerManager().getFoodLevel();
                health = bot.getHealth();
                if (health <= maxHealth * 0.5f || hunger < 18) {
                    state.eatCooldown = 0;
                }
            }
            return;
        }
        
        // Золотые яблоки можно есть при любом голоде (они дают эффекты)
        // Едим если: критическое HP, или низкое HP (< 50%), или просто голодны
        // Не ждём isRetreating - едим сразу когда нужно лечиться
        boolean shouldEat = criticalHealth || needHealth || needFood;
        
        // Также едим золотое яблоко если HP < 50% даже при полном голоде
        boolean shouldEatGoldenApple = needHealth && hasGoldenApple(bot.getInventory());
        
        if ((shouldEat || shouldEatGoldenApple) && state.eatCooldown <= 0 && !state.isBlocking) {
            int foodSlot = findBestFood(bot.getInventory(), needHealth || criticalHealth);
            if (foodSlot >= 0) {
                var inventory = bot.getInventory();
                
                // Проверяем что это действительно еда
                ItemStack foodStack = inventory.getStack(foodSlot);
                if (!foodStack.isEmpty() && foodStack.getItem().getComponents().get(DataComponentTypes.FOOD) != null) {
                    // Перемещаем еду в хотбар слот 8 (последний)
                    if (foodSlot >= 9) {
                        ItemStack food = inventory.getStack(foodSlot);
                        ItemStack current = inventory.getStack(8);
                        inventory.setStack(foodSlot, current);
                        inventory.setStack(8, food);
                        foodSlot = 8;
                    }
                    
                    state.eatingSlot = foodSlot;
                    
                    // Переключаем слот напрямую
                    ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(foodSlot);
                    
                    // Начинаем есть напрямую
                    bot.setCurrentHand(Hand.MAIN_HAND);
                    state.isEating = true;
                    state.eatingTicks = 0;
                    
                    System.out.println("[PVP_BOT] " + bot.getName().getString() + " starting to eat from slot " + foodSlot + ", item: " + foodStack.getItem().getName().getString());
                }
            }
        }
    }
    
    /**
     * Проверяет есть ли золотое яблоко в инвентаре
     */
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
    
    /**
     * Авто-щит - блокирует только когда враг очень близко и атакует
     */
    private static void handleAutoShield(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        var inventory = bot.getInventory();
        int shieldSlot = findShield(inventory);
        if (shieldSlot < 0) {
            state.isBlocking = false;
            return;
        }
        
        var combatState = BotCombat.getState(bot.getName().getString());
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
        
        // Блокируем если:
        // 1. Враг близко и атакует
        // 2. Отступаем с низким HP
        boolean shouldBlock = false;
        
        if (distance <= 4.0) {
            // Блокируем если враг атакует
            if (target instanceof PlayerEntity player && player.handSwinging) {
                shouldBlock = true;
            }
            // Или если отступаем с низким HP
            if (isRetreating && lowHealth) {
                shouldBlock = true;
            }
        }
        
        // Не блокируем если едим
        if (state.isEating) {
            shouldBlock = false;
        }
        
        if (shouldBlock && !state.isBlocking) {
            startBlocking(bot, state, shieldSlot, server);
            state.shieldCooldown = 30; // Блокируем максимум 1.5 секунды
        } else if (!shouldBlock && state.isBlocking) {
            stopBlocking(bot, state, server);
        }
    }
    
    private static void startBlocking(ServerPlayerEntity bot, BotState state, int shieldSlot, MinecraftServer server) {
        var inventory = bot.getInventory();
        
        if (shieldSlot != 40) {
            ItemStack shield = inventory.getStack(shieldSlot);
            ItemStack offhand = inventory.getStack(40);
            inventory.setStack(shieldSlot, offhand);
            inventory.setStack(40, shield);
        }
        
        // Блокируем через Carpet
        executeCommand(server, bot, "player " + bot.getName().getString() + " use continuous");
        state.isBlocking = true;
    }
    
    private static void stopBlocking(ServerPlayerEntity bot, BotState state, MinecraftServer server) {
        executeCommand(server, bot, "player " + bot.getName().getString() + " stop");
        state.isBlocking = false;
    }
    
    private static int findShield(net.minecraft.entity.player.PlayerInventory inventory) {
        if (inventory.getStack(40).getItem() == Items.SHIELD) return 40;
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.SHIELD) return i;
        }
        return -1;
    }
    
    /**
     * Использовать Wind Charge через команду Carpet
     */
    public static void useWindCharge(ServerPlayerEntity bot, MinecraftServer server) {
        BotState state = getState(bot.getName().getString());
        if (state.windChargeCooldown > 0) return;
        if (state.isEating) return; // Не прерываем еду
        
        var inventory = bot.getInventory();
        int slot = findWindCharge(inventory);
        if (slot < 0) return;
        
        // Перемещаем в хотбар
        if (slot >= 9) {
            ItemStack wc = inventory.getStack(slot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(slot, current);
            inventory.setStack(0, wc);
            slot = 0;
        }
        
        ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(slot);
        
        // Смотрим вниз
        bot.setPitch(90);
        
        // Используем через Carpet
        executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
        
        state.windChargeCooldown = 20; // 1 секунда кулдаун
    }
    
    private static int findWindCharge(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.WIND_CHARGE) return i;
        }
        return -1;
    }
    
    /**
     * Сбить щит топором
     */
    public static boolean tryDisableShield(ServerPlayerEntity bot, Entity target) {
        // Не прерываем еду
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
            ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(0);
        } else {
            ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(axeSlot);
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
    
    /**
     * Выполнить команду Carpet
     */
    private static void executeCommand(MinecraftServer server, ServerPlayerEntity bot, String command) {
        try {
            server.getCommandManager().getDispatcher().execute(command, server.getCommandSource());
        } catch (Exception e) {
            // Игнорируем ошибки
        }
    }
}
