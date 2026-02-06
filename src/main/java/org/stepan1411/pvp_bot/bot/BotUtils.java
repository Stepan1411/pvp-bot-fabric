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
        public int eatingSlot = -1; // Слот с едой которую едим
        public int potionCooldown = 0; // Кулдаун на зелья
        public int buffPotionCooldown = 0; // Кулдаун на баффовые зелья
        public boolean isThrowingPotion = false; // Бросаем зелье - не смотреть на цель
        public int throwingPotionTicks = 0;
        public boolean isMending = false; // Чинимся - не смотреть на цель, убегать
        public int mendingCooldown = 0; // Кулдаун между бросками XP бутылок
        public int xpBottlesThrown = 0; // Сколько бутылок уже бросили
        public int xpBottlesNeeded = 0; // Сколько бутылок нужно бросить
        public java.util.List<Integer> potionsToThrow = new java.util.ArrayList<>(); // Очередь зелий для броска
        public ItemStack savedOffhandItem = ItemStack.EMPTY; // Сохранённый предмет из offhand (тотем) перед блокировкой
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
        if (state.potionCooldown > 0) state.potionCooldown--;
        if (state.buffPotionCooldown > 0) state.buffPotionCooldown--;
        if (state.mendingCooldown > 0) state.mendingCooldown--;
        
        // ПРИОРИТЕТ 1: Авто-ремонт брони (если нужно - отступаем и чинимся)
        if (settings.isAutoMendEnabled()) {
            boolean needsMending = handleAutoMend(bot, state, settings, server);
            if (needsMending) {
                return; // Чинимся - не делаем ничего другого
            }
        }
        
        // Обработка броска зелья - смотрим вниз и бросаем
        if (state.isThrowingPotion) {
            state.throwingPotionTicks++;
            // Принудительно смотрим вниз
            bot.setPitch(90);
            
            if (state.throwingPotionTicks == 2) {
                // Бросаем на 2-й тик
                executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
            }
            if (state.throwingPotionTicks >= 5) {
                // Проверяем есть ли ещё зелья в очереди
                if (!state.potionsToThrow.isEmpty()) {
                    int nextSlot = state.potionsToThrow.remove(0);
                    var inventory = bot.getInventory();
                    
                    // Перемещаем в хотбар если нужно
                    if (nextSlot >= 9) {
                        ItemStack potion = inventory.getStack(nextSlot);
                        ItemStack current = inventory.getStack(8);
                        inventory.setStack(nextSlot, current);
                        inventory.setStack(8, potion);
                        nextSlot = 8;
                    }
                    
                    ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(nextSlot);
                    state.throwingPotionTicks = 0; // Сбрасываем для следующего зелья
                } else {
                    // Очередь пуста - заканчиваем
                    state.isThrowingPotion = false;
                    state.throwingPotionTicks = 0;
                }
            }
            return; // Не делаем ничего другого пока бросаем
        }
        
        // Плавание
        handleSwimming(bot);
        
        // Авто-тотем
        if (settings.isAutoTotemEnabled()) {
            handleAutoTotem(bot);
        }
        
        // Авто-баффы (зелья силы, скорости, огнестойкости) когда в бою
        if (settings.isAutoPotionEnabled() && !state.isEating) {
            handleAutoBuffPotions(bot, state, server);
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
     * НЕ запускается когда бот блокирует щитом
     */
    private static void handleAutoTotem(ServerPlayerEntity bot) {
        BotState state = getState(bot.getName().getString());
        
        // НЕ меняем offhand когда бот блокирует щитом
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
    
    /**
     * Авто-баффы - зелья силы, скорости, огнестойкости
     * Используются когда бот в бою и эффект отсутствует или заканчивается
     * Бросает ВСЕ нужные зелья сразу
     */
    private static void handleAutoBuffPotions(ServerPlayerEntity bot, BotState state, MinecraftServer server) {
        // Проверяем в бою ли бот
        var combatState = BotCombat.getState(bot.getName().getString());
        if (combatState.target == null) return; // Не в бою
        
        if (state.buffPotionCooldown > 0) return; // Кулдаун
        if (state.isThrowingPotion) return; // Уже бросаем
        
        var inventory = bot.getInventory();
        
        // Собираем все нужные зелья в очередь
        java.util.List<Integer> potionsToUse = new java.util.ArrayList<>();
        
        // Проверяем нужны ли баффы (эффект отсутствует или заканчивается < 5 сек)
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
        
        // Если есть зелья для броска - начинаем
        if (!potionsToUse.isEmpty()) {
            // Берём первое зелье
            int firstSlot = potionsToUse.remove(0);
            
            // Перемещаем в хотбар если нужно
            if (firstSlot >= 9) {
                ItemStack potion = inventory.getStack(firstSlot);
                ItemStack current = inventory.getStack(8);
                inventory.setStack(firstSlot, current);
                inventory.setStack(8, potion);
                firstSlot = 8;
            }
            
            ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(firstSlot);
            
            // Сохраняем остальные зелья в очередь
            state.potionsToThrow.clear();
            state.potionsToThrow.addAll(potionsToUse);
            
            // Начинаем бросок
            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.buffPotionCooldown = 100; // Кулдаун после всех баффов (5 сек)
        }
    }
    
    /**
     * Ищет ВЗРЫВНОЕ баффовое зелье по типу (для броска под себя)
     */
    private static int findSplashBuffPotion(net.minecraft.entity.player.PlayerInventory inventory, String effectName) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            // Только взрывные зелья для броска
            if (!(item instanceof SplashPotionItem) && !(item instanceof LingeringPotionItem)) {
                continue;
            }
            
            var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContents == null) continue;
            
            // Проверяем по ID зелья
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
    
    /**
     * Проверяет есть ли эффект с минимальной длительностью
     */
    private static boolean hasEffect(ServerPlayerEntity bot, net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effect, int minDuration) {
        var instance = bot.getStatusEffect(effect);
        if (instance == null) return false;
        return instance.getDuration() > minDuration;
    }
    
    /**
     * Ищет баффовое зелье по типу
     */
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
            
            // Проверяем по ID зелья
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
    
    /**
     * Использует баффовое зелье
     */
    private static boolean useBuffPotion(ServerPlayerEntity bot, BotState state, int slot, MinecraftServer server) {
        var inventory = bot.getInventory();
        ItemStack potionStack = inventory.getStack(slot);
        Item potionItem = potionStack.getItem();
        
        // Перемещаем в хотбар если нужно
        if (slot >= 9) {
            ItemStack current = inventory.getStack(8);
            inventory.setStack(slot, current);
            inventory.setStack(8, potionStack);
            slot = 8;
        }
        
        // Переключаем слот
        ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(slot);
        
        if (potionItem instanceof SplashPotionItem || potionItem instanceof LingeringPotionItem) {
            // Взрывное зелье - начинаем процесс броска под себя
            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.buffPotionCooldown = 15;
            return true;
        } else if (potionItem instanceof PotionItem) {
            // Обычное зелье - пьём
            state.isEating = true;
            state.eatingTicks = 0;
            state.eatingSlot = slot;
            state.buffPotionCooldown = 10;
            bot.setCurrentHand(Hand.MAIN_HAND);
            return true;
        }
        
        return false;
    }
    
    /**
     * Авто-еда с использованием команд Carpet
     */
    private static void handleAutoEat(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        // Проверяем включена ли авто-еда
        if (!settings.isAutoEatEnabled()) {
            // Если авто-еда выключена, останавливаем еду если бот ест
            if (state.isEating) {
                bot.stopUsingItem();
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
     * Учитывает приоритет тотема
     */
    private static void handleAutoShield(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        var inventory = bot.getInventory();
        int shieldSlot = findShield(inventory);
        if (shieldSlot < 0) {
            state.isBlocking = false;
            return;
        }
        
        // Если включен приоритет тотема - не заменяем тотем на щит
        if (settings.isTotemPriority()) {
            ItemStack offhand = inventory.getStack(40);
            if (offhand.getItem() == Items.TOTEM_OF_UNDYING) {
                // Тотем в offhand - не блокируем
                if (state.isBlocking) {
                    stopBlocking(bot, state, server);
                }
                return;
            }
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
            
            // Сохраняем что было в offhand (обычно тотем)
            state.savedOffhandItem = offhand.copy();
            
            // Меняем местами щит и offhand
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
        
        var inventory = bot.getInventory();
        ItemStack currentOffhand = inventory.getStack(40);
        
        // Если в offhand щит и у нас есть сохранённый предмет (тотем)
        if (currentOffhand.getItem() == Items.SHIELD && !state.savedOffhandItem.isEmpty()) {
            // Ищем свободный слот для щита
            int emptySlot = -1;
            for (int i = 0; i < 36; i++) {
                if (inventory.getStack(i).isEmpty()) {
                    emptySlot = i;
                    break;
                }
            }
            
            if (emptySlot >= 0) {
                // Возвращаем щит в инвентарь
                inventory.setStack(emptySlot, currentOffhand.copy());
                // Возвращаем сохранённый предмет (тотем) в offhand
                inventory.setStack(40, state.savedOffhandItem.copy());
            }
            
            // Очищаем сохранённый предмет
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
    
    /**
     * Проверяет есть ли еда в инвентаре
     */
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
    
    /**
     * Пробует использовать зелье исцеления
     * Возвращает true если начал пить/бросил зелье
     */
    public static boolean tryUseHealingPotion(ServerPlayerEntity bot, MinecraftServer server) {
        BotState state = getState(bot.getName().getString());
        if (state.isEating) return false; // Уже что-то делаем
        if (state.potionCooldown > 0) return false; // Кулдаун на зелья
        
        var inventory = bot.getInventory();
        
        // Ищем зелье исцеления (обычное или взрывное)
        int potionSlot = findHealingPotion(inventory);
        if (potionSlot < 0) return false;
        
        ItemStack potionStack = inventory.getStack(potionSlot);
        Item potionItem = potionStack.getItem();
        
        // Перемещаем в хотбар если нужно
        if (potionSlot >= 9) {
            ItemStack current = inventory.getStack(8);
            inventory.setStack(potionSlot, current);
            inventory.setStack(8, potionStack);
            potionSlot = 8;
        }
        
        // Переключаем слот
        ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(potionSlot);
        
        if (potionItem instanceof SplashPotionItem || potionItem instanceof LingeringPotionItem) {
            // Взрывное зелье - начинаем процесс броска под себя
            state.isThrowingPotion = true;
            state.throwingPotionTicks = 0;
            state.potionCooldown = 10;
            return true;
        } else if (potionItem instanceof PotionItem) {
            // Обычное зелье - пьём
            state.isEating = true;
            state.eatingTicks = 0;
            state.eatingSlot = potionSlot;
            state.potionCooldown = 5; // 5 тиков после питья
            bot.setCurrentHand(Hand.MAIN_HAND);
            return true;
        }
        
        return false;
    }
    
    /**
     * Ищет зелье исцеления в инвентаре
     */
    private static int findHealingPotion(net.minecraft.entity.player.PlayerInventory inventory) {
        // Приоритет: взрывное > обычное
        int splashSlot = -1;
        int normalSlot = -1;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            
            // Проверяем что это зелье исцеления
            if (isHealingPotion(stack)) {
                if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
                    if (splashSlot < 0) splashSlot = i;
                } else if (item instanceof PotionItem) {
                    if (normalSlot < 0) normalSlot = i;
                }
            }
        }
        
        // Предпочитаем взрывное (быстрее)
        return splashSlot >= 0 ? splashSlot : normalSlot;
    }
    
    /**
     * Проверяет является ли зелье зельем исцеления
     */
    private static boolean isHealingPotion(ItemStack stack) {
        var potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) return false;
        
        // Проверяем эффекты зелья
        for (var effect : potionContents.getEffects()) {
            var effectType = effect.getEffectType().value();
            String effectName = effectType.toString().toLowerCase();
            if (effectName.contains("healing") || effectName.contains("instant_health")) {
                return true;
            }
        }
        
        // Также проверяем по ID зелья
        var potion = potionContents.potion();
        if (potion.isPresent()) {
            String potionName = potion.get().getIdAsString().toLowerCase();
            if (potionName.contains("healing") || potionName.contains("health")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Авто-ремонт брони с Mending через XP бутылки
     * Проверяет прочность брони и использует XP бутылки когда нужно
     * Возвращает true если бот чинится (нужно отступать)
     */
    private static boolean handleAutoMend(ServerPlayerEntity bot, BotState state, BotSettings settings, MinecraftServer server) {
        var inventory = bot.getInventory();
        
        // Проверяем есть ли XP бутылки
        int xpBottleSlot = findXpBottle(inventory);
        if (xpBottleSlot < 0) {
            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false; // Нет XP бутылок
        }
        
        // Проверяем каждый слот брони и считаем сколько урона нужно починить
        int totalDamageToRepair = 0;
        int itemsNeedingRepair = 0;
        
        for (int armorSlot = 36; armorSlot < 40; armorSlot++) {
            ItemStack armorPiece = inventory.getStack(armorSlot);
            if (armorPiece.isEmpty()) continue;
            
            // Проверяем есть ли Mending на броне
            if (!hasMendingEnchantment(armorPiece)) continue;
            
            // Проверяем прочность
            int maxDamage = armorPiece.getMaxDamage();
            int currentDamage = armorPiece.getDamage();
            double durabilityPercent = 1.0 - ((double) currentDamage / maxDamage);
            
            // Если прочность ниже порога - нужен ремонт
            if (durabilityPercent < settings.getMendDurabilityThreshold()) {
                // Считаем сколько нужно починить до 90%
                int targetDamage = (int) (maxDamage * 0.1); // 90% = 10% урона
                int damageToRepair = currentDamage - targetDamage;
                if (damageToRepair > 0) {
                    totalDamageToRepair += damageToRepair;
                    itemsNeedingRepair++;
                }
            }
        }
        
        if (totalDamageToRepair <= 0) {
            // Вся броня починена!
            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false;
        }
        
        // Если только начинаем чиниться - рассчитываем сколько бутылок нужно ОДИН РАЗ
        if (!state.isMending) {
            // Реальные данные: незеритовый нагрудник 0→95% = ~20 бутылок для ~562 урона
            // Это значит ~28 урона на бутылку
            // Формула: totalDamageToRepair / 28
            state.xpBottlesNeeded = (totalDamageToRepair / 28) + 2; // +2 для запаса
            if (state.xpBottlesNeeded < 5) state.xpBottlesNeeded = 5; // Минимум 5 бутылок
            state.xpBottlesThrown = 0;
        }
        
        // БРОНЯ СЛОМАНА - ОТСТУПАЕМ И ЧИНИМСЯ!
        state.isMending = true;
        
        // Получаем цель из BotCombat
        var combatState = BotCombat.getState(bot.getName().getString());
        Entity target = combatState.target;
        
        // Отступаем от врага если он есть (скорость 1.3 = быстро!)
        if (target != null) {
            BotNavigation.lookAway(bot, target);
            BotNavigation.moveAway(bot, target, 1.3); // 1.3 = быстро с bhop!
        }
        
        // Проверяем бросили ли уже достаточно бутылок
        if (state.xpBottlesThrown >= state.xpBottlesNeeded) {
            // Закончили бросать - выходим из режима починки
            state.isMending = false;
            state.xpBottlesThrown = 0;
            state.xpBottlesNeeded = 0;
            return false;
        }
        
        // Перемещаем XP бутылку в хотбар если нужно
        if (xpBottleSlot >= 9) {
            ItemStack xpBottle = inventory.getStack(xpBottleSlot);
            ItemStack current = inventory.getStack(8);
            inventory.setStack(xpBottleSlot, current);
            inventory.setStack(8, xpBottle);
            xpBottleSlot = 8;
        }
        
        // Переключаем слот на XP бутылку
        ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(xpBottleSlot);
        
        // Смотрим максимально вниз
        bot.setPitch(90);
        
        // КИДАЕМ БУТЫЛКУ КАЖДЫЙ ТИК!
        executeCommand(server, bot, "player " + bot.getName().getString() + " use once");
        
        state.xpBottlesThrown++;
        
        return true; // Возвращаем true - бот чинится
    }
    
    /**
     * Проверяет есть ли зачарование Mending на предмете
     */
    private static boolean hasMendingEnchantment(ItemStack stack) {
        var enchantments = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantments == null) return false;
        
        // Проверяем все зачарования
        for (var entry : enchantments.getEnchantments()) {
            String enchantName = entry.getIdAsString().toLowerCase();
            if (enchantName.contains("mending")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Ищет XP бутылку в инвентаре
     */
    private static int findXpBottle(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getStack(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                return i;
            }
        }
        return -1;
    }
}
