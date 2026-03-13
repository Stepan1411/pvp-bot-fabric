package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.stepan1411.pvp_bot.bot.BotSettings;

/**
 * ElytraMace трюк - использование элитр и булавы для атаки сверху
 */
public class BotElytraMace {
    
    /**
     * Состояние ElytraMace трюка
     */
    private static class ElytraMaceState {
        int step = 0;
        int cooldownTicks = 0;
        int stuckCounter = 0;
        int lastStep = -1;
        
        // Специфичные поля для ElytraMace
        boolean elytraEquipped = false;
        int takeoffTicks = 0; // Счетчик тиков взлета
        double startY = 0; // Начальная высота для отслеживания взлета
        int waitTicks = 0; // Счетчик ожидания
        
        // Слоты предметов
        int elytraSlot = -1;
        int chestplateSlot = -1;
        int fireworkSlot = -1;
        int maceSlot = -1;
    }
    
    private static final java.util.Map<String, ElytraMaceState> states = new java.util.HashMap<>();
    
    /**
     * Получить состояние бота
     */
    private static ElytraMaceState getState(String botName) {
        return states.computeIfAbsent(botName, k -> new ElytraMaceState());
    }
    
    /**
     * Проверить возможность использования ElytraMace
     */
    public static boolean canUseElytraMace(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        if (!settings.isElytraMaceEnabled()) return false; // Исправлено!
        
        double distance = bot.distanceTo(target);
        if (distance > 15.0) return false; // Слишком далеко
        
        PlayerInventory inventory = bot.getInventory();
        boolean hasAllItems = hasElytra(inventory) && hasMace(inventory) && hasFireworks(inventory) && hasChestplate(inventory);
        
        // Добавим отладочную информацию
        if (!hasAllItems) {
            int elytraSlot = findElytra(inventory);
            int chestSlot = findChestplate(inventory);
            String elytraInfo = elytraSlot == 38 ? "equipped" : (elytraSlot >= 0 ? "slot " + elytraSlot : "none");
            String chestInfo = chestSlot == 38 ? "equipped" : (chestSlot >= 0 ? "slot " + chestSlot : "none");
            System.out.println("[ElytraMace] " + bot.getName().getString() + " missing items - Elytra: " + hasElytra(inventory) + " (" + elytraInfo + ")" +
                             ", Mace: " + hasMace(inventory) + ", Fireworks: " + hasFireworks(inventory) + 
                             ", Chestplate: " + hasChestplate(inventory) + " (" + chestInfo + ")");
        } else {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " can use ElytraMace! Distance: " + String.format("%.1f", distance));
        }
        
        return hasAllItems;
    }
    
    /**
     * Выполнить ElytraMace трюк
     */
    public static boolean doElytraMace(ServerPlayerEntity bot, Entity target, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        ElytraMaceState state = getState(bot.getName().getString());
        World world = bot.getEntityWorld();
        double distance = bot.distanceTo(target);
        
        // Отладочная информация в начале каждого цикла
        if (state.step == 0 && state.cooldownTicks == 0) {
            PlayerInventory inventory = bot.getInventory();
            int elytraSlot = findElytra(inventory);
            int chestSlot = findChestplate(inventory);
            String elytraInfo = elytraSlot == 38 ? "equipped" : (elytraSlot >= 0 ? "slot " + elytraSlot : "none");
            String chestInfo = chestSlot == 38 ? "equipped" : (chestSlot >= 0 ? "slot " + chestSlot : "none");
            System.out.println("[ElytraMace] " + bot.getName().getString() + " starting new cycle - Elytra: " + elytraInfo + ", Chestplate: " + chestInfo);
        }
        
        // Проверка на застревание
        if (state.step == state.lastStep) {
            state.stuckCounter++;
            if (state.stuckCounter > 100) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " STUCK on step " + state.step + "! Resetting state.");
                resetState(state);
                return true;
            }
        } else {
            state.stuckCounter = 0;
            state.lastStep = state.step;
        }
        
        // Обработка кулдаунов
        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
            return true;
        }
        
        if (state.waitTicks > 0) {
            state.waitTicks--;
            return true;
        }
        
        // Если цель слишком далеко - сброс
        if (distance > 20.0) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " target too far, resetting");
            resetState(state);
            return true;
        }
        
        // Выполнение шагов
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
    
    /**
     * Шаг 0: Подготовка элитр
     */
    private static boolean stepPrepareElytra(ServerPlayerEntity bot, Entity target, ElytraMaceState state, 
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // Находим все необходимые предметы
        state.elytraSlot = findElytra(inventory);
        state.chestplateSlot = findChestplate(inventory);
        state.fireworkSlot = findFireworks(inventory);
        state.maceSlot = findMace(inventory);
        
        if (state.elytraSlot < 0 || state.fireworkSlot < 0 || state.maceSlot < 0) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " missing required items!");
            return false; // Выход из ElytraMace режима
        }
        
        // Берем элитры в руку
        if (!selectItem(bot, state.elytraSlot)) {
            return true;
        }
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " equipping elytra...");
        
        // Одеваем элитры (ПКМ)
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            state.elytraEquipped = true;
            state.step = 1;
            state.cooldownTicks = 5;
            System.out.println("[ElytraMace] " + bot.getName().getString() + " elytra equipped, moving to step 1");
            
        } catch (Exception e) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " error equipping elytra: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * Шаг 1: Взлет
     */
    private static boolean stepTakeoff(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                     net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // Инициализация счетчика тиков взлета
        if (state.takeoffTicks == 0) {
            state.startY = bot.getY(); // Запоминаем начальную высоту
            System.out.println("[ElytraMace] " + bot.getName().getString() + " starting takeoff sequence...");
        }
        
        state.takeoffTicks++;
        
        // Первый прыжок
        if (state.takeoffTicks == 1) {
            // Поворачиваем взгляд максимально вверх
            bot.setPitch(-90.0f);
            
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " jump", 
                    server.getCommandSource()
                );
                System.out.println("[ElytraMace] " + bot.getName().getString() + " jumping...");
            } catch (Exception e) {
                bot.jump(); // Fallback
                System.out.println("[ElytraMace] " + bot.getName().getString() + " jumping (fallback)...");
            }
        }
        
        // Активируем элитры через команду HeroBot
        if (state.takeoffTicks == 5) {
            try {
                // Попробуем активировать элитры через sneak
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " sneak", 
                    server.getCommandSource()
                );
                System.out.println("[ElytraMace] " + bot.getName().getString() + " activating elytra with sneak...");
            } catch (Exception e) {
                try {
                    // Альтернативный способ - двойной jump
                    server.getCommandManager().getDispatcher().execute(
                        "player " + bot.getName().getString() + " jump", 
                        server.getCommandSource()
                    );
                    System.out.println("[ElytraMace] " + bot.getName().getString() + " activating elytra with jump...");
                } catch (Exception e2) {
                    bot.jump(); // Fallback
                    System.out.println("[ElytraMace] " + bot.getName().getString() + " activating elytra (fallback)...");
                }
            }
        }
        
        // Используем фейерверки для ускорения
        if (state.takeoffTicks >= 10) {
            // Выбираем фейерверки
            if (!selectItem(bot, state.fireworkSlot)) {
                return true;
            }
            
            System.out.println("[ElytraMace] " + bot.getName().getString() + " launching with fireworks...");
            
            // Запускаем фейерверки (ПКМ) несколько раз для гарантии взлета
            try {
                for (int i = 0; i < 3; i++) {
                    server.getCommandManager().getDispatcher().execute(
                        "player " + bot.getName().getString() + " use once", 
                        server.getCommandSource()
                    );
                }
                
                // Принудительно задаем скорость вверх
                bot.setVelocity(bot.getVelocity().x, 2.0, bot.getVelocity().z);
                
                state.step = 2;
                state.cooldownTicks = 3;
                System.out.println("[ElytraMace] " + bot.getName().getString() + " fireworks launched, moving to step 2");
                
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error launching fireworks: " + e.getMessage());
            }
        }
        
        return true;
    }
    
    /**
     * Шаг 2: Ждем взлета на 10-20 блоков
     */
    private static boolean stepWaitAltitude(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                          net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        state.takeoffTicks++;
        double currentHeight = bot.getY() - state.startY;
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " altitude: " + String.format("%.1f", currentHeight) + " blocks (tick " + state.takeoffTicks + ")");
        
        // Проверяем высоту или таймаут
        if (currentHeight >= 10.0 || state.takeoffTicks >= 60) { // 60 тиков = 3 секунды максимум
            System.out.println("[ElytraMace] " + bot.getName().getString() + " reached altitude " + String.format("%.1f", currentHeight) + ", moving to step 3");
            state.step = 3;
            state.cooldownTicks = 0;
        }
        
        return true;
    }
    
    /**
     * Шаг 3: Снимаем элитры и ждем
     */
    private static boolean stepRemoveElytraAndWait(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                                 net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // Выбираем нагрудник
        if (state.chestplateSlot >= 0) {
            // Если нагрудник уже надет (слот 38), не нужно его выбирать
            if (state.chestplateSlot != 38) {
                if (!selectItem(bot, state.chestplateSlot)) {
                    return true;
                }
            }
            
            System.out.println("[ElytraMace] " + bot.getName().getString() + " equipping chestplate...");
            
            // Одеваем нагрудник (ПКМ) - это снимет элитры
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error equipping chestplate: " + e.getMessage());
            }
        } else {
            // Если нагрудника нет, просто снимаем элитры
            System.out.println("[ElytraMace] " + bot.getName().getString() + " no chestplate, removing elytra manually...");
            // Можно попробовать снять элитры другим способом, но пока пропустим
        }
        
        state.elytraEquipped = false;
        state.waitTicks = 5; // Ждем 5 тиков
        state.step = 4;
        System.out.println("[ElytraMace] " + bot.getName().getString() + " elytra removed, waiting 5 ticks");
        
        return true;
    }
    
    /**
     * Шаг 4: Планирование к цели
     */
    private static boolean stepGlideToTarget(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        double distance = bot.distanceTo(target);
        
        // Поворачиваем бота прямо вниз
        bot.setPitch(90.0f);
        
        // Одеваем элитры обратно
        if (!state.elytraEquipped) {
            if (!selectItem(bot, state.elytraSlot)) {
                return true;
            }
            
            System.out.println("[ElytraMace] " + bot.getName().getString() + " re-equipping elytra for glide...");
            
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
                state.elytraEquipped = true;
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error re-equipping elytra: " + e.getMessage());
            }
            return true;
        }
        
        // Смотрим на цель для планирования
        lookAtEntity(bot, target);
        
        System.out.println("[ElytraMace] " + bot.getName().getString() + " gliding to target, distance: " + String.format("%.1f", distance));
        
        // Проверяем дистанцию до цели
        if (distance <= 7.0) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " close to target (" + String.format("%.1f", distance) + "), moving to step 5");
            state.step = 5;
            state.cooldownTicks = 0;
        }
        
        return true;
    }
    
    /**
     * Шаг 5: Атака булавой
     */
    private static boolean stepMaceAttack(ServerPlayerEntity bot, Entity target, ElytraMaceState state,
                                        net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // Снимаем элитры (одеваем нагрудник)
        if (state.elytraEquipped && state.chestplateSlot >= 0) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " removing elytra, chestplate slot: " + state.chestplateSlot);
            
            // Если нагрудник уже надет (слот 38), не нужно его выбирать
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
                
                // Проверяем где теперь элитры
                int elytraSlot = findElytra(inventory);
                String elytraInfo = elytraSlot == 38 ? "still equipped" : (elytraSlot >= 0 ? "moved to slot " + elytraSlot : "disappeared");
                System.out.println("[ElytraMace] " + bot.getName().getString() + " elytra removed for mace attack, elytra now: " + elytraInfo);
            } catch (Exception e) {
                System.out.println("[ElytraMace] " + bot.getName().getString() + " error removing elytra: " + e.getMessage());
            }
            return true;
        }
        
        // Выбираем булаву
        if (!selectItem(bot, state.maceSlot)) {
            return true;
        }
        
        // Смотрим на цель
        lookAtEntity(bot, target);
        
        // Спамим атаку булавой
        System.out.println("[ElytraMace] " + bot.getName().getString() + " attacking with mace!");
        
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " attack once", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            System.out.println("[ElytraMace] " + bot.getName().getString() + " error attacking with mace: " + e.getMessage());
            bot.swingHand(Hand.MAIN_HAND);
        }
        
        // Сброс состояния для повтора цикла
        resetState(state);
        state.cooldownTicks = 20; // Кулдаун перед следующим циклом
        
        return true;
    }
    
    /**
     * Сброс состояния
     */
    private static void resetState(ElytraMaceState state) {
        state.step = 0;
        state.elytraEquipped = false;
        state.takeoffTicks = 0;
        state.startY = 0;
        state.waitTicks = 0;
        state.stuckCounter = 0;
        state.lastStep = -1;
        state.elytraSlot = -1;
        state.chestplateSlot = -1;
        state.fireworkSlot = -1;
        state.maceSlot = -1;
    }
    
    /**
     * Смотреть на сущность
     */
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
    
    /**
     * Выбрать предмет в слоте
     */
    private static boolean selectItem(ServerPlayerEntity bot, int slot) {
        if (slot < 0 || slot >= 36) return false;
        
        PlayerInventory inventory = bot.getInventory();
        
        // Если предмет не в хотбаре, перемещаем его
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
    
    /**
     * Найти элитры в инвентаре или проверить что они уже надеты
     */
    private static int findElytra(PlayerInventory inventory) {
        // Сначала проверяем, надеты ли уже элитры (слот 38)
        ItemStack equippedChest = inventory.getStack(38);
        if (!equippedChest.isEmpty() && equippedChest.getItem() == Items.ELYTRA) {
            return 38; // Возвращаем слот брони
        }
        
        // Если не надеты, ищем в обычном инвентаре
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.ELYTRA) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Найти нагрудник в инвентаре
     */
    /**
     * Найти нагрудник в инвентаре или проверить что он уже надет
     */
    private static int findChestplate(PlayerInventory inventory) {
        // Сначала проверяем, надет ли уже нагрудник (слот 38)
        ItemStack equippedChest = inventory.getStack(38);
        if (!equippedChest.isEmpty()) {
            String itemName = equippedChest.getItem().toString().toLowerCase();
            if (itemName.contains("chestplate")) {
                return 38; // Возвращаем слот брони
            }
        }
        
        // Если не надет, ищем в обычном инвентаре
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            String itemName = stack.getItem().toString().toLowerCase();
            if (itemName.contains("chestplate")) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Найти фейерверки в инвентаре
     */
    private static int findFireworks(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Найти булаву в инвентаре
     */
    private static int findMace(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.MACE) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Проверить наличие элитр
     */
    private static boolean hasElytra(PlayerInventory inventory) {
        return findElytra(inventory) >= 0;
    }
    
    /**
     * Проверить наличие булавы
     */
    private static boolean hasMace(PlayerInventory inventory) {
        return findMace(inventory) >= 0;
    }
    
    /**
     * Проверить наличие фейерверков
     */
    private static boolean hasFireworks(PlayerInventory inventory) {
        return findFireworks(inventory) >= 0;
    }
    
    /**
     * Проверить наличие нагрудника
     */
    private static boolean hasChestplate(PlayerInventory inventory) {
        return findChestplate(inventory) >= 0;
    }
    
    /**
     * Сброс состояния бота
     */
    public static void reset(String botName) {
        ElytraMaceState state = states.get(botName);
        if (state != null) {
            resetState(state);
        }
    }
}