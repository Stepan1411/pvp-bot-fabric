package org.stepan1411.pvp_bot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import org.stepan1411.pvp_bot.bot.BotCombat;
import org.stepan1411.pvp_bot.bot.BotFaction;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotNameGenerator;
import org.stepan1411.pvp_bot.bot.BotPath;
import org.stepan1411.pvp_bot.bot.BotPresets;
import org.stepan1411.pvp_bot.bot.BotSettings;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BotCommand {

    private static final String BASE_PERMISSION = "pvpbot.use";
    private static final int ADMIN_OP_LEVEL = 2;

    private static String commandPerm(String path) {
        return "pvpbot.command." + path;
    }

    private static Predicate<ServerCommandSource> requirePerm(String node) {
        return Permissions.require(node, PermissionLevel.fromLevel(ADMIN_OP_LEVEL));
    }

    private static final SuggestionProvider<ServerCommandSource> BOT_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String bot : BotManager.getAllBots()) {
                if (bot.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(bot);
                }
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> KIT_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String kit : BotKits.getKitNames()) {
                if (kit.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(kit);
                }
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> PRESET_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String preset : BotPresets.getPresetNames()) {
                if (preset.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(preset);
                }
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (var player : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                String name = player.getName().getString();
                if (name.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        };

    private static final SuggestionProvider<ServerCommandSource> PATH_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String pathName : BotPath.getAllPaths().keySet()) {
                if (pathName.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(pathName);
                }
            }
            return builder.buildFuture();
        };

    private static LiteralArgumentBuilder<ServerCommandSource> cmd(String literal, String description, String permPath) {
        return CommandManager.literal(literal)
            .requires(requirePerm(commandPerm(permPath)))
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§7[Tip] §a" + description), false);
                return 1;
            });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> cmd(String literal, String description) {
        return CommandManager.literal(literal)
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§7[Tip] §a" + description), false);
                return 1;
            });
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(cmd("pvpbot", "PvP Bot control")
            .requires(requirePerm(BASE_PERMISSION))

            // ========== SPAWN ==========
            .then(CommandManager.literal("spawn")
                .requires(requirePerm(commandPerm("spawn")))
                .executes(ctx -> spawn(ctx, null))
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> spawn(ctx, StringArgumentType.getString(ctx, "name")))))

            // ========== REMOVE ==========
            .then(cmd("remove", "Remove a bot", "remove")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(BOT_SUGGESTIONS)
                    .executes(BotCommand::remove)))

            // ========== REMOVEALL ==========
            .then(CommandManager.literal("reload")
                .requires(requirePerm(commandPerm("reload")))
                .executes(BotCommand::reload))
            .then(CommandManager.literal("removeall")
                .requires(requirePerm(commandPerm("removeall")))
                .executes(BotCommand::removeAll))

            // ========== SETTINGS ==========
            .then(buildSettings())

            // ========== BOT-MANAGEMENT ==========
            .then(cmd("bot-management", "Bot management", "bot-management")
                .then(cmd("mass-spawn", "Mass spawn bots", "bot-management.mass-spawn")
                    .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 10000))
                        .executes(BotCommand::massSpawn)))
                .then(cmd("attack", "Attack a target", "bot-management.attack")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .then(CommandManager.argument("target", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::botAttack))))
                .then(cmd("stop-attack", "Stop attack", "bot-management.stop-attack")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(BotCommand::botStopAttack)))
                .then(cmd("inventory", "Show bot inventory", "bot-management.inventory")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(BotCommand::botInventory)))
                .then(CommandManager.literal("list")
                    .requires(requirePerm(commandPerm("bot-management.list")))
                    .executes(BotCommand::botList))
                .then(cmd("path", "Path management", "bot-management.path")
                    .then(cmd("create", "Create a path", "bot-management.path.create")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(BotCommand::pathCreate)))
                    .then(cmd("delete", "Delete a path", "bot-management.path.delete")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathDelete)))
                    .then(cmd("add-point", "Add point to path", "bot-management.path.add-point")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathAddPoint)))
                    .then(cmd("remove-point", "Remove point from path", "bot-management.path.remove-point")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(CommandManager.argument("index", IntegerArgumentType.integer())
                                .executes(BotCommand::pathRemovePoint))
                            .executes(BotCommand::pathRemovePointLast)))
                    .then(cmd("clear", "Clear all path points", "bot-management.path.clear")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathClear)))
                    .then(cmd("loop", "Toggle path looping", "bot-management.path.loop")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(CommandManager.argument("value", BoolArgumentType.bool())
                                .executes(BotCommand::pathLoop))))
                    .then(cmd("start", "Start bot on path", "bot-management.path.start")
                        .then(CommandManager.argument("bot", StringArgumentType.word())
                            .suggests(BOT_SUGGESTIONS)
                            .then(CommandManager.argument("path", StringArgumentType.word())
                                .suggests(PATH_SUGGESTIONS)
                                .executes(BotCommand::pathStart))))
                    .then(cmd("stop", "Stop bot on path", "bot-management.path.stop")
                        .then(CommandManager.argument("bot", StringArgumentType.word())
                            .suggests(BOT_SUGGESTIONS)
                            .executes(BotCommand::pathStop)))
                    .then(CommandManager.literal("list")
                        .requires(requirePerm(commandPerm("bot-management.path.list")))
                        .executes(BotCommand::pathList))
                    .then(cmd("show", "Show/hide path particles", "bot-management.path.show")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(CommandManager.argument("visible", BoolArgumentType.bool())
                                .executes(BotCommand::pathShow))))
                    .then(cmd("info", "Path info", "bot-management.path.info")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathInfo)))
                    .then(cmd("distribute", "Distribute bots along path", "bot-management.path.distribute")
                        .then(CommandManager.argument("path", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathDistribute)))
                    .then(cmd("start-near", "Start bots near path", "bot-management.path.start-near")
                        .then(CommandManager.argument("path", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1))
                                .executes(BotCommand::pathStartNear))))
                    .then(cmd("stop-all", "Stop all bots on path", "bot-management.path.stop-all")
                        .then(CommandManager.argument("path", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .executes(BotCommand::pathStopAll)))
                    .then(cmd("walk-type", "Path walk type", "bot-management.path.walk-type")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(PATH_SUGGESTIONS)
                            .then(CommandManager.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("bhop");
                                    builder.suggest("sprint");
                                    builder.suggest("walk");
                                    return builder.buildFuture();
                                })
                                .executes(BotCommand::pathWalkType))))))

            // ========== KIT ==========
            .then(cmd("kit", "Kit management", "kit")
                .then(cmd("create-kit", "Create kit from inventory", "kit.create-kit")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(BotCommand::kitCreate)))
                .then(cmd("delete-kit", "Delete a kit", "kit.delete-kit")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(KIT_SUGGESTIONS)
                        .executes(BotCommand::kitDelete)))
                .then(cmd("give-kit", "Give kit to player", "kit.give-kit")
                    .then(CommandManager.argument("playername", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTIONS)
                        .then(CommandManager.argument("kitname", StringArgumentType.word())
                            .suggests(KIT_SUGGESTIONS)
                            .executes(BotCommand::kitGive))))
                .then(cmd("give-kit-near", "Give kit to bots within radius", "kit.give-kit-near")
                    .then(CommandManager.argument("kitname", StringArgumentType.word())
                        .suggests(KIT_SUGGESTIONS)
                        .executes(ctx -> kitGiveNear(ctx, 10.0))
                        .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1, 1000))
                            .executes(ctx -> kitGiveNear(ctx, DoubleArgumentType.getDouble(ctx, "radius"))))))
                .then(cmd("give-kit-near-random", "Give random kit to bots within radius", "kit.give-kit-near-random")
                    .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1, 1000))
                        .then(CommandManager.argument("kits", StringArgumentType.greedyString())
                            .suggests(RANDOM_KIT_SUGGESTIONS)
                            .executes(BotCommand::kitGiveNearRandom))))
                .then(cmd("kits", "List all kits", "kit.kits")
                    .executes(BotCommand::kitList)))

            // ========== FACTION ==========
            .then(cmd("faction", "Faction management", "faction")
                .then(cmd("list", "List all factions", "faction.list")
                    .executes(BotCommand::factionList))
                .then(cmd("create", "Create a faction", "faction.create")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(BotCommand::factionCreate)))
                .then(cmd("delete", "Delete a faction", "faction.delete")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .executes(BotCommand::factionDelete)))
                .then(cmd("add", "Add player to faction", "faction.add")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(CommandManager.argument("player", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionAdd))))
                .then(cmd("remove", "Remove player from faction", "faction.remove")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(CommandManager.argument("player", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionRemove))))
                .then(cmd("hostile", "Set faction hostility", "faction.hostile")
                    .then(CommandManager.argument("faction1", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(CommandManager.argument("faction2", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("hostile", BoolArgumentType.bool())
                                .executes(ctx -> factionHostile(ctx, BoolArgumentType.getBool(ctx, "hostile"))))
                            .executes(ctx -> factionHostile(ctx, true)))))
                .then(cmd("info", "Faction info", "faction.info")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .executes(BotCommand::factionInfo)))
                .then(cmd("add-near", "Add nearby players to faction", "faction.add-near")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1, 10000))
                            .executes(BotCommand::factionAddNear))))
                .then(cmd("add-all", "Add all players to faction", "faction.add-all")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .executes(BotCommand::factionAddAll)))
                .then(cmd("give", "Give item to faction", "faction.give")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(CommandManager.argument("item", StringArgumentType.greedyString())
                            .executes(BotCommand::factionGive))))
                .then(cmd("attack", "Faction attack target", "faction.attack")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(CommandManager.argument("target", StringArgumentType.word())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionAttack))))
                .then(cmd("path", "Faction path control", "faction.path")
                    .then(cmd("start", "Start faction on path", "faction.path.start")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("path", StringArgumentType.word())
                                .suggests(PATH_SUGGESTIONS)
                                .executes(BotCommand::factionStartPath))))
                    .then(cmd("stop", "Stop faction on path", "faction.path.stop")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .executes(BotCommand::factionStopPath))))
                .then(cmd("tp", "Teleport faction bots to location", "faction.tp")
                    .then(CommandManager.argument("faction", StringArgumentType.word())
                        .suggests(FACTION_SUGGESTIONS)
                        .then(CommandManager.argument("location", StringArgumentType.greedyString())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(BotCommand::factionTp))))
                .then(cmd("kit", "Faction kit management", "faction.kit")
                    .then(cmd("give-kit", "Give kit to faction", "faction.kit.give-kit")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("kitname", StringArgumentType.word())
                                .suggests(KIT_SUGGESTIONS)
                                .executes(BotCommand::factionGiveKit))))
                    .then(cmd("give-kit-random", "Give random kit to faction by percentage", "faction.kit.give-kit-random")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("kits", StringArgumentType.greedyString())
                                .suggests(RANDOM_KIT_SUGGESTIONS)
                                .executes(BotCommand::factionGiveKitRandom))))))

        );
    }

    private static final SuggestionProvider<ServerCommandSource> RANDOM_KIT_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining();
            int tokenCount;
            String partial;
            if (remaining.isEmpty()) {
                tokenCount = 0;
                partial = "";
            } else {
                int lastSpace = remaining.lastIndexOf(' ');
                if (lastSpace >= 0) {
                    partial = remaining.substring(lastSpace + 1);
                    tokenCount = remaining.substring(0, lastSpace).split(" ", -1).length;
                    if (tokenCount == 1 && remaining.substring(0, lastSpace).isEmpty()) tokenCount = 0;
                } else {
                    partial = remaining;
                    tokenCount = 0;
                }
            }
            var wordBuilder = builder.createOffset(builder.getStart() + remaining.length() - partial.length());
            if (tokenCount % 2 == 1) {
                for (int i = 1; i <= 100; i++) {
                    wordBuilder.suggest(i + "%");
                }
            } else {
                for (String kit : BotKits.getKitNames()) {
                    if (kit.regionMatches(true, 0, partial, 0, partial.length())) {
                        wordBuilder.suggest(kit);
                    }
                }
            }
            return wordBuilder.buildFuture();
        };

    // ========== SETTINGS BUILDER ==========

    private static LiteralArgumentBuilder<ServerCommandSource> buildSettings() {
        var settings = CommandManager.literal("settings")
            .requires(requirePerm(commandPerm("settings")))
            .executes(BotCommand::settings);

        settings.then(boolSetting("auto-armor", "Auto-equip armor", () -> BotSettings.get().isAutoEquipArmor(), v -> BotSettings.get().setAutoEquipArmor(v), BotSettings.DEFAULTS::isAutoEquipArmor, "settings.auto-armor"));
        settings.then(boolSetting("auto-weapon", "Auto-equip best weapon", () -> BotSettings.get().isAutoEquipWeapon(), v -> BotSettings.get().setAutoEquipWeapon(v), BotSettings.DEFAULTS::isAutoEquipWeapon, "settings.auto-weapon"));
        settings.then(boolSetting("drop-armor", "Drop worse armor on pickup", () -> BotSettings.get().isDropWorseArmor(), v -> BotSettings.get().setDropWorseArmor(v), BotSettings.DEFAULTS::isDropWorseArmor, "settings.drop-armor"));
        settings.then(boolSetting("drop-weapon", "Drop worse weapons on pickup", () -> BotSettings.get().isDropWorseWeapons(), v -> BotSettings.get().setDropWorseWeapons(v), BotSettings.DEFAULTS::isDropWorseWeapons, "settings.drop-weapon"));
        settings.then(doubleSetting("drop-distance", "Drop check distance", () -> BotSettings.get().getDropDistance(), v -> BotSettings.get().setDropDistance(v), 1.0, 10.0, BotSettings.DEFAULTS::getDropDistance, "settings.drop-distance"));
        settings.then(intSetting("interval", "Equipment check interval (ticks)", () -> BotSettings.get().getCheckInterval(), v -> BotSettings.get().setCheckInterval(v), 1, 100, BotSettings.DEFAULTS::getCheckInterval, "settings.interval"));
        settings.then(boolSetting("combat", "Enable combat AI", () -> BotSettings.get().isCombatEnabled(), v -> BotSettings.get().setCombatEnabled(v), BotSettings.DEFAULTS::isCombatEnabled, "settings.combat"));
        settings.then(boolSetting("revenge", "Auto-attack last damager", () -> BotSettings.get().isRevengeEnabled(), v -> BotSettings.get().setRevengeEnabled(v), BotSettings.DEFAULTS::isRevengeEnabled, "settings.revenge"));
        settings.then(boolSetting("auto-target", "Auto-target nearest player", () -> BotSettings.get().isAutoTargetEnabled(), v -> BotSettings.get().setAutoTargetEnabled(v), BotSettings.DEFAULTS::isAutoTargetEnabled, "settings.auto-target"));
        settings.then(boolSetting("target-players", "Target real players", () -> BotSettings.get().isTargetPlayers(), v -> BotSettings.get().setTargetPlayers(v), BotSettings.DEFAULTS::isTargetPlayers, "settings.target-players"));
        settings.then(boolSetting("target-mobs", "Target hostile mobs", () -> BotSettings.get().isTargetHostileMobs(), v -> BotSettings.get().setTargetHostileMobs(v), BotSettings.DEFAULTS::isTargetHostileMobs, "settings.target-mobs"));
        settings.then(boolSetting("target-bots", "Target other bots", () -> BotSettings.get().isTargetOtherBots(), v -> BotSettings.get().setTargetOtherBots(v), BotSettings.DEFAULTS::isTargetOtherBots, "settings.target-bots"));
        settings.then(boolSetting("criticals", "Jump-crit on melee attacks", () -> BotSettings.get().isCriticalsEnabled(), v -> BotSettings.get().setCriticalsEnabled(v), BotSettings.DEFAULTS::isCriticalsEnabled, "settings.criticals"));
        settings.then(intSetting("crit-fall-ticks", "Falling ticks before crit attack", () -> BotSettings.get().getCriticalFallTicks(), v -> BotSettings.get().setCriticalFallTicks(v), 1, 10, BotSettings.DEFAULTS::getCriticalFallTicks, "settings.crit-fall-ticks"));
        settings.then(boolSetting("ranged", "Use bows/crossbows", () -> BotSettings.get().isRangedEnabled(), v -> BotSettings.get().setRangedEnabled(v), BotSettings.DEFAULTS::isRangedEnabled, "settings.ranged"));
        settings.then(doubleSetting("ranged-min-range", "Min bow distance", () -> BotSettings.get().getRangedMinRange(), v -> BotSettings.get().setRangedMinRange(v), 3.0, 20.0, BotSettings.DEFAULTS::getRangedMinRange, "settings.ranged-min-range"));
        settings.then(doubleSetting("ranged-optimal-range", "Ideal bow distance", () -> BotSettings.get().getRangedOptimalRange(), v -> BotSettings.get().setRangedOptimalRange(v), 10.0, 50.0, BotSettings.DEFAULTS::getRangedOptimalRange, "settings.ranged-optimal-range"));
        settings.then(doubleSetting("ranged-max-range", "Max bow engagement range", () -> BotSettings.get().getRangedMaxRange(), v -> BotSettings.get().setRangedMaxRange(v), 15.0, 100.0, BotSettings.DEFAULTS::getRangedMaxRange, "settings.ranged-max-range"));
        settings.then(intSetting("bow-draw-ticks", "Bow full draw time (ticks)", () -> BotSettings.get().getBowMinDrawTime(), v -> BotSettings.get().setBowMinDrawTime(v), 5, 100, BotSettings.DEFAULTS::getBowMinDrawTime, "settings.bow-draw-ticks"));
        settings.then(boolSetting("arrow-prediction", "Predictive bow aiming", () -> BotSettings.get().isArrowPredictionEnabled(), v -> BotSettings.get().setArrowPredictionEnabled(v), BotSettings.DEFAULTS::isArrowPredictionEnabled, "settings.arrow-prediction"));
        settings.then(boolSetting("ranged-strafe", "Strafe while shooting bow", () -> BotSettings.get().isRangedStrafeEnabled(), v -> BotSettings.get().setRangedStrafeEnabled(v), BotSettings.DEFAULTS::isRangedStrafeEnabled, "settings.ranged-strafe"));
        settings.then(boolSetting("ranged-retreat", "Retreat with bow when target close", () -> BotSettings.get().isRangedRetreatOnClose(), v -> BotSettings.get().setRangedRetreatOnClose(v), BotSettings.DEFAULTS::isRangedRetreatOnClose, "settings.ranged-retreat"));
        settings.then(boolSetting("mace", "Use mace smash attack", () -> BotSettings.get().isMaceEnabled(), v -> BotSettings.get().setMaceEnabled(v), BotSettings.DEFAULTS::isMaceEnabled, "settings.mace"));
        settings.then(boolSetting("special-names", "Use special bot names", () -> BotSettings.get().isUseSpecialNames(), v -> BotSettings.get().setUseSpecialNames(v), BotSettings.DEFAULTS::isUseSpecialNames, "settings.special-names"));
        settings.then(boolSetting("shield-mace", "Auto-shield against mace", () -> BotSettings.get().isShieldMace(), v -> BotSettings.get().setShieldMace(v), BotSettings.DEFAULTS::isShieldMace, "settings.shield-mace"));
        settings.then(intSetting("attack-cooldown", "Ticks between melee attacks", () -> BotSettings.get().getAttackCooldown(), v -> BotSettings.get().setAttackCooldown(v), 1, 40, BotSettings.DEFAULTS::getAttackCooldown, "settings.attack-cooldown"));
        settings.then(intSetting("heal-retreat", "Seconds to retreat before healing", () -> BotSettings.get().getHealRetreatSeconds(), v -> BotSettings.get().setHealRetreatSeconds(v), 0, 10, BotSettings.DEFAULTS::getHealRetreatSeconds, "settings.heal-retreat"));
        settings.then(boolSetting("attack-enemy-heal", "Attack healing enemy (heal only below 1 heart)", () -> BotSettings.get().isAttackWhileEnemyHeals(), v -> BotSettings.get().setAttackWhileEnemyHeals(v), BotSettings.DEFAULTS::isAttackWhileEnemyHeals, "settings.attack-enemy-heal"));
        settings.then(doubleSetting("melee-range", "Melee attack range", () -> BotSettings.get().getMeleeRange(), v -> BotSettings.get().setMeleeRange(v), 2.0, 6.0, BotSettings.DEFAULTS::getMeleeRange, "settings.melee-range"));
        settings.then(doubleSetting("move-speed", "Movement speed multiplier", () -> BotSettings.get().getMoveSpeed(), v -> BotSettings.get().setMoveSpeed(v), 0.1, 2.0, BotSettings.DEFAULTS::getMoveSpeed, "settings.move-speed"));
        settings.then(boolSetting("auto-totem", "Auto-equip totem to offhand", () -> BotSettings.get().isAutoTotemEnabled(), v -> BotSettings.get().setAutoTotemEnabled(v), BotSettings.DEFAULTS::isAutoTotemEnabled, "settings.auto-totem"));
        settings.then(boolSetting("prefer-totem", "Prefer totem: no shield if totem exists, mace-smash only shield in right hand", () -> BotSettings.get().isPreferTotem(), v -> BotSettings.get().setPreferTotem(v), BotSettings.DEFAULTS::isPreferTotem, "settings.prefer-totem"));
        settings.then(boolSetting("auto-shield", "Auto-use shield", () -> BotSettings.get().isAutoShieldEnabled(), v -> BotSettings.get().setAutoShieldEnabled(v), BotSettings.DEFAULTS::isAutoShieldEnabled, "settings.auto-shield"));
        settings.then(boolSetting("auto-potion", "Auto-use potions", () -> BotSettings.get().isAutoPotionEnabled(), v -> BotSettings.get().setAutoPotionEnabled(v), BotSettings.DEFAULTS::isAutoPotionEnabled, "settings.auto-potion"));
        settings.then(boolSetting("shield-break", "Auto-axe enemy shield", () -> BotSettings.get().isShieldBreakEnabled(), v -> BotSettings.get().setShieldBreakEnabled(v), BotSettings.DEFAULTS::isShieldBreakEnabled, "settings.shield-break"));
        settings.then(boolSetting("prefer-sword", "Prefer sword over axe", () -> BotSettings.get().isPreferSword(), v -> BotSettings.get().setPreferSword(v), BotSettings.DEFAULTS::isPreferSword, "settings.prefer-sword"));
        settings.then(boolSetting("bhop", "Bunny-hop movement", () -> BotSettings.get().isBhopEnabled(), v -> BotSettings.get().setBhopEnabled(v), BotSettings.DEFAULTS::isBhopEnabled, "settings.bhop"));
        settings.then(boolSetting("idle", "Wander when idle", () -> BotSettings.get().isIdleWanderEnabled(), v -> BotSettings.get().setIdleWanderEnabled(v), BotSettings.DEFAULTS::isIdleWanderEnabled, "settings.idle"));
        settings.then(doubleSetting("idle-radius", "Idle wander radius", () -> BotSettings.get().getIdleWanderRadius(), v -> BotSettings.get().setIdleWanderRadius(v), 3.0, 50.0, BotSettings.DEFAULTS::getIdleWanderRadius, "settings.idle-radius"));
        settings.then(boolSetting("friendly-fire", "Allow attacking allies", () -> BotSettings.get().isFriendlyFireEnabled(), v -> BotSettings.get().setFriendlyFireEnabled(v), BotSettings.DEFAULTS::isFriendlyFireEnabled, "settings.friendly-fire"));
        settings.then(intSetting("miss-chance", "Chance to miss (%)", () -> BotSettings.get().getMissChance(), v -> BotSettings.get().setMissChance(v), 0, 100, BotSettings.DEFAULTS::getMissChance, "settings.miss-chance"));
        settings.then(intSetting("mistake-chance", "Chance to aim wrong (%)", () -> BotSettings.get().getMistakeChance(), v -> BotSettings.get().setMistakeChance(v), 0, 100, BotSettings.DEFAULTS::getMistakeChance, "settings.mistake-chance"));
        settings.then(intSetting("shield-break-chance", "Shield break attempt chance (%)", () -> BotSettings.get().getShieldBreakChance(), v -> BotSettings.get().setShieldBreakChance(v), 0, 100, BotSettings.DEFAULTS::getShieldBreakChance, "settings.shield-break-chance"));
        settings.then(intSetting("shield-hold-ticks", "Max ticks to hold shield", () -> BotSettings.get().getShieldHoldTicks(), v -> BotSettings.get().setShieldHoldTicks(v), 10, 200, BotSettings.DEFAULTS::getShieldHoldTicks, "settings.shield-hold-ticks"));
        settings.then(intSetting("shield-raise-ticks", "Ticks to predict enemy attack", () -> BotSettings.get().getShieldRaiseTicks(), v -> BotSettings.get().setShieldRaiseTicks(v), 2, 40, BotSettings.DEFAULTS::getShieldRaiseTicks, "settings.shield-raise-ticks"));
        settings.then(boolSetting("retreat", "Retreat when low HP", () -> BotSettings.get().isRetreatEnabled(), v -> BotSettings.get().setRetreatEnabled(v), BotSettings.DEFAULTS::isRetreatEnabled, "settings.retreat"));
        settings.then(boolSetting("auto-eat", "Auto-eat when hungry", () -> BotSettings.get().isAutoEatEnabled(), v -> BotSettings.get().setAutoEatEnabled(v), BotSettings.DEFAULTS::isAutoEatEnabled, "settings.auto-eat"));
        settings.then(boolSetting("auto-mend", "Auto-use XP for mending", () -> BotSettings.get().isAutoMendEnabled(), v -> BotSettings.get().setAutoMendEnabled(v), BotSettings.DEFAULTS::isAutoMendEnabled, "settings.auto-mend"));
        settings.then(boolSetting("bot-leave-on-death", "Bot leaves on death", () -> BotSettings.get().isBotLeaveOnDeath(), v -> BotSettings.get().setBotLeaveOnDeath(v), BotSettings.DEFAULTS::isBotLeaveOnDeath, "settings.bot-leave-on-death"));
        settings.then(boolSetting("attack-invincible", "Attack creative/spectator", () -> BotSettings.get().isAttackInvincible(), v -> BotSettings.get().setAttackInvincible(v), BotSettings.DEFAULTS::isAttackInvincible, "settings.attack-invincible"));
        settings.then(boolSetting("profile-lagg-fix", "Pre-populate profile cache to prevent lag on bot spawn", () -> BotSettings.get().isProfileLagFix(), v -> BotSettings.get().setProfileLagFix(v), BotSettings.DEFAULTS::isProfileLagFix, "settings.profile-lagg-fix"));
        settings.then(boolSetting("safe-spawn", "Spread mass-spawned bots to prevent entity cramming", () -> BotSettings.get().isSafeSpawn(), v -> BotSettings.get().setSafeSpawn(v), BotSettings.DEFAULTS::isSafeSpawn, "settings.safe-spawn"));
        settings.then(boolSetting("clear-on-remove", "Clear bot inventory before remove/kill", () -> BotSettings.get().isClearOnRemove(), v -> BotSettings.get().setClearOnRemove(v), BotSettings.DEFAULTS::isClearOnRemove, "settings.clear-on-remove"));
        settings.then(doubleSetting("aim-speed", "Aim rotation speed", () -> BotSettings.get().getAimSpeed(), v -> BotSettings.get().setAimSpeed(v), 3.0, 45.0, BotSettings.DEFAULTS::getAimSpeed, "settings.aim-speed"));
        settings.then(doubleSetting("view-distance", "Max target acquisition range", () -> BotSettings.get().getMaxTargetDistance(), v -> BotSettings.get().setMaxTargetDistance(v), 5.0, 128.0, BotSettings.DEFAULTS::getMaxTargetDistance, "settings.view-distance"));
        settings.then(intSetting("max-mass-spawn", "Max bots per mass-spawn command", () -> BotSettings.get().getMaxMassSpawn(), v -> BotSettings.get().setMaxMassSpawn(v), 50, 10000, BotSettings.DEFAULTS::getMaxMassSpawn, "settings.max-mass-spawn"));

        settings.then(cmd("preset", "Save/load settings presets", "settings.preset")
            .then(cmd("save", "Save current settings as a preset", "settings.preset.save")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(PRESET_SUGGESTIONS)
                    .executes(BotCommand::presetSave)))
            .then(cmd("load", "Apply a saved settings preset", "settings.preset.load")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(PRESET_SUGGESTIONS)
                    .executes(BotCommand::presetLoad)))
            .then(cmd("delete", "Delete a settings preset", "settings.preset.delete")
                .then(CommandManager.argument("name", StringArgumentType.word())
                    .suggests(PRESET_SUGGESTIONS)
                    .executes(BotCommand::presetDelete)))
            .then(cmd("list", "List all settings presets", "settings.preset.list")
                .executes(BotCommand::presetList)));

        return settings;
    }

    // ========== FAST COMMANDS ==========

    private static final SuggestionProvider<ServerCommandSource> FACTION_SUGGESTIONS =
        (ctx, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
            for (String f : BotFaction.getAllFactions()) {
                if (f.toLowerCase(java.util.Locale.ROOT).contains(remaining)) {
                    builder.suggest(f);
                }
            }
            return builder.buildFuture();
        };

    // ========== SETTING HELPERS ==========

    private static LiteralArgumentBuilder<ServerCommandSource> boolSetting(String name, String desc, BooleanSupplier getter, Consumer<Boolean> setter, BooleanSupplier defaultGetter, String permPath) {
        return CommandManager.literal(name)
            .requires(requirePerm(commandPerm(permPath)))
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§7[Tip] §a" + desc + " §7(current: " + getter.getAsBoolean() + ", default: " + defaultGetter.getAsBoolean() + ")"), false);
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

    private static LiteralArgumentBuilder<ServerCommandSource> intSetting(String name, String desc, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, int min, int max, java.util.function.IntSupplier defaultGetter, String permPath) {
        return CommandManager.literal(name)
            .requires(requirePerm(commandPerm(permPath)))
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§7[Tip] §a" + desc + " §7(current: " + getter.getAsInt() + ", default: " + defaultGetter.getAsInt() + ")"), false);
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

    private static LiteralArgumentBuilder<ServerCommandSource> doubleSetting(String name, String desc, java.util.function.DoubleSupplier getter, java.util.function.DoubleConsumer setter, double min, double max, java.util.function.DoubleSupplier defaultGetter, String permPath) {
        return CommandManager.literal(name)
            .requires(requirePerm(commandPerm(permPath)))
            .executes(ctx -> {
                ctx.getSource().sendFeedback(() -> Text.literal("§7[Tip] §a" + desc + " §7(current: " + getter.getAsDouble() + ", default: " + defaultGetter.getAsDouble() + ")"), false);
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
        if (name != null && name.length() > 16) {
            source.sendError(Text.literal("Bot name '" + name + "' is too long (" + name.length() + " characters). Minecraft names are limited to 16 characters."));
            return 0;
        }
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
        int maxAllowed = BotSettings.get().getMaxMassSpawn();
        if (count > maxAllowed) {
            source.sendError(Text.literal("Cannot spawn more than " + maxAllowed + " bots at once. Use /pvpbot settings max-mass-spawn to increase the limit."));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Spawning " + count + " bots..."), false);
        int[] spawned = {0};
        int[] current = {0};
        Vec3d basePos = null;
        if (BotSettings.get().isSafeSpawn()) {
            var player = source.getPlayer();
            if (player != null) {
                basePos = new Vec3d(player.getX(), player.getY(), player.getZ());
            }
        }
        scheduleSpawn(server, source, count, spawned, current, basePos);
        return 1;
    }

    private static void scheduleSpawn(MinecraftServer server, ServerCommandSource source, int total, int[] spawned, int[] current, Vec3d basePos) {
        if (current[0] >= total) {
            source.sendFeedback(() -> Text.literal("Finished! Spawned " + spawned[0] + " bots."), true);
            return;
        }
        String name = BotNameGenerator.generateUniqueName();
        Vec3d spawnPos = null;
        if (basePos != null) {
            var rng = java.util.concurrent.ThreadLocalRandom.current();
            double dx = (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
            double dz = (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
            spawnPos = new Vec3d(basePos.x + dx, basePos.y, basePos.z + dz);
        }
        if (BotManager.spawnBot(server, name, source, spawnPos)) {
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
                        scheduleSpawn(server, source, total, spawned, current, basePos);
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
        BotPresets.reload(server);
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
        source.sendFeedback(() -> Text.literal("ranged-min-range: " + s.getRangedMinRange()), false);
        source.sendFeedback(() -> Text.literal("ranged-optimal-range: " + s.getRangedOptimalRange()), false);
        source.sendFeedback(() -> Text.literal("ranged-max-range: " + s.getRangedMaxRange()), false);
        source.sendFeedback(() -> Text.literal("bow-draw-ticks: " + s.getBowMinDrawTime() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("arrow-prediction: " + s.isArrowPredictionEnabled()), false);
        source.sendFeedback(() -> Text.literal("ranged-strafe: " + s.isRangedStrafeEnabled()), false);
        source.sendFeedback(() -> Text.literal("ranged-retreat: " + s.isRangedRetreatOnClose()), false);
        source.sendFeedback(() -> Text.literal("mace: " + s.isMaceEnabled()), false);
        source.sendFeedback(() -> Text.literal("special-names: " + s.isUseSpecialNames()), false);
        source.sendFeedback(() -> Text.literal("shield-mace: " + s.isShieldMace()), false);
        source.sendFeedback(() -> Text.literal("attack-cooldown: " + s.getAttackCooldown() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("heal-retreat: " + s.getHealRetreatSeconds() + " seconds"), false);
        source.sendFeedback(() -> Text.literal("attack-enemy-heal: " + s.isAttackWhileEnemyHeals() + " (heal only below 1 heart while enemy heals)"), false);
        source.sendFeedback(() -> Text.literal("melee-range: " + s.getMeleeRange()), false);
        source.sendFeedback(() -> Text.literal("move-speed: " + s.getMoveSpeed()), false);
        source.sendFeedback(() -> Text.literal("=== Utilities ==="), false);
        source.sendFeedback(() -> Text.literal("auto-totem: " + s.isAutoTotemEnabled()), false);
        source.sendFeedback(() -> Text.literal("prefer-totem: " + s.isPreferTotem() + " (on: no shield while totem held, mace-smash -> shield in right hand; off: shield in left hand, totem when <3 hearts / mace-smash)"), false);
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

    private static int pathWalkType(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        String type = StringArgumentType.getString(ctx, "type");
        if (!type.equals("bhop") && !type.equals("sprint") && !type.equals("walk")) {
            source.sendError(Text.literal("Invalid walk type. Use: bhop, sprint, or walk"));
            return 0;
        }
        if (BotPath.setWalkType(name, type)) {
            source.sendFeedback(() -> Text.literal("Path '" + name + "' walk type: " + type), true);
            return 1;
        } else {
            source.sendError(Text.literal("Path '" + name + "' not found"));
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
            source.sendFeedback(() -> Text.literal(String.format("%s: %d points, loop: %s, attack: %s, walk-type: %s", name, path.points.size(), path.loop, path.attack, path.walkType)), false);
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
        source.sendFeedback(() -> Text.literal("Walk type: " + path.walkType), false);
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

    private static int presetSave(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        boolean existed = BotPresets.presetExists(name);
        if (BotPresets.savePreset(name)) {
            source.sendFeedback(() -> Text.literal(existed
                ? "Settings preset '" + name + "' updated with current settings!"
                : "Settings preset '" + name + "' saved from current settings!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to save settings preset '" + name + "'"));
            return 0;
        }
    }

    private static int presetLoad(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (!BotPresets.presetExists(name)) {
            source.sendError(Text.literal("Settings preset '" + name + "' not found!"));
            return 0;
        }
        if (BotPresets.loadPreset(name)) {
            source.sendFeedback(() -> Text.literal("Loaded settings preset '" + name + "'. Use /pvpbot settings to review."), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to load settings preset '" + name + "'"));
            return 0;
        }
    }

    private static int presetDelete(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (BotPresets.deletePreset(name)) {
            source.sendFeedback(() -> Text.literal("Settings preset '" + name + "' deleted!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Settings preset '" + name + "' not found!"));
            return 0;
        }
    }

    private static int presetList(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        var presets = BotPresets.getPresetNames();
        if (presets.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No settings presets saved. Use /pvpbot settings preset save <name> to create one."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Settings presets (" + presets.size() + "): " + String.join(", ", presets)), false);
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

    // ========== GIVE-KIT-NEAR ==========

    private static int kitGiveNear(CommandContext<ServerCommandSource> ctx, double radius) {
        var source = ctx.getSource();
        String kitname = StringArgumentType.getString(ctx, "kitname");
        if (!BotKits.kitExists(kitname)) {
            source.sendError(Text.literal("Kit '" + kitname + "' not found!"));
            return 0;
        }
        var player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player!"));
            return 0;
        }
        var server = source.getServer();
        int count = 0;
        for (String botName : BotManager.getAllBots()) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(botName);
            if (bot != null && bot.distanceTo(player) <= radius) {
                if (BotKits.giveKit(kitname, bot)) {
                    count++;
                }
            }
        }
        if (count > 0) {
            int finalCount = count;
            source.sendFeedback(() -> Text.literal("Gave kit '" + kitname + "' to " + finalCount + " bots within " + radius + " blocks"), true);
            return count;
        } else {
            source.sendError(Text.literal("No bots found within " + radius + " blocks"));
            return 0;
        }
    }

    // ========== GIVE-KIT-NEAR-RANDOM ==========

    private static int kitGiveNearRandom(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        String kitsArg = StringArgumentType.getString(ctx, "kits");
        var player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player!"));
            return 0;
        }
        String[] parts = kitsArg.split(" ");
        if (parts.length < 2 || parts.length % 2 != 0) {
            source.sendError(Text.literal("Usage: /pvpbot kit give-kit-near-random <radius> <kit> <weight>% [<kit> <weight>% ...]"));
            return 0;
        }
        java.util.LinkedHashMap<String, Integer> weights = new java.util.LinkedHashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            String kitName = parts[i];
            String weightStr = parts[i + 1];
            if (!weightStr.endsWith("%")) {
                source.sendError(Text.literal("Invalid weight '" + weightStr + "' for kit '" + kitName + "'. Use format: <weight>%"));
                return 0;
            }
            if (!BotKits.kitExists(kitName)) {
                source.sendError(Text.literal("Kit '" + kitName + "' not found!"));
                return 0;
            }
            try {
                int w = Integer.parseInt(weightStr.substring(0, weightStr.length() - 1));
                if (w <= 0) {
                    source.sendError(Text.literal("Weight must be positive for kit '" + kitName + "'"));
                    return 0;
                }
                weights.put(kitName, w);
            } catch (NumberFormatException e) {
                source.sendError(Text.literal("Invalid weight '" + weightStr + "' for kit '" + kitName + "'"));
                return 0;
            }
        }
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        var server = source.getServer();
        int count = 0;
        java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();
        for (String botName : BotManager.getAllBots()) {
            ServerPlayerEntity bot = server.getPlayerManager().getPlayer(botName);
            if (bot == null || bot.distanceTo(player) > radius) continue;
            int roll = rng.nextInt(totalWeight);
            int cumulative = 0;
            String selected = null;
            for (var entry : weights.entrySet()) {
                cumulative += entry.getValue();
                if (roll < cumulative) {
                    selected = entry.getKey();
                    break;
                }
            }
            if (selected != null && BotKits.giveKit(selected, bot)) {
                count++;
            }
        }
        if (count > 0) {
            int finalCount = count;
            source.sendFeedback(() -> Text.literal("Gave random kits to " + finalCount + " bots within " + radius + " blocks"), true);
            return count;
        } else {
            source.sendError(Text.literal("No bots found within " + radius + " blocks"));
            return 0;
        }
    }

    // ========== FACTION GIVE-KIT-RANDOM ==========

    private static int factionGiveKitRandom(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        String kitsArg = StringArgumentType.getString(ctx, "kits");
        String[] parts = kitsArg.split(" ");
        if (parts.length < 2 || parts.length % 2 != 0) {
            source.sendError(Text.literal("Usage: /pvpbot faction give-kit-random <faction> <kit> <weight>% [<kit> <weight>% ...]"));
            return 0;
        }
        java.util.LinkedHashMap<String, Integer> weights = new java.util.LinkedHashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            String kitName = parts[i];
            String weightStr = parts[i + 1];
            if (!weightStr.endsWith("%")) {
                source.sendError(Text.literal("Invalid weight '" + weightStr + "' for kit '" + kitName + "'. Use format: <weight>%"));
                return 0;
            }
            if (!BotKits.kitExists(kitName)) {
                source.sendError(Text.literal("Kit '" + kitName + "' not found!"));
                return 0;
            }
            try {
                int w = Integer.parseInt(weightStr.substring(0, weightStr.length() - 1));
                if (w <= 0) {
                    source.sendError(Text.literal("Weight must be positive for kit '" + kitName + "'"));
                    return 0;
                }
                weights.put(kitName, w);
            } catch (NumberFormatException e) {
                source.sendError(Text.literal("Invalid weight '" + weightStr + "' for kit '" + kitName + "'"));
                return 0;
            }
        }
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        var members = BotFaction.getMembers(faction);
        if (members.isEmpty()) {
            source.sendError(Text.literal("Faction '" + faction + "' has no members!"));
            return 0;
        }
        var server = source.getServer();
        int count = 0;
        java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();
        for (String memberName : members) {
            if (!BotManager.getAllBots().contains(memberName)) continue;
            var bot = BotManager.getBot(server, memberName);
            if (bot == null) continue;
            int roll = rng.nextInt(totalWeight);
            int cumulative = 0;
            String selected = null;
            for (var entry : weights.entrySet()) {
                cumulative += entry.getValue();
                if (roll < cumulative) {
                    selected = entry.getKey();
                    break;
                }
            }
            if (selected != null && BotKits.giveKit(selected, bot)) {
                count++;
            }
        }
        if (count > 0) {
            int finalCount = count;
            source.sendFeedback(() -> Text.literal("Gave random kits to " + finalCount + " bots in faction '" + faction + "'"), true);
            return count;
        } else {
            source.sendError(Text.literal("No bots in faction '" + faction + "' received a kit"));
            return 0;
        }
    }

    // ========== FACTION TP ==========

    private static int factionTp(CommandContext<ServerCommandSource> ctx) {
        var source = ctx.getSource();
        String faction = StringArgumentType.getString(ctx, "faction");
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        String location = StringArgumentType.getString(ctx, "location");
        String[] parts = location.split(" ");
        double tx, ty, tz;
        if (parts.length == 3) {
            var player = source.getPlayer();
            double ox = (player != null) ? player.getX() : 0;
            double oy = (player != null) ? player.getY() : 0;
            double oz = (player != null) ? player.getZ() : 0;
            try {
                tx = parseCoord(parts[0], ox);
                ty = parseCoord(parts[1], oy);
                tz = parseCoord(parts[2], oz);
            } catch (NumberFormatException e) {
                source.sendError(Text.literal("Invalid coordinates. Usage: /pvpbot faction tp <faction> <x y z> or <playername>"));
                return 0;
            }
        } else {
            var server = source.getServer();
            var target = server.getPlayerManager().getPlayer(location);
            if (target == null) {
                source.sendError(Text.literal("Player or bot '" + location + "' not found!"));
                return 0;
            }
            tx = target.getX();
            ty = target.getY();
            tz = target.getZ();
        }
        var members = new java.util.ArrayList<>(BotFaction.getMembers(faction));
        members.removeIf(m -> !BotManager.getAllBots().contains(m));
        if (members.isEmpty()) {
            source.sendError(Text.literal("Faction '" + faction + "' has no bots!"));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("Teleporting " + members.size() + " bots in faction '" + faction + "'..."), false);
        var server = source.getServer();
        boolean safe = BotSettings.get().isSafeSpawn();
        int[] count = {0};
        scheduleFactionTp(server, source, members, tx, ty, tz, safe, 0, count);
        return 1;
    }

    private static double parseCoord(String s, double origin) {
        if (s.startsWith("~")) {
            double offset = s.length() > 1 ? Double.parseDouble(s.substring(1)) : 0;
            return origin + offset;
        }
        return Double.parseDouble(s);
    }

    private static final java.util.concurrent.ScheduledExecutorService TP_SCHEDULER =
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FactionTP");
            t.setDaemon(true);
            return t;
        });

    private static void scheduleFactionTp(MinecraftServer server, ServerCommandSource source,
                                          java.util.List<String> members, double tx, double ty, double tz,
                                          boolean safe, int index, int[] count) {
        if (index >= members.size()) {
            source.sendFeedback(() -> Text.literal("Teleported " + count[0] + " bots in faction"), true);
            return;
        }
        int end = Math.min(index + 5, members.size());
        TP_SCHEDULER.schedule(() -> server.execute(() -> {
            var rng = java.util.concurrent.ThreadLocalRandom.current();
            for (int i = index; i < end; i++) {
                String name = members.get(i);
                double x = tx, z = tz;
                if (safe) {
                    x += (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
                    z += (rng.nextDouble() * 0.4 + 0.1) * (rng.nextBoolean() ? 1 : -1);
                }
                ServerPlayerEntity bot = BotManager.getBot(server, name);
                if (bot != null) {
                    bot.teleport((net.minecraft.server.world.ServerWorld) bot.getEntityWorld(), x, ty, tz, java.util.Set.of(), 0.0f, 0.0f, false);
                    count[0]++;
                }
            }
            scheduleFactionTp(server, source, members, tx, ty, tz, safe, end, count);
        }), 100, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}


