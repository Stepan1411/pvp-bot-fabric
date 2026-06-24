package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CustomCommand {
    public static class Entry {
        public String pattern;
        public List<String> argTypes;

        public Entry() {}

        public Entry(String pattern, List<String> argTypes) {
            this.pattern = pattern;
            this.argTypes = argTypes != null ? argTypes : new ArrayList<>();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, Entry> commands = new LinkedHashMap<>();
    private static Path configPath;

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
        }
        configPath = configDir.resolve("custom_commands.json");
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Type type = new TypeToken<LinkedHashMap<String, Entry>>() {}.getType();
                Map<String, Entry> loaded = GSON.fromJson(reader, type);
                if (loaded != null) commands = new LinkedHashMap<>(loaded);
            } catch (Exception e) {
            }
        }
    }

    public static void save() {
        if (configPath == null) return;
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(commands, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean create(String name, String pattern, List<String> argTypes) {
        if (commands.containsKey(name)) return false;
        commands.put(name, new Entry(pattern, argTypes));
        save();
        return true;
    }

    public static boolean delete(String name) {
        boolean removed = commands.remove(name) != null;
        if (removed) save();
        return removed;
    }

    public static Entry get(String name) {
        return commands.get(name);
    }

    public static Map<String, Entry> getAll() {
        return commands;
    }

    public static boolean exists(String name) {
        return commands.containsKey(name);
    }

    public static int size() {
        return commands.size();
    }
}
