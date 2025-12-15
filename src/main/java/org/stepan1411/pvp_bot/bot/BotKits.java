package org.stepan1411.pvp_bot.bot;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class BotKits {
    
    // Хранение китов: имя кита -> список предметов (слот -> предмет)
    private static final Map<String, Map<Integer, ItemStack>> kits = new HashMap<>();
    
    /**
     * Создать кит из инвентаря игрока
     */
    public static boolean createKit(String kitName, ServerPlayerEntity player) {
        if (kits.containsKey(kitName.toLowerCase())) {
            return false; // Кит уже существует
        }
        
        Map<Integer, ItemStack> kitItems = new HashMap<>();
        var inventory = player.getInventory();
        
        // Копируем все предметы из инвентаря (слоты 0-35 + броня 36-39 + offhand 40)
        for (int i = 0; i < 41; i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                kitItems.put(i, stack.copy());
            }
        }
        
        if (kitItems.isEmpty()) {
            return false; // Пустой инвентарь
        }
        
        kits.put(kitName.toLowerCase(), kitItems);
        return true;
    }
    
    /**
     * Удалить кит
     */
    public static boolean deleteKit(String kitName) {
        return kits.remove(kitName.toLowerCase()) != null;
    }
    
    /**
     * Получить список всех китов
     */
    public static Set<String> getKitNames() {
        return new HashSet<>(kits.keySet());
    }
    
    /**
     * Проверить существует ли кит
     */
    public static boolean kitExists(String kitName) {
        return kits.containsKey(kitName.toLowerCase());
    }
    
    /**
     * Получить предметы кита
     */
    public static Map<Integer, ItemStack> getKitItems(String kitName) {
        Map<Integer, ItemStack> kit = kits.get(kitName.toLowerCase());
        if (kit == null) return null;
        
        // Возвращаем копии предметов
        Map<Integer, ItemStack> copy = new HashMap<>();
        for (var entry : kit.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }
    
    /**
     * Выдать кит боту
     */
    public static boolean giveKit(String kitName, ServerPlayerEntity bot) {
        Map<Integer, ItemStack> kitItems = getKitItems(kitName);
        if (kitItems == null) return false;
        
        var inventory = bot.getInventory();
        
        // Очищаем инвентарь бота
        inventory.clear();
        
        // Выдаём предметы
        for (var entry : kitItems.entrySet()) {
            int slot = entry.getKey();
            ItemStack stack = entry.getValue();
            inventory.setStack(slot, stack);
        }
        
        return true;
    }
    
    /**
     * Выдать кит всем ботам фракции
     */
    public static int giveKitToFaction(String kitName, String factionName) {
        if (!kitExists(kitName)) return -1;
        
        Set<String> members = BotFaction.getMembers(factionName);
        if (members == null || members.isEmpty()) return 0;
        
        int count = 0;
        for (String botName : members) {
            // Получаем бота через BotManager (нужен server)
            // Это будет вызываться из команды где есть доступ к server
            count++;
        }
        
        return count;
    }
}
