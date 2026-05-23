package org.stepan1411.pvp_bot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.stepan1411.pvp_bot.bot.BotCombat;
import org.stepan1411.pvp_bot.bot.BotFaction;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotNameGenerator;
import org.stepan1411.pvp_bot.bot.BotPath;
import org.stepan1411.pvp_bot.bot.BotSettings;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class BotCommand {

    private static final SuggestionProvider<ServerCommandSource> BOT_SUGGESTIONS =
        (ctx, builder) -> {
            for (String bot : BotManager.getAllBots()) {
                builder.suggest(bot);
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> KIT_SUGGESTIONS =
        (ctx, builder) -> {
            for (String kit : BotKits.getKitNames()) {
                builder.suggest(kit);
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS =
        (ctx, builder) -> {
            for (var player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                builder.suggest(player.getName().getString());
            }
            return builder.buildFuture();
        };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("pvpbot")
            .requires(s -> true)

            // ========== SPAWN ==========
            .then(CommandManager.literal("spawn")
                .executes(ctx -> spawn(ctx, null))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> spawn(ctx, StringArgumentType.getString(ctx, "name")))))

            // ========== REMOVE ==========
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(BOT_SUGGESTIONS)
                    .executes(BotCommand::remove)))

            // ========== REMOVEALL ==========
            .then(CommandManager.literal("reload")
                .executes(BotCommand::reload))
            .then(CommandManager.literal("removeall")
                .executes(BotCommand::removeAll))

            // ========== SETTINGS ==========
            .then(buildSettings())

            // ========== BOT-MANAGEMENT ==========
            .then(CommandManager.literal("bot-management")
                .then(CommandManager.literal("mass-spawn")
                    .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 50))
                        .executes(BotCommand::massSpawn)))
                .then(CommandManager.literal("attack")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .then(CommandManager.argument("target", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::botAttack))))
                .then(CommandManager.literal("stop-attack")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(BotCommand::botStopAttack)))
                .then(CommandManager.literal("inventory")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(BotCommand::botInventory)))
                .then(CommandManager.literal("list")
                    .executes(BotCommand::botList))
                .then(CommandManager.literal("path")
                    .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(BotCommand::pathCreate)))
                    .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(BotCommand::pathDelete)))
                    .then(CommandManager.literal("addpoint")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(BotCommand::pathAddPoint)))
                    .then(CommandManager.literal("removepoint")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .then(CommandManager.argument("index", IntegerArgumentType.integer())
                                .executes(BotCommand::pathRemovePoint))
                            .executes(BotCommand::pathRemovePointLast)))
                    .then(CommandManager.literal("clear")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(BotCommand::pathClear)))
                    .then(CommandManager.literal("loop")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .then(CommandManager.argument("value", BoolArgumentType.bool())
                                .executes(BotCommand::pathLoop))))
                    .then(CommandManager.literal("attack")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .then(CommandManager.argument("value", BoolArgumentType.bool())
                                .executes(BotCommand::pathAttack))))
                    .then(CommandManager.literal("start")
                        .then(CommandManager.argument("bot", StringArgumentType.word())
                            .suggests(BOT_SUGGESTIONS)
                            .then(CommandManager.argument("path", StringArgumentType.word())
                                .executes(BotCommand::pathStart))))
                    .then(CommandManager.literal("stop")
                        .then(CommandManager.argument("bot", StringArgumentType.word())
                            .suggests(BOT_SUGGESTIONS)
                            .executes(BotCommand::pathStop)))
                    .then(CommandManager.literal("list")
                        .executes(BotCommand::pathList))
                    .then(CommandManager.literal("show")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .then(CommandManager.argument("visible", BoolArgumentType.bool())
                                .executes(BotCommand::pathShow))))
                    .then(CommandManager.literal("info")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(BotCommand::pathInfo)))
                    .then(CommandManager.literal("distribute")
                        .then(CommandManager.argument("path", StringArgumentType.word())
                            .executes(BotCommand::pathDistribute)))
                    .then(CommandManager.literal("startnear")
                        .then(CommandManager.argument("path", StringArgumentType.word())
                            .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1))
                                .executes(BotCommand::pathStartNear))))
                    .then(CommandManager.literal("stopall")
                        .then(CommandManager.argument("path", StringArgumentType.word())
                            .executes(BotCommand::pathStopAll)))))

            // ========== KIT ==========
            .then(CommandManager.literal("kit")
                .then(CommandManager.literal("create-kit")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(BotCommand::kitCreate)))
                .then(CommandManager.literal("delete-kit")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(KIT_SUGGESTIONS)
                        .executes(BotCommand::kitDelete)))
                .then(CommandManager.literal("give-kit")
                    .then(CommandManager.argument("playername", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTIONS)
                        .then(CommandManager.argument("kitname", StringArgumentType.word())
                            .suggests(KIT_SUGGESTIONS)
                            .executes(BotCommand::kitGive))))
                .then(CommandManager.literal("kits")
                    .executes(BotCommand::kitList)))

            // ========== FACTION ==========
            .then(CommandManager.literal("faction")
                .then(CommandManager.literal("list")
                    .executes(BotCommand::factionList))
                .then(CommandManager.literal("create")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(BotCommand::factionCreate)))
                .then(CommandManager.literal("delete")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(BotCommand::factionDelete)))
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .then(CommandManager.argument("player", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionAdd))))
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .then(CommandManager.argument("player", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionRemove))))
                .then(CommandManager.literal("hostile")
                    .then(CommandManager.argument("faction1", StringArgumentType.word())
                        .then(CommandManager.argument("faction2", StringArgumentType.word())
                            .then(CommandManager.argument("hostile", BoolArgumentType.bool())
                                .executes(ctx -> factionHostile(ctx, BoolArgumentType.getBool(ctx, "hostile"))))
                            .executes(ctx -> factionHostile(ctx, true)))))
                .then(CommandManager.literal("info")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .executes(BotCommand::factionInfo)))
                .then(CommandManager.literal("addnear")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1, 10000))
                            .executes(BotCommand::factionAddNear))))
                .then(CommandManager.literal("addall")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .executes(BotCommand::factionAddAll)))
                .then(CommandManager.literal("give")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .then(CommandManager.argument("item", StringArgumentType.greedyString())
                            .executes(BotCommand::factionGive))))
                .then(CommandManager.literal("attack")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .then(CommandManager.argument("target", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionAttack))))
                .then(CommandManager.literal("startpath")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .then(CommandManager.argument("path", StringArgumentType.word())
                            .executes(BotCommand::factionStartPath))))
                .then(CommandManager.literal("stoppath")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .executes(BotCommand::factionStopPath)))
                .then(CommandManager.literal("givekit")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .then(CommandManager.argument("kitname", StringArgumentType.word())
                            .suggests(KIT_SUGGESTIONS)
                            .executes(BotCommand::factionGiveKit)))))
        );
    }

    // ========== SETTINGS BUILDER ==========

    private static LiteralArgumentBuilder<ServerCommandSource> buildSettings() {
        var settings = CommandManager.literal("settings")
            .executes(BotCommand::settings);

        settings.then(boolSetting("auto-armor", () -> BotSettings.get().isAutoEquipArmor(), v -> BotSettings.get().setAutoEquipArmor(v)));
        settings.then(boolSetting("auto-weapon", () -> BotSettings.get().isAutoEquipWeapon(), v -> BotSettings.get().setAutoEquipWeapon(v)));
        settings.then(boolSetting("drop-armor", () -> BotSettings.get().isDropWorseArmor(), v -> BotSettings.get().setDropWorseArmor(v)));
        settings.then(boolSetting("drop-weapon", () -> BotSettings.get().isDropWorseWeapons(), v -> BotSettings.get().setDropWorseWeapons(v)));
        settings.then(doubleSetting("drop-distance", () -> BotSettings.get().getDropDistance(), v -> BotSettings.get().setDropDistance(v), 1.0, 10.0));
        settings.then(intSetting("interval", () -> BotSettings.get().getCheckInterval(), v -> BotSettings.get().setCheckInterval(v), 1, 100));
        settings.then(boolSetting("combat", () -> BotSettings.get().isCombatEnabled(), v -> BotSettings.get().setCombatEnabled(v)));
        settings.then(boolSetting("revenge", () -> BotSettings.get().isRevengeEnabled(), v -> BotSettings.get().setRevengeEnabled(v)));
        settings.then(boolSetting("auto-target", () -> BotSettings.get().isAutoTargetEnabled(), v -> BotSettings.get().setAutoTargetEnabled(v)));
        settings.then(boolSetting("target-players", () -> BotSettings.get().isTargetPlayers(), v -> BotSettings.get().setTargetPlayers(v)));
        settings.then(boolSetting("target-mobs", () -> BotSettings.get().isTargetHostileMobs(), v -> BotSettings.get().setTargetHostileMobs(v)));
        settings.then(boolSetting("target-bots", () -> BotSettings.get().isTargetOtherBots(), v -> BotSettings.get().setTargetOtherBots(v)));
        settings.then(boolSetting("criticals", () -> BotSettings.get().isCriticalsEnabled(), v -> BotSettings.get().setCriticalsEnabled(v)));
        settings.then(boolSetting("ranged", () -> BotSettings.get().isRangedEnabled(), v -> BotSettings.get().setRangedEnabled(v)));
        settings.then(boolSetting("mace", () -> BotSettings.get().isMaceEnabled(), v -> BotSettings.get().setMaceEnabled(v)));
        settings.then(boolSetting("special-names", () -> BotSettings.get().isUseSpecialNames(), v -> BotSettings.get().setUseSpecialNames(v)));
        settings.then(boolSetting("shield-mace", () -> BotSettings.get().isShieldMace(), v -> BotSettings.get().setShieldMace(v)));
        settings.then(intSetting("attack-cooldown", () -> BotSettings.get().getAttackCooldown(), v -> BotSettings.get().setAttackCooldown(v), 1, 40));
        settings.then(doubleSetting("melee-range", () -> BotSettings.get().getMeleeRange(), v -> BotSettings.get().setMeleeRange(v), 2.0, 6.0));
        settings.then(doubleSetting("move-speed", () -> BotSettings.get().getMoveSpeed(), v -> BotSettings.get().setMoveSpeed(v), 0.1, 2.0));
        settings.then(boolSetting("auto-totem", () -> BotSettings.get().isAutoTotemEnabled(), v -> BotSettings.get().setAutoTotemEnabled(v)));
        settings.then(boolSetting("totem-priority", () -> BotSettings.get().isTotemPriority(), v -> BotSettings.get().setTotemPriority(v)));
        settings.then(boolSetting("auto-shield", () -> BotSettings.get().isAutoShieldEnabled(), v -> BotSettings.get().setAutoShieldEnabled(v)));
        settings.then(boolSetting("auto-potion", () -> BotSettings.get().isAutoPotionEnabled(), v -> BotSettings.get().setAutoPotionEnabled(v)));
        settings.then(boolSetting("shield-break", () -> BotSettings.get().isShieldBreakEnabled(), v -> BotSettings.get().setShieldBreakEnabled(v)));
        settings.then(boolSetting("prefer-sword", () -> BotSettings.get().isPreferSword(), v -> BotSettings.get().setPreferSword(v)));
        settings.then(boolSetting("bhop", () -> BotSettings.get().isBhopEnabled(), v -> BotSettings.get().setBhopEnabled(v)));
        settings.then(boolSetting("idle", () -> BotSettings.get().isIdleWanderEnabled(), v -> BotSettings.get().setIdleWanderEnabled(v)));
        settings.then(doubleSetting("idle-radius", () -> BotSettings.get().getIdleWanderRadius(), v -> BotSettings.get().setIdleWanderRadius(v), 3.0, 50.0));
        settings.then(boolSetting("friendly-fire", () -> BotSettings.get().isFriendlyFireEnabled(), v -> BotSettings.get().setFriendlyFireEnabled(v)));
        settings.then(intSetting("miss-chance", () -> BotSettings.get().getMissChance(), v -> BotSettings.get().setMissChance(v), 0, 100));
        settings.then(intSetting("mistake-chance", () -> BotSettings.get().getMistakeChance(), v -> BotSettings.get().setMistakeChance(v), 0, 100));
        settings.then(intSetting("shield-break-chance", () -> BotSettings.get().getShieldBreakChance(), v -> BotSettings.get().setShieldBreakChance(v), 0, 100));
        settings.then(boolSetting("retreat", () -> BotSettings.get().isRetreatEnabled(), v -> BotSettings.get().setRetreatEnabled(v)));
        settings.then(boolSetting("auto-eat", () -> BotSettings.get().isAutoEatEnabled(), v -> BotSettings.get().setAutoEatEnabled(v)));
        settings.then(boolSetting("auto-mend", () -> BotSettings.get().isAutoMendEnabled(), v -> BotSettings.get().setAutoMendEnabled(v)));
        settings.then(boolSetting("bot-leave-on-death", () -> BotSettings.get().isBotLeaveOnDeath(), v -> BotSettings.get().setBotLeaveOnDeath(v)));
        settings.then(boolSetting("attack-invincible", () -> BotSettings.get().isAttackInvincible(), v -> BotSettings.get().setAttackInvincible(v)));
        settings.then(doubleSetting("aim-speed", () -> BotSettings.get().getAimSpeed(), v -> BotSettings.get().setAimSpeed(v), 3.0, 45.0));
        settings.then(doubleSetting("view-distance", () -> BotSettings.get().getMaxTargetDistance(), v -> BotSettings.get().setMaxTargetDistance(v), 5.0, 128.0));

        return settings;
    }

    // ========== SETTING HELPERS ==========

    private static LiteralArgumentBuilder<ServerCommandSource> boolSetting(String name, BooleanSupplier getter, Consumer<Boolean> setter) {
        return CommandManager.literal(name)
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal(name + ": " + getter.getAsBoolean()), false);
                return 1;
            })
            .then(CommandManager.argument("value", BoolArgumentType.bool())
                .executes(ctx -> {
                    boolean value = BoolArgumentType.getBool(ctx, "value");
                    setter.accept(value);
                    ctx.getSource().sendFeedback(() -> Text.literal(name + ": " + value), true);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> intSetting(String name, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, int min, int max) {
        return CommandManager.literal(name)
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal(name + ": " + getter.getAsInt()), false);
                return 1;
            })
            .then(CommandManager.argument("value", IntegerArgumentType.integer(min, max))
                .executes(ctx -> {
                    int value = IntegerArgumentType.getInteger(ctx, "value");
                    setter.accept(value);
                    ctx.getSource().sendFeedback(() -> Text.literal(name + ": " + value), true);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> doubleSetting(String name, java.util.function.DoubleSupplier getter, java.util.function.DoubleConsumer setter, double min, double max) {
        return CommandManager.literal(name)
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal(name + ": " + getter.getAsDouble()), false);
                return 1;
            })
            .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(min, max))
                .executes(ctx -> {
                    double value = DoubleArgumentType.getDouble(ctx, "value");
                    setter.accept(value);
                    ctx.getSource().sendFeedback(() -> Text.literal(name + ": " + value), true);
                    return 1;
                }));
    }

    // ========== COMMAND HANDLERS ==========

    private static int spawn(CommandContext<ServerCommandSource> ctx, String name) {
        var source = ctx.getSource();
        String botName = name != null ? name : BotNameGenerator.generateUniqueName();
        var server = source.getServer();
        var existingPlayer = server.getPlayerManager().getPlayer(botName);
        if (existingPlayer != null && !BotManager.getAllBots().contains(botName)) {
            source.sendError(Text.literal("Cannot create bot '" + botName + "': a real player with this name is online!"));
            return 0;
        }
        if (BotManager.spawnBot(server, botName, source)) {
            source.sendFeedback(() -> Text.literal("PvP Bot '" + botName + "' spawned!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to spawn bot '" + botName + "' (bot already exists or name is taken)"));
            return 0;
        }
    }

    private static int massSpawn(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var server = source.getServer();
        int count = IntegerArgumentType.getInteger(ctx, "count");
        source.sendFeedback(() -> Text.literal("Spawning " + count + " bots "), false);
        int[] spawned = {0};
        int[] current = {0};
        scheduleSpawn(server, source, count, spawned, current);
        return 1;
    }

    private static void scheduleSpawn(net.minecraft.server.MinecraftServer server, ServerCommandSource source, int total, int[] spawned, int[] current) {
        if (current[0] >= total) {
            source.sendFeedback(() -> Text.literal("Finished! Spawned " + spawned[0] + " bots."), true);
            return;
        }
        String name = BotNameGenerator.generateUniqueName();
        if (BotManager.spawnBot(server, name, source)) {
            spawned[0]++;
        }
        current[0]++;
        server.execute(() -> {
            int[] delay = {0};
            server.execute(new Runnable() {
                @Override
                public void run() {
                    delay[0]++;
                    if (delay[0] < 5) {
                        server.execute(this);
                    } else {
                        scheduleSpawn(server, source, total, spawned, current);
                    }
                }
            });
        });
    }

    private static int remove(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotManager.removeBot(source.getServer(), name, source)) {
            source.sendFeedback(() -> Text.literal("Bot '" + name + "' removed!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Bot '" + name + "' not found!"));
            return 0;
        }
    }

    private static int removeAll(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        int count = BotManager.getBotCount();
        BotManager.removeAllBots(source.getServer(), source);
        source.sendFeedback(() -> Text.literal("Removed " + count + " bots"), true);
        return count;
    }

    private static int reload(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var server = source.getServer();
        BotSettings.load();
        BotKits.reload(server);
        BotPath.init();
        BotManager.reloadBots();
        source.sendFeedback(() -> Text.literal("All configurations reloaded!"), true);
        return 1;
    }

    private static int settings(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        BotSettings s = BotSettings.get();
        source.sendFeedback(() -> Text.literal("=== Equipment Settings ==="), false);
        source.sendFeedback(() -> Text.literal("auto-armor: " + s.isAutoEquipArmor()), false);
        source.sendFeedback(() -> Text.literal("auto-weapon: " + s.isAutoEquipWeapon()), false);
        source.sendFeedback(() -> Text.literal("drop-armor: " + s.isDropWorseArmor()), false);
        source.sendFeedback(() -> Text.literal("drop-weapon: " + s.isDropWorseWeapons()), false);
        source.sendFeedback(() -> Text.literal("drop-distance: " + s.getDropDistance()), false);
        source.sendFeedback(() -> Text.literal("interval: " + s.getCheckInterval() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("=== Combat Settings ==="), false);
        source.sendFeedback(() -> Text.literal("combat: " + s.isCombatEnabled()), false);
        source.sendFeedback(() -> Text.literal("revenge: " + s.isRevengeEnabled()), false);
        source.sendFeedback(() -> Text.literal("auto-target: " + s.isAutoTargetEnabled()), false);
        source.sendFeedback(() -> Text.literal("target-players: " + s.isTargetPlayers()), false);
        source.sendFeedback(() -> Text.literal("target-mobs: " + s.isTargetHostileMobs()), false);
        source.sendFeedback(() -> Text.literal("target-bots: " + s.isTargetOtherBots()), false);
        source.sendFeedback(() -> Text.literal("criticals: " + s.isCriticalsEnabled()), false);
        source.sendFeedback(() -> Text.literal("ranged: " + s.isRangedEnabled()), false);
        source.sendFeedback(() -> Text.literal("mace: " + s.isMaceEnabled()), false);
        source.sendFeedback(() -> Text.literal("special-names: " + s.isUseSpecialNames()), false);
        source.sendFeedback(() -> Text.literal("shield-mace: " + s.isShieldMace()), false);
        source.sendFeedback(() -> Text.literal("attack-cooldown: " + s.getAttackCooldown() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("melee-range: " + s.getMeleeRange()), false);
        source.sendFeedback(() -> Text.literal("move-speed: " + s.getMoveSpeed()), false);
        source.sendFeedback(() -> Text.literal("=== Utilities ==="), false);
        source.sendFeedback(() -> Text.literal("auto-totem: " + s.isAutoTotemEnabled()), false);
        source.sendFeedback(() -> Text.literal("totem-priority: " + s.isTotemPriority() + " (don't replace totem with shield)"), false);
        source.sendFeedback(() -> Text.literal("auto-shield: " + s.isAutoShieldEnabled()), false);
        source.sendFeedback(() -> Text.literal("auto-potion: " + s.isAutoPotionEnabled()), false);
        source.sendFeedback(() -> Text.literal("shield-break: " + s.isShieldBreakEnabled()), false);
        source.sendFeedback(() -> Text.literal("prefer-sword: " + s.isPreferSword()), false);
        source.sendFeedback(() -> Text.literal("bot-leave-on-death: " + s.isBotLeaveOnDeath()), false);
        source.sendFeedback(() -> Text.literal("attack-invincible: " + s.isAttackInvincible()), false);
        source.sendFeedback(() -> Text.literal("aim-speed: " + s.getAimSpeed()), false);
        source.sendFeedback(() -> Text.literal("=== Navigation Settings ==="), false);
        source.sendFeedback(() -> Text.literal("bhop: " + s.isBhopEnabled()), false);

        source.sendFeedback(() -> Text.literal("idle: " + s.isIdleWanderEnabled()), false);
        source.sendFeedback(() -> Text.literal("idle-radius: " + s.getIdleWanderRadius()), false);
        source.sendFeedback(() -> Text.literal("=== Factions & Mistakes ==="), false);
        source.sendFeedback(() -> Text.literal("factions: " + s.isFactionsEnabled()), false);
        source.sendFeedback(() -> Text.literal("friendly-fire: " + s.isFriendlyFireEnabled()), false);
        source.sendFeedback(() -> Text.literal("miss-chance: " + s.getMissChance() + "%"), false);
        source.sendFeedback(() -> Text.literal("mistake-chance: " + s.getMistakeChance() + "%"), false);
        source.sendFeedback(() -> Text.literal("shield-break-chance: " + s.getShieldBreakChance() + "%"), false);

        return 1;
    }

    // ========== BOT-MANAGEMENT HANDLERS ==========

    private static int botAttack(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String botname = StringArgumentType.getString(ctx, "botname");
        String target = StringArgumentType.getString(ctx, "target");
        if (!BotManager.getAllBots().contains(botname)) {
            source.sendError(Text.literal("Bot '" + botname + "' not found!"));
            return 0;
        }
        BotCombat.setTarget(botname, target);
        source.sendFeedback(() -> Text.literal("Bot '" + botname + "' now attacking '" + target + "'"), true);
        return 1;
    }

    private static int botStopAttack(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String botname = StringArgumentType.getString(ctx, "botname");
        if (!BotManager.getAllBots().contains(botname)) {
            source.sendError(Text.literal("Bot '" + botname + "' not found!"));
            return 0;
        }
        BotCombat.clearTarget(botname);
        source.sendFeedback(() -> Text.literal("Bot '" + botname + "' stopped attacking"), true);
        return 1;
    }

    private static int botInventory(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String botname = StringArgumentType.getString(ctx, "botname");
        if (!BotManager.getAllBots().contains(botname)) {
            source.sendError(Text.literal("Bot '" + botname + "' not found!"));
            return 0;
        }
        var bot = source.getServer().getPlayerManager().getPlayer(botname);
        if (bot == null) {
            source.sendError(Text.literal("Bot '" + botname + "' not online!"));
            return 0;
        }
        var inventory = bot.getInventory();
        source.sendFeedback(() -> Text.literal("=== Inventory: " + botname + " ==="), false);
        StringBuilder hotbar = new StringBuilder("Hotbar: ");
        for (int i = 0; i < 9; i++) {
            var stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                hotbar.append("[").append(stack.getName().getString()).append(" x").append(stack.getCount()).append("] ");
            }
        }
        source.sendFeedback(() -> Text.literal(hotbar.toString().trim()), false);
        StringBuilder mainInv = new StringBuilder("Main: ");
        for (int i = 9; i < 36; i++) {
            var stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                mainInv.append("[").append(stack.getName().getString()).append(" x").append(stack.getCount()).append("] ");
            }
        }
        source.sendFeedback(() -> Text.literal(mainInv.toString().trim()), false);
        StringBuilder armor = new StringBuilder("Armor: ");
        for (int i = 36; i < 40; i++) {
            var stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                armor.append("[").append(stack.getName().getString()).append("] ");
            }
        }
        String armorStr = armor.toString().trim();
        if (!armorStr.equals("Armor:")) {
            source.sendFeedback(() -> Text.literal(armorStr), false);
        }
        var offhand = inventory.getStack(40);
        if (!offhand.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Offhand: [" + offhand.getName().getString() + " x" + offhand.getCount() + "]"), false);
        }
        source.sendFeedback(() -> Text.literal("HP: " + String.format("%.1f", bot.getHealth()) + "/" + String.format("%.1f", bot.getMaxHealth()) +
            " | Food: " + bot.getHungerManager().getFoodLevel() +
            " | XP: " + bot.experienceLevel), false);
        return 1;
    }

    private static int botList(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var bots = BotManager.getAllBots();
        if (bots.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No active PvP bots"), false);
        } else {
            source.sendFeedback(() -> Text.literal("Active PvP bots (" + bots.size() + "):"), false);
            for (String botName : bots) {
                source.sendFeedback(() -> Text.literal(" - " + botName), false);
            }
        }
        return bots.size();
    }

    // ========== PATH HANDLERS ==========

    private static int pathCreate(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.createPath(name)) {
            BotPath.setPathVisible(name, true);
            source.sendFeedback(() -> Text.literal("Path '" + name + "' created"), true);
            source.sendFeedback(() -> Text.literal("Visualization enabled. To disable: /pvpbot path show " + name + " false"), false);
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' already exists"));
            return 0;
        }
    }

    private static int pathDelete(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.deletePath(name)) {
            source.sendFeedback(() -> Text.literal("Path '" + name + "' deleted"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathAddPoint(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Only players can add path points"));
            return 0;
        }
        Vec3d pos = new Vec3d(player.getX(), player.getY(), player.getZ());
        if (BotPath.addPoint(name, pos)) {
            var path = BotPath.getPath(name);
            if (!BotPath.isPathVisible(name)) {
                BotPath.setPathVisible(name, true);
                source.sendFeedback(() -> Text.literal("Visualization enabled. To disable: /pvpbot path show " + name + " false"), false);
            }
            source.sendFeedback(() -> Text.literal(String.format("Point #%d added to path '%s' at (%.1f, %.1f, %.1f)", path.points.size(), name, pos.x, pos.y, pos.z)), true);
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathRemovePoint(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        int index = IntegerArgumentType.getInteger(ctx, "index");
        if (BotPath.removePoint(name, index)) {
            source.sendFeedback(() -> Text.literal("Point #" + index + " removed from path '" + name + "'"), true);
            return 1;
        }
        source.sendError(Text.literal("Invalid path or index"));
        return 0;
    }

    private static int pathRemovePointLast(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.removeLastPoint(name)) {
            source.sendFeedback(() -> Text.literal("Last point removed from path '" + name + "'"), true);
            return 1;
        }
        source.sendError(Text.literal("Invalid path or index"));
        return 0;
    }

    private static int pathClear(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPath.clearPath(name)) {
            source.sendFeedback(() -> Text.literal("All points cleared from path '" + name + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathLoop(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        boolean value = BoolArgumentType.getBool(ctx, "value");
        if (BotPath.setLoop(name, value)) {
            source.sendFeedback(() -> Text.literal("Path '" + name + "' loop: " + value), true);
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathAttack(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        boolean value = BoolArgumentType.getBool(ctx, "value");
        if (BotPath.setAttack(name, value)) {
            if (value) {
                source.sendFeedback(() -> Text.literal("Path '" + name + "' attack: enabled"), true);
            } else {
                source.sendFeedback(() -> Text.literal("Path '" + name + "' attack: disabled"), true);
                source.sendFeedback(() -> Text.literal("Bot will ignore attacks and continue following path"), false);
            }
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathStart(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String bot = StringArgumentType.getString(ctx, "bot");
        String path = StringArgumentType.getString(ctx, "path");
        if (BotPath.startFollowing(bot, path)) {
            source.sendFeedback(() -> Text.literal("Bot '" + bot + "' started following path '" + path + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + path + "' not found or empty"));
            return 0;
        }
    }

    private static int pathStop(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String bot = StringArgumentType.getString(ctx, "bot");
        if (BotPath.stopFollowing(bot)) {
            source.sendFeedback(() -> Text.literal("Bot '" + bot + "' stopped following path"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Bot '" + bot + "' is not following any path"));
            return 0;
        }
    }

    private static int pathList(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var paths = BotPath.getAllPaths();
        if (paths.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No paths created"), false);
            return 0;
        }
        source.sendFeedback(() -> Text.literal("=== Paths ==="), false);
        for (var entry : paths.entrySet()) {
            String name = entry.getKey();
            var path = entry.getValue();
            source.sendFeedback(() -> Text.literal(String.format("%s: %d points, loop: %s, attack: %s", name, path.points.size(), path.loop, path.attack)), false);
        }
        return paths.size();
    }

    private static int pathShow(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        boolean visible = BoolArgumentType.getBool(ctx, "visible");
        if (BotPath.setPathVisible(name, visible)) {
            if (visible) {
                source.sendFeedback(() -> Text.literal("Path '" + name + "' visualization enabled"), true);
                source.sendFeedback(() -> Text.literal("To disable: /pvpbot path show " + name + " false"), false);
            } else {
                source.sendFeedback(() -> Text.literal("Path '" + name + "' visualization disabled"), true);
            }
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' not found"));
            return 0;
        }
    }

    private static int pathInfo(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        var path = BotPath.getPath(name);
        if (path == null) {
            source.sendError(Text.literal("Path '" + name + "' not found"));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("=== Path: " + name + " ==="), false);
        source.sendFeedback(() -> Text.literal("Points: " + path.points.size()), false);
        source.sendFeedback(() -> Text.literal("Loop: " + path.loop), false);
        source.sendFeedback(() -> Text.literal("Attack: " + path.attack), false);
        for (int i = 0; i < path.points.size(); i++) {
            var point = path.points.get(i);
            int index = i;
            source.sendFeedback(() -> Text.literal(String.format("#%d: (%.1f, %.1f, %.1f)", index, point.x, point.y, point.z)), false);
        }
        return 1;
    }

    private static int pathDistribute(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String pathName = StringArgumentType.getString(ctx, "path");
        var path = BotPath.getPath(pathName);
        if (path == null) {
            source.sendError(Text.literal("Path '" + pathName + "' not found"));
            return 0;
        }
        if (path.points.isEmpty()) {
            source.sendError(Text.literal("Path '" + pathName + "' has no points"));
            return 0;
        }
        var server = source.getServer();
        var botsOnPath = new java.util.ArrayList<String>();
        for (String botName : BotManager.getAllBots()) {
            if (BotPath.isFollowing(botName, pathName)) {
                botsOnPath.add(botName);
            }
        }
        if (botsOnPath.isEmpty()) {
            source.sendError(Text.literal("No bots are following path '" + pathName + "'"));
            return 0;
        }
        int totalPoints = path.points.size();
        int botCount = botsOnPath.size();
        for (int i = 0; i < botCount; i++) {
            String botName = botsOnPath.get(i);
            int pointIndex = (i * totalPoints) / botCount;
            BotPath.setBotPathIndex(botName, pointIndex);
            var point = path.points.get(pointIndex);
            try {
                String tpCommand = String.format(java.util.Locale.US, "tp %s %.2f %.2f %.2f", botName, point.x, point.y + 1.0, point.z);
                server.getCommandManager().getDispatcher().execute(tpCommand, server.getCommandSource());
            } catch (Exception e) {
            }
        }
        source.sendFeedback(() -> Text.literal("Distributed " + botCount + " bots along path '" + pathName + "'"), true);
        return botCount;
    }

    private static int pathStartNear(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String pathName = StringArgumentType.getString(ctx, "path");
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        var path = BotPath.getPath(pathName);
        if (path == null) {
            source.sendError(Text.literal("Path '" + pathName + "' not found"));
            return 0;
        }
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command can only be used by a player"));
            return 0;
        }
        var server = source.getServer();
        int started = 0;
        for (String botName : BotManager.getAllBots()) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(botName);
            if (bot != null && bot.distanceTo(player) <= radius) {
                if (BotPath.startFollowing(botName, pathName)) {
                    started++;
                }
            }
        }
        if (started > 0) {
            int finalStarted = started;
            source.sendFeedback(() -> Text.literal("Started path '" + pathName + "' for " + finalStarted + " bots within " + radius + " blocks"), true);
            return started;
        } else {
            source.sendError(Text.literal("No bots found within " + radius + " blocks"));
            return 0;
        }
    }

    private static int pathStopAll(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String pathName = StringArgumentType.getString(ctx, "path");
        var path = BotPath.getPath(pathName);
        if (path == null) {
            source.sendError(Text.literal("Path '" + pathName + "' not found"));
            return 0;
        }
        int stopped = 0;
        for (String botName : BotManager.getAllBots()) {
            if (BotPath.isFollowing(botName, pathName)) {
                if (BotPath.stopFollowing(botName)) {
                    stopped++;
                }
            }
        }
        if (stopped > 0) {
            int finalStopped = stopped;
            source.sendFeedback(() -> Text.literal("Stopped " + finalStopped + " bots on path '" + pathName + "'"), true);
            return stopped;
        } else {
            source.sendError(Text.literal("No bots are following path '" + pathName + "'"));
            return 0;
        }
    }

    // ========== KIT HANDLERS ==========

    private static int kitCreate(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        var player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player!"));
            return 0;
        }
        if (BotKits.kitExists(name)) {
            source.sendError(Text.literal("Kit '" + name + "' already exists!"));
            return 0;
        }
        if (BotKits.createKit(name, player)) {
            source.sendFeedback(() -> Text.literal("Kit '" + name + "' created from your inventory!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to create kit (empty inventory?)"));
            return 0;
        }
    }

    private static int kitDelete(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotKits.deleteKit(name)) {
            source.sendFeedback(() -> Text.literal("Kit '" + name + "' deleted!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Kit '" + name + "' not found!"));
            return 0;
        }
    }

    private static int kitGive(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String playername = StringArgumentType.getString(ctx, "playername");
        String kitname = StringArgumentType.getString(ctx, "kitname");
        if (!BotKits.kitExists(kitname)) {
            source.sendError(Text.literal("Kit '" + kitname + "' not found!"));
            return 0;
        }
        var player = source.getServer().getPlayerManager().getPlayer(playername);
        if (player == null) {
            source.sendError(Text.literal("Player '" + playername + "' not found!"));
            return 0;
        }
        if (BotKits.giveKit(kitname, player)) {
            source.sendFeedback(() -> Text.literal("Gave kit '" + kitname + "' to '" + playername + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to give kit!"));
            return 0;
        }
    }

    private static int kitList(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var kits = BotKits.getKitNames();
        if (kits.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No kits created. Use /pvpbot kit create-kit <name> to create one."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Kits (" + kits.size() + "): " + String.join(", ", kits)), false);
        }
        return 1;
    }

    // ========== FACTION HANDLERS ==========

    private static int factionList(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var factions = BotFaction.getAllFactions();
        if (factions.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No factions created"), false);
        } else {
            source.sendFeedback(() -> Text.literal("Factions (" + factions.size() + "):"), false);
            for (String faction : factions) {
                var members = BotFaction.getMembers(faction);
                var enemies = BotFaction.getHostileFactions(faction);
                source.sendFeedback(() -> Text.literal(" - " + faction + " (" + members.size() + " members, " + enemies.size() + " enemies)"), false);
            }
        }
        return factions.size();
    }

    private static int factionCreate(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotFaction.createFaction(name)) {
            source.sendFeedback(() -> Text.literal("Faction '" + name + "' created!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Faction '" + name + "' already exists!"));
            return 0;
        }
    }

    private static int factionDelete(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotFaction.deleteFaction(name)) {
            source.sendFeedback(() -> Text.literal("Faction '" + name + "' deleted!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Faction '" + name + "' not found!"));
            return 0;
        }
    }

    private static int factionAdd(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String player = StringArgumentType.getString(ctx, "player");
        if (BotFaction.addMember(faction, player)) {
            source.sendFeedback(() -> Text.literal("Added '" + player + "' to faction '" + faction + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
    }

    private static int factionRemove(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String player = StringArgumentType.getString(ctx, "player");
        if (BotFaction.removeMember(faction, player)) {
            source.sendFeedback(() -> Text.literal("Removed '" + player + "' from faction '" + faction + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to remove '" + player + "' from faction '" + faction + "'"));
            return 0;
        }
    }

    private static int factionHostile(CommandContext<ServerCommandSource> ctx, boolean isHostile) {
        var source = ctx.getSource();
        String faction1 = StringArgumentType.getString(ctx, "faction1");
        String faction2 = StringArgumentType.getString(ctx, "faction2");
        if (BotFaction.setHostile(faction1, faction2, isHostile)) {
            if (isHostile) {
                source.sendFeedback(() -> Text.literal("Factions '" + faction1 + "' and '" + faction2 + "' are now hostile!"), true);
            } else {
                source.sendFeedback(() -> Text.literal("Factions '" + faction1 + "' and '" + faction2 + "' are now neutral"), true);
            }
            return 1;
        } else {
            source.sendError(Text.literal("One or both factions not found, or same faction!"));
            return 0;
        }
    }

    private static int factionInfo(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        var members = BotFaction.getMembers(faction);
        var enemies = BotFaction.getHostileFactions(faction);
        if (members.isEmpty() && enemies.isEmpty() && !BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("=== Faction: " + faction + " ==="), false);
        source.sendFeedback(() -> Text.literal("Members (" + members.size() + "): " + String.join(", ", members)), false);
        source.sendFeedback(() -> Text.literal("Hostile to (" + enemies.size() + "): " + String.join(", ", enemies)), false);
        return 1;
    }

    private static int factionAddNear(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        var entity = source.getEntity();
        if (entity == null) {
            source.sendError(Text.literal("This command must be run by a player!"));
            return 0;
        }
        int count = 0;
        var allBots = BotManager.getAllBots();
        var server = source.getServer();
        for (String botName : allBots) {
            var bot = server.getPlayerManager().getPlayer(botName);
            if (bot != null && bot.distanceTo(entity) <= radius) {
                BotFaction.addMember(faction, botName);
                count++;
            }
        }
        int finalCount = count;
        source.sendFeedback(() -> Text.literal("Added " + finalCount + " bots to faction '" + faction + "'"), true);
        return count;
    }

    private static int factionAddAll(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        var allBots = BotManager.getAllBots();
        int count = 0;
        for (String botName : allBots) {
            BotFaction.addMember(faction, botName);
            count++;
        }
        int finalCount = count;
        source.sendFeedback(() -> Text.literal("Added " + finalCount + " bots to faction '" + faction + "'"), true);
        return count;
    }

    private static int factionGive(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String itemCommand = StringArgumentType.getString(ctx, "item");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        var members = BotFaction.getMembers(faction);
        var server = source.getServer();
        int count = 0;
        for (String memberName : members) {
            try {
                server.getCommandManager().getDispatcher().execute("give " + memberName + " " + itemCommand, server.getCommandSource());
                count++;
            } catch (Exception e) {
            }
        }
        int finalCount = count;
        source.sendFeedback(() -> Text.literal("Gave items to " + finalCount + " members of faction '" + faction + "'"), true);
        return count;
    }

    private static int factionAttack(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String target = StringArgumentType.getString(ctx, "target");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        var members = BotFaction.getMembers(faction);
        int count = 0;
        for (String memberName : members) {
            if (BotManager.getAllBots().contains(memberName)) {
                BotCombat.setTarget(memberName, target);
                count++;
            }
        }
        int finalCount = count;
        source.sendFeedback(() -> Text.literal("Faction '" + faction + "' (" + finalCount + " bots) attacking " + target + "!"), true);
        return count;
    }

    private static int factionStartPath(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String path = StringArgumentType.getString(ctx, "path");
        var members = BotFaction.getMembers(faction);
        if (members.isEmpty()) {
            source.sendError(Text.literal("Faction '" + faction + "' not found or has no members"));
            return 0;
        }
        var botPath = BotPath.getPath(path);
        if (botPath == null) {
            source.sendError(Text.literal("Path '" + path + "' not found"));
            return 0;
        }
        int started = 0;
        for (String member : members) {
            if (BotManager.getAllBots().contains(member)) {
                if (BotPath.startFollowing(member, path)) {
                    started++;
                }
            }
        }
        if (started > 0) {
            int finalStarted = started;
            source.sendFeedback(() -> Text.literal("Started path '" + path + "' for " + finalStarted + " bots in faction '" + faction + "'"), true);
            return started;
        } else {
            source.sendError(Text.literal("No bots in faction '" + faction + "'"));
            return 0;
        }
    }

    private static int factionStopPath(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        var members = BotFaction.getMembers(faction);
        if (members.isEmpty()) {
            source.sendError(Text.literal("Faction '" + faction + "' not found or has no members"));
            return 0;
        }
        int stopped = 0;
        for (String member : members) {
            if (BotManager.getAllBots().contains(member)) {
                if (BotPath.stopFollowing(member)) {
                    stopped++;
                }
            }
        }
        if (stopped > 0) {
            int finalStopped = stopped;
            source.sendFeedback(() -> Text.literal("Stopped path for " + finalStopped + " bots in faction '" + faction + "'"), true);
            return stopped;
        } else {
            source.sendError(Text.literal("No bots in faction '" + faction + "' were following a path"));
            return 0;
        }
    }

    private static int factionGiveKit(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        String kitname = StringArgumentType.getString(ctx, "kitname");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        if (!BotKits.kitExists(kitname)) {
            source.sendError(Text.literal("Kit '" + kitname + "' not found!"));
            return 0;
        }
        var members = BotFaction.getMembers(faction);
        if (members == null || members.isEmpty()) {
            source.sendError(Text.literal("Faction '" + faction + "' has no members!"));
            return 0;
        }
        int count = 0;
        for (String memberName : members) {
            if (BotManager.getAllBots().contains(memberName)) {
                var bot = BotManager.getBot(source.getServer(), memberName);
                if (bot != null && BotKits.giveKit(kitname, bot)) {
                    count++;
                }
            }
        }
        int finalCount = count;
        source.sendFeedback(() -> Text.literal("Gave kit '" + kitname + "' to " + finalCount + " bots in faction '" + faction + "'"), true);
        return 1;
    }
}
