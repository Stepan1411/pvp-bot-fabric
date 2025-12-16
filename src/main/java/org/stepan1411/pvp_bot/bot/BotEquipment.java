package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class BotEquipment {

    public static void autoEquip(ServerPlayerEntity bot) {
        // НЕ экипируем оружие если бот ест!
        var utilsState = BotUtils.getState(bot.getName().getString());
        if (utilsState.isEating) {
            return;
        }
        
        BotSettings settings = BotSettings.get();
        
        if (settings.isAutoEquipArmor()) {
            equipBestArmor(bot);
        }
        if (settings.isAutoEquipWeapon()) {
            equipBestWeapon(bot);
        }
    }

    private static void equipBestArmor(ServerPlayerEntity bot) {
        equipBestForSlot(bot, EquipmentSlot.HEAD);
        equipBestForSlot(bot, EquipmentSlot.CHEST);
        equipBestForSlot(bot, EquipmentSlot.LEGS);
        equipBestForSlot(bot, EquipmentSlot.FEET);
    }

    private static void equipBestForSlot(ServerPlayerEntity bot, EquipmentSlot slot) {
        BotSettings settings = BotSettings.get();
        var inventory = bot.getInventory();
        
        // Шаг 1: Найти лучшую броню для этого слота (включая текущую экипированную)
        ItemStack currentArmor = bot.getEquippedStack(slot);
        double currentValue = getArmorValue(currentArmor, slot);
        
        int bestInvSlot = -1;
        double bestValue = currentValue;
        
        // Ищем лучшую броню в инвентаре
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            if (!isArmorForSlot(stack.getItem(), slot)) continue;
            
            double value = getArmorValue(stack, slot);
            if (value > bestValue) {
                bestValue = value;
                bestInvSlot = i;
            }
        }
        
        // Шаг 2: Экипируем лучшую броню если она в инвентаре
        if (bestInvSlot >= 0) {
            ItemStack newArmor = inventory.getStack(bestInvSlot).copy();
            ItemStack oldArmor = currentArmor.copy();
            
            // Надеваем новую броню
            bot.equipStack(slot, newArmor);
            inventory.setStack(bestInvSlot, ItemStack.EMPTY);
            
            // Старую броню кладём в инвентарь (если была)
            if (!oldArmor.isEmpty()) {
                inventory.setStack(bestInvSlot, oldArmor);
            }
            
            currentArmor = newArmor;
            currentValue = bestValue;
        }

        
        // Шаг 3: Выбрасываем худшую броню (только если включено)
        if (settings.isDropWorseArmor()) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (stack.isEmpty()) continue;
                if (!isArmorForSlot(stack.getItem(), slot)) continue;
                
                double value = getArmorValue(stack, slot);
                
                // Выбрасываем только если хуже текущей экипированной
                if (value < currentValue) {
                    dropItemBackward(bot, stack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * Выбрасывает предмет назад или вбок от бота (как нажатие Q с поворотом)
     */
    private static void dropItemBackward(ServerPlayerEntity bot, ItemStack stack) {
        if (stack.isEmpty()) return;
        
        BotSettings settings = BotSettings.get();
        double distance = settings.getDropDistance();
        
        // Сохраняем текущий угол
        float oldYaw = bot.getYaw();
        float oldHeadYaw = bot.getHeadYaw();
        
        // Поворачиваем назад или вбок (90-270 градусов от текущего направления)
        float turnAngle = 90 + (float)(Math.random() * 180); // от 90 до 270 градусов
        float newYaw = oldYaw + turnAngle;
        
        bot.setYaw(newYaw);
        bot.setHeadYaw(newYaw);
        
        // Выбрасываем как при нажатии Q (throwRandomly=false, retainOwnership=true)
        ItemEntity dropped = bot.dropItem(stack.copy(), false, true);
        
        if (dropped != null) {
            // Скорость в направлении взгляда
            double yawRad = Math.toRadians(newYaw);
            double speed = 0.3 * distance;
            dropped.setVelocity(
                -Math.sin(yawRad) * speed,
                0.2,
                Math.cos(yawRad) * speed
            );
            dropped.setPickupDelay(60); // 3 секунды задержки
        }
        
        // Возвращаем угол обратно
        bot.setYaw(oldYaw);
        bot.setHeadYaw(oldHeadYaw);
    }

    private static boolean isArmorForSlot(Item item, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> item == Items.NETHERITE_HELMET || item == Items.DIAMOND_HELMET ||
                         item == Items.IRON_HELMET || item == Items.CHAINMAIL_HELMET ||
                         item == Items.GOLDEN_HELMET || item == Items.LEATHER_HELMET ||
                         item == Items.TURTLE_HELMET;
            case CHEST -> item == Items.NETHERITE_CHESTPLATE || item == Items.DIAMOND_CHESTPLATE ||
                          item == Items.IRON_CHESTPLATE || item == Items.CHAINMAIL_CHESTPLATE ||
                          item == Items.GOLDEN_CHESTPLATE || item == Items.LEATHER_CHESTPLATE ||
                          item == Items.ELYTRA;
            case LEGS -> item == Items.NETHERITE_LEGGINGS || item == Items.DIAMOND_LEGGINGS ||
                         item == Items.IRON_LEGGINGS || item == Items.CHAINMAIL_LEGGINGS ||
                         item == Items.GOLDEN_LEGGINGS || item == Items.LEATHER_LEGGINGS;
            case FEET -> item == Items.NETHERITE_BOOTS || item == Items.DIAMOND_BOOTS ||
                         item == Items.IRON_BOOTS || item == Items.CHAINMAIL_BOOTS ||
                         item == Items.GOLDEN_BOOTS || item == Items.LEATHER_BOOTS;
            default -> false;
        };
    }

    private static double getArmorValue(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        
        if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE ||
            item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS) return 100;
        if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE ||
            item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS) return 80;
        if (item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE ||
            item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS) return 60;
        if (item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE ||
            item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS) return 50;
        if (item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE ||
            item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS) return 40;
        if (item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE ||
            item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS) return 20;
        if (item == Items.TURTLE_HELMET) return 55;
        if (item == Items.ELYTRA) return 10;
        
        return 0;
    }


    private static void equipBestWeapon(ServerPlayerEntity bot) {
        BotSettings settings = BotSettings.get();
        var inventory = bot.getInventory();
        
        // Шаг 1: Найти лучшее оружие
        int bestSlotIndex = -1;
        double bestDamage = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            double damage = getWeaponDamage(stack);
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlotIndex = i;
            }
        }
        
        // Шаг 2: Экипируем лучшее оружие
        if (bestSlotIndex >= 0) {
            if (bestSlotIndex >= 9) {
                // Перемещаем в хотбар слот 0
                ItemStack weapon = inventory.getStack(bestSlotIndex);
                ItemStack slot0 = inventory.getStack(0);
                inventory.setStack(bestSlotIndex, slot0);
                inventory.setStack(0, weapon);
                bestSlotIndex = 0;
            }
            ((org.stepan1411.pvp_bot.mixin.PlayerInventoryAccessor) inventory).setSelectedSlot(bestSlotIndex);
        }
        
        // Шаг 3: Выбрасываем худшее оружие
        if (settings.isDropWorseWeapons() && bestDamage > 0) {
            for (int i = 0; i < 36; i++) {
                if (i == bestSlotIndex) continue;
                
                ItemStack stack = inventory.getStack(i);
                if (stack.isEmpty()) continue;
                
                double damage = getWeaponDamage(stack);
                if (damage > 0 && damage < bestDamage) {
                    dropItemBackward(bot, stack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private static double getWeaponDamage(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        
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
        if (item == Items.MACE) return 6;
        
        return 0;
    }
}
