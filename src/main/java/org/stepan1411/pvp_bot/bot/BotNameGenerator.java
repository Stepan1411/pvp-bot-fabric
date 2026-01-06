package org.stepan1411.pvp_bot.bot;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Генератор уникальных имён для ботов
 * Разбивает имена типа "AetherClaw" на части и комбинирует их
 */
public class BotNameGenerator {
    
    private static final List<String> PREFIXES = new ArrayList<>();
    private static final List<String> SUFFIXES = new ArrayList<>();
    private static final Random random = new Random();
    
    // Паттерн для разделения CamelCase (AetherClaw -> Aether, Claw)
    private static final Pattern CAMEL_CASE = Pattern.compile("([A-Z][a-z0-9]+)");
    
    // Базовые имена для разбора
    private static final String[] BASE_NAMES = {
        "AetherClaw", "BlazeRunner", "NetherScribe", "VoidCarver", "StoneVigil",
        "HexaStrike", "CinderBlade", "FrostShard", "NightRavager", "WarpedSentinel",
        "CrimsonBolt", "EchoMiner", "ShadowCrafter", "IronSpecter", "BlueTalon",
        "CoreBreaker", "SilentObsidian", "EnderHarvester", "GhastSilencer", "LavaWalker",
        "NullCaster", "RiftHunter", "DeepStrider", "BoneSpark", "PhantomTide",
        "AncientSting", "SolarVortex", "HollowKnight", "PixelNomad", "DustReaver",
        "AmberWarden", "FuryCrafter", "BedrockFang", "ThunderFloe", "DreadCircuit",
        "QuartzStalker", "SkyboundRogue", "FallenCobalt", "ObsidianHornet", "RuneShatter",
        "PrismFire", "VortexHermit", "Ashborne", "Netherling", "SilentCrux",
        "EbonShade", "StormGlider", "IronWanderer", "WarpDiver", "StellarForge",
        "GhostMire", "CobaltBreaker", "BlitzCraze", "ScarletPiercer", "FeralCircuit",
        "DarklingRush", "CoreFrost", "MoltenCode", "SpireWalker", "EtherRune",
        "GlassReaver", "DeltaCrafter", "DeepFang", "MistHowler", "CopperVandal",
        "IronNova", "NightAggregate", "ScaledComet", "VileHarvester", "DustHopper",
        "MarrowRush", "PhantomVerse", "NullShard", "WardenTamer", "PrimalSlicer",
        "Stormborne", "GlintTracer", "FluxReaver", "ShadeWielder", "CliffJumper",
        "AshenSickle", "HauntEdge", "SnareStrike", "CrimsonVoxel", "SlateBreaker",
        "HuskRider", "Echolite", "VoidWalker", "SkyForge", "DarkBlade"
    };
    
    static {
        initializeParts();
    }
    
    /**
     * Разбирает базовые имена на части (префиксы и суффиксы)
     */
    private static void initializeParts() {
        Set<String> prefixSet = new HashSet<>();
        Set<String> suffixSet = new HashSet<>();
        
        for (String name : BASE_NAMES) {
            List<String> parts = splitCamelCase(name);
            if (parts.size() >= 2) {
                prefixSet.add(parts.get(0));
                suffixSet.add(parts.get(parts.size() - 1));
            } else if (parts.size() == 1 && parts.get(0).length() > 4) {
                // Одно слово - используем как префикс
                prefixSet.add(parts.get(0));
            }
        }
        
        PREFIXES.addAll(prefixSet);
        SUFFIXES.addAll(suffixSet);
    }
    
    /**
     * Разделяет CamelCase строку на части
     */
    private static List<String> splitCamelCase(String name) {
        List<String> parts = new ArrayList<>();
        Matcher matcher = CAMEL_CASE.matcher(name);
        while (matcher.find()) {
            parts.add(matcher.group(1));
        }
        return parts;
    }
    
    /**
     * Генерирует уникальное имя бота
     */
    public static String generateUniqueName() {
        Set<String> existingBots = BotManager.getAllBots();
        
        for (int attempt = 0; attempt < 100; attempt++) {
            String name = generateName();
            if (!existingBots.contains(name)) {
                return name;
            }
        }
        
        // Если не удалось найти уникальное - добавляем число
        String baseName = generateName();
        int num = 1;
        while (existingBots.contains(baseName + num)) {
            num++;
        }
        return baseName + num;
    }
    
    /**
     * Генерирует случайное имя (Prefix + Suffix)
     */
    private static String generateName() {
        String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
        String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
        return prefix + suffix;
    }
}
