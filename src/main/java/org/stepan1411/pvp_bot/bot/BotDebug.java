package org.stepan1411.pvp_bot.bot;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Система отладки для визуализации поведения ботов
 */
public class BotDebug {
    
    // Цвета для частиц (RGB в hex)
    private static final int COLOR_GREEN = 0x00FF00;   // Зелёный для углов блоков пути
    private static final int COLOR_GRAY = 0x808080;    // Серый для линии пути
    private static final int COLOR_RED = 0xFF0000;     // Красный для целевого блока (navigation)
    private static final int COLOR_PURPLE = 0xFF00FF;  // Фиолетовый для хитбокса цели (target)
    private static final int COLOR_BLUE = 0x0080FF;    // Синий для направления взгляда
    private static final int COLOR_YELLOW = 0xFFFF00;  // Жёлтый для боя
    
    // Настройки отладки для каждого бота
    private static final Map<String, DebugSettings> debugSettings = new HashMap<>();
    
    /**
     * Настройки отладки для бота
     */
    public static class DebugSettings {
        public boolean pathVisualization = false;    // Показывать путь движения (зелёная линия)
        public boolean targetVisualization = false;  // Показывать хитбокс цели (фиолетовый)
        public boolean combatInfo = false;           // Показывать информацию о бое (жёлтый)
        public boolean navigationInfo = false;       // Показывать целевой блок (красный куб)
        
        // Счётчики для контроля частоты отображения
        public int pathTickCounter = 0;
        public int targetTickCounter = 0;
        public int navigationTickCounter = 0;
        
        public boolean isAnyEnabled() {
            return pathVisualization || targetVisualization || combatInfo || navigationInfo;
        }
    }
    
    /**
     * Получить настройки отладки для бота
     */
    public static DebugSettings getSettings(String botName) {
        return debugSettings.computeIfAbsent(botName, k -> new DebugSettings());
    }
    
    /**
     * Включить/выключить визуализацию пути
     */
    public static void setPathVisualization(String botName, boolean enabled) {
        getSettings(botName).pathVisualization = enabled;
    }
    
    /**
     * Включить/выключить визуализацию цели
     */
    public static void setTargetVisualization(String botName, boolean enabled) {
        getSettings(botName).targetVisualization = enabled;
    }
    
    /**
     * Включить/выключить информацию о бое
     */
    public static void setCombatInfo(String botName, boolean enabled) {
        getSettings(botName).combatInfo = enabled;
    }
    
    /**
     * Включить/выключить информацию о навигации
     */
    public static void setNavigationInfo(String botName, boolean enabled) {
        getSettings(botName).navigationInfo = enabled;
    }
    
    /**
     * Включить все режимы отладки
     */
    public static void enableAll(String botName) {
        DebugSettings settings = getSettings(botName);
        settings.pathVisualization = true;
        settings.targetVisualization = true;
        settings.combatInfo = true;
        settings.navigationInfo = true;
    }
    
    /**
     * Выключить все режимы отладки
     */
    public static void disableAll(String botName) {
        debugSettings.remove(botName);
    }
    
    /**
     * Проверить включена ли отладка для бота
     */
    public static boolean isEnabled(String botName) {
        DebugSettings settings = debugSettings.get(botName);
        return settings != null && settings.isAnyEnabled();
    }
    
    /**
     * Показать путь движения с минимальным количеством частиц
     * Показывает реальную историю + планируемый путь до цели
     * Обновляется каждые 5 тиков для уменьшения наложения частиц
     */
    public static void showPath(ServerPlayerEntity bot, Vec3d targetPos, java.util.LinkedList<Vec3d> pathHistory) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.pathVisualization) {
            return;
        }
        
        // Показываем частицы только каждые 5 тиков
        settings.pathTickCounter++;
        if (settings.pathTickCounter < 5) {
            return;
        }
        settings.pathTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // Создаём частицы
        DustParticleEffect greenDust = new DustParticleEffect(COLOR_GREEN, 1.0f);
        DustParticleEffect grayDust = new DustParticleEffect(COLOR_GRAY, 0.7f);
        
        // Собираем все позиции: история + текущая позиция + цель
        List<Vec3d> allPositions = new ArrayList<>(pathHistory);
        Vec3d currentPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        // Добавляем текущую позицию
        if (allPositions.isEmpty() || currentPos.distanceTo(allPositions.get(allPositions.size() - 1)) > 0.3) {
            allPositions.add(currentPos);
        }
        
        // Добавляем целевую позицию (планируемый путь)
        allPositions.add(targetPos);
        
        if (allPositions.isEmpty()) {
            return;
        }
        
        // Собираем уникальные блоки по всему пути
        Set<BlockPos> uniqueBlocks = new LinkedHashSet<>();
        for (Vec3d pos : allPositions) {
            BlockPos blockPos = new BlockPos(
                (int) Math.floor(pos.x), 
                (int) Math.floor(pos.y), 
                (int) Math.floor(pos.z)
            );
            uniqueBlocks.add(blockPos);
        }
        
        // Рисуем только 2 угла каждого блока для производительности
        for (BlockPos blockPos : uniqueBlocks) {
            int bx = blockPos.getX();
            int by = blockPos.getY();
            int bz = blockPos.getZ();
            
            // Только 2 противоположных угла
            world.spawnParticles(greenDust, bx, by + 1, bz, 1, 0, 0, 0, 0);
            world.spawnParticles(greenDust, bx + 1, by + 1, bz + 1, 1, 0, 0, 0, 0);
        }
        
        // Рисуем серую линию по всему пути (история + планируемый)
        if (allPositions.size() > 1) {
            for (int i = 0; i < allPositions.size() - 1; i++) {
                Vec3d pos1 = allPositions.get(i);
                Vec3d pos2 = allPositions.get(i + 1);
                
                // Линия с большим шагом (меньше частиц)
                drawLine(world, grayDust, 
                    pos1.x, pos1.y + 0.1, pos1.z, 
                    pos2.x, pos2.y + 0.1, pos2.z, 
                    0.3);
            }
        }
    }
    
    /**
     * Показать целевой блок красным кубом (куда бот хочет прийти)
     * Обновляется каждые 5 тиков для уменьшения наложения частиц
     */
    public static void showTargetBlock(ServerPlayerEntity bot, Vec3d targetPos) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.navigationInfo) {
            return;
        }
        
        // Показываем частицы только каждые 5 тиков
        settings.navigationTickCounter++;
        if (settings.navigationTickCounter < 5) {
            return;
        }
        settings.navigationTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // Получаем координаты блока
        int blockX = (int) Math.floor(targetPos.x);
        int blockY = (int) Math.floor(targetPos.y);
        int blockZ = (int) Math.floor(targetPos.z);
        
        // Создаём красную dust частицу (RGB: 255, 0, 0)
        DustParticleEffect redDust = new DustParticleEffect(COLOR_RED, 1.0f);
        
        // Рисуем рёбра куба (12 рёбер) с увеличенным шагом
        double step = 0.2; // Увеличен с 0.1 до 0.2 для производительности
        
        // Нижние 4 ребра (y = blockY)
        drawLine(world, redDust, blockX, blockY, blockZ, blockX + 1, blockY, blockZ, step);
        drawLine(world, redDust, blockX, blockY, blockZ + 1, blockX + 1, blockY, blockZ + 1, step);
        drawLine(world, redDust, blockX, blockY, blockZ, blockX, blockY, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ, blockX + 1, blockY, blockZ + 1, step);
        
        // Верхние 4 ребра (y = blockY + 1)
        drawLine(world, redDust, blockX, blockY + 1, blockZ, blockX + 1, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX, blockY + 1, blockZ + 1, blockX + 1, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX, blockY + 1, blockZ, blockX, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY + 1, blockZ, blockX + 1, blockY + 1, blockZ + 1, step);
        
        // Вертикальные 4 ребра
        drawLine(world, redDust, blockX, blockY, blockZ, blockX, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ, blockX + 1, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX, blockY, blockZ + 1, blockX, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ + 1, blockX + 1, blockY + 1, blockZ + 1, step);
    }
    
    /**
     * Показать хитбокс цели фиолетовым цветом
     * Обновляется каждые 3 тика для уменьшения наложения частиц
     */
    public static void showTargetEntity(ServerPlayerEntity bot, net.minecraft.entity.Entity target) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.targetVisualization) {
            return;
        }
        
        // Показываем частицы только каждые 3 тика (цель движется)
        settings.targetTickCounter++;
        if (settings.targetTickCounter < 3) {
            return;
        }
        settings.targetTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // Получаем хитбокс цели
        var box = target.getBoundingBox();
        
        // Создаём фиолетовую dust частицу (RGB: 255, 0, 255)
        DustParticleEffect purpleDust = new DustParticleEffect(COLOR_PURPLE, 1.0f);
        
        double step = 0.2; // Увеличен с 0.1 до 0.2 для производительности
        
        // Нижние 4 ребра
        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, step);
        
        // Верхние 4 ребра
        drawLine(world, purpleDust, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, step);
        
        // Вертикальные 4 ребра
        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);
    }
    
    /**
     * Нарисовать линию dust частицами между двумя точками
     */
    private static void drawLine(ServerWorld world, DustParticleEffect dust, double x1, double y1, double z1, double x2, double y2, double z2, double step) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = (int) Math.ceil(length / step);
        
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;
            
            // Dust частицы
            world.spawnParticles(
                dust,
                x, y, z,
                1,
                0, 0, 0,
                0
            );
        }
    }
    
    /**
     * Показать направление взгляда синими частицами
     */
    public static void showLookDirection(ServerPlayerEntity bot) {
        if (!getSettings(bot.getName().getString()).navigationInfo) {
            return;
        }
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        Vec3d lookVec = bot.getRotationVec(1.0f);
        Vec3d startPos = bot.getEyePos();
        
        // Создаём синюю dust частицу (RGB: 0, 128, 255)
        DustParticleEffect blueDust = new DustParticleEffect(0x0080FF, 1.0f);
        
        // Рисуем линию взгляда синими частицами
        for (int i = 1; i <= 10; i++) {
            double x = startPos.x + lookVec.x * i * 0.5;
            double y = startPos.y + lookVec.y * i * 0.5;
            double z = startPos.z + lookVec.z * i * 0.5;
            
            world.spawnParticles(
                blueDust,
                x, y, z,
                1,
                0, 0, 0,
                0
            );
        }
    }
    
    /**
     * Показать позицию атаки жёлтыми частицами
     */
    public static void showAttackPosition(ServerPlayerEntity bot, Vec3d attackPos) {
        if (!getSettings(bot.getName().getString()).combatInfo) {
            return;
        }
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // Создаём жёлтую dust частицу (RGB: 255, 255, 0)
        DustParticleEffect yellowDust = new DustParticleEffect(0xFFFF00, 1.0f);
        
        // Жёлтые частицы на позиции атаки
        world.spawnParticles(
            yellowDust,
            attackPos.x, attackPos.y + 1, attackPos.z,
            5,
            0.2, 0.2, 0.2,
            0.02
        );
    }
    
    /**
     * Очистить все настройки отладки
     */
    public static void clearAll() {
        debugSettings.clear();
    }
}
