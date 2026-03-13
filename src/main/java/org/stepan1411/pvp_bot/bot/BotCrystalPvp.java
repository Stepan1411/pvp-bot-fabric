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


public class BotCrystalPvp {
    

    private static class CrystalState {
        int step = 0;
        BlockPos lastObsidianPos = null;
        long lastActionTime = 0;
        int cooldownTicks = 0;
        int stuckCounter = 0;
        int lastStep = -1;
        int crystalNotFoundCounter = 0;
        int crystalPlaceFailCounter = 0;
        java.util.Set<BlockPos> triedPositions = new java.util.HashSet<>();
        int obsidianPlaceAttempts = 0;
        
        // Новые поля для атак мечом
        boolean swordAttackDone = false; // Была ли атака мечом на текущем шаге
        int swordAttackCooldown = 0; // Кулдаун между атаками мечом
    }
    
    private static final java.util.Map<String, CrystalState> states = new java.util.HashMap<>();
    
    
    private static CrystalState getState(String botName) {
        return states.computeIfAbsent(botName, k -> new CrystalState());
    }
    
    
    public static boolean canUseCrystalPvp(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        if (!settings.isCrystalPvpEnabled()) return false;
        
        double distance = bot.distanceTo(target);
        if (distance < 2.5 || distance > 8.0) return false;
        
        PlayerInventory inventory = bot.getInventory();
        return hasObsidian(inventory) && hasEndCrystal(inventory);
    }
    
    
    public static boolean doCrystalPvp(ServerPlayerEntity bot, Entity target, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        CrystalState state = getState(bot.getName().getString());
        World world = bot.getEntityWorld();
        double distance = bot.distanceTo(target);
        

        if (state.step == state.lastStep) {
            state.stuckCounter++;
            if (state.stuckCounter > 100) {
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK on step " + state.step + "! Resetting state.");
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 0;
                state.stuckCounter = 0;
                state.swordAttackDone = false; // Сброс атаки мечом
                return true;
            }
        } else {
            state.stuckCounter = 0;
            state.lastStep = state.step;
        }
        
        

        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;

            maintainDistance(bot, target, settings);
            return true;
        }
        
        // Обрабатываем кулдаун атак мечом
        if (state.swordAttackCooldown > 0) {
            state.swordAttackCooldown--;
        }
        

        if (distance > 8.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            state.step = 0;
            state.lastObsidianPos = null;
            return true;
        }
        

        if (distance < 2.5) {
            moveAway(bot, target, settings.getMoveSpeed());
            return true;
        }
        


        Entity nearCrystal = findNearestEndCrystal(bot, target, 6.0);
        if (nearCrystal != null && state.step != 2 && state.cooldownTicks == 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " РѕР±РЅР°СЂСѓР¶РёР» РєСЂРёСЃС‚Р°Р»Р», РїРµСЂРµС…РѕРґ Рє С€Р°РіСѓ 2 (С‚РµРєСѓС‰РёР№ step: " + state.step + ")");
            state.step = 2;
            state.cooldownTicks = 0;
        }
        

        switch (state.step) {
            case 0:
                return stepPlaceObsidian(bot, target, state, server, world, settings);
                
            case 1:
                return stepPlaceCrystal(bot, target, state, server, world, settings);
                
            case 2:
                return stepAttackCrystal(bot, target, state, server, world, settings, distance);
                
            default:
                state.step = 0;
                return true;
        }
    }
    
    
    private static boolean stepPlaceObsidian(ServerPlayerEntity bot, Entity target, CrystalState state, 
                                            net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // НОВОЕ: Атака мечом в начале шага 0
        if (!state.swordAttackDone && state.swordAttackCooldown <= 0) {
            double distance = bot.distanceTo(target);
            if (distance <= settings.getMeleeRange() + 1.0) { // Немного больший радиус для атаки
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " Step 0: Attacking player with sword first");
                boolean attacked = attackPlayerWithSword(bot, target, state, server, settings);
                if (attacked) {
                    state.swordAttackDone = true;
                    state.swordAttackCooldown = 10; // Кулдаун между атаками мечом
                    return true;
                }
            }
        }
        

        BlockPos existingObsidian = findExistingObsidian(bot, target, world, 5.0);
        if (existingObsidian != null) {
            double distToExisting = Math.sqrt(bot.squaredDistanceTo(existingObsidian.getX() + 0.5, existingObsidian.getY() + 0.5, existingObsidian.getZ() + 0.5));
            
            if (distToExisting <= 4.0) {

                System.out.println("[Crystal PVP] " + bot.getName().getString() + " using existing obsidian at " + existingObsidian);
                state.lastObsidianPos = existingObsidian;
                state.step = 1;
                state.cooldownTicks = 0;
                state.stuckCounter = 0;
                state.swordAttackDone = false; // Сброс для нового шага
                return true;
            } else {

                System.out.println("[Crystal PVP] " + bot.getName().getString() + " approaching existing obsidian, distance: " + String.format("%.2f", distToExisting));
                moveToward(bot, target, settings.getMoveSpeed());
                return true;
            }
        }
        

        

        int obsidianSlot = findObsidian(inventory);
        if (obsidianSlot < 0) {
            return false;
        }
        

        BlockPos obsidianPos = findBestObsidianPosition(bot, target, world, state.triedPositions);
        if (obsidianPos == null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " could not find obsidian position!");

            if (!state.triedPositions.isEmpty()) {
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " clearing tried positions (" + state.triedPositions.size() + " positions)");
                state.triedPositions.clear();
            }

            maintainDistance(bot, target, settings);
            return true;
        }
        

        double distToPos = Math.sqrt(bot.squaredDistanceTo(obsidianPos.getX() + 0.5, obsidianPos.getY() + 0.5, obsidianPos.getZ() + 0.5));
        
        if (distToPos > 3.0) {
            moveToward(bot, target, settings.getMoveSpeed());
            return true;
        }
        

        bot.setVelocity(0, bot.getVelocity().y, 0);
        

        if (!selectItem(bot, obsidianSlot)) {
            return true;
        }
        

        lookAt(bot, obsidianPos);
        

        state.obsidianPlaceAttempts++;
        

        if (state.obsidianPlaceAttempts >= 3) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " failed to place obsidian 3 times at " + obsidianPos + ", trying different position");
            state.triedPositions.add(obsidianPos);
            state.obsidianPlaceAttempts = 0;
            state.cooldownTicks = 3;
            return true;
        }
        

        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " placed obsidian at " + obsidianPos + " (attempt " + state.obsidianPlaceAttempts + "/3)");
            

            state.lastObsidianPos = obsidianPos;
            state.step = 1;
            state.cooldownTicks = 5;
            state.stuckCounter = 0;
            state.obsidianPlaceAttempts = 0;
            state.swordAttackDone = false; // Сброс для нового шага
            
        } catch (Exception e) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " error placing obsidian: " + e.getMessage());

        }
        

        return true;
    }
    
    
    private static boolean stepPlaceCrystal(ServerPlayerEntity bot, Entity target, CrystalState state,
                                           net.minecraft.server.MinecraftServer server, World world, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        
        // НОВОЕ: Атака мечом в начале шага 1
        if (!state.swordAttackDone && state.swordAttackCooldown <= 0) {
            double distance = bot.distanceTo(target);
            if (distance <= settings.getMeleeRange() + 1.0) { // Немного больший радиус для атаки
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " Step 1: Attacking player with sword first");
                boolean attacked = attackPlayerWithSword(bot, target, state, server, settings);
                if (attacked) {
                    state.swordAttackDone = true;
                    state.swordAttackCooldown = 10; // Кулдаун между атаками мечом
                    return true;
                }
            }
        }
        

        if (state.lastObsidianPos == null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " no obsidian position!");
            state.step = 0;
            return true;
        }
        

        net.minecraft.block.BlockState blockAtPos = world.getBlockState(state.lastObsidianPos);
        
        if (!blockAtPos.getBlock().toString().contains("obsidian")) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " NO obsidian at position! Returning to step 0");

            if (state.lastObsidianPos != null) {
                state.triedPositions.add(state.lastObsidianPos);
            }
            state.step = 0;
            state.lastObsidianPos = null;
            state.obsidianPlaceAttempts = 0;
            return true;
        }
        

        net.minecraft.block.BlockState blockAbove = world.getBlockState(state.lastObsidianPos.up());
        
        if (!blockAbove.isAir() && !blockAbove.isReplaceable()) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " NO space above obsidian! Returning to step 0");
            state.step = 0;
            state.lastObsidianPos = null;
            return true;
        }
        

        int crystalSlot = findEndCrystal(inventory);
        if (crystalSlot < 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " no crystals in inventory! Exiting Crystal PVP.");
            state.step = 0;
            state.lastObsidianPos = null;
            state.stuckCounter = 0;
            return false;
        }
        

        bot.setVelocity(0, bot.getVelocity().y, 0);
        

        if (!selectItem(bot, crystalSlot)) {
            return true;
        }
        


        lookAt(bot, state.lastObsidianPos);
        

        if (state.crystalPlaceFailCounter >= 5) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK - crystal placement failed 5 times! Resetting state.");
            state.step = 0;
            state.lastObsidianPos = null;
            state.cooldownTicks = 5;
            state.crystalPlaceFailCounter = 0;
            return true;
        }
        

        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use once", 
                server.getCommandSource()
            );
            
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " placed crystal (clicked obsidian at " + state.lastObsidianPos + ") - attempt " + (state.crystalPlaceFailCounter + 1) + "/5");
            

            state.crystalPlaceFailCounter++;
            

            state.step = 2;
            state.cooldownTicks = 5;
            state.stuckCounter = 0;
            state.swordAttackDone = false; // Сброс для нового шага
            
        } catch (Exception e) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " error placing crystal: " + e.getMessage());

            state.step = 0;
            state.lastObsidianPos = null;
            state.crystalPlaceFailCounter = 0;
        }
        

        return true;
    }
    
    
    private static boolean stepAttackCrystal(ServerPlayerEntity bot, Entity target, CrystalState state,
                                            net.minecraft.server.MinecraftServer server, World world, 
                                            BotSettings settings, double distance) {
        PlayerInventory inventory = bot.getInventory();
        

        bot.setVelocity(0, bot.getVelocity().y, 0);
        

        int weaponSlot = findMeleeWeapon(inventory);
        if (weaponSlot >= 0) {
            selectItem(bot, weaponSlot);
        }
        

        Entity crystal = findNearestEndCrystal(bot, target, 6.0);
        
        if (crystal != null) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " found crystal at distance " + bot.distanceTo(crystal));
            

            lookAtEntity(bot, crystal);
            

            state.crystalNotFoundCounter = 0;

            state.crystalPlaceFailCounter = 0;
            

            bot.attack(crystal);
            bot.swingHand(Hand.MAIN_HAND);
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " hit crystal!");
            

            if (distance <= 4.5 && state.lastObsidianPos != null) {

                state.step = 1;
                state.cooldownTicks = 2;
                state.swordAttackDone = false; // Сброс для нового шага
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " moving to step 1 (new crystal)");
            } else {

                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 5;
                state.swordAttackDone = false; // Сброс для нового шага
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " moving to step 0 (new obsidian)");
            }
            
        } else {
            state.crystalNotFoundCounter++;
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " DID NOT FIND crystal! (" + state.crystalNotFoundCounter + "/3)");
            

            if (state.crystalNotFoundCounter >= 3) {
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " STUCK - crystal not found 3 times! Resetting state.");
                state.step = 0;
                state.lastObsidianPos = null;
                state.cooldownTicks = 5;
                state.crystalNotFoundCounter = 0;
                return true;
            }
            

            if (state.lastObsidianPos != null) {
                state.step = 1;
                state.swordAttackDone = false; // Сброс для нового шага
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " returning to step 1 (place crystal)");
            } else {
                state.step = 0;
                state.swordAttackDone = false; // Сброс для нового шага
                System.out.println("[Crystal PVP] " + bot.getName().getString() + " returning to step 0 (place obsidian)");
            }
            state.cooldownTicks = 3;
        }
        


        
        return true;
    }
    
    
    private static void maintainDistance(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        double distance = bot.distanceTo(target);
        
        if (distance < 3.0) {

            moveAway(bot, target, settings.getMoveSpeed() * 0.7);
        } else if (distance > 5.5) {

            moveToward(bot, target, settings.getMoveSpeed() * 0.7);
        } else {

            lookAtEntity(bot, target);
        }
    }
    
    
    private static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = targetPos.subtract(botPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    
    private static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d direction = botPos.subtract(targetPos).normalize();
        
        bot.setVelocity(direction.x * speed, bot.getVelocity().y, direction.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    
    private static void strafe(ServerPlayerEntity bot, Entity target, double speed) {
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        Vec3d toTarget = targetPos.subtract(botPos).normalize();
        

        Vec3d strafeDir = new Vec3d(-toTarget.z, 0, toTarget.x);
        
        bot.setVelocity(strafeDir.x * speed, bot.getVelocity().y, strafeDir.z * speed);
        bot.velocityDirty = true;
        
        lookAtEntity(bot, target);
    }
    
    
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
    
    
    private static boolean selectItem(ServerPlayerEntity bot, int slot) {
        PlayerInventory inventory = bot.getInventory();
        

        if (slot >= 9) {
            ItemStack item = inventory.getStack(slot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(slot, current);
            inventory.setStack(0, item);
            slot = 0;
        }
        

        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, slot);
        return true;
    }
    
    
    private static BlockPos findExistingObsidian(ServerPlayerEntity bot, Entity target, World world, double maxDistance) {
        BlockPos targetPos = target.getBlockPos();
        

        int radius = (int) Math.ceil(maxDistance);
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = targetPos.add(dx, dy, dz);
                    

                    double distFromTarget = Math.sqrt(target.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (distFromTarget > maxDistance) continue;
                    

                    net.minecraft.block.BlockState blockState = world.getBlockState(pos);
                    if (!blockState.getBlock().toString().contains("obsidian")) continue;
                    

                    net.minecraft.block.BlockState blockAbove = world.getBlockState(pos.up());
                    if (!blockAbove.isAir() && !blockAbove.isReplaceable()) continue;
                    

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
    
    
    private static BlockPos findBestObsidianPosition(ServerPlayerEntity bot, Entity target, World world, java.util.Set<BlockPos> triedPositions) {
        BlockPos targetPos = target.getBlockPos();
        

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

            if (triedPositions.contains(pos)) {
                continue;
            }
            

            if (!world.getBlockState(pos).isAir() && !world.getBlockState(pos).isReplaceable()) {
                continue;
            }
            

            if (!world.getBlockState(pos.up()).isAir() && !world.getBlockState(pos.up()).isReplaceable()) {
                continue;
            }
            

            double dist = Math.sqrt(bot.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            
            if (dist <= 4.0) {
                System.out.println("[Crystal PVP] Found suitable position: " + pos);
                return pos;
            }
        }
        
        System.out.println("[Crystal PVP] No suitable positions found!");
        return null;
    }
    
    
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
                System.out.println("[Crystal PVP] Р­С‚Рѕ РєСЂРёСЃС‚Р°Р»Р»! Р”РёСЃС‚Р°РЅС†РёСЏ from target: " + String.format("%.2f", dist));
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestCrystal = entity;
                }
            }
        }
        
        if (crystalCount > 0) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " found " + crystalCount + " crystals in radius " + maxDistance);
        } else {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " РќР• found РЅРё РѕРґРЅРѕРіРѕ РєСЂРёСЃС‚Р°Р»Р»Р°!");
        }
        
        return nearestCrystal;
    }
    
    
    private static int findObsidian(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.OBSIDIAN) return i;
        }
        return -1;
    }
    
    
    private static int findEndCrystal(PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.END_CRYSTAL) return i;
        }
        return -1;
    }
    
    
    private static int findMeleeWeapon(PlayerInventory inventory) {

        int bestSlot = -1;
        int bestPriority = -1;
        

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
    
    
    private static boolean hasObsidian(PlayerInventory inventory) {
        return findObsidian(inventory) >= 0;
    }
    
    
    private static boolean hasEndCrystal(PlayerInventory inventory) {
        return findEndCrystal(inventory) >= 0;
    }
    
    /**
     * Атака игрока мечом в Crystal PvP
     */
    private static boolean attackPlayerWithSword(ServerPlayerEntity bot, Entity target, CrystalState state, 
                                                net.minecraft.server.MinecraftServer server, BotSettings settings) {
        PlayerInventory inventory = bot.getInventory();
        double distance = bot.distanceTo(target);
        
        // Проверяем дистанцию для атаки мечом
        if (distance > settings.getMeleeRange()) {
            // Подходим ближе
            moveToward(bot, target, settings.getMoveSpeed());
            return true;
        }
        
        // Находим меч
        int weaponSlot = findMeleeWeapon(inventory);
        if (weaponSlot >= 0) {
            selectItem(bot, weaponSlot);
        }
        
        // Смотрим на цель
        lookAtEntity(bot, target);
        
        // Проверяем кулдаун атаки
        if (bot.getAttackCooldownProgress(0.5f) < 1.0f) {
            return true; // Ждем кулдаун
        }
        
        // Атакуем мечом
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " attack once", 
                server.getCommandSource()
            );
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " attacked player with sword!");
            return true;
        } catch (Exception e) {
            System.out.println("[Crystal PVP] " + bot.getName().getString() + " error attacking with sword: " + e.getMessage());
            bot.swingHand(Hand.MAIN_HAND);
            return true;
        }
    }
    
    
    public static void reset(String botName) {
        CrystalState state = states.get(botName);
        if (state != null) {
            state.step = 0;
            state.lastObsidianPos = null;
            state.cooldownTicks = 0;
            state.stuckCounter = 0;
            state.lastStep = -1;
            state.crystalNotFoundCounter = 0;
            state.crystalPlaceFailCounter = 0;
            state.triedPositions.clear();
            state.obsidianPlaceAttempts = 0;
            state.swordAttackDone = false;
            state.swordAttackCooldown = 0;
        }
    }
}
