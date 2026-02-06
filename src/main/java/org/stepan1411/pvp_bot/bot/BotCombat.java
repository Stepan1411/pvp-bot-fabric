package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class BotCombat {
    
    // Хранение состояния боя для каждого бота
    private static final Map<String, CombatState> combatStates = new HashMap<>();
    
    public static class CombatState {
        public Entity target = null;
        public String forcedTargetName = null; // Принудительная цель по команде
        public int attackCooldown = 0;
        public int bowDrawTicks = 0;
        public boolean isDrawingBow = false;
        public Entity lastAttacker = null;
        public long lastAttackTime = 0;
        public WeaponMode currentMode = WeaponMode.MELEE;
        public float lastHealth = 20.0f; // Для отслеживания урона
        public boolean isRetreating = false; // Отступает для лечения
        
        // Состояние копья (Spear) - 1.21.11
        public boolean isChargingSpear = false;
        public int spearChargeTicks = 0;
        
        // Состояние паутины
        public int cobwebCooldown = 0; // Кулдаун на размещение паутины
        
        // Состояние щита
        public boolean isUsingShield = false; // Щит активен через Carpet команду
        public int shieldToggleCooldown = 0; // Кулдаун на переключение щита (предотвращает спам)
        public int airTimeTicks = 0; // Сколько тиков бот в воздухе
        public boolean shieldBroken = false; // Щит был сбит топором
        public long shieldBrokenTime = 0; // Время когда щит был сбит
        public boolean isPlacingCobweb = false; // Процесс размещения паутины
        public int cobwebPlaceTicks = 0; // Тики размещения
        
        // Состояние Crystal PVP
        public boolean isCrystalPvping = false; // Режим Crystal PVP активен
        public int crystalCooldown = 0; // Кулдаун между кристаллами
        public net.minecraft.util.math.BlockPos lastObsidianPos = null; // Последний поставленный обсидиан
        public int crystalPvpStep = 0; // Текущий шаг: 0=ставим обсидиан, 1=ставим кристалл, 2=бьём кристалл
        public int crystalPvpTicks = 0; // Тики на текущем шаге
        
        public enum WeaponMode {
            MELEE,      // Ближний бой (меч/топор)
            RANGED,     // Дальний бой (лук/арбалет)
            MACE,       // Булава (прыжок + удар)
            SPEAR,      // Копьё (charge + jab) - 1.21.11
            CRYSTAL,    // Crystal PVP (обсидиан + кристалл + удар)
            ANCHOR      // Anchor PVP (якорь + glowstone + взрыв)
        }
    }
    
    public static CombatState getState(String botName) {
        return combatStates.computeIfAbsent(botName, k -> new CombatState());
    }
    
    public static void removeState(String botName) {
        combatStates.remove(botName);
    }
    
    /**
     * Основной метод обновления боя - вызывается каждый тик
     */
    public static void update(ServerPlayerEntity bot, net.minecraft.server.MinecraftServer server) {
        BotSettings settings = BotSettings.get();
        if (!settings.isCombatEnabled()) return;
        
        CombatState state = getState(bot.getName().getString());
        
        // Проверяем получил ли бот урон (альтернатива mixin)
        float currentHealth = bot.getHealth();
        if (currentHealth < state.lastHealth && settings.isRevengeEnabled()) {
            // Бот получил урон - ищем кто атаковал
            Entity attacker = bot.getAttacker();
            if (attacker != null && attacker != bot && attacker instanceof LivingEntity) {
                state.lastAttacker = attacker;
                state.lastAttackTime = System.currentTimeMillis();
            }
        }
        state.lastHealth = currentHealth;
        
        // Уменьшаем кулдаун
        if (state.attackCooldown > 0) {
            state.attackCooldown--;
        }
        if (state.shieldToggleCooldown > 0) {
            state.shieldToggleCooldown--;
        }
        if (state.cobwebCooldown > 0) {
            state.cobwebCooldown--;
        }
        if (state.crystalCooldown > 0) {
            state.crystalCooldown--;
        }
        
        // Находим цель
        Entity target = findTarget(bot, state, settings, server);
        state.target = target;
        
        // === DEBUG: Показываем хитбокс цели ===
        if (target != null) {
            BotDebug.showTargetEntity(bot, target);
        }
        // ======================================
        
        // Обработка размещения паутины (приоритет над всем)
        if (state.isPlacingCobweb && target != null) {
            handleCobwebPlacement(bot, target, state, server);
            return; // Не делаем ничего другого пока ставим паутину
        }
        
        if (target == null) {
            // Нет цели - прекращаем натягивать лук
            if (state.isDrawingBow) {
                stopUsingBow(bot, state);
            }
            // Idle блуждание когда нет цели
            BotNavigation.idleWander(bot);
            return;
        }
        
        // Есть цель - сбрасываем idle
        BotNavigation.resetIdle(bot.getName().getString());
        
        // Определяем дистанцию до цели
        double distance = bot.distanceTo(target);
        
        // Проверяем нужно ли отступать (низкое HP)
        float health = bot.getHealth();
        float maxHealth = bot.getMaxHealth();
        float healthPercent = health / maxHealth;
        boolean lowHealth = healthPercent <= settings.getRetreatHealthPercent();
        boolean criticalHealth = healthPercent <= settings.getCriticalHealthPercent();
        
        // Проверяем есть ли еда для отступления
        boolean hasFood = BotUtils.hasFood(bot);
        
        // Сбрасываем флаг сбитого щита через 5 секунд
        if (state.shieldBroken && System.currentTimeMillis() - state.shieldBrokenTime > 5000) {
            state.shieldBroken = false;
        }
        
        // Проверяем ест ли бот - НИКОГДА не переключаем слоты пока едим!
        var utilsState = BotUtils.getState(bot.getName().getString());
        boolean isEating = utilsState.isEating;
        
        if (isEating && settings.isRetreatEnabled()) {
            // Бот ест И retreat включён - отступаем
            state.isRetreating = true;
            if (state.isDrawingBow) {
                stopUsingBow(bot, state);
            }
            // Убегаем от врага с навигацией (скорость 1.2 = бхоп включён)
            BotNavigation.lookAway(bot, target);
            BotNavigation.moveAway(bot, target, 1.2);
            return;
        } else if (isEating) {
            // Бот ест НО retreat выключен - просто не переключаем слоты, продолжаем драться
            if (state.isDrawingBow) {
                stopUsingBow(bot, state);
            }
            // Не отступаем, продолжаем бой
        }
        
        // Пробуем использовать зелье исцеления если низкое HP
        if (lowHealth && settings.isAutoPotionEnabled()) {
            if (BotUtils.tryUseHealingPotion(bot, server)) {
                // Используем зелье - не отступаем пока пьём
                return;
            }
        }
        
        // Логика отступления:
        // 1. Если HP критическое (< 15%) - ВСЕГДА отступаем, даже если щит сбит
        // 2. Если HP низкое (< 30%) И щит НЕ сбит - отступаем
        // 3. Если щит сбит - продолжаем драться пока HP не станет критическим
        boolean shouldRetreat = settings.isRetreatEnabled() && hasFood && 
                               (criticalHealth || (lowHealth && !state.shieldBroken));
        
        // Отступаем если низкое HP, включено отступление И есть еда
        // Если еды нет - нет смысла отступать, лучше драться до конца
        if (shouldRetreat) {
            state.isRetreating = true;
            
            // Используем щит при отступлении для защиты
            if (settings.isAutoShieldEnabled()) {
                var inventory = bot.getInventory();
                // Экипируем щит в offhand если его там нет
                ItemStack offhandItem = bot.getOffHandStack();
                if (offhandItem.isEmpty() || !offhandItem.getItem().toString().contains("shield")) {
                    int shieldSlot = findShield(inventory);
                    if (shieldSlot >= 0) {
                        // Перемещаем щит в offhand (слот 40)
                        ItemStack shield = inventory.getStack(shieldSlot);
                        inventory.setStack(40, shield);
                        inventory.setStack(shieldSlot, ItemStack.EMPTY);
                    }
                }
                
                // Поднимаем щит при отступлении
                if (!state.isUsingShield && state.shieldToggleCooldown <= 0) {
                    startUsingShield(bot, server);
                    state.isUsingShield = true;
                }
            }
            
            // Убегаем пока враг ближе 25 блоков (скорость 1.5 = максимальный бхоп)
            if (distance < 25.0) {
                // Пробуем поставить паутину под врага при отступлении
                if (settings.isCobwebEnabled() && distance < 8.0 && state.cobwebCooldown <= 0 && !state.isPlacingCobweb) {
                    tryPlaceCobweb(bot, target, server);
                }
                BotNavigation.lookAway(bot, target);
                BotNavigation.moveAway(bot, target, 1.5);
            }
            // Не атакуем пока HP низкое
            return;
        }
        state.isRetreating = false;
        
        // Проверяем чинится ли бот - если да, не трогаем его
        if (utilsState.isMending) {
            return; // Бот чинится - не мешаем ему
        }
        
        // Выбираем режим боя
        selectWeaponMode(bot, state, distance, settings);
        
        // Поворачиваемся к цели (если не бросаем зелье)
        if (!utilsState.isThrowingPotion) {
            BotNavigation.lookAt(bot, target);
        }
        
        // Если враг слишком далеко для текущего режима - идём к нему
        double maxRange = switch (state.currentMode) {
            case MELEE -> settings.getMeleeRange() * 2;
            case RANGED -> settings.getRangedOptimalRange() + 15;
            case MACE -> settings.getMaceRange() * 2;
            case SPEAR -> settings.getSpearChargeRange();
            case CRYSTAL -> 10.0; // Crystal PVP эффективен до 10 блоков
            case ANCHOR -> 10.0;  // Anchor PVP эффективен до 10 блоков (increased)
        };
        
        if (distance > maxRange) {
            // Враг далеко - идём к нему
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
            return;
        }
        
        // Выполняем действие в зависимости от режима
        switch (state.currentMode) {
            case MELEE -> handleMeleeCombat(bot, target, state, distance, settings, server);
            case RANGED -> handleRangedCombat(bot, target, state, distance, settings, server);
            case MACE -> handleMaceCombat(bot, target, state, distance, settings, server);
            case SPEAR -> handleSpearCombat(bot, target, state, distance, settings, server);
            case CRYSTAL -> {
                // Используем отдельный класс для Crystal PVP с полным контролем
                boolean handled = BotCrystalPvp.doCrystalPvp(bot, target, settings, server);
                if (!handled) {
                    // Если Crystal PVP не может работать - переключаемся на ближний бой
                    state.currentMode = CombatState.WeaponMode.MELEE;
                    handleMeleeCombat(bot, target, state, distance, settings, server);
                }
            }
            case ANCHOR -> {
                // Используем отдельный класс для Anchor PVP с полным контролем
                boolean handled = BotAnchorPvp.doAnchorPvp(bot, target, settings, server);
                if (!handled) {
                    // Если Anchor PVP не может работать - переключаемся на ближний бой
                    state.currentMode = CombatState.WeaponMode.MELEE;
                    handleMeleeCombat(bot, target, state, distance, settings, server);
                }
            }
        }
    }
    
    /**
     * Поиск цели
     */
    private static Entity findTarget(ServerPlayerEntity bot, CombatState state, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        // Приоритет 1: Принудительная цель по команде (ВСЕГДА работает)
        if (state.forcedTargetName != null) {
            Entity forced = findEntityByName(bot, state.forcedTargetName, server);
            if (forced != null && forced.isAlive()) {
                double dist = bot.distanceTo(forced);
                if (dist <= settings.getMaxTargetDistance()) {
                    return forced;
                }
            }
            // НЕ сбрасываем цель если она временно далеко - пусть бот идёт к ней
            // state.forcedTargetName = null;
        }
        
        // Приоритет 2: Тот кто нас атаковал (реванш) - держим цель пока враг жив
        if (settings.isRevengeEnabled() && state.lastAttacker != null) {
            // Проверяем что атакующий ещё существует и жив
            if (!state.lastAttacker.isRemoved() && state.lastAttacker.isAlive()) {
                // Проверяем friendlyfire - не атакуем союзников даже в реванже
                if (!settings.isFriendlyFireEnabled() && state.lastAttacker instanceof PlayerEntity) {
                    String attackerName = state.lastAttacker.getName().getString();
                    if (BotFaction.areAllies(bot.getName().getString(), attackerName)) {
                        state.lastAttacker = null; // Сбрасываем - это союзник
                    }
                }
                
                if (state.lastAttacker != null) {
                    double dist = bot.distanceTo(state.lastAttacker);
                    if (dist <= settings.getMaxTargetDistance()) {
                        // Обновляем время если враг близко (не сбрасываем пока враг рядом)
                        if (dist <= 10.0) {
                            state.lastAttackTime = System.currentTimeMillis();
                        }
                        return state.lastAttacker;
                    }
                }
            }
            // Сбрасываем только если цель мертва или далеко больше 30 секунд
            if (state.lastAttacker == null || state.lastAttacker.isRemoved() || !state.lastAttacker.isAlive() || 
                System.currentTimeMillis() - state.lastAttackTime >= 30000) {
                state.lastAttacker = null;
            }
        }
        
        // Приоритет 3: Враги по фракциям (всегда если фракции включены)
        if (settings.isFactionsEnabled()) {
            Entity factionEnemy = findFactionEnemy(bot, settings, server);
            if (factionEnemy != null) {
                return factionEnemy;
            }
        }
        
        // Приоритет 4: Ближайший враг (только если autotarget включён)
        if (settings.isAutoTargetEnabled()) {
            return findNearestEnemy(bot, settings, server);
        }
        
        return null;
    }
    
    /**
     * Поиск врага по фракциям
     */
    private static Entity findFactionEnemy(ServerPlayerEntity bot, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        String botName = bot.getName().getString();
        String botFaction = BotFaction.getFaction(botName);
        
        // Если бот не в фракции - не ищем врагов по фракциям
        if (botFaction == null) return null;
        
        double maxDist = settings.getMaxTargetDistance();
        Entity nearest = null;
        double nearestDist = maxDist + 1;
        
        if (server != null) {
            for (var player : server.getPlayerManager().getPlayerList()) {
                if (player == bot) continue;
                if (!player.isAlive()) continue;
                if (player.isSpectator() || player.isCreative()) continue;
                
                String targetName = player.getName().getString();
                
                // Проверяем враждебность по фракциям
                if (BotFaction.areEnemies(botName, targetName)) {
                    double dist = bot.distanceTo(player);
                    if (dist < nearestDist && dist <= maxDist) {
                        nearestDist = dist;
                        nearest = player;
                    }
                }
            }
        }
        
        return nearest;
    }
    
    private static Entity findEntityByName(ServerPlayerEntity bot, String name, net.minecraft.server.MinecraftServer server) {
        // Ищем игрока
        if (server != null) {
            var player = server.getPlayerManager().getPlayer(name);
            if (player != null && player != bot) return player;
        }
        
        // Ищем сущность по имени в мире бота
        if (server != null) {
            for (var world : server.getWorlds()) {
                for (Entity entity : world.iterateEntities()) {
                    if (entity.getName().getString().equalsIgnoreCase(name) && entity != bot) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }
    
    private static Entity findNearestEnemy(ServerPlayerEntity bot, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        double maxDist = settings.getMaxTargetDistance();
        Box searchBox = bot.getBoundingBox().expand(maxDist);
        
        Entity nearest = null;
        double nearestDist = maxDist + 1;
        
        // Получаем мир через сервер
        if (server != null) {
            for (var world : server.getWorlds()) {
                for (Entity entity : world.getOtherEntities(bot, searchBox)) {
                    if (!isValidTarget(bot, entity, settings)) continue;
                    
                    double dist = bot.distanceTo(entity);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = entity;
                    }
                }
            }
        }
        
        return nearest;
    }
    
    private static boolean isValidTarget(ServerPlayerEntity bot, Entity entity, BotSettings settings) {
        if (entity == bot) return false;
        if (!entity.isAlive()) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        
        String botName = bot.getName().getString();
        
        // Игроки и боты
        if (entity instanceof PlayerEntity player) {
            if (player.isSpectator() || player.isCreative()) return false;
            
            String targetName = player.getName().getString();
            
            // Проверяем фракции
            if (settings.isFactionsEnabled()) {
                // Союзники - не атакуем (если friendlyfire выключен)
                if (!settings.isFriendlyFireEnabled() && BotFaction.areAllies(botName, targetName)) {
                    return false;
                }
                // Враги по фракции - атакуем
                if (BotFaction.areEnemies(botName, targetName)) {
                    return true;
                }
            }
            
            // Проверяем настройки для ботов
            if (BotManager.getAllBots().contains(targetName)) {
                if (!settings.isTargetOtherBots()) return false;
            } else {
                if (!settings.isTargetPlayers()) return false;
            }
            
            return true;
        }
        
        // Враждебные мобы
        if (entity instanceof HostileEntity) {
            return settings.isTargetHostileMobs();
        }
        
        // Другие мобы
        if (living instanceof net.minecraft.entity.mob.MobEntity) {
            return settings.isTargetHostileMobs();
        }
        
        return false;
    }

    
    /**
     * Выбор режима оружия
     */
    private static void selectWeaponMode(ServerPlayerEntity bot, CombatState state, double distance, BotSettings settings) {
        var inventory = bot.getInventory();
        Entity target = state.target;
        
        boolean hasMelee = findMeleeWeapon(inventory) >= 0;
        boolean hasRanged = findRangedWeapon(inventory) >= 0;
        boolean hasMace = findMace(inventory) >= 0;
        boolean hasSpear = findSpear(inventory) >= 0;
        
        double meleeRange = settings.getMeleeRange();
        double rangedMinRange = settings.getRangedMinRange();
        double maceRange = settings.getMaceRange();
        double spearRange = settings.getSpearRange();
        
        // Логика выбора оружия
        // Приоритет 1: Crystal PVP (если доступен) - быстрее
        if (target != null && BotCrystalPvp.canUseCrystalPvp(bot, target, settings)) {
            state.currentMode = CombatState.WeaponMode.CRYSTAL;
        } else if (target != null && BotAnchorPvp.canUseAnchorPvp(bot, target, settings)) {
            // Приоритет 2: Anchor PVP (если кристаллы недоступны) - больше урона но медленнее
            state.currentMode = CombatState.WeaponMode.ANCHOR;
        } else if (hasMace && distance <= maceRange && settings.isMaceEnabled()) {
            // Булава - если враг близко и можно прыгнуть
            state.currentMode = CombatState.WeaponMode.MACE;
        } else if (hasSpear && distance <= spearRange && settings.isSpearEnabled()) {
            // Копьё - средняя дистанция, charge атака при движении
            state.currentMode = CombatState.WeaponMode.SPEAR;
        } else if (hasRanged && distance > rangedMinRange && settings.isRangedEnabled()) {
            // Лук - если враг далеко
            state.currentMode = CombatState.WeaponMode.RANGED;
        } else if (hasMelee && distance <= meleeRange * 2) {
            // Меч - ближний бой
            state.currentMode = CombatState.WeaponMode.MELEE;
        } else if (hasSpear && settings.isSpearEnabled()) {
            // Копьё как запасной вариант для средней дистанции
            state.currentMode = CombatState.WeaponMode.SPEAR;
        } else if (hasRanged && settings.isRangedEnabled()) {
            // Лук как запасной вариант
            state.currentMode = CombatState.WeaponMode.RANGED;
        } else {
            state.currentMode = CombatState.WeaponMode.MELEE;
        }
    }
    
    /**
     * Ближний бой
     */
    private static void handleMeleeCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        var inventory = bot.getInventory();
        
        // Прекращаем натягивать лук если натягивали
        if (state.isDrawingBow) {
            stopUsingBow(bot, state);
        }
        
        double meleeRange = settings.getMeleeRange();
        
        // Пробуем поставить паутину под врага если он близко и бежит на нас
        if (settings.isCobwebEnabled() && distance < 6.0 && distance > 2.0 && state.cobwebCooldown <= 0 && !state.isPlacingCobweb) {
            // Проверяем что враг движется к нам
            if (target instanceof net.minecraft.entity.LivingEntity living) {
                Vec3d targetVel = living.getVelocity();
                double targetSpeed = Math.sqrt(targetVel.x * targetVel.x + targetVel.z * targetVel.z);
                if (targetSpeed > 0.08) { // Враг движется
                    tryPlaceCobweb(bot, target, server);
                }
            }
        }
        
        // Движение к цели с навигацией
        if (distance > meleeRange) {
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
        } else if (distance < 1.5) {
            // Слишком близко - отходим немного
            BotNavigation.moveAway(bot, target, 0.3);
        }
        
        // Проверяем HP и используем щит если нужно
        float healthPercent = bot.getHealth() / bot.getMaxHealth();
        boolean shouldUseShield = settings.isAutoShieldEnabled() && healthPercent < settings.getShieldHealthThreshold();
        
        // Определяем нужно ли держать щит поднятым
        // Опускаем щит ТОЛЬКО когда attackCooldown == 1 (за 1 тик до атаки)
        boolean willAttackSoon = distance <= meleeRange && state.attackCooldown == 1;
        boolean shouldHoldShield = shouldUseShield && !willAttackSoon;
        
        if (shouldUseShield) {
            // Экипируем щит в offhand если его там нет
            ItemStack offhandItem = bot.getOffHandStack();
            if (offhandItem.isEmpty() || !offhandItem.getItem().toString().contains("shield")) {
                int shieldSlot = findShield(inventory);
                if (shieldSlot >= 0) {
                    // Перемещаем щит в offhand (слот 40)
                    ItemStack shield = inventory.getStack(shieldSlot);
                    inventory.setStack(40, shield);
                    inventory.setStack(shieldSlot, ItemStack.EMPTY);
                }
            }
            
            // Управляем щитом через Carpet команды
            if (shouldHoldShield && !state.isUsingShield) {
                // Нужно поднять щит
                startUsingShield(bot, server);
                state.isUsingShield = true;
            } else if (!shouldHoldShield && state.isUsingShield) {
                // Нужно опустить щит (за 1 тик до удара)
                stopUsingShield(bot, server);
                state.isUsingShield = false;
            }
        } else {
            // HP нормальное - опускаем щит если он поднят
            if (state.isUsingShield && state.shieldToggleCooldown <= 0) {
                stopUsingShield(bot, server);
                state.isUsingShield = false;
                state.shieldToggleCooldown = 20; // 1 секунда кулдаун
            }
        }
        
        // Атака
        if (distance <= meleeRange && state.attackCooldown <= 0) {
            // Проверяем кулдаун атаки игрока (важно для 1.9+ боя)
            if (bot.getAttackCooldownProgress(0.5f) < 1.0f) {
                // Оружие ещё не готово к атаке - ждём
                return;
            }
            
            // Проверяем нужно ли сбить щит
            if (settings.isShieldBreakEnabled() && target instanceof PlayerEntity player && player.isBlocking()) {
                // Переключаемся на топор для сбития щита
                int axeSlot = findAxe(inventory);
                if (axeSlot >= 0) {
                    // Перемещаем топор в хотбар если нужно
                    if (axeSlot >= 9) {
                        ItemStack axe = inventory.getStack(axeSlot);
                        ItemStack current = inventory.getStack(0);
                        inventory.setStack(axeSlot, current);
                        inventory.setStack(0, axe);
                        axeSlot = 0;
                    }
                    ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(axeSlot);
                    
                    // Атакуем топором чтобы сбить щит
                    attackWithCarpet(bot, target, server);
                    
                    // Отмечаем что щит сбит - бот продолжит драться
                    state.shieldBroken = true;
                    state.shieldBrokenTime = System.currentTimeMillis();
                    
                    // Увеличенный кулдаун если используем щит (более осторожная атака)
                    int cooldown = shouldUseShield ? (int)(settings.getAttackCooldown() * 1.5) : settings.getAttackCooldown();
                    state.attackCooldown = cooldown;
                    
                    // НЕ переключаемся обратно на меч сразу - это произойдёт в следующем тике
                    // когда щит уже будет сбит и бот продолжит атаковать
                    return;
                }
            }
            
            // Обычная атака - экипируем меч/топор (только если текущее оружие не подходит)
            int currentSlot = ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).getSelectedSlot();
            ItemStack currentItem = inventory.getStack(currentSlot);
            double currentScore = getMeleeScore(currentItem.getItem(), settings.isPreferSword());
            
            int weaponSlot = findMeleeWeapon(inventory);
            if (weaponSlot >= 0 && weaponSlot < 9) {
                // Переключаемся только если новое оружие лучше текущего
                ItemStack newWeapon = inventory.getStack(weaponSlot);
                double newScore = getMeleeScore(newWeapon.getItem(), settings.isPreferSword());
                
                if (newScore > currentScore || currentScore == 0) {
                    ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(weaponSlot);
                }
            }
            
            // Критический удар - прыжок перед ударом, но атакуем только когда падаем
            if (settings.isCriticalsEnabled()) {
                if (bot.isOnGround()) {
                    // Прыгаем для крита
                    bot.jump();
                    return; // Не атакуем сразу, ждём пока начнём падать
                } else {
                    // В воздухе - проверяем что мы падаем И прошли достаточно времени после прыжка
                    double velocityY = bot.getVelocity().y;
                    double fallDistance = bot.fallDistance;
                    
                    // Атакуем только если:
                    // 1. Падаем вниз (velocity.y < 0)
                    // 2. Прошли хотя бы небольшое расстояние падения (fallDistance > 0.1)
                    // Это гарантирует что мы уже прошли пик прыжка
                    if (velocityY < 0 && fallDistance > 0.1) {
                        // Падаем - делаем критический удар
                        attackWithCarpet(bot, target, server);
                        // Увеличенный кулдаун если используем щит (более осторожная атака)
                        int cooldown = shouldUseShield ? (int)(settings.getAttackCooldown() * 1.5) : settings.getAttackCooldown();
                        state.attackCooldown = cooldown;
                    }
                    // Иначе просто ждём пока не начнём падать
                }
            } else {
                // Критические удары выключены - атакуем сразу
                attackWithCarpet(bot, target, server);
                // Увеличенный кулдаун если используем щит (более осторожная атака)
                int cooldown = shouldUseShield ? (int)(settings.getAttackCooldown() * 1.5) : settings.getAttackCooldown();
                state.attackCooldown = cooldown;
            }
        } else {
            // Не атакуем - просто держим оружие в руке (только если текущее оружие не подходит)
            int currentSlot = ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).getSelectedSlot();
            ItemStack currentItem = inventory.getStack(currentSlot);
            double currentScore = getMeleeScore(currentItem.getItem(), settings.isPreferSword());
            
            int weaponSlot = findMeleeWeapon(inventory);
            if (weaponSlot >= 0 && weaponSlot < 9) {
                // Переключаемся только если новое оружие лучше текущего
                ItemStack newWeapon = inventory.getStack(weaponSlot);
                double newScore = getMeleeScore(newWeapon.getItem(), settings.isPreferSword());
                
                if (newScore > currentScore || currentScore == 0) {
                    ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(weaponSlot);
                }
            }
        }
    }
    
    /**
     * Дальний бой (лук/арбалет)
     */
    private static void handleRangedCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        var inventory = bot.getInventory();
        
        // Экипируем лук
        int bowSlot = findRangedWeapon(inventory);
        if (bowSlot >= 0 && bowSlot < 9) {
            ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(bowSlot);
        }
        
        // Проверяем есть ли стрелы
        if (!hasArrows(inventory)) {
            // Нет стрел - переключаемся на ближний бой
            state.currentMode = CombatState.WeaponMode.MELEE;
            return;
        }
        
        ItemStack weapon = bot.getMainHandStack();
        boolean isCrossbow = weapon.getItem() instanceof CrossbowItem;
        
        if (isCrossbow) {
            handleCrossbowCombat(bot, target, state, distance, settings);
        } else {
            handleBowCombat(bot, target, state, distance, settings);
        }
        
        // Держим дистанцию с навигацией
        double optimalRange = settings.getRangedOptimalRange();
        if (distance < optimalRange - 5) {
            BotNavigation.moveAway(bot, target, settings.getMoveSpeed());
        } else if (distance > optimalRange + 10) {
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
        }
    }
    
    private static void handleBowCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings) {
        if (!state.isDrawingBow) {
            // Начинаем натягивать лук
            bot.setCurrentHand(Hand.MAIN_HAND);
            state.isDrawingBow = true;
            state.bowDrawTicks = 0;
        } else {
            state.bowDrawTicks++;
            
            // Лук полностью натянут после 20 тиков (1 секунда)
            int minDrawTime = settings.getBowMinDrawTime();
            if (state.bowDrawTicks >= minDrawTime) {
                // Стреляем
                bot.stopUsingItem();
                state.isDrawingBow = false;
                state.bowDrawTicks = 0;
                state.attackCooldown = 5; // Небольшая задержка между выстрелами
            }
        }
    }
    
    private static void handleCrossbowCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings) {
        ItemStack crossbow = bot.getMainHandStack();
        
        if (CrossbowItem.isCharged(crossbow)) {
            // Арбалет заряжен - стреляем через stopUsingItem
            bot.stopUsingItem();
            state.attackCooldown = 5;
            state.isDrawingBow = false;
        } else if (!state.isDrawingBow) {
            // Начинаем заряжать
            bot.setCurrentHand(Hand.MAIN_HAND);
            state.isDrawingBow = true;
            state.bowDrawTicks = 0;
        } else {
            state.bowDrawTicks++;
            // Арбалет заряжается ~25 тиков
            if (state.bowDrawTicks >= 25) {
                bot.stopUsingItem();
                state.isDrawingBow = false;
            }
        }
    }
    
    private static void stopUsingBow(ServerPlayerEntity bot, CombatState state) {
        if (state.isDrawingBow) {
            bot.stopUsingItem();
            state.isDrawingBow = false;
            state.bowDrawTicks = 0;
        }
    }
    
    /**
     * Бой булавой - использует wind charge для высокого прыжка
     */
    private static void handleMaceCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        var inventory = bot.getInventory();
        
        if (state.isDrawingBow) {
            stopUsingBow(bot, state);
        }
        
        // Если в воздухе - экипируем булаву и атакуем при падении
        if (!bot.isOnGround()) {
            int maceSlot = findMace(inventory);
            if (maceSlot >= 0 && maceSlot < 9) {
                ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(maceSlot);
            }
            
            // Атакуем при падении - раньше для максимального урона
            // Атакуем когда начинаем падать (velocity.y < 0) и близко к цели
            double verticalSpeed = bot.getVelocity().y;
            if (verticalSpeed < 0 && distance <= 5.0 && state.attackCooldown <= 0) {
                // Атакуем сразу как начинаем падать
                attackWithCarpet(bot, target, server);
                state.attackCooldown = 5; // Короткий кулдаун для повторной атаки
            }
            return;
        }
        
        // На земле - используем wind charge для прыжка
        if (bot.isOnGround() && distance <= settings.getMaceRange()) {
            // Ищем wind charge
            int windChargeSlot = findWindCharge(inventory);
            
            if (windChargeSlot >= 0) {
                // Используем wind charge через BotUtils
                BotUtils.useWindCharge(bot, server);
                bot.jump();
            } else {
                // Нет wind charge - обычный прыжок с булавой
                int maceSlot = findMace(inventory);
                if (maceSlot >= 0 && maceSlot < 9) {
                    ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(maceSlot);
                }
                
                bot.jump();
                double dx = target.getX() - bot.getX();
                double dz = target.getZ() - bot.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) {
                    dx /= dist;
                    dz /= dist;
                }
                bot.addVelocity(dx * 0.3, 0.3, dz * 0.3);
            }
        }
        
        // Атакуем на земле если близко
        if (bot.isOnGround() && distance <= 3.5 && state.attackCooldown <= 0) {
            int maceSlot = findMace(inventory);
            if (maceSlot >= 0 && maceSlot < 9) {
                ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(maceSlot);
            }
            attackWithCarpet(bot, target, server);
            state.attackCooldown = settings.getAttackCooldown();
        }
        
        // Движение к цели с навигацией
        if (distance > settings.getMaceRange()) {
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
        }
    }
    
    /**
     * Бой копьём (Spear) - 1.21.11
     * Два режима атаки:
     * - Удар с разбега (charge): держать ПКМ и врезаться в цель - урон наносится при столкновении
     * - Укол (jab): обычная атака ЛКМ (требует 100% заряда между уколами)
     * 
     * ВАЖНО: Нельзя делать обычную атаку пока charge активен - это сбросит его!
     * Удар с разбега наносит урон автоматически при столкновении с целью.
     */
    private static void handleSpearCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        var inventory = bot.getInventory();
        
        // Прекращаем натягивать лук если натягивали
        if (state.isDrawingBow) {
            stopUsingBow(bot, state);
        }
        
        // Экипируем копьё
        int spearSlot = findSpear(inventory);
        if (spearSlot >= 0 && spearSlot < 9) {
            ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(spearSlot);
        }
        
        double chargeStartDistance = 10.0; // Начинаем charge за 5 блоков
        double chargeHitDistance = 0.1;   // Дистанция столкновения для charge (вплотную)
        
        // Логика боя копьём:
        // 1. Далеко (> 5 блоков) - бежим к врагу БЕЗ charge
        // 2. За 5 блоков - начинаем charge (держим ПКМ) и бежим к врагу
        // 3. При столкновении (< 1.5 блока) - урон наносится автоматически, отпускаем charge
        // 4. После charge можно сразу делать jab, и наоборот
        
        if (distance > chargeStartDistance) {
            // Далеко - бежим к врагу БЕЗ charge
            if (state.isChargingSpear) {
                bot.stopUsingItem();
                state.isChargingSpear = false;
                state.spearChargeTicks = 0;
            }
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
            
        } else if (distance > chargeHitDistance) {
            // Средняя дистанция - charge атака (держим ПКМ и бежим)
            if (!state.isChargingSpear) {
                // Начинаем charge - выставляем копьё вперёд
                bot.setCurrentHand(Hand.MAIN_HAND);
                state.isChargingSpear = true;
                state.spearChargeTicks = 0;
            }
            
            state.spearChargeTicks++;
            
            // Бежим к врагу с charge - урон нанесётся при столкновении
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed() * 1.3);
            
            // Проверяем стадии charge (усталость после ~40 тиков, разрядка после ~60)
            if (state.spearChargeTicks > 60) {
                // Стадия разрядки - лучше отпустить и начать заново
                bot.stopUsingItem();
                state.isChargingSpear = false;
                state.spearChargeTicks = 0;
            }
            
        } else {
            // Очень близко (столкновение) - урон от charge уже нанесён
            if (state.isChargingSpear) {
                // Отпускаем charge после столкновения
                bot.stopUsingItem();
                state.isChargingSpear = false;
                state.spearChargeTicks = 0;
                // После charge можно сразу делать jab
                state.attackCooldown = 0;
            }
            
            // Отходим назад чтобы снова разбежаться для charge
            BotNavigation.moveAway(bot, target, settings.getMoveSpeed());
        }
    }

    
    private static final java.util.Random random = new java.util.Random();
    
    /**
     * Атака цели
     */
    private static void attack(ServerPlayerEntity bot, Entity target) {
        BotSettings settings = BotSettings.get();
        
        // Шанс промаха
        if (random.nextInt(100) < settings.getMissChance()) {
            // Промах - просто машем рукой
            bot.swingHand(Hand.MAIN_HAND);
            return;
        }
        
        bot.attack(target);
        bot.swingHand(Hand.MAIN_HAND);
    }
    
    /**
     * Атака через команду Carpet (более надёжно)
     */
    private static void attackWithCarpet(ServerPlayerEntity bot, Entity target, net.minecraft.server.MinecraftServer server) {
        BotSettings settings = BotSettings.get();
        
        // Проверка friendlyfire - не атакуем союзников
        if (!settings.isFriendlyFireEnabled() && target instanceof PlayerEntity) {
            String botName = bot.getName().getString();
            String targetName = target.getName().getString();
            if (BotFaction.areAllies(botName, targetName)) {
                // Союзник - не атакуем, просто машем рукой
                bot.swingHand(Hand.MAIN_HAND);
                return;
            }
        }
        
        // Шанс промаха
        if (random.nextInt(100) < settings.getMissChance()) {
            // Промах - просто машем рукой
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " swinghand", 
                    server.getCommandSource()
                );
            } catch (Exception e) {
                bot.swingHand(Hand.MAIN_HAND);
            }
            return;
        }
        
        // Шанс ошибки - атакуем не туда
        if (random.nextInt(100) < settings.getMistakeChance()) {
            // Поворачиваемся немного в сторону
            float yawOffset = (random.nextFloat() - 0.5f) * 60; // ±30 градусов
            bot.setYaw(bot.getYaw() + yawOffset);
        }
        
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " attack once", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            bot.swingHand(Hand.MAIN_HAND);
        }
    }
    
    /**
     * Начать использование щита через Carpet команду
     */
    private static void startUsingShield(ServerPlayerEntity bot, net.minecraft.server.MinecraftServer server) {
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use continuous", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            // Fallback - используем обычный способ
            bot.setCurrentHand(Hand.OFF_HAND);
        }
    }
    
    /**
     * Прекратить использование щита через Carpet команду
     */
    private static void stopUsingShield(ServerPlayerEntity bot, net.minecraft.server.MinecraftServer server) {
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " stop", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            // Fallback - используем обычный способ
            bot.clearActiveItem();
        }
    }
    
    /**
     * Поворот к цели
     */
    private static void lookAtTarget(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetPos.x - botPos.x;
        double dy = targetPos.y - botPos.y;
        double dz = targetPos.z - botPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(MathHelper.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Поворот ОТ цели (для убегания)
     */
    private static void lookAwayFromTarget(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        // Направление ОТ цели (противоположное)
        double dx = botPos.x - targetPos.x;
        double dz = botPos.z - targetPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        
        bot.setYaw(yaw);
        bot.setPitch(0); // Смотрим прямо
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Движение к цели
     */
    private static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        double botX = bot.getX(), botY = bot.getY(), botZ = bot.getZ();
        double targetX = target.getX(), targetY = target.getY(), targetZ = target.getZ();
        double dx = targetX - botX;
        double dz = targetZ - botZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0) {
            dx /= dist;
            dz /= dist;
        }
        
        bot.setSprinting(true);
        bot.forwardSpeed = (float) speed;
        bot.sidewaysSpeed = 0;
        
        // Добавляем импульс движения
        if (bot.isOnGround()) {
            bot.addVelocity(dx * speed * 0.1, 0, dz * speed * 0.1);
        }
    }
    
    /**
     * Движение от цели (убегание)
     */
    private static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        double botX = bot.getX(), botY = bot.getY(), botZ = bot.getZ();
        double targetX = target.getX(), targetY = target.getY(), targetZ = target.getZ();
        double dx = botX - targetX;
        double dz = botZ - targetZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0) {
            dx /= dist;
            dz /= dist;
        }
        
        // Бежим ВПЕРЁД (в направлении от врага)
        bot.setSprinting(true);
        bot.forwardSpeed = (float) speed;
        
        if (bot.isOnGround()) {
            bot.addVelocity(dx * speed * 0.1, 0, dz * speed * 0.1);
        }
    }
    
    // ============ Поиск оружия в инвентаре ============
    
    private static int findMeleeWeapon(net.minecraft.entity.player.PlayerInventory inventory) {
        BotSettings settings = BotSettings.get();
        boolean preferSword = settings.isPreferSword();
        
        int bestSlot = -1;
        double bestScore = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            double score = getMeleeScore(item, preferSword);
            
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
    
    /**
     * Поиск топора для сбития щита
     */
    private static int findAxe(net.minecraft.entity.player.PlayerInventory inventory) {
        int bestSlot = -1;
        double bestDamage = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            if (item instanceof AxeItem) {
                double damage = getAxeDamage(item);
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }
        
        return bestSlot;
    }
    
    /**
     * Поиск щита в инвентаре
     */
    private static int findShield(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            // Проверяем что это щит
            if (stack.getItem().toString().contains("shield")) {
                return i;
            }
        }
        return -1;
    }
    
    private static double getAxeDamage(Item item) {
        if (item == Items.NETHERITE_AXE) return 10;
        if (item == Items.DIAMOND_AXE) return 9;
        if (item == Items.IRON_AXE) return 9;
        if (item == Items.STONE_AXE) return 9;
        if (item == Items.GOLDEN_AXE) return 7;
        if (item == Items.WOODEN_AXE) return 7;
        return 0;
    }
    
    /**
     * Получить "очки" оружия с учётом preferSword
     * Если preferSword = true, мечи получают бонус +5 к очкам
     */
    private static double getMeleeScore(Item item, boolean preferSword) {
        double baseDamage = getMeleeDamage(item);
        if (baseDamage == 0) return 0;
        
        // Если предпочитаем меч - даём мечам бонус
        if (preferSword && isSword(item)) {
            return baseDamage + 5; // Меч всегда будет выбран если есть
        }
        
        return baseDamage;
    }
    
    /**
     * Проверяет, является ли предмет мечом
     */
    private static boolean isSword(Item item) {
        return item == Items.NETHERITE_SWORD || 
               item == Items.DIAMOND_SWORD || 
               item == Items.IRON_SWORD || 
               item == Items.GOLDEN_SWORD || 
               item == Items.STONE_SWORD || 
               item == Items.WOODEN_SWORD;
    }
    
    private static double getMeleeDamage(Item item) {
        if (item == Items.NETHERITE_SWORD) return 8;
        if (item == Items.NETHERITE_AXE) return 10;
        if (item == Items.DIAMOND_SWORD) return 7;
        if (item == Items.DIAMOND_AXE) return 9;
        if (item == Items.IRON_SWORD) return 6;
        if (item == Items.IRON_AXE) return 9;
        if (item == Items.STONE_SWORD) return 5;
        if (item == Items.STONE_AXE) return 9;
        if (item == Items.GOLDEN_SWORD) return 4;
        if (item == Items.GOLDEN_AXE) return 7;
        if (item == Items.WOODEN_SWORD) return 4;
        if (item == Items.WOODEN_AXE) return 7;
        if (item == Items.TRIDENT) return 9;
        return 0;
    }
    
    private static int findRangedWeapon(net.minecraft.entity.player.PlayerInventory inventory) {
        // Приоритет: арбалет > лук
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof CrossbowItem) return i;
        }
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof BowItem) return i;
        }
        return -1;
    }
    
    private static int findMace(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.MACE) return i;
        }
        return -1;
    }
    
    private static int findWindCharge(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.WIND_CHARGE) return i;
        }
        return -1;
    }
    
    /**
     * Поиск копья (Spear) в инвентаре - 1.21.11
     * Копьё - новое оружие с charge атакой
     */
    private static int findSpear(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            // Проверяем по имени предмета, так как Items.SPEAR может не существовать в текущей версии
            String itemName = stack.getItem().toString().toLowerCase();
            if (itemName.contains("spear")) return i;
        }
        return -1;
    }
    
    /**
     * Поиск паутины в инвентаре
     */
    private static int findCobweb(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.COBWEB) return i;
        }
        return -1;
    }
    
    /**
     * Начинает процесс размещения паутины под врага
     * Бот берёт паутину в руку, смотрит на врага и кликает ПКМ
     */
    private static boolean tryPlaceCobweb(ServerPlayerEntity bot, Entity target, net.minecraft.server.MinecraftServer server) {
        CombatState state = getState(bot.getName().getString());
        
        // Уже размещаем паутину
        if (state.isPlacingCobweb) return false;
        
        var inventory = bot.getInventory();
        int cobwebSlot = findCobweb(inventory);
        if (cobwebSlot < 0) return false;
        
        // Проверяем что враг не в паутине уже
        var world = bot.getEntityWorld();
        net.minecraft.util.math.BlockPos targetPos = target.getBlockPos();
        if (world.getBlockState(targetPos).getBlock() == net.minecraft.block.Blocks.COBWEB) {
            return false; // Уже в паутине
        }
        
        // Перемещаем паутину в хотбар если нужно
        if (cobwebSlot >= 9) {
            ItemStack cobweb = inventory.getStack(cobwebSlot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(cobwebSlot, current);
            inventory.setStack(0, cobweb);
            cobwebSlot = 0;
        }
        
        // Переключаем на паутину
        ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(cobwebSlot);
        
        // Начинаем процесс размещения
        state.isPlacingCobweb = true;
        state.cobwebPlaceTicks = 0;
        
        return true;
    }
    
    /**
     * Обработка процесса размещения паутины
     * Вызывается каждый тик когда isPlacingCobweb = true
     */
    private static void handleCobwebPlacement(ServerPlayerEntity bot, Entity target, CombatState state, net.minecraft.server.MinecraftServer server) {
        state.cobwebPlaceTicks++;
        
        // Смотрим на позицию врага (под ноги)
        Vec3d targetFeet = new Vec3d(target.getX(), target.getY() - 0.5, target.getZ());
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetFeet.x - botPos.x;
        double dy = targetFeet.y - botPos.y;
        double dz = targetFeet.z - botPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
        
        // Кликаем ПКМ несколько раз для надёжности
        if (state.cobwebPlaceTicks % 2 == 0 && state.cobwebPlaceTicks <= 6) {
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
            } catch (Exception e) {
                // Игнорируем
            }
        }
        
        // Заканчиваем через 8 тиков
        if (state.cobwebPlaceTicks >= 8) {
            state.isPlacingCobweb = false;
            state.cobwebPlaceTicks = 0;
            state.cobwebCooldown = 20; // 1 секунда кулдаун
        }
    }
    
    private static boolean hasArrows(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof ArrowItem) return true;
        }
        return false;
    }
    
    // ============ Публичные методы для команд ============
    
    /**
     * Установить принудительную цель
     */
    public static void setTarget(String botName, String targetName) {
        CombatState state = getState(botName);
        state.forcedTargetName = targetName;
    }
    
    /**
     * Сбросить цель (полностью останавливает бой)
     */
    public static void clearTarget(String botName) {
        CombatState state = getState(botName);
        state.forcedTargetName = null;
        state.target = null;
        state.lastAttacker = null; // Сбрасываем revenge
        state.lastAttackTime = 0;
        state.isRetreating = false;
    }
    
    /**
     * Вызывается когда бота атакуют
     */
    public static void onBotDamaged(ServerPlayerEntity bot, DamageSource source) {
        // Пробуем получить атакующего разными способами
        Entity attacker = source.getAttacker();
        if (attacker == null) {
            attacker = source.getSource();
        }
        if (attacker == null || attacker == bot) return;
        
        // Не реагируем на урон от себя или от окружения
        if (!(attacker instanceof LivingEntity)) return;
        
        CombatState state = getState(bot.getName().getString());
        state.lastAttacker = attacker;
        state.lastAttackTime = System.currentTimeMillis();
    }
    
    /**
     * Получить текущую цель бота
     */
    public static Entity getTarget(String botName) {
        return getState(botName).target;
    }
}
