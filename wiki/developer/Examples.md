# Examples

Ready-to-use examples of PVP Bot API usage.
Or use [example mod](https://github.com/Stepan1411/pvpbot-example-mod)

## Example 1: Bot Action Logger

```java
public class BotLogger implements ModInitializer {
    
    @Override
    public void onInitialize() {
        BotEventManager events = PvpBotAPI.getEventManager();
        
        events.registerSpawnHandler(bot -> {
            System.out.println("[BOT] " + bot.getName().getString() + " spawned at " + bot.getPos());
        });
        
        events.registerDeathHandler(bot -> {
            System.out.println("[BOT] " + bot.getName().getString() + " died");
        });
        
        events.registerAttackHandler((bot, target) -> {
            System.out.println("[BOT] " + bot.getName().getString() + " attacked " + target.getName().getString());
            return false;
        });
    }
}
```

## Example 2: Rank System

```java
public class BotRankSystem implements ModInitializer {
    
    private final Map<String, Integer> experience = new HashMap<>();
    private final Map<String, String> ranks = new HashMap<>();
    
    @Override
    public void onInitialize() {
        PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
            if (target.isDead()) {
                String botName = bot.getName().getString();
                int exp = experience.getOrDefault(botName, 0) + 10;
                experience.put(botName, exp);
                
                updateRank(bot, exp);
            }
            return false;
        });
    }
    
    private void updateRank(ServerPlayerEntity bot, int exp) {
        String botName = bot.getName().getString();
        String rank = getRankForExp(exp);
        
        if (!rank.equals(ranks.get(botName))) {
            ranks.put(botName, rank);
            bot.sendMessage(Text.literal("New rank: " + rank));
        }
    }
    
    private String getRankForExp(int exp) {
        if (exp >= 1000) return "Legend";
        if (exp >= 500) return "Master";
        if (exp >= 100) return "Warrior";
        return "Novice";
    }
}
```

## Example 3: Custom Combat Strategy

```java
public class FireballStrategy implements CombatStrategy {
    
    @Override
    public String getName() {
        return "FireballStrategy";
    }
    
    @Override
    public int getPriority() {
        return 120;
    }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        // Check distance and fire charges
        double distance = bot.distanceTo(target);
        return distance > 5 && distance < 20 
            && bot.getInventory().contains(Items.FIRE_CHARGE.getDefaultStack());
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
        // Create fireball
        Vec3d direction = target.getPos().subtract(bot.getPos()).normalize();
        
        // Use server parameter passed to execute() method
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        FireballEntity fireball = new FireballEntity(
            world, 
            bot, 
            direction.x, 
            direction.y, 
            direction.z, 
            1
        );
        
        fireball.setPosition(bot.getEyePos());
        world.spawnEntity(fireball);
        
        return true;
    }
    
    @Override
    public int getCooldown() {
        return 60; // 3 seconds
    }
}
```

## Example 4: Team System

```java
public class BotTeamSystem implements ModInitializer {
    
    private final Map<String, String> botTeams = new HashMap<>();
    
    @Override
    public void onInitialize() {
        // Prevent friendly fire
        PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
            if (target instanceof ServerPlayerEntity) {
                String botName = bot.getName().getString();
                String targetName = target.getName().getString();
                
                String botTeam = botTeams.get(botName);
                String targetTeam = botTeams.get(targetName);
                
                // Cancel attack if same team
                if (botTeam != null && botTeam.equals(targetTeam)) {
                    return true;
                }
            }
            return false;
        });
        
        // Register command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("botteam")
                .then(literal("set")
                    .then(argument("bot", StringArgumentType.string())
                        .then(argument("team", StringArgumentType.string())
                            .executes(context -> {
                                String bot = StringArgumentType.getString(context, "bot");
                                String team = StringArgumentType.getString(context, "team");
                                botTeams.put(bot, team);
                                context.getSource().sendFeedback(
                                    () -> Text.literal("Bot " + bot + " added to team " + team),
                                    false
                                );
                                return 1;
                            })
                        )
                    )
                )
            );
        });
    }
}
```

## Example 5: Auto Heal

```java
public class AutoHeal implements ModInitializer {
    
    @Override
    public void onInitialize() {
        PvpBotAPI.getEventManager().registerTickHandler(bot -> {
            // Check every 2 seconds (40 ticks)
            if (bot.age % 40 != 0) return;
            
            float health = bot.getHealth();
            float maxHealth = bot.getMaxHealth();
            
            // If HP below 50%
            if (health < maxHealth * 0.5f) {
                // Find healing potion
                for (int i = 0; i < bot.getInventory().size(); i++) {
                    ItemStack stack = bot.getInventory().getStack(i);
                    
                    if (stack.getItem() == Items.POTION) {
                        // Use potion
                        bot.setHealth(Math.min(maxHealth, health + 4.0f));
                        stack.decrement(1);
                        break;
                    }
                }
            }
        });
    }
}
```

## Example 6: Achievement System

```java
public class BotAchievements implements ModInitializer {
    
    private final Map<String, BotStats> stats = new HashMap<>();
    
    @Override
    public void onInitialize() {
        BotEventManager events = PvpBotAPI.getEventManager();
        
        // Track kills
        events.registerAttackHandler((bot, target) -> {
            if (target.isDead()) {
                String botName = bot.getName().getString();
                BotStats botStats = stats.computeIfAbsent(botName, k -> new BotStats());
                botStats.kills++;
                
                checkAchievements(bot, botStats);
            }
            return false;
        });
        
        // Track deaths
        events.registerDeathHandler(bot -> {
            String botName = bot.getName().getString();
            BotStats botStats = stats.computeIfAbsent(botName, k -> new BotStats());
            botStats.deaths++;
        });
    }
    
    private void checkAchievements(ServerPlayerEntity bot, BotStats stats) {
        if (stats.kills == 10 && !stats.hasAchievement("first_blood")) {
            stats.addAchievement("first_blood");
            bot.sendMessage(Text.literal("🏆 Achievement: First Blood (10 kills)"));
        }
        
        if (stats.kills == 100 && !stats.hasAchievement("warrior")) {
            stats.addAchievement("warrior");
            bot.sendMessage(Text.literal("🏆 Achievement: Warrior (100 kills)"));
        }
        
        if (stats.kills >= 1000 && !stats.hasAchievement("legend")) {
            stats.addAchievement("legend");
            bot.sendMessage(Text.literal("🏆 Achievement: Legend (1000 kills)"));
        }
    }
    
    private static class BotStats {
        int kills = 0;
        int deaths = 0;
        Set<String> achievements = new HashSet<>();
        
        boolean hasAchievement(String name) {
            return achievements.contains(name);
        }
        
        void addAchievement(String name) {
            achievements.add(name);
        }
    }
}
```
