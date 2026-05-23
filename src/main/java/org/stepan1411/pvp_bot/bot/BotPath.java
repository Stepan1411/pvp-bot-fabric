package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.Vec3d;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class BotPath {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    
    private static final Map<String, PathData> paths = new HashMap<>();
    private static final Map<String, PathFollower> followers = new HashMap<>();
    private static final Set<String> visiblePaths = new HashSet<>();
    
    public static class PathData {
        public String name;
        public List<Vec3d> points = new ArrayList<>();
        public boolean loop = false;
        public boolean attack = true;
        
        public PathData(String name) {
            this.name = name;
        }
    }
    
    public static class PathFollower {
        public String pathName;
        public int currentPoint = 0;
        public boolean reverse = false;
        public Vec3d pausedAtPoint = null;
        public boolean inCombat = false;
        
        public PathFollower(String pathName) {
            this.pathName = pathName;
        }
    }
    
    
    public static boolean createPath(String name) {
        if (paths.containsKey(name)) {
            return false;
        }
        paths.put(name, new PathData(name));
        save();
        return true;
    }
    
    
    public static boolean deletePath(String name) {
        if (!paths.containsKey(name)) {
            return false;
        }
        paths.remove(name);

        followers.entrySet().removeIf(entry -> entry.getValue().pathName.equals(name));
        save();
        return true;
    }
    
    
    public static boolean addPoint(String pathName, Vec3d point) {
        PathData path = paths.get(pathName);
        if (path == null) {
            return false;
        }
        path.points.add(point);
        save();
        return true;
    }
    
    
    public static boolean removeLastPoint(String pathName) {
        PathData path = paths.get(pathName);
        if (path == null || path.points.isEmpty()) {
            return false;
        }
        path.points.remove(path.points.size() - 1);
        save();
        return true;
    }
    
    
    public static boolean removePoint(String pathName, int index) {
        PathData path = paths.get(pathName);
        if (path == null || index < 0 || index >= path.points.size()) {
            return false;
        }
        path.points.remove(index);
        save();
        return true;
    }
    
    
    public static boolean clearPath(String pathName) {
        PathData path = paths.get(pathName);
        if (path == null) {
            return false;
        }
        path.points.clear();
        save();
        return true;
    }
    
    
    public static boolean setLoop(String pathName, boolean loop) {
        PathData path = paths.get(pathName);
        if (path == null) {
            return false;
        }
        path.loop = loop;
        save();
        return true;
    }
    
    
    public static boolean setAttack(String pathName, boolean attack) {
        PathData path = paths.get(pathName);
        if (path == null) {
            return false;
        }
        path.attack = attack;
        save();
        return true;
    }
    
    
    public static boolean startFollowing(String botName, String pathName) {
        PathData path = paths.get(pathName);
        if (path == null || path.points.isEmpty()) {
            return false;
        }
        followers.put(botName, new PathFollower(pathName));
        return true;
    }
    
    
    public static boolean stopFollowing(String botName) {
        return followers.remove(botName) != null;
    }
    
    
    public static Vec3d getNextPoint(String botName) {
        PathFollower follower = followers.get(botName);
        if (follower == null) {
            return null;
        }
        
        PathData path = paths.get(follower.pathName);
        if (path == null || path.points.isEmpty()) {
            return null;
        }
        
        return path.points.get(follower.currentPoint);
    }
    
    
    public static void advanceToNextPoint(String botName) {
        PathFollower follower = followers.get(botName);
        if (follower == null) {
            return;
        }
        
        PathData path = paths.get(follower.pathName);
        if (path == null || path.points.isEmpty()) {
            return;
        }
        
        if (path.loop) {

            if (follower.reverse) {
                follower.currentPoint--;
                if (follower.currentPoint < 0) {
                    follower.currentPoint = 1;
                    follower.reverse = false;
                }
            } else {
                follower.currentPoint++;
                if (follower.currentPoint >= path.points.size()) {
                    follower.currentPoint = path.points.size() - 2;
                    follower.reverse = true;
                }
            }
        } else {

            follower.currentPoint = (follower.currentPoint + 1) % path.points.size();
        }
    }
    
    
    public static boolean isFollowing(String botName) {
        return followers.containsKey(botName);
    }
    
    
    public static boolean isFollowing(String botName, String pathName) {
        PathFollower follower = followers.get(botName);
        return follower != null && follower.pathName.equals(pathName);
    }
    
    
    public static boolean setBotPathIndex(String botName, int index) {
        PathFollower follower = followers.get(botName);
        if (follower == null) {
            return false;
        }
        
        PathData path = paths.get(follower.pathName);
        if (path == null || index < 0 || index >= path.points.size()) {
            return false;
        }
        
        follower.currentPoint = index;
        return true;
    }
    
    
    public static PathData getPath(String name) {
        return paths.get(name);
    }
    
    
    public static Map<String, PathData> getAllPaths() {
        return paths;
    }
    
    
    public static PathFollower getFollower(String botName) {
        return followers.get(botName);
    }
    
    
    public static boolean setPathVisible(String pathName, boolean visible) {
        PathData path = paths.get(pathName);
        if (path == null) {
            return false;
        }
        
        if (visible) {
            visiblePaths.add(pathName);
        } else {
            visiblePaths.remove(pathName);
        }
        return true;
    }
    
    
    public static boolean isPathVisible(String pathName) {
        return visiblePaths.contains(pathName);
    }
    
    
    public static Set<String> getVisiblePaths() {
        return visiblePaths;
    }
    
    
    public static void startCombat(String botName, Vec3d currentTarget) {
        PathFollower follower = followers.get(botName);
        if (follower != null && !follower.inCombat) {
            follower.inCombat = true;
            follower.pausedAtPoint = currentTarget;
        }
    }
    
    
    public static void endCombat(String botName) {
        PathFollower follower = followers.get(botName);
        if (follower != null) {
            follower.inCombat = false;

        }
    }
    
    
    public static boolean isInCombat(String botName) {
        PathFollower follower = followers.get(botName);
        return follower != null && follower.inCombat;
    }
    
    
    public static Vec3d getPausedPoint(String botName) {
        PathFollower follower = followers.get(botName);
        return follower != null ? follower.pausedAtPoint : null;
    }
    
    
    public static void clearPausedPoint(String botName) {
        PathFollower follower = followers.get(botName);
        if (follower != null) {
            follower.pausedAtPoint = null;
        }
    }
    
    
    public static boolean shouldAttack(String botName) {
        PathFollower follower = followers.get(botName);
        if (follower == null) {
            return true;
        }
        
        PathData path = paths.get(follower.pathName);
        return path != null && path.attack;
    }
    
    
    public static void init() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        
        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            System.err.println("[PVP_BOT] Failed to create config directory: " + e.getMessage());
        }
        
        configPath = org.stepan1411.pvp_bot.config.WorldConfigHelper.getWorldConfigDir().resolve("paths.json");
        load();
    }
    
    
    public static void save() {
        if (configPath == null) return;
        
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(paths, writer);
        } catch (Exception e) {
            System.err.println("[PVP_BOT] Failed to save paths: " + e.getMessage());
        }
    }
    
    
    private static void load() {
        if (configPath == null || !Files.exists(configPath)) {
            return;
        }
        
        try (Reader reader = Files.newBufferedReader(configPath)) {
            Map<String, PathData> loadedPaths = GSON.fromJson(reader, 
                new TypeToken<Map<String, PathData>>(){}.getType());
            
            if (loadedPaths != null) {
                paths.clear();
                paths.putAll(loadedPaths);
            }
        } catch (Exception e) {
            System.err.println("[PVP_BOT] Failed to load paths: " + e.getMessage());
        }
    }
}
