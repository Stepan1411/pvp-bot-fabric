package org.stepan1411.pvp_bot.bot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BotNameGenerator {
    
    private static final List<String> PREFIXES = new ArrayList<>();
    private static final List<String> SUFFIXES = new ArrayList<>();
    private static final Random random = new Random();
    

    private static final String SPECIAL_NAMES_URL = "https://stepan1411.github.io/pvp-bot-fabric/special_names.json";
    

    private static List<String> specialNames = new ArrayList<>();
    private static int spawnChance = 10;
    private static boolean specialNamesLoaded = false;
    private static long lastLoadTime = 0;
    private static final long RELOAD_INTERVAL = 60000;
    

    private static final Pattern CAMEL_CASE = Pattern.compile("([A-Z][a-z0-9]+)");
    

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
        "HuskRider", "Echolite", "VoidWalker", "SkyForge", "DarkBlade",
        "ShadowWarden", "IronPhantom", "SkywardDrift", "EmberTalon", "ObsidianReaper",
        "CrystalWisp", "ThunderHawk", "MoltenVeil", "DuskStrider", "AstralForge",
        "GloomWeaver", "StormCaller", "AshenKnight", "FrozenOrb", "EnderHarbinger",
        "QuartzSerpent", "LunarWraith", "InfernoSage", "DeepWarden", "MythicFlame",
        "RuneBreaker", "SilentHowl", "SolarViper", "ChaosMender", "NeonSpectre",
        "BlightFang", "ArcanePulse", "CrimsonDrake", "GalaxyRider", "MossWarden",
        "PhantomEdge", "TwilightSeer", "VolcanicCore", "StarlitNomad", "BoneHarvester",
        "CelestialWolf", "DarkenedSoul", "ElectricMoth", "FrostbiteKing", "GildedRaven",
        "HollowCry", "IcyTempest", "JaggedSpire", "KarmaBlade", "LavaWhisper",
        "MysticHound", "NovaStriker", "OblivionEye", "PlasmaFist", "QuillStorm",
        "RiftWalker", "SableFury", "ToxicVein", "UmbralFlare", "ViperShade",
        "WickedGale", "XenoBlade", "YawningVoid", "ZenithHawk", "AuroraSting",
        "BasiliskGaze", "CursedFlame", "DreadedMaw", "EclipseWing", "FeralEcho",
        "GhastlyGrin", "HollowSpine", "IgnisRook", "JaggedFang", "KrakenTide",
        "LunarFang", "MoltenHeart", "NebulaFist", "OnyxScourge", "PolarClash",
        "QuakeBeast", "RavenousGhost", "SanguineThorn", "TidalWraith", "UmberHowl",
        "VermilionSoul", "WarpTalon", "XyloSpecter", "YmirFrost", "ZephyrBlade",
        "AshbornRogue", "BloodiedCrest", "CipherWing", "DriftSoul", "EmberWing",
        "Frostborne", "GloomRider", "HavocFang", "IroncladEye", "JinxedVeil",
        "KoboldKing", "Lichborne", "MireWalker", "Netherborn", "ObsidianFang",
        "PaleWarden", "QuillWing", "RimeStriker", "ScorchTalon", "ThornedSoul",
        "UndyingGaze", "VexedSpirit", "WardenFlame", "XenonRider", "YewbowHunter",
        "ZombieLord", "AegisFang", "BlightedEye", "CinderWing", "Doomsayer",
        "EbonClaw", "Frostfang", "Galeborn", "HellfireSoul", "IceboundWarden",
        "JaggedSoul", "Kingslayer", "LunarTide", "MagmaFist", "NightshadeEye",
        "Oathbreaker", "PhoenixWing", "Quicksilver", "RuneWarden", "Skullcrusher",
        "TombRaider", "UnseenHand", "Voidborn", "WitchingHour", "XiphosBlade",
        "YellowFang", "ZealotSoul", "AstralClaw", "BlazingSoul", "CrimsonWing",
        "DreadWarden", "EldritchEye", "Frostborn", "GrimReaper", "Hollowborn",
        "InfernalEye", "JungleStalker", "Knightfall", "LichKing", "Moonshadow",
        "NecroWarden", "Overseer", "Plaguebringer", "RavenQuill", "Shadowborn",
        "Thunderborn", "UndeadKing", "VampireLord", "Winterborn", "Xenomorph",
        "YoungPharaoh", "ZombieSlayer", "AncientWarden", "BlackenedSoul", "CursedWing",
        "Deathbringer", "Eclipseborn", "FrostWarden", "Ghostwalker", "Hellborn",
        "IceWarden", "Jackalope", "KillerBee", "Lunarborn", "MysticWarden",
        "Nightborn", "Oblivionborn", "PoisonFang", "Runeborn", "SkeletonKing",
        "TreantGuard", "UndyingSoul", "VileWarden", "Witherborn", "Xenowarden",
        "YetiFang", "ZombieWarden", "AbyssalEye", "Bloodmoon", "CryptKeeper",
        "Dragonborn", "Elderguard", "Firestarter", "Golemheart", "HydraFang",
        "Ironborn", "JesterKing", "Krakenborn", "LightningSoul", "Manticore",
        "Netherking", "OgreSmash", "Pandemonium", "Quakeborn", "Ragnarok",
        "SerpentEye", "Titanborn", "UndyingFlame", "Valkyrie", "WarlockEye",
        "Xenoborn", "Yggdrasil", "ZombieEye", "AlphaWolf", "BrutalFang",
        "Chaosborn", "Doomsday", "EnchantedSoul", "Frostwolf", "GrimSoul",
        "Hellhound", "Icefang", "Juggernaut", "KillerWhale", "Lichborn",
        "Moonfang", "Necromancer", "OrcSlayer", "Phoenixborn", "Quillborn",
        "RaptorClaw", "Shadowfang", "Thunderfang", "UndeadEye", "Viperborn",
        "Witchborn", "Xenofang", "Yetiborn", "Zombieborn", "Aetherborn",
        "Blazefang", "Cinderborn", "Darkborn", "Earthshaker", "Frostborn",
        "GoblinKing", "HawkEye", "Ironfang", "JadeSoul", "Knightborn",
        "Lionheart", "Magmaborn", "Nighteye", "OwlWarden", "PantherClaw",
        "Quartzborn", "Ravenborn", "ScorpionTail", "TigerClaw", "UnicornHorn",
        "VampireEye", "Wolfborn", "Xenoclaw", "Yakborn", "ZebraFang",
        "AetherWing", "Blizzard", "CaveCrawler", "Dustborn", "Elfborn",
        "Fireborn", "Giantborn", "HedgeWitch", "Iceborn", "Jellyfish",
        "Kangaroo", "LeopardClaw", "MantisShrimp", "Nebulaborn", "OctopusKing",
        "PenguinSlayer", "QuokkaSmile", "RhinoCharge", "SnakeCharmer", "TurtleShell",
        "UmbraWing", "VultureEye", "WalrusTusk", "Xenowolf", "YakFang",
        "ZombieTusk", "AetherFang", "BansheeWail", "CactusSpine", "DaisyChain",
        "EagleEye", "Foxfire", "Gargoyle", "HoneyBadger", "IcicleSpear",
        "JaguarClaw", "KoalaHug", "LemurLeap", "MoleDig", "NarwhalTusk",
        "OstrichRun", "PeacockFeather", "QuailCall", "RaccoonBandit", "Seahorse",
        "ToucanBeak", "UnicornTail", "ViperStrike", "WombatBurrow", "Xenobeak",
        "YakHorn", "ZebraStripe", "AetherSoul", "BeetleShell", "CoralReef",
        "DolphinLeap", "EmuSprint", "FlamingoLeg", "GeckoToe", "HamsterWheel",
        "IguanaScale", "JellyfishSting", "KangarooHop", "LadybugSpot", "MeerkatWatch",
        "NewtTail", "OrcaSong", "ParrotTalk", "QuetzalFeather", "RobinSong",
        "SlothClimb", "TermiteMound", "UrchinSpine", "VultureScreech", "WhaleSong",
        "Xenofin", "YakHoof", "ZebraHoof", "AetherEye", "BisonCharge",
        "CheetahDash", "DeerAntler", "ElephantTusk", "FrogLeap", "GazelleRun",
        "HippoYawn", "ImpalaJump", "JackalHowl", "Klipspringer", "LynxPounce",
        "MooseAntler", "NumbatSnout", "OkapiStripe", "PumaPounce", "QuollBite",
        "ReindeerHorn", "SkunkSpray", "TapirSnout", "UrialHorn", "VicunaWool",
        "Wildebeest", "Xenohorn", "YakFur", "ZebuHump", "AetherHorn",
        "BadgerClaw", "CougarRoar", "DingoHowl", "ElkAntler", "FalconDive",
        "GrizzlyRoar", "HawkSwoop", "IbisBeak", "JaguarRoar", "Kinkajou",
        "LeopardLeap", "ManateeFloat", "NarwhalSpear", "OtterSlide", "PangolinScale",
        "QuailFeather", "Rattlesnake", "SalmonLeap", "TarsierEye", "UakariFace",
        "VoleBurrow", "WallabyHop", "Xenotail", "YakTail", "ZorillaStink",
        "EmberFox", "QuartzPike", "LunarDrift", "SolarFlare", "ObsidianEdge",
		"PrismWarden", "StormCaller", "DustWalker", "MossGlider", "AshReaper",
		"JadeViper", "OnyxHawk", "PearlDiver", "RubySlayer", "SapphireKnight",
		"TopazHunter", "AmethystMage", "DiamondBack", "EmeraldEye", "GoldFang",
		"SilverTail", "BronzeShield", "CopperWire", "TinSoldier", "LeadWeight",
		"ZincCore", "NickelGear", "CobaltStar", "TitanFist", "SteelBone",
		"ChromeWing", "PlasmaJet", "NeonGhost", "LaserBeam", "PhotonRay",
		"QuantumLeap", "AtomicSpin", "NuclearWinter", "FusionCore", "FissionTrack",
		"GravityWell", "BlackHole", "WhiteDwarf", "RedGiant", "BlueSupernova",
		"GreenNebula", "PurpleVoid", "OrangeSunset", "YellowDawn", "PinkCloud",
		"CyanRiver", "MagentaSky", "LimeForest", "TealOcean", "IndigoMountain",
		"VioletValley", "ScarletDesert", "CoralReef", "TurquoiseLake", "MaroonCave",
		"OliveGrove", "NavySea", "BeigeSand", "IvoryTower", "CharcoalPit",
		"SlateRoof", "RustPipe", "MintLeaf", "PeachSkin", "PlumTree",
		"BerryBush", "GrapeVine", "AppleSeed", "CherryBlossom", "LemonZest",
		"OrangePeel", "BananaSplit", "KiwiFruit", "MelonSlice", "PumpkinPie",
		"CookieJar", "CakeWalk", "CandyCrush", "SugarRush", "SaltRock",
		"PepperMill", "SpiceRoute", "HerbGarden", "RootCellar", "StemCell",
		"LeafBlower", "BranchOut", "TrunkLine", "BarkBit", "SapFlow",
		"ResinDrop", "PitchBlack", "TarPit", "OilSpill", "GasMask",
		"CoalMine", "IronOre", "GoldNugget", "DiamondGem", "EmeraldStone",
		"LapisLazuli", "RedstoneDust", "QuartzBlock", "NetheriteIngot", "AncientDebris",
		"CryingObsidian", "RespawnAnchor", "LodestoneCompass", "SmithingTable", "FletchingTable",
		"CartographyTable", "GrindstoneWheel", "StonecutterSaw", "LoomWeaver", "ComposterBin",
		"BarrelStore", "SmokerCook", "BlastFurnace", "CampfireLog", "SoulFire",
		"LanternLight", "TorchFlame", "SeaPickle", "GlowBerries", "GlowSquid",
		"AxolotlPlay", "FrogJump", "TadpoleSwim", "AllayFly", "WardenRoar",
		"SnifferDig", "CamelRide", "GoatHorn", "BeeHive", "HoneyComb",
		"HoneyBottle", "SugarCane", "BambooShoot", "CactusSpike", "DeadBush",
		"FernLeaf", "GrassBlock", "TallGrass", "LargeFern", "RoseBush",
		"PeonyFlower", "LilacBloom", "SunflowerHead", "DandelionSeed", "PoppyRed",
		"BlueOrchid", "AlliumPurple", "AzureBluet", "TulipRed", "TulipOrange",
		"TulipWhite", "TulipPink", "OxeyeDaisy", "CornflowerBlue", "LilyOfValley",
		"WitherRose", "SporeBlossom", "MossCarpet", "HangingRoots", "BigDripleaf",
		"SmallDripleaf", "AzaleaBush", "FloweringAzalea", "MangroveProp", "MudBrick",
		"PackedMud", "MuddyMane", "SculkSensor", "SculkCatalyst", "SculkShrieker",
		"SculkVein", "DeepslateTile", "CobbledDeepslate", "PolishedDeepslate", "DeepslateBrick",
		"DeepslateSlab", "DeepslateStair", "DeepslateWall", "InfestedStone", "InfestedCobble",
		"InfestedMossy", "InfestedCracked", "InfestedChiseled", "SilverfishBug", "EndermitePop",
		"ShulkerBox", "ShulkerBullet", "EnderPearl", "EnderDragon", "WitherBoss",
		"ElderGuardian", "EvokerSpell", "VindicatorAxe", "PillagerCrossbow", "RavagerBeast",
		"VexGhost", "IllagerFlag", "WitchPotion", "StrayArrow", "HuskSand",
		"DrownedTrident", "PhantomWing", "SlimeBall", "MagmaCube", "GhastTear",
		"BlazeRod", "ZombiePigman", "WitherSkeleton", "StriderLava", "HoglinTusk",
		"PiglinBarter", "PiglinBrute", "ZoglinRage", "ChickenEgg", "CowMilk",
		"SheepWool", "PorkChop", "FishRaw", "SalmonPink", "CodBlue",
		"TropicalFish", "PufferFish", "SquidInk", "GlowInk", "RabbitFoot",
		"RabbitHide", "LeatherTan", "FeatherFly", "GunpowderBang", "SpiderEye",
		"StringWeb", "BoneMeal", "RottingFlesh", "GoldenApple", "EnchantedApple",
		"ChorusFruit", "PoppedChorus", "DragonBreath", "ShulkerShell", "TotemUndying",
		"NautilusShell", "HeartOfSea", "PrismarineCrystal", "PrismarineShard", "SpongeWet",
		"DrySponge", "GlassPane", "StainedGlass", "HardenedClay", "TerracottaPot",
		"ConcretePowder", "SolidConcrete", "WhiteWool", "OrangeWool", "MagentaWool",
		"LightBlueWool", "YellowWool", "LimeWool", "PinkWool", "GrayWool",
		"LightGrayWool", "CyanWool", "PurpleWool", "BlueWool", "BrownWool",
		"GreenWool", "RedWool", "BlackWool", "BedRock", "EndStone",
		"PurpurBlock", "PurpurPillar", "PurpurSlab", "PurpurStair", "EndRod",
		"BridgeEnd", "GatewayPortal", "ElytraWing", "FireworkRocket", "FireworkStar",
		"BookQuill", "WrittenBook", "KnowledgeBook", "MapEmpty", "MapFull",
		"CompassSpin", "ClockTick", "SpyglassZoom", "RecoveryCompass", "GoatHornSound",
		"MusicDisc13", "MusicDiscCat", "MusicDiscBlocks", "MusicDiscChirp", "MusicDiscFar",
		"MusicDiscMall", "MusicDiscMellohi", "MusicDiscStal", "MusicDiscStrad", "MusicDiscWard",
		"MusicDisc11", "MusicDiscWait", "MusicDiscOtherside", "MusicDisc5", "MusicDiscPigstep",
		"DiscFragment", "JukeboxSpin", "NoteBlockHit", "ItemFrameHang", "GlowFrame",
		"PaintingArt", "ArmorStandPose", "HeadSkull", "PlayerHead", "ZombieHead",
		"CreeperFace", "DragonHead", "WitherHead", "BeaconBeam", "ConduitPower",
		"TurtleShell", "ScuteDrop", "SeagrassCut", "KelpDry", "DriedKelp",
		"DriedKelpBlock", "SweetBerries", "GlowBerriesEat", "CaveVines", "SporeBloom",
		"MossBlockPlace", "RootsHanging", "AzaleaLeaves", "FloweringLeaves", "MangroveRoots",
		"MangroveLog", "StrippedMangrove", "MangrovePlanks", "MangroveStairs", "MangroveSlab",
		"MangroveFence", "MangroveDoor", "MangroveTrapdoor", "MangrovePressurePlate", "MangroveButton",
		"MangroveSign", "HangingSign", "CherryLog", "CherryPlanks", "CherryLeaves",
		"CherrySapling", "PinkPetals", "BambooRaft", "BambooChestRaft", "ChiseledBookshelf",
		"DecoratedPot", "BrushSweep", "SuspiciousSand", "SuspiciousGravel", "PotterySherd",
		"ArcherSherd", "ArmsUpSherd", "BladeSherd", "BrewerSherd", "BurnSherd",
		"DangerSherd", "ExplorerSherd", "FriendSherd", "HeartSherd", "HeartbreakSherd",
		"HowlSherd", "MinerSherd", "MournerSherd", "PlentySherd", "PrizeSherd",
		"SheafSherd", "ShelterSherd", "SkullSherd", "SnortSherd", "TrailSherd",
		"AnglerSherd", "WayfinderSherd", "RaiserSherd", "ShaperSherd", "HostArmor",
		"WolfArmor", "SnifferEgg", "BrushableBlock", "CalibratedSculk", "CrafterBlock",
		"CopperBulb", "ExposedCopper", "WeatheredCopper", "OxidizedCopper", "WaxedCopper",
		"WaxedExposed", "WaxedWeathered", "WaxedOxidized", "CopperGrate", "CopperDoor",
		"CopperTrapdoor", "TuffBlock", "PolishedTuff", "TuffBricks", "TuffSlab",
		"TuffStairs", "TuffWall", "ChiseledTuff", "TrialSpawner", "VaultBlock",
		"HeavyCore", "MaceWeapon", "WindCharge", "BreezeRod", "OminousBottle",
		"OminousTrial", "FlowArmor", "BoltArmor", "CreakingHeart", "PaleHanging",
		"PaleOakLog", "PaleOakPlanks", "PaleOakLeaves", "ResinClump", "WolfSpawn",
		"ArmoredWolf", "BoggedSkeleton", "SpiderJockey", "StrayJockey", "HuskJockey",
		"DrownedJockey", "ZombieVillager", "CuredVillager", "NitwitVillager", "BabyVillager",
		"WanderingTrader", "LlamaSpit", "TraderLlama", "ParrotCopy", "CatGift",
		"OcelotTrust", "PandaRoll", "LazyPanda", "AggressivePanda", "WorriedPanda",
		"PlayfulPanda", "WeakPanda", "BrownPanda", "RedPandaFake", "FoxSleep",
		"FoxItem", "SnowFox", "DesertRabbit", "KillerBunny", "ToastRabbit",
		"BlackRabbit", "SaltPepper", "JebSheep", "DreamSheep", "TechnoBlade",
		"PhilzaWing", "TommyInnit", "TubboBee", "RanbooMask", "WilburSong",
		"GeorgeNotFound", "SapnapDream", "BadBoyHalo", "SkeppyGem", "AntfrostCat",
		"PunzGreen", "AimseyCloud", "NihachuBoat", "JackManifold", "QuackityDuck",
		"KarlJacobs", "HBomb94", "ConnorEats", "LaniVlog", "VelvetIsCake",
		"CorpseHusband", "SykkunoHi", "LilNasXRap", "BillieEilish", "TaylorSwift",
		"ElonMusk", "MarkZuckerberg", "BillGates", "SteveJobs", "LinusTorvalds",
		"NotchCreater", "JebDeveloper", "DinnerboneFlip", "GrummUpside", "Entity303",
		"HerobrineLegend", "NullVoid", "CraftMiner", "BlockBreaker", "ItemCollector",
		"MobHunter", "BossSlayer", "DungeonCrawler", "CaveExplorer", "MountainClimber",
		"OceanDiver", "SkyBuilder", "LandscaperPro", "RedstoneEngineer", "CommandBlockGod",
		"FunctionWizard", "DatapackMaker", "ResourceArtist", "ShaderRunner", "ModDeveloper",
		"PluginCoder", "ServerAdmin", "CommunityLead", "ContentCreator", "StreamLive",
		"VideoEditor", "ThumbnailArt", "ClickBaitKing", "SubGoalMet", "DonationAlert",
		"MerchStore", "DiscordMod", "TelegramBot", "GithubRepo", "GitCommit",
		"PullRequest", "MergeConflict", "CodeReview", "BugFixer", "FeatureRequest",
		"IssueTracker", "VersionControl", "BranchMaster", "MainBranch", "DevBranch",
		"TestServer", "ProductionBuild", "SnapshotRelease", "BetaTester", "AlphaUser",
		"EarlyAccess", "PremiumMember", "VIPStatus", "OPPermission", "WhitelistOnly",
		"BanHammer", "KickPlayer", "MuteChat", "WarnUser", "TempBan",
		"PermBanIP", "GriefPrevent", "ClaimLand", "TrustPlayer", "UntrustUser",
		"ProtectRegion", "FlagSet", "WorldEditWand", "WorldGuardZone", "EssentialsKit",
		"LuckPermsNode", "VaultEconomy", "ChestShopBuy", "AdminShopSell", "PlayerAuction",
		"TradeAccept", "PartyInvite", "GuildCreate", "ClanWar", "FactionRaid",
		"SiegeMode", "CapturePoint", "KingdomRule", "EmpireBuild", "CivilizationStart",
		"AgeOfStone", "AgeOfIron", "AgeOfGold", "IndustrialRev", "DigitalAge",
		"FutureTech", "SpaceTravel", "MarsColony", "MoonBase", "StarShipCmd",
		"GalaxyMap", "UniverseGen", "MultiverseHub", "DimensionDoor", "PortalLink",
		"TeleportHome", "SpawnPoint", "BedSet", "AnchorCharge", "CompassLock",
		"MapMarker", "WaypointSave", "PathFindAI", "NavMeshGen", "CollisionDetect",
		"PhysicsEngine", "GravitySim", "FluidDynamic", "ParticleSys", "LightingCalc",
		"ShadowMap", "RayTrace", "GlobalIllum", "AmbientOccl", "BloomEffect",
		"MotionBlur", "DepthField", "AntiAlias", "TexturePack", "ModelMesh",
		"Animation Rig", "SoundFont", "MusicTrack", "VoiceChat", "MicCheck",
		"HeadsetOn", "KeyboardClick", "MousePadSlide", "ChairComfort", "DeskClean",
		"MonitorHigh", "GPUHot", "CPUCool", "RAMFull", "SSDFast",
		"HDDSlow", "InternetLag", "PingLow", "PacketLoss", "BandwidthCap",
		"DataLimit", "CloudSave", "LocalBackup", "AutoUpdate", "PatchNotes",
		"ChangelogRead", "WikiPage", "ForumPost", "RedditThread", "TweetPost",
		"InstaStory", "TikTokClip", "YouTubeShort", "TwitchPrime", "KickStream",
		"TrovoLive", "FacebookGame", "TwitterX", "LinkedInPro", "GitHubStar",
		"StackOverflow", "DevToBlog", "MediumArticle", "SubstackNews", "PatreonTier",
		"KoFiDonate", "PayPalSend", "CryptoPay", "NFTDrop", "MetaverseLand",
		"VirtualReal", "AvatarSkin", "EmoteDance", "GestureWave", "ChatBubble",
		"SpeechText", "TypingFast", "AFKMode", "IdleAnim", "SleepCycle",
		"WakeUpCall", "MorningCoffee", "NightOwl", "EarlyBird", "NightShift",
		"DayWorker", "WeekendWarrior", "HolidayMode", "VacationTime", "SickDay",
		"PersonalDay", "MaternityLeave", "PaternityCare", "RetirementPlan", "PensionFund",
		"StockMarket", "CryptoCoin", "BitcoinMine", "EthereumGas", "DogecoinWow",
		"LitecoinFast", "RippleXrp", "CardanoAda", "SolanaSol", "PolkadotDot",
		"ChainlinkLink", "UniswapUni", "AaveLend", "CompoundFin", "MakerDao",
		"YearnFinance", "SushiSwap", "PancakeSwap", "CurveFi", "BalancerPool",
		"Synthetix", "RenProtocol", "Loopring", "ZcashPriv", "MoneroXmr",
		"DashPay", "DecredDcr", "QtumQtm", "WavesPlatform", "LiskApp",
		"StratisStrat", "ArkArk", "KomodoKmd", "VertcoinVtc", "GroestlGrs",
		"DigibyteDgb", "SyscoinSys", "NamecoinNmc", "PeercoinPpc", "PrimecoinXpm",
		"FeathercoinFtc", "NovacoinNvc", "TerracoinTrc", "MegacoinMec", "WorldcoinWdc",
		"InfinitecoinIfc", "IxcoinIxc", "DevcoinDvc", "FreicoinFrc", "BBQcoinBqc",
		"YacoinYac", "FastcoinFst", "TagcoinTag", "AnoncoinAnc", "SexcoinSxc",
		"PhoenixcoinPxc", "DigitalcoinDgc", "GoldcoinGld", "ElacoinElc", "NetcoinNet",
		"MincoinMnc", "ArgentumArg", "CraftcoinXcc", "JunkcoinJkc", "SpotsSpt",
		"ExtremecoinExc", "FlorincoinFlc", "AlphacoinAlf", "OmnicoinOmc", "CosmosAtom",
		"TezosXtz", "AlgorandAlgo", "VechainVet", "ThetaToken", "FilecoinFil",
		"HederaHbar", "InternetComp", "NeoSmart", "OntologyOnt", "IconIcx",
		"ZilliqaZil", "HarmonyOne", "ElrondEgld", "NearProtocol", "FantomFtm",
		"AvalancheAvax", "PolygonMatic", "CeloCelo", "KusamaKsm", "MoonbeamGlmr",
		"MoonriverMovr", "AstarAstr", "ShidenSdn", "CloverClv", "KaruraKara",
        "AcalaAca", "ParallelPara", "HydraHdx", "DockDock", "PhalaPha",
        "PlasmPlm", "EdgewareEdg", "CentrifugeCfg", "KiltNetwork", "UniqueUnq",
        "QuartzQtz", "BitcountryBcn", "InterlayIntr", "EquilibriumEq", "BifrostBnc",
        "DarwiniaRing", "CrabNetwork", "LitentryLitr", "IntegriteeTee", "RobonomicsXrt",
        "SoraXor", "PolkadexPdex", "OriginTrailTrac", "ChainflipFlip", "ComposablePica",
        "PicassoPica", "ComposablCmpl", "BasmatiBsmt", "FrequencyFrq", "T0T0",
        "AutomataAtma", "MantaManta", "CalamariKma", "DolphinDol", "SubsocialSubx",
        "ZeitgeistZtg", "InvArchInv", "KpronKpr", "AltairAir", "ParallelHeiko",
        "KhalaPha", "BasiliskBsx", "SneakySnek", "ImbueImbu", "AmplitudeAmp",
        "PendulumPen", "CurioCgu", "T3rnT3rn", "BrainnetBrn", "AjunaAjun",
        "GenshiroGns", "OpalOpl", "GamifyGmf", "MythosMyth", "RoccocoRoc",
        "WestendWst", "VersiVrs", "LocalhostLoc", "TestnetTst", "DevnetDev",
        "StagenetStg", "CanaryCnr", "SandboxSbx", "PlaygroundPly", "DemoDem",
        "PreviewPrv", "AlphaAlp", "BetaBet", "GammaGam", "DeltaDel",
        "EpsilonEps", "ZetaZet", "EtaEta", "ThetaThet", "IotaIot",
        "KappaKap", "LambdaLam", "MuMu", "NuNu", "XiXi",
        "OmicronOmi", "PiPi", "RhoRho", "SigmaSig", "TauTau",
        "UpsilonUps", "PhiPhi", "ChiChi", "PsiPsi", "OmegaOmg",
        "AlphaWolf", "BetaFish", "GammaRay", "DeltaForce", "EpsilonTeam",
        "ZetaPotential", "EtaCarinae", "ThetaBrain", "IotaDevice", "KappaLogic",
        "LambdaCalc", "MuParticle", "NuClear", "XiPlanet", "OmicronStar",
        "PiValue", "RhoMethod", "SigmaRule", "TauTime", "UpsilonBond",
        "PhiAngle", "ChiSquare", "PsiWave", "OmegaEnd", "AlphaStart",
        "BetaTest", "GammaFunc", "DeltaVar", "EpsilonErr", "ZetaZero",
        "EtaForm", "ThetaState", "IotaIndex", "KappaKey", "LambdaExpr",
        "MuVal", "NuNum", "XiSet", "OmicronObj", "PiConst",
        "RhoRef", "SigmaSum", "TauType", "UpsilonUnit", "PhiFunc",
        "ChiClass", "PsiProc", "OmegaOp", "AlphaGo", "BetaBit",
        "GammaGen", "DeltaDat", "EpsilonEnc", "ZetaZip", "EtaExe",
        "ThetaTxt", "IotaImg", "KappaKey", "LambdaLib", "MuMod",
        "NuNet", "XiXml", "OmicronOpt", "PiPkg", "RhoRun",
        "SigmaSrc", "TauTmp", "UpsilonUsr", "PhiVar", "ChiCfg",
        "PsiPrj", "OmegaWrk", "AlphaApp", "BetaBin", "GammaCab",
        "DeltaDir", "EpsilonEnv", "ZetaExt", "EtaFmt", "ThetaGui",
        "IotaIni", "KappaJs", "LambdaLog", "MuMem", "NuNav",
        "XiObj", "OmicronOut", "PiPtr", "RhoReg", "SigmaScr",
        "TauTab", "UpsilonUi", "PhiVal", "ChiWin", "PsiXml",
        "OmegaZip", "AlphaOne", "BetaTwo", "GammaThree", "DeltaFour",
        "EpsilonFive", "ZetaSix", "EtaSeven", "ThetaEight", "IotaNine",
        "KappaTen", "LambdaEleven", "MuTwelve", "NuThirteen", "XiFourteen",
        "OmicronFifteen", "PiSixteen", "RhoSeventeen", "SigmaEighteen", "TauNineteen",
        "UpsilonTwenty", "PhiThirty", "ChiForty", "PsiFifty", "OmegaHundred"
    };
    
    static {
        initializeParts();
        loadSpecialNames();
    }
    
    
    private static void loadSpecialNames() {

        long currentTime = System.currentTimeMillis();
        if (specialNamesLoaded && (currentTime - lastLoadTime) < RELOAD_INTERVAL) {
            return;
        }
        

        try {
            System.out.println("[PVP Bot] Trying to load special names from URL: " + SPECIAL_NAMES_URL);
            String json = downloadJson(SPECIAL_NAMES_URL);
            if (json != null && parseSpecialNames(json)) {
                System.out.println("[PVP Bot] Successfully loaded special names from URL");
                specialNamesLoaded = true;
                lastLoadTime = currentTime;
                return;
            }
        } catch (Exception e) {
            System.out.println("[PVP Bot] Failed to load from URL: " + e.getMessage());
        }
        

        if (!specialNamesLoaded) {
            try {
                System.out.println("[PVP Bot] Loading special names from local resources");
                InputStream stream = BotNameGenerator.class.getResourceAsStream("/special_names.json");
                if (stream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                    reader.close();
                    
                    if (parseSpecialNames(json.toString())) {
                        System.out.println("[PVP Bot] Successfully loaded special names from resources");
                        specialNamesLoaded = true;
                        lastLoadTime = currentTime;
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("[PVP Bot] Failed to load from resources: " + e.getMessage());
            }
            

            System.out.println("[PVP Bot] Using default special names");
            specialNames.add("nantag");
            specialNames.add("Stepan1411");
            spawnChance = 10;
            specialNamesLoaded = true;
            lastLoadTime = currentTime;
        }
    }
    
    
    private static String downloadJson(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "PVP-Bot-Fabric");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
        } catch (Exception e) {

        }
        return null;
    }
    
    
    private static boolean parseSpecialNames(String json) {
        try {
            Gson gson = new Gson();
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            
            if (obj.has("special_names") && obj.get("special_names").isJsonArray()) {
                specialNames.clear();
                obj.getAsJsonArray("special_names").forEach(element -> {
                    String name = element.getAsString();
                    if (name != null && !name.isEmpty() && name.length() <= 16) {
                        specialNames.add(name);
                    }
                });
            }
            
            if (obj.has("spawn_chance")) {
                spawnChance = obj.get("spawn_chance").getAsInt();
                if (spawnChance < 0) spawnChance = 0;
                if (spawnChance > 100) spawnChance = 100;
            }
            
            return !specialNames.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    
    public static List<String> getSpecialNames() {
        return new ArrayList<>(specialNames);
    }
    
    
    public static int getSpawnChance() {
        return spawnChance;
    }
    
    
    private static void initializeParts() {
        Set<String> prefixSet = new HashSet<>();
        Set<String> suffixSet = new HashSet<>();
        
        for (String name : BASE_NAMES) {
            List<String> parts = splitCamelCase(name);
            if (parts.size() >= 2) {
                prefixSet.add(parts.get(0));
                suffixSet.add(parts.get(parts.size() - 1));
            } else if (parts.size() == 1 && parts.get(0).length() > 4) {

                prefixSet.add(parts.get(0));
            }
        }
        
        PREFIXES.addAll(prefixSet);
        SUFFIXES.addAll(suffixSet);
    }
    
    
    private static List<String> splitCamelCase(String name) {
        List<String> parts = new ArrayList<>();
        Matcher matcher = CAMEL_CASE.matcher(name);
        while (matcher.find()) {
            parts.add(matcher.group(1));
        }
        return parts;
    }
    
    
    public static String generateUniqueName() {

        loadSpecialNames();
        
        Set<String> existingBots = BotManager.getAllBots();
        

        if (!specialNames.isEmpty() && random.nextInt(100) < spawnChance) {
            String specialName = specialNames.get(random.nextInt(specialNames.size()));
            if (!existingBots.contains(specialName)) {
                return specialName;
            }
        }
        
        for (int attempt = 0; attempt < 100; attempt++) {
            String name = generateName();

            if (name.length() <= 16 && !existingBots.contains(name)) {
                return name;
            }
        }
        

        for (int num = 1; num < 1000; num++) {
            String name = "Bot" + num;
            if (!existingBots.contains(name)) {
                return name;
            }
        }
        return "Bot" + System.currentTimeMillis() % 10000;
    }
    
    
    private static String generateName() {
        for (int i = 0; i < 20; i++) {
            String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
            String suffix = SUFFIXES.get(random.nextInt(SUFFIXES.size()));
            String name = prefix + suffix;
            if (name.length() <= 16) {
                return name;
            }
        }

        String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
        if (prefix.length() > 12) prefix = prefix.substring(0, 12);
        return prefix + random.nextInt(1000);
    }
}
