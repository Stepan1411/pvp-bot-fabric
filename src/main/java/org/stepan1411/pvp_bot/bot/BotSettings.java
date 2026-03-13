package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.stepan1411.pvp_bot.config.WorldConfigHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BotSettings {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BotSettings INSTANCE;
    private static Path configPath;
    

    private boolean autoEquipArmor = true;
    private boolean autoEquipWeapon = true;
    

    private boolean dropWorseArmor = false;
    private boolean dropWorseWeapons = false;
    private double dropDistance = 3.0;
    private int dropDelay = 20;
    

    private int checkInterval = 20;
    

    private int minArmorLevel = 0;
    

    private boolean combatEnabled = true;
    private boolean revengeEnabled = true;
    private boolean autoTargetEnabled = false;
    private boolean targetPlayers = true;
    private boolean targetHostileMobs = false;
    private boolean targetOtherBots = false;
    

    private double maxTargetDistance = 64.0;
    private double meleeRange = 3.5;
    private double rangedMinRange = 8.0;
    private double rangedOptimalRange = 20.0;
    private double maceRange = 6.0;
    

    private int attackCooldown = 10;
    private double moveSpeed = 1.0;
    private boolean criticalsEnabled = true;
    private int bowMinDrawTime = 15;
    

    private boolean rangedEnabled = true;
    private boolean maceEnabled = true;
    private boolean spearEnabled = false;
    private boolean crystalPvpEnabled = true;
    private boolean anchorPvpEnabled = true;
    private boolean elytraMaceEnabled = true; // Новая настройка
    

    private double spearRange = 4.5;
    private double spearChargeRange = 12.0;
    private int spearMinChargeTime = 15;
    private int spearMaxChargeTime = 40;
    

    private boolean autoTotemEnabled = true;
    private boolean totemPriority = true;
    private boolean autoEatEnabled = true;
    private boolean autoShieldEnabled = true;
    private boolean autoMendEnabled = true;
    private double mendDurabilityThreshold = 0.25;
    private double shieldHealthThreshold = 0.5;
    private boolean shieldBreakEnabled = true;
    private boolean preferSword = true;
    private int minHungerToEat = 14;
    private boolean autoPotionEnabled = true;
    private boolean cobwebEnabled = true;
    

    private boolean useBaritone = true;
    private boolean retreatEnabled = true;
    private double retreatHealthPercent = 0.3;
    private double criticalHealthPercent = 0.15;
    private boolean bhopEnabled = true;
    private int bhopCooldown = 12;
    private double jumpBoost = 0.0;
    private boolean idleWanderEnabled = false;
    private double idleWanderRadius = 10.0;
    

    private boolean factionsEnabled = true;
    private boolean friendlyFireEnabled = false;
    private int missChance = 10;
    private int mistakeChance = 5;
    private int reactionDelay = 0;
    private boolean botsRelogs = true;
    

    private boolean sendStats = true;
    
    private BotSettings() {}
    
    public static BotSettings get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }
    
    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {

        }
        
        configPath = WorldConfigHelper.getWorldConfigDir().resolve("settings.json");
        
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                INSTANCE = GSON.fromJson(reader, BotSettings.class);
                if (INSTANCE == null) {
                    INSTANCE = new BotSettings();
                }
            } catch (Exception e) {
                INSTANCE = new BotSettings();
            }
        } else {
            INSTANCE = new BotSettings();
            save();
        }
    }

    
    public static void save() {
        if (INSTANCE == null || configPath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public boolean isAutoEquipArmor() { return autoEquipArmor; }
    public boolean isAutoEquipWeapon() { return autoEquipWeapon; }
    public boolean isDropWorseArmor() { return dropWorseArmor; }
    public boolean isDropWorseWeapons() { return dropWorseWeapons; }
    public double getDropDistance() { return dropDistance; }
    public int getDropDelay() { return dropDelay; }
    public int getCheckInterval() { return checkInterval; }
    public int getMinArmorLevel() { return minArmorLevel; }
    

    public boolean isCombatEnabled() { return combatEnabled; }
    public boolean isRevengeEnabled() { return revengeEnabled; }
    public boolean isAutoTargetEnabled() { return autoTargetEnabled; }
    public boolean isTargetPlayers() { return targetPlayers; }
    public boolean isTargetHostileMobs() { return targetHostileMobs; }
    public boolean isTargetOtherBots() { return targetOtherBots; }
    public double getMaxTargetDistance() { return maxTargetDistance; }
    public double getMeleeRange() { return meleeRange; }
    public double getRangedMinRange() { return rangedMinRange; }
    public double getRangedOptimalRange() { return rangedOptimalRange; }
    public double getMaceRange() { return maceRange; }
    public int getAttackCooldown() { return attackCooldown; }
    public double getMoveSpeed() { return moveSpeed; }
    public boolean isCriticalsEnabled() { return criticalsEnabled; }
    public int getBowMinDrawTime() { return bowMinDrawTime; }
    public boolean isRangedEnabled() { return rangedEnabled; }
    public boolean isMaceEnabled() { return maceEnabled; }
    public boolean isSpearEnabled() { return spearEnabled; }
    public boolean isCrystalPvpEnabled() { return crystalPvpEnabled; }
    public boolean isAnchorPvpEnabled() { return anchorPvpEnabled; }
    public boolean isElytraMaceEnabled() { return elytraMaceEnabled; } // Новый геттер
    public double getSpearRange() { return spearRange; }
    public double getSpearChargeRange() { return spearChargeRange; }
    public int getSpearMinChargeTime() { return spearMinChargeTime; }
    public int getSpearMaxChargeTime() { return spearMaxChargeTime; }
    

    public boolean isAutoTotemEnabled() { return autoTotemEnabled; }
    public boolean isTotemPriority() { return totemPriority; }
    public boolean isAutoEatEnabled() { return autoEatEnabled; }
    public boolean isAutoShieldEnabled() { return autoShieldEnabled; }
    public boolean isAutoMendEnabled() { return autoMendEnabled; }
    public double getMendDurabilityThreshold() { return mendDurabilityThreshold; }
    public double getShieldHealthThreshold() { return shieldHealthThreshold; }
    public boolean isShieldBreakEnabled() { return shieldBreakEnabled; }
    public boolean isPreferSword() { return preferSword; }
    public int getMinHungerToEat() { return minHungerToEat; }
    public boolean isAutoPotionEnabled() { return autoPotionEnabled; }
    public boolean isCobwebEnabled() { return cobwebEnabled; }
    

    public boolean isUseBaritone() { return useBaritone; }
    public boolean isRetreatEnabled() { return retreatEnabled; }
    public double getRetreatHealthPercent() { return retreatHealthPercent; }
    public double getCriticalHealthPercent() { return criticalHealthPercent; }
    public boolean isBhopEnabled() { return bhopEnabled; }
    public int getBhopCooldown() { return bhopCooldown; }
    public double getJumpBoost() { return jumpBoost; }
    public boolean isIdleWanderEnabled() { return idleWanderEnabled; }
    public double getIdleWanderRadius() { return idleWanderRadius; }
    

    public boolean isFactionsEnabled() { return factionsEnabled; }
    public boolean isFriendlyFireEnabled() { return friendlyFireEnabled; }
    public int getMissChance() { return missChance; }
    public int getMistakeChance() { return mistakeChance; }
    public int getReactionDelay() { return reactionDelay; }
    public boolean isBotsRelogs() { return botsRelogs; }
    

    public boolean isSendStats() { return sendStats; }
    

    public void setAutoEquipArmor(boolean value) { 
        this.autoEquipArmor = value; 
        save();
    }
    public void setAutoEquipWeapon(boolean value) { 
        this.autoEquipWeapon = value; 
        save();
    }
    public void setDropWorseArmor(boolean value) { 
        this.dropWorseArmor = value; 
        save();
    }
    public void setDropWorseWeapons(boolean value) { 
        this.dropWorseWeapons = value; 
        save();
    }
    public void setDropDistance(double value) { 
        this.dropDistance = Math.max(1.0, Math.min(10.0, value)); 
        save();
    }
    public void setDropDelay(int value) { 
        this.dropDelay = Math.max(1, Math.min(200, value)); 
        save();
    }
    public void setCheckInterval(int value) { 
        this.checkInterval = Math.max(1, Math.min(100, value)); 
        save();
    }
    public void setMinArmorLevel(int value) { 
        this.minArmorLevel = Math.max(0, Math.min(100, value)); 
        save();
    }
    

    public void setCombatEnabled(boolean value) { this.combatEnabled = value; save(); }
    public void setRevengeEnabled(boolean value) { this.revengeEnabled = value; save(); }
    public void setAutoTargetEnabled(boolean value) { this.autoTargetEnabled = value; save(); }
    public void setTargetPlayers(boolean value) { this.targetPlayers = value; save(); }
    public void setTargetHostileMobs(boolean value) { this.targetHostileMobs = value; save(); }
    public void setTargetOtherBots(boolean value) { this.targetOtherBots = value; save(); }
    
    public void setMaxTargetDistance(double value) { 
        this.maxTargetDistance = Math.max(5.0, Math.min(128.0, value)); 
        save(); 
    }
    public void setMeleeRange(double value) { 
        this.meleeRange = Math.max(2.0, Math.min(6.0, value)); 
        save(); 
    }
    public void setRangedMinRange(double value) { 
        this.rangedMinRange = Math.max(3.0, Math.min(20.0, value)); 
        save(); 
    }
    public void setRangedOptimalRange(double value) { 
        this.rangedOptimalRange = Math.max(10.0, Math.min(50.0, value)); 
        save(); 
    }
    public void setMaceRange(double value) { 
        this.maceRange = Math.max(3.0, Math.min(10.0, value)); 
        save(); 
    }
    public void setAttackCooldown(int value) { 
        this.attackCooldown = Math.max(1, Math.min(40, value)); 
        save(); 
    }
    public void setMoveSpeed(double value) { 
        this.moveSpeed = Math.max(0.1, Math.min(2.0, value)); 
        save(); 
    }
    public void setCriticalsEnabled(boolean value) { this.criticalsEnabled = value; save(); }
    public void setBowMinDrawTime(int value) { 
        this.bowMinDrawTime = Math.max(5, Math.min(30, value)); 
        save(); 
    }
    public void setRangedEnabled(boolean value) { this.rangedEnabled = value; save(); }
    public void setMaceEnabled(boolean value) { this.maceEnabled = value; save(); }
    public void setSpearEnabled(boolean value) { this.spearEnabled = value; save(); }
    public void setCrystalPvpEnabled(boolean value) { this.crystalPvpEnabled = value; save(); }
    public void setAnchorPvpEnabled(boolean value) { this.anchorPvpEnabled = value; save(); }
    public void setElytraMaceEnabled(boolean value) { this.elytraMaceEnabled = value; save(); } // Новый сеттер
    public void setSpearRange(double value) { 
        this.spearRange = Math.max(2.0, Math.min(8.0, value)); 
        save(); 
    }
    public void setSpearChargeRange(double value) { 
        this.spearChargeRange = Math.max(5.0, Math.min(20.0, value)); 
        save(); 
    }
    public void setSpearMinChargeTime(int value) { 
        this.spearMinChargeTime = Math.max(5, Math.min(30, value)); 
        save(); 
    }
    public void setSpearMaxChargeTime(int value) { 
        this.spearMaxChargeTime = Math.max(20, Math.min(60, value)); 
        save(); 
    }
    

    public void setAutoTotemEnabled(boolean value) { this.autoTotemEnabled = value; save(); }
    public void setTotemPriority(boolean value) { this.totemPriority = value; save(); }
    public void setAutoEatEnabled(boolean value) { this.autoEatEnabled = value; save(); }
    public void setAutoShieldEnabled(boolean value) { this.autoShieldEnabled = value; save(); }
    public void setAutoMendEnabled(boolean value) { this.autoMendEnabled = value; save(); }
    public void setMendDurabilityThreshold(double value) { 
        this.mendDurabilityThreshold = Math.max(0.1, Math.min(0.9, value)); 
        save(); 
    }
    public void setShieldHealthThreshold(double value) { 
        this.shieldHealthThreshold = Math.max(0.1, Math.min(1.0, value)); 
        save(); 
    }
    public void setShieldBreakEnabled(boolean value) { this.shieldBreakEnabled = value; save(); }
    public void setPreferSword(boolean value) { this.preferSword = value; save(); }
    public void setMinHungerToEat(int value) { 
        this.minHungerToEat = Math.max(1, Math.min(20, value)); 
        save(); 
    }
    public void setAutoPotionEnabled(boolean value) { this.autoPotionEnabled = value; save(); }
    public void setCobwebEnabled(boolean value) { this.cobwebEnabled = value; save(); }
    

    public void setUseBaritone(boolean value) { this.useBaritone = value; save(); }
    public void setRetreatEnabled(boolean value) { this.retreatEnabled = value; save(); }
    public void setRetreatHealthPercent(double value) { 
        this.retreatHealthPercent = Math.max(0.1, Math.min(0.9, value)); 
        save(); 
    }
    public void setCriticalHealthPercent(double value) { 
        this.criticalHealthPercent = Math.max(0.05, Math.min(0.5, value)); 
        save(); 
    }
    public void setBhopEnabled(boolean value) { this.bhopEnabled = value; save(); }
    public void setBhopCooldown(int value) { 
        this.bhopCooldown = Math.max(5, Math.min(30, value)); 
        save(); 
    }
    public void setJumpBoost(double value) { 
        this.jumpBoost = Math.max(0.0, Math.min(0.5, value)); 
        save(); 
    }
    public void setIdleWanderEnabled(boolean value) { this.idleWanderEnabled = value; save(); }
    public void setIdleWanderRadius(double value) { 
        this.idleWanderRadius = Math.max(3.0, Math.min(50.0, value)); 
        save(); 
    }
    

    public void setFactionsEnabled(boolean value) { this.factionsEnabled = value; save(); }
    public void setFriendlyFireEnabled(boolean value) { this.friendlyFireEnabled = value; save(); }
    public void setMissChance(int value) { 
        this.missChance = Math.max(0, Math.min(100, value)); 
        save(); 
    }
    public void setMistakeChance(int value) { 
        this.mistakeChance = Math.max(0, Math.min(100, value)); 
        save(); 
    }
    public void setReactionDelay(int value) { 
        this.reactionDelay = Math.max(0, Math.min(20, value)); 
        save(); 
    }
    public void setBotsRelogs(boolean value) { this.botsRelogs = value; save(); }
}
