package org.stepan1411.pvp_bot.bot;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BotNavigation {
    
    private static final Map<String, NavigationState> navStates = new HashMap<>();
    
    public static class NavigationState {
        public int stuckTicks = 0;
        public Vec3d lastPosition = null;
        public int avoidDirection = 0; // -1 = лево, 1 = право, 0 = прямо
        public int avoidTicks = 0;
        public int jumpCooldown = 0;
        
        // Idle wandering
        public Vec3d spawnPosition = null;    // Начальная позиция бота
        public Vec3d wanderTarget = null;     // Текущая цель блуждания
        public int wanderCooldown = 0;        // Кулдаун до следующей смены цели
        public int idleTicks = 0;             // Сколько тиков бот в idle
    }
    
    public static NavigationState getState(String botName) {
        return navStates.computeIfAbsent(botName, k -> new NavigationState());
    }
    
    public static void removeState(String botName) {
        navStates.remove(botName);
    }
    
    /**
     * Двигаться к цели с обходом препятствий
     */
    public static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        NavigationState state = getState(bot.getName().getString());
        
        // Уменьшаем кулдауны
        if (state.jumpCooldown > 0) state.jumpCooldown--;
        if (state.avoidTicks > 0) state.avoidTicks--;
        
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        moveTowardPos(bot, targetPos, speed, state);
    }
    
    /**
     * Двигаться от цели с обходом препятствий
     */
    public static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        NavigationState state = getState(bot.getName().getString());
        
        // Уменьшаем кулдауны
        if (state.jumpCooldown > 0) state.jumpCooldown--;
        if (state.avoidTicks > 0) state.avoidTicks--;
        
        // Вычисляем позицию в противоположном направлении от цели
        double dx = bot.getX() - target.getX();
        double dz = bot.getZ() - target.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0) {
            dx /= dist;
            dz /= dist;
        }
        
        // Целевая позиция - 10 блоков от врага
        Vec3d awayPos = new Vec3d(bot.getX() + dx * 10, bot.getY(), bot.getZ() + dz * 10);
        moveTowardPos(bot, awayPos, speed, state);
    }
    
    /**
     * Основная логика движения к позиции
     */
    private static void moveTowardPos(ServerPlayerEntity bot, Vec3d targetPos, double speed, NavigationState state) {
        var world = bot.getEntityWorld();
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        // Проверяем застряли ли мы
        checkIfStuck(bot, state);
        
        // Вычисляем базовое направление
        double dx = targetPos.x - botPos.x;
        double dz = targetPos.z - botPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        if (horizontalDist > 0.1) {
            dx /= horizontalDist;
            dz /= horizontalDist;
        }
        
        // Если застряли или обходим препятствие - корректируем направление
        if (state.avoidTicks > 0) {
            // Поворачиваем в сторону обхода
            double tempDx = dx;
            if (state.avoidDirection > 0) {
                // Поворот вправо на 90 градусов
                dx = -dz;
                dz = tempDx;
            } else {
                // Поворот влево на 90 градусов
                dx = dz;
                dz = -tempDx;
            }
        }
        
        // Проверяем препятствия впереди (ближе к боту)
        BlockPos feetPos = new BlockPos(
            (int) Math.floor(botPos.x + dx * 0.5),
            (int) Math.floor(botPos.y),
            (int) Math.floor(botPos.z + dz * 0.5)
        );
        
        BlockPos headPos = feetPos.up();
        BlockPos aboveHeadPos = feetPos.up(2);
        
        // Блок на уровне ног - нужно прыгнуть
        boolean blockAtFeet = isBlockSolid(world, feetPos);
        // Блок на уровне головы - нельзя пройти
        boolean blockAtHead = isBlockSolid(world, headPos);
        // Можно запрыгнуть если блок только на уровне ног и свободно выше
        boolean canJumpUp = blockAtFeet && !blockAtHead && !isBlockSolid(world, aboveHeadPos);
        // Стена - блок и на уровне ног и на уровне головы
        boolean isWall = blockAtFeet && blockAtHead;
        
        // Проверяем лестницу или лиану
        boolean onLadder = isClimbable(world, bot.getBlockPos()) || isClimbable(world, bot.getBlockPos().up());
        
        // Проверяем яму впереди
        BlockPos groundFront = new BlockPos(
            (int) Math.floor(botPos.x + dx * 1.2),
            (int) Math.floor(botPos.y - 1),
            (int) Math.floor(botPos.z + dz * 1.2)
        );
        boolean holeAhead = !isBlockSolid(world, groundFront) && !isBlockSolid(world, groundFront.down());
        
        // Получаем настройки
        BotSettings settings = BotSettings.get();
        double jumpBoost = settings.getJumpBoost();
        
        // Прыжок
        if (bot.isOnGround() && state.jumpCooldown <= 0) {
            if (canJumpUp) {
                // Прыгаем на блок
                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
                // Добавляем импульс вперёд чтобы запрыгнуть
                bot.addVelocity(dx * 0.2, 0, dz * 0.2);
                state.jumpCooldown = 8;
            } else if (onLadder) {
                // На лестнице - прыгаем вверх
                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
                state.jumpCooldown = 5;
            } else if (holeAhead) {
                // Прыгаем через яму
                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
                bot.addVelocity(dx * 0.35, 0.05, dz * 0.35);
                state.jumpCooldown = 12;
            } else if (isWall && state.avoidTicks <= 0) {
                // Стена - начинаем обход
                state.avoidDirection = (Math.random() > 0.5) ? 1 : -1;
                state.avoidTicks = 25;
                // Всё равно пробуем прыгнуть - вдруг поможет
                bot.jump();
                if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
                state.jumpCooldown = 10;
            }
        }
        
        // Если на лестнице - карабкаемся вверх
        if (onLadder) {
            bot.addVelocity(0, 0.12, 0);
            bot.setSprinting(false);
            bot.forwardSpeed = 1.0f;
            state.lastPosition = botPos;
            return;
        }
        
        // Бхоп (bunny hop) - прыгаем во время бега для скорости
        // Прыгаем только если включено в настройках и нет препятствий
        boolean bhopEnabled = settings.isBhopEnabled();
        int bhopCooldown = settings.getBhopCooldown();
        boolean shouldBhop = bhopEnabled && speed >= 1.0 && !canJumpUp && !isWall && !holeAhead && state.jumpCooldown <= 0;
        if (shouldBhop && bot.isOnGround()) {
            bot.jump();
            if (jumpBoost > 0) bot.addVelocity(0, jumpBoost, 0);
            state.jumpCooldown = bhopCooldown;
        }
        
        // Применяем движение
        bot.setSprinting(true);
        bot.forwardSpeed = 1.0f; // Всегда максимальная скорость вперёд
        bot.sidewaysSpeed = 0;
        
        // Импульс движения - сильнее когда в воздухе (бхоп эффект)
        double moveForce = bot.isOnGround() ? 0.1 : 0.02; // В воздухе меньше контроля но сохраняем момент
        bot.addVelocity(dx * speed * moveForce, 0, dz * speed * moveForce);
        
        // Сохраняем позицию для проверки застревания
        state.lastPosition = botPos;
    }

    
    /**
     * Проверка застревания
     */
    private static void checkIfStuck(ServerPlayerEntity bot, NavigationState state) {
        Vec3d currentPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        if (state.lastPosition == null) {
            state.lastPosition = currentPos;
            return;
        }
        
        double moved = currentPos.distanceTo(state.lastPosition);
        
        if (moved < 0.05 && bot.isOnGround()) {
            state.stuckTicks++;
            
            if (state.stuckTicks > 10) {
                // Застряли - пробуем обойти
                if (state.avoidTicks <= 0) {
                    // Меняем направление обхода
                    state.avoidDirection = (state.avoidDirection == 0) ? 1 : -state.avoidDirection;
                    state.avoidTicks = 30; // Обходим 1.5 секунды
                }
                
                // Пробуем прыгнуть
                if (state.jumpCooldown <= 0) {
                    bot.jump();
                    state.jumpCooldown = 10;
                }
                
                state.stuckTicks = 0;
            }
        } else {
            state.stuckTicks = 0;
        }
    }
    
    /**
     * Проверка твёрдости блока
     */
    private static boolean isBlockSolid(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        // Блок твёрдый если он не воздух и не проходимый
        return !state.isAir() && state.isSolidBlock(world, pos);
    }
    
    /**
     * Проверка можно ли карабкаться по блоку (лестница, лиана)
     */
    private static boolean isClimbable(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof LadderBlock || 
               state.getBlock() instanceof VineBlock ||
               state.isOf(Blocks.SCAFFOLDING) ||
               state.isOf(Blocks.TWISTING_VINES) ||
               state.isOf(Blocks.TWISTING_VINES_PLANT) ||
               state.isOf(Blocks.WEEPING_VINES) ||
               state.isOf(Blocks.WEEPING_VINES_PLANT);
    }
    
    /**
     * Поворот к цели
     */
    public static void lookAt(ServerPlayerEntity bot, Entity target) {
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
     * Поворот от цели (для убегания)
     */
    public static void lookAway(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        // Направление ОТ цели
        double dx = botPos.x - targetPos.x;
        double dz = botPos.z - targetPos.z;
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        
        bot.setYaw(yaw);
        bot.setPitch(0);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Idle блуждание когда нет цели
     */
    public static void idleWander(ServerPlayerEntity bot) {
        BotSettings settings = BotSettings.get();
        if (!settings.isIdleWanderEnabled()) {
            return;
        }
        
        NavigationState state = getState(bot.getName().getString());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        // Запоминаем начальную позицию
        if (state.spawnPosition == null) {
            state.spawnPosition = botPos;
        }
        
        // Уменьшаем кулдауны
        if (state.jumpCooldown > 0) state.jumpCooldown--;
        if (state.avoidTicks > 0) state.avoidTicks--;
        if (state.wanderCooldown > 0) state.wanderCooldown--;
        
        double radius = settings.getIdleWanderRadius();
        
        // Выбираем новую цель если нужно
        if (state.wanderTarget == null || state.wanderCooldown <= 0 || 
            botPos.distanceTo(state.wanderTarget) < 1.5) {
            // Случайная точка в радиусе от спавна
            double angle = Math.random() * Math.PI * 2;
            double dist = Math.random() * radius;
            state.wanderTarget = new Vec3d(
                state.spawnPosition.x + Math.cos(angle) * dist,
                state.spawnPosition.y,
                state.spawnPosition.z + Math.sin(angle) * dist
            );
            state.wanderCooldown = 60 + (int)(Math.random() * 100); // 3-8 секунд
        }
        
        // Идём к цели медленно
        double dx = state.wanderTarget.x - botPos.x;
        double dz = state.wanderTarget.z - botPos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        
        if (dist > 0.5) {
            dx /= dist;
            dz /= dist;
            
            // Поворачиваемся в направлении движения
            float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
            bot.setYaw(yaw);
            bot.setHeadYaw(yaw);
            bot.setPitch(0);
            
            // Медленно идём (не бежим)
            bot.setSprinting(false);
            bot.forwardSpeed = 0.5f;
            bot.addVelocity(dx * 0.03, 0, dz * 0.03);
            
            // Проверяем препятствия
            var world = bot.getEntityWorld();
            BlockPos feetPos = new BlockPos(
                (int) Math.floor(botPos.x + dx * 0.5),
                (int) Math.floor(botPos.y),
                (int) Math.floor(botPos.z + dz * 0.5)
            );
            
            if (isBlockSolid(world, feetPos) && !isBlockSolid(world, feetPos.up()) && 
                bot.isOnGround() && state.jumpCooldown <= 0) {
                bot.jump();
                state.jumpCooldown = 10;
            }
        } else {
            // Стоим на месте
            bot.forwardSpeed = 0;
            bot.sidewaysSpeed = 0;
        }
        
        state.lastPosition = botPos;
    }
    
    /**
     * Сбросить idle состояние (когда появилась цель)
     */
    public static void resetIdle(String botName) {
        NavigationState state = navStates.get(botName);
        if (state != null) {
            state.wanderTarget = null;
            state.wanderCooldown = 0;
            state.idleTicks = 0;
        }
    }
}
