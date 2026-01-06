package org.stepan1411.pvp_bot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.stepan1411.pvp_bot.bot.BotCombat;
import org.stepan1411.pvp_bot.bot.BotFaction;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotNameGenerator;
import org.stepan1411.pvp_bot.bot.BotSettings;

import java.util.stream.Collectors;

public class BotCommand {
    
    // Подсказки для имён ботов
    private static final SuggestionProvider<ServerCommandSource> BOT_SUGGESTIONS = (ctx, builder) -> 
        CommandSource.suggestMatching(BotManager.getAllBots(), builder);
    
    // Подсказки для целей (все игроки на сервере)
    private static final SuggestionProvider<ServerCommandSource> TARGET_SUGGESTIONS = (ctx, builder) -> 
        CommandSource.suggestMatching(
            ctx.getSource().getServer().getPlayerManager().getPlayerList().stream()
                .map(p -> p.getName().getString())
                .collect(Collectors.toList()), 
            builder);
    
    // Подсказки для фракций
    private static final SuggestionProvider<ServerCommandSource> FACTION_SUGGESTIONS = (ctx, builder) -> 
        CommandSource.suggestMatching(BotFaction.getAllFactions(), builder);
    
    // Подсказки для китов
    private static final SuggestionProvider<ServerCommandSource> KIT_SUGGESTIONS = (ctx, builder) -> 
        CommandSource.suggestMatching(BotKits.getKitNames(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("pvpbot")
                
                // /pvpbot spawn [name] - без имени генерирует случайное
                .then(CommandManager.literal("spawn")
                    .executes(ctx -> spawnBot(ctx.getSource(), BotNameGenerator.generateUniqueName()))
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> spawnBot(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                    )
                )
                
                // /pvpbot massspawn <num> - спавнит несколько ботов с рандомными именами
                .then(CommandManager.literal("massspawn")
                    .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 50))
                        .executes(ctx -> massSpawnBots(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count")))
                    )
                )
                
                // /pvpbot remove <name> - с подсказками ботов
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(ctx -> removeBot(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                    )
                )
                
                // /pvpbot removeall
                .then(CommandManager.literal("removeall")
                    .executes(ctx -> removeAllBots(ctx.getSource()))
                )
                
                // /pvpbot list
                .then(CommandManager.literal("list")
                    .executes(ctx -> listBots(ctx.getSource()))
                )
                
                // /pvpbot settings
                .then(CommandManager.literal("settings")
                    .executes(ctx -> showSettings(ctx.getSource()))
                    
                    // /pvpbot settings autoarmor [true/false]
                    .then(CommandManager.literal("autoarmor")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("autoarmor: " + BotSettings.get().isAutoEquipArmor()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setAutoEquipArmor(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Auto equip armor: " + BotSettings.get().isAutoEquipArmor()), true);
                                return 1;
                            })
                        )
                    )
                    
                    // /pvpbot settings autoweapon [true/false]
                    .then(CommandManager.literal("autoweapon")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("autoweapon: " + BotSettings.get().isAutoEquipWeapon()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setAutoEquipWeapon(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Auto equip weapon: " + BotSettings.get().isAutoEquipWeapon()), true);
                                return 1;
                            })
                        )
                    )
                    
                    // /pvpbot settings droparmor [true/false]
                    .then(CommandManager.literal("droparmor")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("droparmor: " + BotSettings.get().isDropWorseArmor()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setDropWorseArmor(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Drop worse armor: " + BotSettings.get().isDropWorseArmor()), true);
                                return 1;
                            })
                        )
                    )
                    
                    // /pvpbot settings dropweapon [true/false]
                    .then(CommandManager.literal("dropweapon")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("dropweapon: " + BotSettings.get().isDropWorseWeapons()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setDropWorseWeapons(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Drop worse weapons: " + BotSettings.get().isDropWorseWeapons()), true);
                                return 1;
                            })
                        )
                    )
                    
                    // /pvpbot settings dropdistance [1-10]
                    .then(CommandManager.literal("dropdistance")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("dropdistance: " + BotSettings.get().getDropDistance()), false); return 1; })
                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg(1.0, 10.0))
                            .executes(ctx -> {
                                BotSettings.get().setDropDistance(DoubleArgumentType.getDouble(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Drop distance: " + BotSettings.get().getDropDistance()), true);
                                return 1;
                            })
                        )
                    )
                    
                    // /pvpbot settings interval [1-100]
                    .then(CommandManager.literal("interval")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("interval: " + BotSettings.get().getCheckInterval() + " ticks"), false); return 1; })
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(1, 100))
                            .executes(ctx -> {
                                BotSettings.get().setCheckInterval(IntegerArgumentType.getInteger(ctx, "ticks"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Check interval: " + BotSettings.get().getCheckInterval() + " ticks"), true);
                                return 1;
                            })
                        )
                    )
                    
                    // /pvpbot settings minarmorlevel [0-100]
                    .then(CommandManager.literal("minarmorlevel")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("minarmorlevel: " + BotSettings.get().getMinArmorLevel()), false); return 1; })
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> {
                                BotSettings.get().setMinArmorLevel(IntegerArgumentType.getInteger(ctx, "level"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Min armor level: " + BotSettings.get().getMinArmorLevel() + " (0=any, 20=leather+, 40=gold+, 50=chain+, 60=iron+, 80=diamond+, 100=netherite)"), true);
                                return 1;
                            })
                        )
                    )
                    
                    // === Combat Settings (с показом текущего значения без аргумента) ===
                    .then(CommandManager.literal("combat")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("combat: " + BotSettings.get().isCombatEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setCombatEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Combat enabled: " + BotSettings.get().isCombatEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("revenge")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("revenge: " + BotSettings.get().isRevengeEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setRevengeEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Revenge mode: " + BotSettings.get().isRevengeEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("autotarget")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("autotarget: " + BotSettings.get().isAutoTargetEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setAutoTargetEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Auto target: " + BotSettings.get().isAutoTargetEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("targetplayers")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("targetplayers: " + BotSettings.get().isTargetPlayers()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setTargetPlayers(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Target players: " + BotSettings.get().isTargetPlayers()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("targetmobs")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("targetmobs: " + BotSettings.get().isTargetHostileMobs()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setTargetHostileMobs(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Target hostile mobs: " + BotSettings.get().isTargetHostileMobs()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("targetbots")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("targetbots: " + BotSettings.get().isTargetOtherBots()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setTargetOtherBots(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Target other bots: " + BotSettings.get().isTargetOtherBots()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("criticals")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("criticals: " + BotSettings.get().isCriticalsEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setCriticalsEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Criticals: " + BotSettings.get().isCriticalsEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("ranged")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("ranged: " + BotSettings.get().isRangedEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setRangedEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Ranged weapons: " + BotSettings.get().isRangedEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("mace")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("mace: " + BotSettings.get().isMaceEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setMaceEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Mace combat: " + BotSettings.get().isMaceEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("attackcooldown")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("attackcooldown: " + BotSettings.get().getAttackCooldown() + " ticks"), false); return 1; })
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(1, 40))
                            .executes(ctx -> {
                                BotSettings.get().setAttackCooldown(IntegerArgumentType.getInteger(ctx, "ticks"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Attack cooldown: " + BotSettings.get().getAttackCooldown() + " ticks"), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("meleerange")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("meleerange: " + BotSettings.get().getMeleeRange()), false); return 1; })
                        .then(CommandManager.argument("range", DoubleArgumentType.doubleArg(2.0, 6.0))
                            .executes(ctx -> {
                                BotSettings.get().setMeleeRange(DoubleArgumentType.getDouble(ctx, "range"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Melee range: " + BotSettings.get().getMeleeRange()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("movespeed")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("movespeed: " + BotSettings.get().getMoveSpeed()), false); return 1; })
                        .then(CommandManager.argument("speed", DoubleArgumentType.doubleArg(0.1, 2.0))
                            .executes(ctx -> {
                                BotSettings.get().setMoveSpeed(DoubleArgumentType.getDouble(ctx, "speed"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Move speed: " + BotSettings.get().getMoveSpeed()), true);
                                return 1;
                            })
                        )
                    )
                    
                    // === Navigation Settings ===
                    .then(CommandManager.literal("bhop")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("bhop: " + BotSettings.get().isBhopEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setBhopEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Bhop enabled: " + BotSettings.get().isBhopEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("bhopcooldown")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("bhopcooldown: " + BotSettings.get().getBhopCooldown() + " ticks"), false); return 1; })
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(5, 30))
                            .executes(ctx -> {
                                BotSettings.get().setBhopCooldown(IntegerArgumentType.getInteger(ctx, "ticks"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Bhop cooldown: " + BotSettings.get().getBhopCooldown() + " ticks"), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("jumpboost")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("jumpboost: " + BotSettings.get().getJumpBoost()), false); return 1; })
                        .then(CommandManager.argument("boost", DoubleArgumentType.doubleArg(0.0, 0.5))
                            .executes(ctx -> {
                                BotSettings.get().setJumpBoost(DoubleArgumentType.getDouble(ctx, "boost"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Jump boost: " + BotSettings.get().getJumpBoost()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("idle")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("idle: " + BotSettings.get().isIdleWanderEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setIdleWanderEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Idle wander: " + BotSettings.get().isIdleWanderEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("idleradius")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("idleradius: " + BotSettings.get().getIdleWanderRadius()), false); return 1; })
                        .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(3.0, 50.0))
                            .executes(ctx -> {
                                BotSettings.get().setIdleWanderRadius(DoubleArgumentType.getDouble(ctx, "radius"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Idle wander radius: " + BotSettings.get().getIdleWanderRadius()), true);
                                return 1;
                            })
                        )
                    )
                    
                    // === Realism Settings ===
                    .then(CommandManager.literal("friendlyfire")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("friendlyfire: " + BotSettings.get().isFriendlyFireEnabled()), false); return 1; })
                        .then(CommandManager.argument("value", BoolArgumentType.bool())
                            .executes(ctx -> {
                                BotSettings.get().setFriendlyFireEnabled(BoolArgumentType.getBool(ctx, "value"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Friendly fire: " + BotSettings.get().isFriendlyFireEnabled()), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("misschance")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("misschance: " + BotSettings.get().getMissChance() + "%"), false); return 1; })
                        .then(CommandManager.argument("percent", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> {
                                BotSettings.get().setMissChance(IntegerArgumentType.getInteger(ctx, "percent"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Miss chance: " + BotSettings.get().getMissChance() + "%"), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("mistakechance")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("mistakechance: " + BotSettings.get().getMistakeChance() + "%"), false); return 1; })
                        .then(CommandManager.argument("percent", IntegerArgumentType.integer(0, 100))
                            .executes(ctx -> {
                                BotSettings.get().setMistakeChance(IntegerArgumentType.getInteger(ctx, "percent"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Mistake chance: " + BotSettings.get().getMistakeChance() + "%"), true);
                                return 1;
                            })
                        )
                    )
                    .then(CommandManager.literal("reactiondelay")
                        .executes(ctx -> { ctx.getSource().sendFeedback(() -> Text.literal("reactiondelay: " + BotSettings.get().getReactionDelay() + " ticks"), false); return 1; })
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(0, 20))
                            .executes(ctx -> {
                                BotSettings.get().setReactionDelay(IntegerArgumentType.getInteger(ctx, "ticks"));
                                ctx.getSource().sendFeedback(() -> Text.literal("Reaction delay: " + BotSettings.get().getReactionDelay() + " ticks"), true);
                                return 1;
                            })
                        )
                    )
                )
                
                // /pvpbot attack <botname> <target> - с подсказками
                .then(CommandManager.literal("attack")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .then(CommandManager.argument("target", StringArgumentType.word())
                            .suggests(TARGET_SUGGESTIONS)
                            .executes(ctx -> setAttackTarget(ctx.getSource(), 
                                StringArgumentType.getString(ctx, "botname"),
                                StringArgumentType.getString(ctx, "target")))
                        )
                    )
                )
                
                // /pvpbot stop <botname> - с подсказками
                .then(CommandManager.literal("stop")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(ctx -> stopAttack(ctx.getSource(), StringArgumentType.getString(ctx, "botname")))
                    )
                )
                
                // /pvpbot target <botname> - показать текущую цель
                .then(CommandManager.literal("target")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(ctx -> showTarget(ctx.getSource(), StringArgumentType.getString(ctx, "botname")))
                    )
                )
                
                // /pvpbot inventory <botname> - показать инвентарь бота
                .then(CommandManager.literal("inventory")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .executes(ctx -> showInventory(ctx.getSource(), StringArgumentType.getString(ctx, "botname")))
                    )
                )
                
                // ============ Команды фракций ============
                .then(CommandManager.literal("faction")
                    // /pvpbot faction list
                    .then(CommandManager.literal("list")
                        .executes(ctx -> listFactions(ctx.getSource()))
                    )
                    // /pvpbot faction create <name>
                    .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(ctx -> createFaction(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                        )
                    )
                    // /pvpbot faction delete <name>
                    .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .executes(ctx -> deleteFaction(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                        )
                    )
                    // /pvpbot faction add <faction> <player>
                    .then(CommandManager.literal("add")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("player", StringArgumentType.word())
                                .suggests(TARGET_SUGGESTIONS)
                                .executes(ctx -> addToFaction(ctx.getSource(), 
                                    StringArgumentType.getString(ctx, "faction"),
                                    StringArgumentType.getString(ctx, "player")))
                            )
                        )
                    )
                    // /pvpbot faction remove <faction> <player>
                    .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("player", StringArgumentType.word())
                                .executes(ctx -> removeFromFaction(ctx.getSource(), 
                                    StringArgumentType.getString(ctx, "faction"),
                                    StringArgumentType.getString(ctx, "player")))
                            )
                        )
                    )
                    // /pvpbot faction hostile <faction1> <faction2> [true/false]
                    .then(CommandManager.literal("hostile")
                        .then(CommandManager.argument("faction1", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("faction2", StringArgumentType.word())
                                .suggests(FACTION_SUGGESTIONS)
                                .executes(ctx -> setHostile(ctx.getSource(), 
                                    StringArgumentType.getString(ctx, "faction1"),
                                    StringArgumentType.getString(ctx, "faction2"),
                                    true))
                                .then(CommandManager.argument("hostile", BoolArgumentType.bool())
                                    .executes(ctx -> setHostile(ctx.getSource(), 
                                        StringArgumentType.getString(ctx, "faction1"),
                                        StringArgumentType.getString(ctx, "faction2"),
                                        BoolArgumentType.getBool(ctx, "hostile")))
                                )
                            )
                        )
                    )
                    // /pvpbot faction info <faction>
                    .then(CommandManager.literal("info")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .executes(ctx -> factionInfo(ctx.getSource(), StringArgumentType.getString(ctx, "faction")))
                        )
                    )
                    // /pvpbot faction addnear <faction> <radius> - добавить всех ботов в радиусе
                    .then(CommandManager.literal("addnear")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1.0, 100.0))
                                .executes(ctx -> addNearbyBotsToFaction(ctx.getSource(), 
                                    StringArgumentType.getString(ctx, "faction"),
                                    DoubleArgumentType.getDouble(ctx, "radius")))
                            )
                        )
                    )
                    // /pvpbot faction give <faction> <item> [count] - выдать предмет всей фракции
                    .then(CommandManager.literal("give")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("item", StringArgumentType.greedyString())
                                .executes(ctx -> giveFactionItem(ctx.getSource(), 
                                    StringArgumentType.getString(ctx, "faction"),
                                    StringArgumentType.getString(ctx, "item")))
                            )
                        )
                    )
                    // /pvpbot faction attack <faction> <target> - вся фракция атакует цель
                    .then(CommandManager.literal("attack")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("target", StringArgumentType.word())
                                .suggests(TARGET_SUGGESTIONS)
                                .executes(ctx -> factionAttack(ctx.getSource(), 
                                    StringArgumentType.getString(ctx, "faction"),
                                    StringArgumentType.getString(ctx, "target")))
                            )
                        )
                    )
                )
                
                // /pvpbot settings viewdistance [5-128] - дальность видимости
                .then(CommandManager.literal("settings")
                    .then(CommandManager.literal("viewdistance")
                        .executes(ctx -> { 
                            ctx.getSource().sendFeedback(() -> Text.literal("viewdistance: " + BotSettings.get().getMaxTargetDistance()), false); 
                            return 1; 
                        })
                        .then(CommandManager.argument("distance", DoubleArgumentType.doubleArg(5.0, 128.0))
                            .executes(ctx -> {
                                BotSettings.get().setMaxTargetDistance(DoubleArgumentType.getDouble(ctx, "distance"));
                                ctx.getSource().sendFeedback(() -> Text.literal("View distance: " + BotSettings.get().getMaxTargetDistance()), true);
                                return 1;
                            })
                        )
                    )
                )
                
                // ============ Команды китов ============
                // /pvpbot createkit <name> - создать кит из инвентаря игрока
                .then(CommandManager.literal("createkit")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> createKit(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                    )
                )
                
                // /pvpbot deletekit <name> - удалить кит
                .then(CommandManager.literal("deletekit")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(KIT_SUGGESTIONS)
                        .executes(ctx -> deleteKit(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                    )
                )
                
                // /pvpbot kits - список китов
                .then(CommandManager.literal("kits")
                    .executes(ctx -> listKits(ctx.getSource()))
                )
                
                // /pvpbot givekit <botname> <kitname> - выдать кит боту
                .then(CommandManager.literal("givekit")
                    .then(CommandManager.argument("botname", StringArgumentType.word())
                        .suggests(BOT_SUGGESTIONS)
                        .then(CommandManager.argument("kitname", StringArgumentType.word())
                            .suggests(KIT_SUGGESTIONS)
                            .executes(ctx -> giveKitToBot(ctx.getSource(), 
                                StringArgumentType.getString(ctx, "botname"),
                                StringArgumentType.getString(ctx, "kitname")))
                        )
                    )
                )
                
                // /pvpbot faction givekit <faction> <kitname> - выдать кит всей фракции
                .then(CommandManager.literal("faction")
                    .then(CommandManager.literal("givekit")
                        .then(CommandManager.argument("faction", StringArgumentType.word())
                            .suggests(FACTION_SUGGESTIONS)
                            .then(CommandManager.argument("kitname", StringArgumentType.word())
                                .suggests(KIT_SUGGESTIONS)
                                .executes(ctx -> giveKitToFaction(ctx.getSource(), 
                                    StringArgumentType.getString(ctx, "faction"),
                                    StringArgumentType.getString(ctx, "kitname")))
                            )
                        )
                    )
                )
        );
    }
    
    private static int setAttackTarget(ServerCommandSource source, String botName, String targetName) {
        if (!BotManager.getAllBots().contains(botName)) {
            source.sendError(Text.literal("Bot '" + botName + "' not found!"));
            return 0;
        }
        
        BotCombat.setTarget(botName, targetName);
        source.sendFeedback(() -> Text.literal("Bot '" + botName + "' now attacking '" + targetName + "'"), true);
        return 1;
    }
    
    private static int stopAttack(ServerCommandSource source, String botName) {
        if (!BotManager.getAllBots().contains(botName)) {
            source.sendError(Text.literal("Bot '" + botName + "' not found!"));
            return 0;
        }
        
        BotCombat.clearTarget(botName);
        source.sendFeedback(() -> Text.literal("Bot '" + botName + "' stopped attacking"), true);
        return 1;
    }
    
    private static int showTarget(ServerCommandSource source, String botName) {
        if (!BotManager.getAllBots().contains(botName)) {
            source.sendError(Text.literal("Bot '" + botName + "' not found!"));
            return 0;
        }
        
        var target = BotCombat.getTarget(botName);
        if (target != null) {
            source.sendFeedback(() -> Text.literal("Bot '" + botName + "' target: " + target.getName().getString()), false);
        } else {
            source.sendFeedback(() -> Text.literal("Bot '" + botName + "' has no target"), false);
        }
        return 1;
    }


    private static int spawnBot(ServerCommandSource source, String name) {
        if (BotManager.spawnBot(source.getServer(), name, source)) {
            source.sendFeedback(() -> Text.literal("PvP Bot '" + name + "' spawned!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Bot '" + name + "' already exists!"));
            return 0;
        }
    }
    
    private static int massSpawnBots(ServerCommandSource source, int count) {
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            String name = BotNameGenerator.generateUniqueName();
            if (BotManager.spawnBot(source.getServer(), name, source)) {
                spawned++;
            }
        }
        final int total = spawned;
        source.sendFeedback(() -> Text.literal("Spawned " + total + " bots!"), true);
        return spawned;
    }

    private static int removeBot(ServerCommandSource source, String name) {
        if (BotManager.removeBot(source.getServer(), name, source)) {
            source.sendFeedback(() -> Text.literal("Bot '" + name + "' removed!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Bot '" + name + "' not found!"));
            return 0;
        }
    }

    private static int removeAllBots(ServerCommandSource source) {
        int count = BotManager.getBotCount();
        BotManager.removeAllBots(source.getServer(), source);
        source.sendFeedback(() -> Text.literal("Removed " + count + " bots"), true);
        return count;
    }

    private static int listBots(ServerCommandSource source) {
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

    private static int showSettings(ServerCommandSource source) {
        BotSettings s = BotSettings.get();
        source.sendFeedback(() -> Text.literal("=== Equipment Settings ==="), false);
        source.sendFeedback(() -> Text.literal("autoarmor: " + s.isAutoEquipArmor()), false);
        source.sendFeedback(() -> Text.literal("autoweapon: " + s.isAutoEquipWeapon()), false);
        source.sendFeedback(() -> Text.literal("droparmor: " + s.isDropWorseArmor()), false);
        source.sendFeedback(() -> Text.literal("dropweapon: " + s.isDropWorseWeapons()), false);
        source.sendFeedback(() -> Text.literal("dropdistance: " + s.getDropDistance()), false);
        source.sendFeedback(() -> Text.literal("interval: " + s.getCheckInterval() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("minarmorlevel: " + s.getMinArmorLevel()), false);
        
        source.sendFeedback(() -> Text.literal("=== Combat Settings ==="), false);
        source.sendFeedback(() -> Text.literal("combat: " + s.isCombatEnabled()), false);
        source.sendFeedback(() -> Text.literal("revenge: " + s.isRevengeEnabled()), false);
        source.sendFeedback(() -> Text.literal("autotarget: " + s.isAutoTargetEnabled()), false);
        source.sendFeedback(() -> Text.literal("targetplayers: " + s.isTargetPlayers()), false);
        source.sendFeedback(() -> Text.literal("targetmobs: " + s.isTargetHostileMobs()), false);
        source.sendFeedback(() -> Text.literal("targetbots: " + s.isTargetOtherBots()), false);
        source.sendFeedback(() -> Text.literal("criticals: " + s.isCriticalsEnabled()), false);
        source.sendFeedback(() -> Text.literal("ranged: " + s.isRangedEnabled()), false);
        source.sendFeedback(() -> Text.literal("mace: " + s.isMaceEnabled()), false);
        source.sendFeedback(() -> Text.literal("attackcooldown: " + s.getAttackCooldown() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("meleerange: " + s.getMeleeRange()), false);
        source.sendFeedback(() -> Text.literal("movespeed: " + s.getMoveSpeed()), false);
        source.sendFeedback(() -> Text.literal("=== Navigation Settings ==="), false);
        source.sendFeedback(() -> Text.literal("bhop: " + s.isBhopEnabled()), false);
        source.sendFeedback(() -> Text.literal("bhopcooldown: " + s.getBhopCooldown() + " ticks"), false);
        source.sendFeedback(() -> Text.literal("jumpboost: " + s.getJumpBoost()), false);
        source.sendFeedback(() -> Text.literal("idle: " + s.isIdleWanderEnabled()), false);
        source.sendFeedback(() -> Text.literal("idleradius: " + s.getIdleWanderRadius()), false);
        source.sendFeedback(() -> Text.literal("=== Factions & Mistakes ==="), false);
        source.sendFeedback(() -> Text.literal("factions: " + s.isFactionsEnabled()), false);
        source.sendFeedback(() -> Text.literal("friendlyfire: " + s.isFriendlyFireEnabled()), false);
        source.sendFeedback(() -> Text.literal("misschance: " + s.getMissChance() + "%"), false);
        source.sendFeedback(() -> Text.literal("mistakechance: " + s.getMistakeChance() + "%"), false);
        source.sendFeedback(() -> Text.literal("reactiondelay: " + s.getReactionDelay() + " ticks"), false);
        return 1;
    }
    
    // ============ Команды фракций ============
    
    private static int listFactions(ServerCommandSource source) {
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
    
    private static int createFaction(ServerCommandSource source, String name) {
        if (BotFaction.createFaction(name)) {
            source.sendFeedback(() -> Text.literal("Faction '" + name + "' created!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Faction '" + name + "' already exists!"));
            return 0;
        }
    }
    
    private static int deleteFaction(ServerCommandSource source, String name) {
        if (BotFaction.deleteFaction(name)) {
            source.sendFeedback(() -> Text.literal("Faction '" + name + "' deleted!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Faction '" + name + "' not found!"));
            return 0;
        }
    }
    
    private static int addToFaction(ServerCommandSource source, String faction, String player) {
        if (BotFaction.addMember(faction, player)) {
            source.sendFeedback(() -> Text.literal("Added '" + player + "' to faction '" + faction + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
    }
    
    private static int removeFromFaction(ServerCommandSource source, String faction, String player) {
        if (BotFaction.removeMember(faction, player)) {
            source.sendFeedback(() -> Text.literal("Removed '" + player + "' from faction '" + faction + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to remove '" + player + "' from faction '" + faction + "'"));
            return 0;
        }
    }
    
    private static int setHostile(ServerCommandSource source, String faction1, String faction2, boolean hostile) {
        if (BotFaction.setHostile(faction1, faction2, hostile)) {
            if (hostile) {
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
    
    private static int factionInfo(ServerCommandSource source, String faction) {
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
    
    private static int addNearbyBotsToFaction(ServerCommandSource source, String faction, double radius) {
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
        
        final int added = count;
        source.sendFeedback(() -> Text.literal("Added " + added + " bots to faction '" + faction + "'"), true);
        return count;
    }
    
    private static int showInventory(ServerCommandSource source, String botName) {
        if (!BotManager.getAllBots().contains(botName)) {
            source.sendError(Text.literal("Bot '" + botName + "' not found!"));
            return 0;
        }
        
        var bot = source.getServer().getPlayerManager().getPlayer(botName);
        if (bot == null) {
            source.sendError(Text.literal("Bot '" + botName + "' not online!"));
            return 0;
        }
        
        var inventory = bot.getInventory();
        source.sendFeedback(() -> Text.literal("=== Inventory of " + botName + " ==="), false);
        
        // Хотбар (0-8)
        StringBuilder hotbar = new StringBuilder("Hotbar: ");
        for (int i = 0; i < 9; i++) {
            var stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                hotbar.append("[").append(i).append("]").append(stack.getCount()).append("x").append(stack.getName().getString()).append(" ");
            }
        }
        source.sendFeedback(() -> Text.literal(hotbar.toString()), false);
        
        // Броня
        StringBuilder armor = new StringBuilder("Armor: ");
        for (int i = 36; i < 40; i++) {
            var stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                armor.append(stack.getName().getString()).append(" ");
            }
        }
        source.sendFeedback(() -> Text.literal(armor.toString()), false);
        
        // Offhand
        var offhand = inventory.getStack(40);
        if (!offhand.isEmpty()) {
            source.sendFeedback(() -> Text.literal("Offhand: " + offhand.getCount() + "x" + offhand.getName().getString()), false);
        }
        
        return 1;
    }
    
    private static int giveFactionItem(ServerCommandSource source, String faction, String itemCommand) {
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        
        var members = BotFaction.getMembers(faction);
        var server = source.getServer();
        int count = 0;
        
        for (String memberName : members) {
            // Используем команду give для каждого члена фракции
            try {
                server.getCommandManager().getDispatcher().execute(
                    "give " + memberName + " " + itemCommand,
                    server.getCommandSource()
                );
                count++;
            } catch (Exception e) {
                // Игнорируем ошибки (игрок может быть оффлайн)
            }
        }
        
        final int given = count;
        source.sendFeedback(() -> Text.literal("Gave items to " + given + " members of faction '" + faction + "'"), true);
        return count;
    }
    
    private static int factionAttack(ServerCommandSource source, String faction, String targetName) {
        if (!BotFaction.getAllFactions().contains(faction)) {
            source.sendError(Text.literal("Faction '" + faction + "' not found!"));
            return 0;
        }
        
        var members = BotFaction.getMembers(faction);
        int count = 0;
        
        for (String memberName : members) {
            // Только боты могут атаковать
            if (BotManager.getAllBots().contains(memberName)) {
                BotCombat.setTarget(memberName, targetName);
                count++;
            }
        }
        
        final int attacking = count;
        source.sendFeedback(() -> Text.literal("Faction '" + faction + "' (" + attacking + " bots) attacking " + targetName + "!"), true);
        return count;
    }
    
    // ============ Команды китов ============
    
    private static int createKit(ServerCommandSource source, String kitName) {
        var player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player!"));
            return 0;
        }
        
        if (BotKits.kitExists(kitName)) {
            source.sendError(Text.literal("Kit '" + kitName + "' already exists!"));
            return 0;
        }
        
        if (BotKits.createKit(kitName, player)) {
            source.sendFeedback(() -> Text.literal("Kit '" + kitName + "' created from your inventory!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to create kit (empty inventory?)"));
            return 0;
        }
    }
    
    private static int deleteKit(ServerCommandSource source, String kitName) {
        if (BotKits.deleteKit(kitName)) {
            source.sendFeedback(() -> Text.literal("Kit '" + kitName + "' deleted!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Kit '" + kitName + "' not found!"));
            return 0;
        }
    }
    
    private static int listKits(ServerCommandSource source) {
        var kits = BotKits.getKitNames();
        if (kits.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No kits created. Use /pvpbot createkit <name> to create one."), false);
        } else {
            source.sendFeedback(() -> Text.literal("Kits (" + kits.size() + "): " + String.join(", ", kits)), false);
        }
        return 1;
    }
    
    private static int giveKitToBot(ServerCommandSource source, String botName, String kitName) {
        if (!BotManager.getAllBots().contains(botName)) {
            source.sendError(Text.literal("Bot '" + botName + "' not found!"));
            return 0;
        }
        
        if (!BotKits.kitExists(kitName)) {
            source.sendError(Text.literal("Kit '" + kitName + "' not found!"));
            return 0;
        }
        
        var bot = BotManager.getBot(source.getServer(), botName);
        if (bot == null) {
            source.sendError(Text.literal("Bot '" + botName + "' is offline!"));
            return 0;
        }
        
        if (BotKits.giveKit(kitName, bot)) {
            source.sendFeedback(() -> Text.literal("Gave kit '" + kitName + "' to bot '" + botName + "'"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to give kit!"));
            return 0;
        }
    }
    
    private static int giveKitToFaction(ServerCommandSource source, String factionName, String kitName) {
        if (!BotFaction.getAllFactions().contains(factionName)) {
            source.sendError(Text.literal("Faction '" + factionName + "' not found!"));
            return 0;
        }
        
        if (!BotKits.kitExists(kitName)) {
            source.sendError(Text.literal("Kit '" + kitName + "' not found!"));
            return 0;
        }
        
        var members = BotFaction.getMembers(factionName);
        if (members == null || members.isEmpty()) {
            source.sendError(Text.literal("Faction '" + factionName + "' has no members!"));
            return 0;
        }
        
        int count = 0;
        for (String memberName : members) {
            // Проверяем что это бот
            if (BotManager.getAllBots().contains(memberName)) {
                var bot = BotManager.getBot(source.getServer(), memberName);
                if (bot != null && BotKits.giveKit(kitName, bot)) {
                    count++;
                }
            }
        }
        
        final int given = count;
        source.sendFeedback(() -> Text.literal("Gave kit '" + kitName + "' to " + given + " bots in faction '" + factionName + "'"), true);
        return count;
    }
}
