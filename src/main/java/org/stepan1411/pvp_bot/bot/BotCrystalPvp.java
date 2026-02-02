package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Отдельный класс для Crystal PVP
 * Полностью контролирует движение и бой бота
 */
public class BotCrystalPvp {
    
    // Состояние Crystal PVP для каждого бота
    private static class CrystalState {
        int step = 0;                    // Текущий шаг (0=обсидиан, 1=кристалл, 2=удар)
        BlockPos lastObsidianPos = null; // Позиция последнего обсидиана
        long lastActionTime = 0;         // Время последнего действия
        int cooldownTicks = 0;           // Кулдаун между действиями
        int stuckCounter = 0;            // Счётчик застревания на одном шаге
        int lastStep = -1;               // Последний выполненный шаг
        int crystalNotFoundCounter = 0;  // Счётчик "кристалл не найден"
        int crystalPlaceFailCounter = 0; // Счётчик неудачных попыток поставить кристалл
        java.util.Set<BlockPos> triedPositions = new java.util.HashSet<>(); // Попробованные позиции для обсидиана
        int obsidianPlaceAttempts = 0;   // Счётчик попыток поставить обсидиан на текущей позиции
    }
    
    private static final java.util.Map<String, CrystalState> states = new java.util.HashMap<>();
    
    /**
     * Получить состояние бота
     */
    private static CrystalState getState(String botName) {
        return states.computeIfAbsent(botName, k -> new CrystalState());
    }
    
    /**
     * Проверить может ли бот использовать Crystal PVP
     */
    public static boolean canUseCrystalPvp(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        if (!settings.isCrystalPvpEnabled()) return false;
        
        double distance = bot.distanceTo(target);
        if (distance < 2.5 || distance > 8.0) return false;
        
        PlayerInventory inventory = bot.getInventory();
        return hasObsidian(inventory) && hasEndCrystal(inventory);
    }
    
    /**
     * Главный метод - выполняет Crystal PVP
     * Возвращает true если бот занят Crystal PVP (не нужен обычный combat)
     */
    public static boolean doCrystalPvp(ServerPlayerEntity bot, Entity target, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        CrystalState state = getState(bot.getName().getString());
        World world = bot.getEntityWorld();
        double distance = bot.distanceTo(target);
        
        // Проверка на stuck: если бот долго на одном шаге - сбрасываем
        if (state.step == state.lastStep) {
            state.stuckCounter++;
            if (state.stuckCounter > 100) { // 5 секунд (100 тиков)
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK on step " + state.step + "! Resetting state.");
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 0;
                state.stuckCounter = 0;
                return true;
            }
        } else {
            state.stuckCounter = 0;
            state.lastStep = state.step;
        }
        
        
        // Кулдаун между действиями
        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
            // Во время кулдауна - держим дистанцию
            maintainDistance(bot, target, settings);
            return true;
        }
        
        // Если враг слишком далеко - подходим
        if (distance > 8.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            state.step = 0;
            state.lastObsidianPos = null;
            return true;
        }
        
        // Если враг слишком близко - отходим
        if (distance < 2.5) {
            moveAway(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // ВАЖНО: Если есть кристалл рядом - сразу переходим к атаке
        // НО только если нет кулдауна (чтобы не прерывать установку)
        Entity nearCrystal = findNearestEndCrystal(bot, target, 6.0);
        if (nearCrystal != null && state.step != 2 && state.cooldownTicks == 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " обнаружил кристалл, переход к шагу 2 (текущий step: " + state.step + ")");
            state.step = 2;
            state.cooldownTicks = 0; // Сразу атакуем
        }
        
        // Выполняем текущий шаг
        switch (state.step) {
            case 0: // Шаг 1: Ставим обсидиан
                return stepPlaceObsidian(bot, target, state, server, world, settings);
                
            case 1: // Шаг 2: Ставим кристалл
                return stepPlaceCrystal(bot, target, state, server, world, settings);
                
            case 2: // Шаг 3: Бьём кристалл
                return stepAttackCrystal(bot, target, state, server, world, settings, distance);
                
            default:
                state.step = 0;
                return true;
        }
    }
    
    /**
     * Шаг 0: Установка обсидиана
     */
    private static boolean stepPlaceObsidian(ServerPlayerEntity bot, Entity target, CrystalState state, 
                                            net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // СНАЧАЛА проверяем есть ли уже обсидиан рядом с врагом (в радиусе 5 blocks)
        BlockPos existingObsidian = findExistingObsidian(bot, target, world, 5.0);
        if (existingObsidian != null) {
            double distToExisting = Math.sqrt(bot.squaredDistanceTo(existingObsidian.getX() + 0.5, existingObsidian.getY() + 0.5, existingObsidian.getZ() + 0.5));
            
            if (distToExisting <= 4.0) {
                // Обсидиан есть и бот может до него достать - используем его!
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " using existing obsidian at " + existingObsidian);
                state.lastObsidianPos = existingObsidian;
                state.step = 1; // Сразу к установке кристалла
                state.cooldownTicks = 0; // Без кулдауна
                state.stuckCounter = 0; // Сбрасываем счётчик застревания
                return true;
            } else {
                // Обсидиан есть но далеко - подходим ближе
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " approaching existing obsidian, distance: " + String.format("%.2f", distToExisting));
                moveToward(bot, target, settings.getMoveSpeed());
                return true;
            }
        }
        
        // Обсидиана рядом нет - ставим новый
        
        // Проверяем наличие обсидиана
        int obsidianSlot = findObsidian(inventory);
        if (obsidianSlot < 0) {
            return false; // Нет обсидиана - выходим из Crystal PVP
        }
        
        // Находим позицию для обсидиана
        BlockPos obsidianPos = findBestObsidianPosition(bot, target, world, state.triedPositions);
        if (obsidianPos == null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " could not find obsidian position!");
            // Нет места - сбрасываем попробованные позиции и пробуем снова
            if (!state.triedPositions.isEmpty()) {
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " clearing tried positions (" + state.triedPositions.size() + " positions)");
                state.triedPositions.clear();
            }
            // Держим дистанцию
            maintainDistance(bot, target, settings);
            return true;
        }
        
        // Проверяем дистанцию до позиции
        double distToPos = Math.sqrt(bot.squaredDistanceTo(obsidianPos.getX() + 0.5, obsidianPos.getY() + 0.5, obsidianPos.getZ() + 0.5));
        
        if (distToPos > 3.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // Останавливаем бота
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // Переключаемся на обсидиан
        if (!selectItem(bot, obsidianSlot)) {
            return true; // Не удалось переключить - попробуем в следующем тике
        }
        
        // Смотрим на позицию
        lookAt(bot, obsidianPos);
        
        // Увеличиваем счётчик попыток для этой позиции
        state.obsidianPlaceAttempts++;
        
        // Если 3 попытки на одной позиции не удались - пробуем другую
        if (state.obsidianPlaceAttempts >= 3) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " failed to place obsidian 3 times at " + obsidianPos + ", trying different position");
            state.triedPositions.add(obsidianPos);
            state.obsidianPlaceAttempts = 0;
            state.cooldownTicks = 3;
            return true;
        }
        
        // Ставим блок через Carpet команду
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " placed obsidian at " + obsidianPos + " (attempt " + state.obsidianPlaceAttempts + "/3)");
            
            // Успешно поставили - переходим к следующему шагу
            state.lastObsidianPos = obsidianPos;
            state.step = 1;
            state.cooldownTicks = 5; // Кулдаун чтобы блок успел установиться
            state.stuckCounter = 0; // Сбрасываем счётчик застревания
            state.obsidianPlaceAttempts = 0; // Сбрасываем счётчик попыток
            
        } catch (Exception e) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " error placing obsidian: " + e.getMessage());
            // Ошибка - попробуем снова
        }
        
        // НЕ поворачиваем голову обратно - оставляем смотреть на обсидиан
        return true;
    }
    
    /**
     * Шаг 1: Установка кристалла
     */
    private static boolean stepPlaceCrystal(ServerPlayerEntity bot, Entity target, CrystalState state,
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // Проверяем что обсидиан установлен
        if (state.lastObsidianPos == null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " no obsidian position!");
            state.step = 0;
            return true;
        }
        
        // ВАЖНО: Проверяем что на позиции действительно обсидиан
        net.minecraft.block.BlockState blockAtPos = world.getBlockState(state.lastObsidianPos);
        
        if (!blockAtPos.getBlock().toString().contains("obsidian")) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " NO obsidian at position! Returning to step 0");
            // Добавляем эту позицию в список неудачных
            if (state.lastObsidianPos != null) {
                state.triedPositions.add(state.lastObsidianPos);
            }
            state.step = 0;
            state.lastObsidianPos = null;
            state.obsidianPlaceAttempts = 0;
            return true;
        }
        
        // Проверяем что над обсидианом свободно
        net.minecraft.block.BlockState blockAbove = world.getBlockState(state.lastObsidianPos.up());
        
        if (!blockAbove.isAir() && !blockAbove.isReplaceable()) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " NO space above obsidian! Returning to step 0");
            state.step = 0;
            state.lastObsidianPos = null;
            return true;
        }
        
        // Проверяем наличие кристалла
        int crystalSlot = findEndCrystal(inventory);
        if (crystalSlot < 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " no crystals in inventory! Exiting Crystal PVP.");
            state.step = 0;
            state.lastObsidianPos = null;
            state.stuckCounter = 0;
            return false; // Нет кристалла - выходим из Crystal PVP
        }
        
        // Останавливаем бота
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // Переключаемся на кристалл
        if (!selectItem(bot, crystalSlot)) {
            return true; // Не удалось переключить - попробуем в следующем тике
        }
        
        // Смотрим на САМ ОБСИДИАН (не на воздух над ним!)
        // Кристалл можно поставить тыкнув по ЛЮБОЙ грани обсидиана
        lookAt(bot, state.lastObsidianPos);
        
        // ВАЖНО: Проверяем счётчик неудачных попыток ПЕРЕД установкой
        if (state.crystalPlaceFailCounter >= 5) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK - crystal placement failed 5 times! Resetting state.");
            state.step = 0;
            state.lastObsidianPos = null;
            state.cooldownTicks = 5;
            state.crystalPlaceFailCounter = 0;
            return true;
        }
        
        // Ставим кристалл через Carpet команду
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " placed crystal (clicked obsidian at " + state.lastObsidianPos + ") - attempt " + (state.crystalPlaceFailCounter + 1) + "/5");
            
            // Увеличиваем счётчик неудачных попыток (сбросится если кристалл появится)
            state.crystalPlaceFailCounter++;
            
            // Успешно выполнили команду - переходим к атаке
            state.step = 2;
            state.cooldownTicks = 5; // Кулдаун чтобы кристалл успел появиться
            state.stuckCounter = 0; // Сбрасываем счётчик застревания
            
        } catch (Exception e) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " error placing crystal: " + e.getMessage());
            // Ошибка - возвращаемся к обсидиану
            state.step = 0;
            state.lastObsidianPos = null;
            state.crystalPlaceFailCounter = 0;
        }
        
        // НЕ поворачиваем голову обратно - оставляем смотреть на кристалл
        return true;
    }
    
    /**
     * Шаг 2: Атака кристалла
     */
    private static boolean stepAttackCrystal(ServerPlayerEntity bot, Entity target, CrystalState state,
                                            net.minecraft.server.MinecraftServer server, World world, 
                                            BotSettings settings, double distance) {
        PlayerInventory inventory = bot.getInventory();
        
        // Останавливаем бота во время атаки
        bot.setVelocity(0, bot.getVelocity().y, 0);
        
        // Переключаемся на оружие
        int weaponSlot = findMeleeWeapon(inventory);
        if (weaponSlot >= 0) {
            selectItem(bot, weaponSlot);
        }
        
        // Ищем кристалл
        Entity crystal = findNearestEndCrystal(bot, target, 6.0);
        
        if (crystal != null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " found crystal at distance " + bot.distanceTo(crystal));
            
            // Смотрим на кристалл
            lookAtEntity(bot, crystal);
            
            // Сбрасываем счётчик "кристалл не найден"
            state.crystalNotFoundCounter = 0;
            // Сбрасываем счётчик неудачных попыток установки (кристалл появился!)
            state.crystalPlaceFailCounter = 0;
            
            // Бьём кристалл НАПРЯМУЮ (не через команду)
            bot.attack(crystal);
            bot.swingHand(Hand.MAIN_HAND);
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " hit crystal!");
            
            // Решаем что делать дальше
            if (distance <= 4.5 && state.lastObsidianPos != null) {
                // Враг рядом - спамим кристаллы
                state.step = 1;
                state.cooldownTicks = 2;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " moving to step 1 (new crystal)");
            } else {
                // Враг далеко - начинаем заново
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 5;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " moving to step 0 (new obsidian)");
            }
            
        } else {
            state.crystalNotFoundCounter++;
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " DID NOT FIND crystal! (" + state.crystalNotFoundCounter + "/3)");
            
            // Если 3 раза не нашли кристалл - сбрасываем состояние
            if (state.crystalNotFoundCounter >= 3) {
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK - crystal not found 3 times! Resetting state.");
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 5;
                state.crystalNotFoundCounter = 0;
                return true;
            }
            
            // Кристалла нет - пробуем поставить снова
            if (state.lastObsidianPos != null) {
                state.step = 1;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " returning to step 1 (place crystal)");
            } else {
                state.step = 0;
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " returning to step 0 (place obsidian)");
            }
            state.cooldownTicks = 3;
        }
        
        // НЕ поворачиваем голову обратно к игроку - оставляем смотреть на кристалл
        // Голова повернётся к игроку только в следующем цикле
        
        return true;
    }
    
    /**
     * Поддерживать оптимальную дистанцию (3-5 blocks)
     */
    private static void maintainDistance(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        double distance = bot.distanceTo(target);
        
        if (distance < 3.0) {
            // Слишком близко - отходим
            moveAway(bot, target, settings.getMoveSpeed() * 0.7);
        } else if (distance > 5.5) {
            // Слишком далеко - подходим
            moveToward(bot, target, settings.getMoveSpeed() * 0.7);
        } else {
            // Оптимальная дистанция - просто смотрим на цель, не двигаемся
            lookAtEntity(bot, target);
        }
    }
    
    /**
     * Двигаться к цели
     */
    private static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = targetPos.subtract(botPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * Отходить from target
     */
    private static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = botPos.subtract(targetPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * Стрейф (движение по кругу)
     */
    private static void strafe(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d toTarget = targetPos.subtract(botPos).normalize();
        
        // Перпендикулярное направление (стрейф влево)
        Vec3d strafeDir = new Vec3d(-toTarget.z, 0, toTarget.x);
        
        bot.setVelocity(strafeDir.x * speed, bot.getVelocity().y, strafeDir.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    /**
     * Смотреть на позицию
     */
    private static void lookAt(ServerPlayerEntity bot, BlockPos pos) {
        Vec3d target = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Vec3d botPos = bot.getEyePos();
        
        double dx = target.x - botPos.x;
        double dy = target.y - botPos.y;
        double dz = target.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Смотреть на сущность
     */
    private static void lookAtEntity(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetPos.x - botPos.x;
        double dy = targetPos.y - botPos.y;
        double dz = targetPos.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Выбрать предмет в руке
     */
    private static boolean selectItem(ServerPlayerEntity bot, int slot) {
        PlayerInventory inventory = bot.getInventory();
        
        // Если предмет в инвентаре (не в хотбаре) - перемещаем
        if (slot >= 9) {
            ItemStack item = inventory.getStack(slot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(slot, current);
            inventory.setStack(0, item);
            slot = 0;
        }
        
        // Переключаем слот
        ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(slot);
        return true;
    }
    
    /**
     * Найти обсидиан рядом с врагом (любой, не только тот что поставил бот)
     */
    private static BlockPos findExistingObsidian(ServerPlayerEntity bot, Entity target, World world, double maxDistance) {
        BlockPos targetPos = target.getBlockPos();
        
        // Проверяем блоки вокруг врага в радиусе maxDistance
        int radius = (int) Math.ceil(maxDistance);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = targetPos.add(dx, dy, dz);
                    
                    // Проверяем дистанцию от врага
                    double distFromTarget = Math.sqrt(target.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (distFromTarget > maxDistance) continue;
                    
                    // Проверяем что это обсидиан
                    net.minecraft.block.BlockState blockState = world.getBlockState(pos);
                    if (!blockState.getBlock().toString().contains("obsidian")) continue;
                    
                    // Проверяем что над обсидианом свободно
                    net.minecraft.block.BlockState blockAbove = world.getBlockState(pos.up());
                    if (!blockAbove.isAir() && !blockAbove.isReplaceable()) continue;
                    
                    // ВАЖНО: Проверяем что БОТ может достать до обсидиана (≤ 4 блока)
                    double distFromBot = Math.sqrt(bot.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (distFromBot > 4.0) {
                        continue;
                    }
                    
                    System.out.println("[Crystal PVP] Found suitable obsidian at " + pos + ", distance from enemy: " + String.format("%.2f", distFromTarget) + ", from bot: " + String.format("%.2f", distFromBot));
                    return pos;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Найти лучшую позицию для обсидиана - РЯДОМ с врагом
     */
    private static BlockPos findBestObsidianPosition(ServerPlayerEntity bot, Entity target, World world, java.util.Set<BlockPos> triedPositions) {
        BlockPos targetPos = target.getBlockPos();
        
        // Проверяем блоки вокруг врага
        BlockPos[] candidates = {
            targetPos.north(),
            targetPos.south(),
            targetPos.east(),
            targetPos.west(),
            targetPos.north().east(),
            targetPos.north().west(),
            targetPos.south().east(),
            targetPos.south().west(),
        };
        
        for (BlockPos pos : candidates) {
            // Пропускаем уже попробованные позиции
            if (triedPositions.contains(pos)) {
                continue;
            }
            
            // Проверяем что блок свободен
            if (!world.getBlockState(pos).isAir() && !world.getBlockState(pos).isReplaceable()) {
                continue;
            }
            
            // Проверяем что над блоком есть место для кристалла
            if (!world.getBlockState(pos.up()).isAir() && !world.getBlockState(pos.up()).isReplaceable()) {
                continue;
            }
            
            // Проверяем дистанцию до бота
            double dist = Math.sqrt(bot.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            
            if (dist <= 4.0) {
                System.out.println("[Crystal PVP] Found suitable position: " + pos);
                return pos;
            }
        }
        
        System.out.println("[Crystal PVP] No suitable positions found!");
        return null;
    }
    
    /**
     * Найти ближайший End Crystal
     */
    private static Entity findNearestEndCrystal(ServerPlayerEntity bot, Entity target, double maxDistance) {
        World world = bot.getEntityWorld();
        net.minecraft.util.math.Box searchBox = target.getBoundingBox().expand(maxDistance);
        
        Entity nearestCrystal = null;
        double nearestDist = maxDistance + 1;
        int crystalCount = 0;
        
        
        for (Entity entity : world.getOtherEntities(bot, searchBox)) {
            
            if (entity instanceof net.minecraft.entity.decoration.EndCrystalEntity) {
                crystalCount++;
                double dist = target.distanceTo(entity);
                System.out.println("[Crystal PVP] Это кристалл! Дистанция from target: " + String.format("%.2f", dist));
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestCrystal = entity;
                }
            }
        }
        
        if (crystalCount > 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " found " + crystalCount + " crystals in radius " + maxDistance);
        } else {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " НЕ found ни одного кристалла!");
        }
        
        return nearestCrystal;
    }
    
    /**
     * Найти обсидиан в инвентаре
     */
    private static int findObsidian(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.OBSIDIAN) return i;
        }
        return -1;
    }
    
    /**
     * Найти End Crystal в инвентаре
     */
    private static int findEndCrystal(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.END_CRYSTAL) return i;
        }
        return -1;
    }
    
    /**
     * Найти оружие ближнего боя
     */
    private static int findMeleeWeapon(PlayerInventory inventory) {
        // Приоритет: меч > топор
        int bestSlot = -1;
        int bestPriority = -1;
        
        // Ищем во всём инвентаре (0-35)
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            int priority = -1;
            
            if (stack.getItem().toString().contains("sword")) {
                priority = 10;
            } else if (stack.getItem().toString().contains("axe")) {
                priority = 5;
            }
            
            if (priority > bestPriority) {
                bestPriority = priority;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
    
    /**
     * Проверить наличие обсидиана
     */
    private static boolean hasObsidian(PlayerInventory inventory) {
        return findObsidian(inventory) >= 0;
    }
    
    /**
     * Проверить наличие End Crystal
     */
    private static boolean hasEndCrystal(PlayerInventory inventory) {
        return findEndCrystal(inventory) >= 0;
    }
    
    /**
     * Сбросить состояние бота
     */
    public static void reset(String botName) {
        states.remove(botName);
    }
}
