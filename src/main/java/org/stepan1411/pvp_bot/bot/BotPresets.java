package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.stepan1411.pvp_bot.config.WorldConfigHelper;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Settings presets: named snapshots of the full {@link BotSettings} state that can be
 * saved and re-applied at any time, like the kit system but for settings.
 *
 * Each preset is stored as a JSON snapshot of every setting field. Presets live in the
 * global config dir (shared across worlds) so a setup can be reused anywhere.
 */
public class BotPresets {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;

    // presetName (lowercase) -> full settings snapshot
    private static final Map<String, JsonElement> presets = new HashMap<>();

    public static void init(MinecraftServer srv) {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {

        }

        configPath = WorldConfigHelper.getGlobalConfigDir().resolve("presets.json");
        load();
    }

    public static void reload(MinecraftServer srv) {
        configPath = WorldConfigHelper.getGlobalConfigDir().resolve("presets.json");
        load();
    }

    /**
     * Save the current settings under the given preset name. Overwrites an existing
     * preset with the same name.
     */
    public static boolean savePreset(String presetName) {
        String key = presetName.toLowerCase();
        presets.put(key, BotSettings.exportJson());
        save();
        return true;
    }

    /**
     * Apply a saved preset to the current settings. Returns false if the preset does
     * not exist or could not be applied.
     */
    public static boolean loadPreset(String presetName) {
        JsonElement snapshot = presets.get(presetName.toLowerCase());
        if (snapshot == null) return false;
        return BotSettings.importJson(snapshot);
    }

    public static boolean deletePreset(String presetName) {
        boolean removed = presets.remove(presetName.toLowerCase()) != null;
        if (removed) save();
        return removed;
    }

    public static Set<String> getPresetNames() {
        return new HashSet<>(presets.keySet());
    }

    public static boolean presetExists(String presetName) {
        return presets.containsKey(presetName.toLowerCase());
    }

    private static void load() {
        if (configPath == null || !Files.exists(configPath)) return;

        try (Reader reader = Files.newBufferedReader(configPath)) {
            Map<String, JsonElement> loaded = GSON.fromJson(
                reader,
                new TypeToken<Map<String, JsonElement>>(){}.getType()
            );
            if (loaded != null) {
                presets.clear();
                presets.putAll(loaded);
            }
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to load presets: " + e.getMessage());
        }
    }

    private static void save() {
        if (configPath == null) return;

        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(presets, writer);
        } catch (Exception e) {
            System.out.println("[PVP_BOT] Failed to save presets: " + e.getMessage());
        }
    }
}
