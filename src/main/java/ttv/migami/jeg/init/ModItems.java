package ttv.migami.jeg.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.Attachments;
import ttv.migami.jeg.common.GunModifiers;
import ttv.migami.jeg.item.*;
import ttv.migami.jeg.item.attachment.impl.*;
import ttv.migami.jeg.item.attachment.item.*;

public class ModItems {

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    public static final RegistryObject<AnimatedGunItem> ABSTRACT_GUN =
            REGISTER.register("abstract_gun",
                    () -> new AnimatedGunItem(new Item.Properties().stacksTo(1), "abstract_gun"));

    /* Firearms */
    /* Scrap Tier */
    public static final RegistryObject<AnimatedGunItem> REVOLVER = REGISTER.register("revolver",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128),
                    "revolver"
            ));
    public static final RegistryObject<AnimatedGunItem>WATERPIPE_SHOTGUN = REGISTER.register("waterpipe_shotgun",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128),
                    "waterpipe_shotgun"
            ));
    public static final RegistryObject<AnimatedGunItem>CUSTOM_SMG = REGISTER.register("custom_smg",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(312)
                    .rarity(Rarity.UNCOMMON),
                    "custom_smg"
            ));
    public static final RegistryObject<AnimatedGunItem>DOUBLE_BARREL_SHOTGUN = REGISTER.register("double_barrel_shotgun",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128),
                    "double_barrel_shotgun"
            ));
    /* Gunmetal Tier */
    public static final RegistryObject<AnimatedGunItem> SEMI_AUTO_PISTOL = REGISTER.register("semi_auto_pistol",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(256)
                    .rarity(Rarity.UNCOMMON),
                    "semi_auto_pistol"
            ));
    public static final RegistryObject<AnimatedGunItem> SEMI_AUTO_RIFLE = REGISTER.register("semi_auto_rifle",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(384)
                    .rarity(Rarity.UNCOMMON),
                    "semi_auto_rifle"
            ));
    public static final RegistryObject<AnimatedGunItem> ASSAULT_RIFLE = REGISTER.register("assault_rifle",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(384)
                    .rarity(Rarity.RARE),
                    "assault_rifle"
            ));
    public static final RegistryObject<AnimatedGunItem> PUMP_SHOTGUN = REGISTER.register("pump_shotgun",
            () -> new AnimatedMakeshiftGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(384)
                    .rarity(Rarity.RARE),
                    "pump_shotgun"
            ));
    /* Gunnite Tier */
    public static final RegistryObject<AnimatedGunItem> COMBAT_PISTOL = REGISTER.register("combat_pistol",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(480)
                    .rarity(Rarity.RARE),
                    "combat_pistol"
            ));
    public static final RegistryObject<AnimatedGunItem> BURST_RIFLE = REGISTER.register("burst_rifle",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(640)
                    .rarity(Rarity.EPIC),
                    "burst_rifle"
            ));
    public static final RegistryObject<AnimatedGunItem> COMBAT_RIFLE = REGISTER.register("combat_rifle",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(640)
                    .rarity(Rarity.EPIC),
                    "combat_rifle"
            ));
    public static final RegistryObject<AnimatedGunItem> BOLT_ACTION_RIFLE = REGISTER.register("bolt_action_rifle",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(384)
                    .rarity(Rarity.EPIC),
                    "bolt_action_rifle"
            ));
    public static final RegistryObject<AnimatedGunItem> FLARE_GUN = REGISTER.register("flare_gun",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(32)
                    .rarity(Rarity.UNCOMMON),
                    "flare_gun"
            ));

    /* Blue-printable */
    public static final RegistryObject<AnimatedGunItem> REPEATING_SHOTGUN = REGISTER.register("repeating_shotgun",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(384)
                    .rarity(Rarity.EPIC),
                    "repeating_shotgun"
            ));
    public static final RegistryObject<AnimatedGunItem> INFANTRY_RIFLE = REGISTER.register("infantry_rifle",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(512)
                    .rarity(Rarity.EPIC),
                    "infantry_rifle"
            ));
    public static final RegistryObject<AnimatedGunItem> SERVICE_RIFLE = REGISTER.register("service_rifle",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(512)
                    .rarity(Rarity.EPIC),
                    "service_rifle"
            ));

    /* Spectre Tier */
    public static final RegistryObject<AnimatedGunItem> BLOSSOM_RIFLE = REGISTER.register("blossom_rifle",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(512)
                    .rarity(Rarity.EPIC),
                    "blossom_rifle"
            ));
    public static final RegistryObject<GunItem> HOLY_SHOTGUN = REGISTER.register("holy_shotgun",
            () -> new GunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(384)
                    .rarity(Rarity.EPIC)
            ));

    /* Water Tier */
    public static final RegistryObject<GunItem> ATLANTEAN_SPEAR = REGISTER.register("atlantean_spear",
            () -> new GunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128)
                    .rarity(Rarity.EPIC)
            ));
    public static final RegistryObject<GunItem> TYPHOONEE = REGISTER.register("typhoonee",
            () -> new GunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128)
                    .rarity(Rarity.EPIC)
            ));

    /*public static final RegistryObject<UnderwaterFirearmItem> BUBBLE_CANNON = REGISTER.register("bubble_cannon",
            () -> new UnderwaterFirearmItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128)
                    .rarity(Rarity.EPIC)
            ));*/

    /* Fire Tier */
    public static final RegistryObject<AnimatedGunItem> HOLLENFIRE_MK2 = REGISTER.register("hollenfire_mk2",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(400)
                    .rarity(Rarity.EPIC),
                    "hollenfire_mk2"
            ));
    public static final RegistryObject<AnimatedGunItem> SOULHUNTER_MK2 = REGISTER.register("soulhunter_mk2",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(400)
                    .rarity(Rarity.EPIC),
                    "soulhunter_mk2"
            ));

    /* Sculk Tier */
    public static final RegistryObject<AnimatedGunItem> SUBSONIC_RIFLE = REGISTER.register("subsonic_rifle",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(384)
                    .rarity(Rarity.EPIC),
                    "subsonic_rifle"
            ));
    public static final RegistryObject<AnimatedGunItem> SUPERSONIC_SHOTGUN = REGISTER.register("supersonic_shotgun",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(86)
                    .rarity(Rarity.EPIC),
                    "supersonic_shotgun"
            ));
    public static final RegistryObject<AnimatedGunItem> HYPERSONIC_CANNON = REGISTER.register("hypersonic_cannon",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(100)
                    .rarity(Rarity.EPIC),
                    "hypersonic_cannon"
            ));

    public static final RegistryObject<AnimatedGunItem> ROCKET_LAUNCHER = REGISTER.register("rocket_launcher",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(32)
                    .rarity(Rarity.EPIC),
                    "rocket_launcher"
            ));
    public static final RegistryObject<AnimatedBowItem> COMPOUND_BOW = REGISTER.register("compound_bow",
            () -> new AnimatedBowItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(128)
                    .rarity(Rarity.EPIC),
                    "compound_bow"
            ));
    public static final RegistryObject<AnimatedGunItem> GRENADE_LAUNCHER = REGISTER.register("grenade_launcher",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(64)
                    .rarity(Rarity.EPIC),
                    "grenade_launcher"
            ));
    public static final RegistryObject<AnimatedGunItem> LIGHT_MACHINE_GUN = REGISTER.register("light_machine_gun",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(1024)
                    .rarity(Rarity.EPIC),
                    "light_machine_gun"
            ));
    public static final RegistryObject<AnimatedGunItem> FLAMETHROWER = REGISTER.register("flamethrower",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(1024)
                    .rarity(Rarity.EPIC),
                    "flamethrower"
            ));
    public static final RegistryObject<AnimatedGunItem> MINIGUN = REGISTER.register("minigun",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(2048)
                    .rarity(Rarity.EPIC),
                    "minigun"
            ));

    public static final RegistryObject<AnimatedGunItem> FINGER_GUN = REGISTER.register("finger_gun",
            () -> new AnimatedGunItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC),
                    "finger_gun"
            ));

    //public static final RegistryObject<Item> GRENADE_LAUNCHER = REGISTER.register("grenade_launcher", () -> new GunItem(new Item.Properties().stacksTo(1)));

    // Score Streaks
    public static final RegistryObject<ScoreStreakItem> AIR_STRIKE_FLARE = REGISTER.register("air_strike_flare",
            () -> new AirStrikeFlareItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1), 5000));

    public static final RegistryObject<ScoreStreakItem> PHANTOM_GUNNER_BAIT = REGISTER.register("phantom_gunner_bait",
            () -> new PhantomGunnerBaitItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), 3000));

    /* Projectiles And Throwables */
    public static final RegistryObject<Item> GRENADE = REGISTER.register("grenade",
            () -> new GrenadeItem(new Item.Properties()
                    .stacksTo(16)
                    , 20 * 4
            ));
    public static final RegistryObject<Item> STUN_GRENADE = REGISTER.register("stun_grenade",
            () -> new StunGrenadeItem(new Item.Properties()
                    .stacksTo(16)
                    , 72000
            ));
    public static final RegistryObject<Item> MOLOTOV_COCKTAIL = REGISTER.register("molotov_cocktail",
            () -> new MolotovCocktailItem(new Item.Properties()
                    .stacksTo(16)
                    , 72000
            ));
    // Thanks to An0m3l1 on Discord!
    public static final RegistryObject<Item> SMOKE_GRENADE = REGISTER.register("smoke_grenade",
            () -> new SmokeGrenadeItem(new Item.Properties()
                    .stacksTo(16)
                    , 20 * 5
            ));
    public static final RegistryObject<Item> WATER_BOMB = REGISTER.register("water_bomb",
            () -> new WaterBombItem(new Item.Properties()
                    .stacksTo(16)
                    , 72000
            ));
    public static final RegistryObject<Item> POCKET_BUBBLE = REGISTER.register("pocket_bubble",
            () -> new PocketBubbleItem(new Item.Properties()
                    .stacksTo(16)
            ));
    public static final RegistryObject<Item> ROCKET = REGISTER.register("rocket",
            () -> new AmmoItem(new Item.Properties().rarity(Rarity.EPIC)
                    .stacksTo(4)
            ));
    public static final RegistryObject<Item> EXPLOSIVE_CHARGE = REGISTER.register("explosive_charge",
            () -> new ExplosiveChargeItem(new Item.Properties()
                    .stacksTo(2).rarity(Rarity.EPIC)
                    , 72000
            ));
    public static final RegistryObject<Item> FLARE = REGISTER.register("flare",
            () -> new FlareItem(new Item.Properties()
                    .stacksTo(16)
                    , 72000
            ));
    public static final RegistryObject<Item> TERROR_ARMADA_FLARE = REGISTER.register("terror_armada_flare",
            () -> new FlareItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC)
                    , 72000
            ));


    /* Ammo */
    public static final RegistryObject<Item> RIFLE_AMMO = REGISTER.register("rifle_ammo",
            () -> new AmmoItem(new Item.Properties()
            ));
    public static final RegistryObject<Item> PISTOL_AMMO = REGISTER.register("pistol_ammo",
            () -> new AmmoItem(new Item.Properties()
            ));
    public static final RegistryObject<Item> HANDMADE_SHELL = REGISTER.register("handmade_shell",
            () -> new AmmoItem(new Item.Properties()
                    .stacksTo(16)
            ));
    public static final RegistryObject<Item> SHOTGUN_SHELL = REGISTER.register("shotgun_shell",
            () -> new AmmoItem(new Item.Properties()
                    .stacksTo(16)
            ));
    public static final RegistryObject<Item> SPECTRE_ROUND = REGISTER.register("spectre_round",
            () -> new AmmoItem(new Item.Properties()
            ));
    public static final RegistryObject<Item> BLAZE_ROUND = REGISTER.register("blaze_round",
            () -> new AmmoItem(new Item.Properties()
            ));

    /* Healing Items */
    public static final RegistryObject<Item> HEALING_TALISMAN = REGISTER.register("healing_talisman",
            () -> new HealingTalismanItem(new Item.Properties()
                    .stacksTo(16)
            ));

    /* Utility Items */
    /*public static final RegistryObject<Item> GRAPPLING_HOOK = REGISTER.register("grappling_hook",
            () -> new GrapplingHookItem(new Item.Properties()
                    .stacksTo(1)
            ));*/

    /* Paint Job Cans */
    /*public static final RegistryObject<Item> CAMO_SPRAY_CAN = REGISTER.register("camo_spray_can",
            () -> new PaintJobCanItem(new Item.Properties().stacksTo(1), "camo"));*/

    public static final RegistryObject<Item> CLASSIC_SPRAY_CAN = REGISTER.register("classic_spray_can",
            () -> new PaintJobCanItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1), "classic"));

    public static final RegistryObject<Item> TOY_SPRAY_CAN = REGISTER.register("toy_spray_can",
            () -> new PaintJobCanItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), "toy"));

    public static final RegistryObject<Item> WHITEOUT_SPRAY_CAN = REGISTER.register("whiteout_spray_can",
            () -> new PaintJobCanItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), "whiteout"));

    /*public static final RegistryObject<Item> SCORCHED_SPRAY_CAN = REGISTER.register("scorched_spray_can",
            () -> new PaintJobCanItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), "scorched"));*/

    /*public static final RegistryObject<Item> ANIME_SPRAY_CAN = REGISTER.register("anime_spray_can",
            () -> new PaintJobCanItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1), "anime"));*/

    public static final RegistryObject<Item> GOLDEN_SPRAY_CAN = REGISTER.register("golden_spray_can",
            () -> new PaintJobCanItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1), "golden"));

    /* Kill Effects */
    public static final RegistryObject<Item> CREEPER_BIRTHDAY_PARTY_BADGE   = REGISTER.register("creeper_birthday_party_badge",
            () -> new KillEffectItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    public static final RegistryObject<Item> HEADPOPPER_BADGE = REGISTER.register("headpoppper_badge",
            () -> new KillEffectItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    public static final RegistryObject<Item> TRICKSHOT_BADGE = REGISTER.register("trickshot_badge",
            () -> new KillEffectItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    /* Scope Attachments */
    public static final RegistryObject<Item> REFLEX_SIGHT = REGISTER.register("reflex_sight",
            () -> new ScopeItem(Attachments.REFLEX_SIGHT, new Item.Properties()
                    .stacksTo(1)
                    .durability(800)
            ));

    public static final RegistryObject<Item> MONOCLE_SIGHT = REGISTER.register("monocle_sight",
            () -> new ScopeItem(Attachments.MONOCLE_SIGHT, new Item.Properties()
                    .stacksTo(1)
                    .durability(800)
            ));

    public static final RegistryObject<Item> HOLOGRAPHIC_SIGHT = REGISTER.register("holographic_sight",
            () -> new ScopeItem(Attachments.HOLOGRAPHIC_SIGHT, new Item.Properties().rarity(Rarity.UNCOMMON)
                    .stacksTo(1)
                    .durability(800)
            ));

    public static final RegistryObject<Item> COMBAT_SCOPE = REGISTER.register("combat_scope",
            () -> new ScopeItem(Attachments.COMBAT_SCOPE, new Item.Properties().rarity(Rarity.RARE)
                    .stacksTo(1)
                    .durability(800)
            ));

    public static final RegistryObject<Item> TELESCOPIC_SIGHT = REGISTER.register("telescopic_sight",
            () -> new TelescopicScopeItem(Attachments.TELESCOPIC_SIGHT, new Item.Properties().rarity(Rarity.EPIC)
                    .stacksTo(1)
                    .durability(800)
            ));

    /* Stock Attachments */
    public static final RegistryObject<Item> MAKESHIFT_STOCK = REGISTER.register("makeshift_stock",
            () -> new MakeshiftStockItem(Stock.create(
                    GunModifiers.MAKESHIFT_CONTROL),
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(300)
                            
                    , false
            ));
    public static final RegistryObject<Item> LIGHT_STOCK = REGISTER.register("light_stock",
            () -> new StockItem(Stock.create(
                    GunModifiers.BETTER_CONTROL),
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(600)
                            
                    , false
            ));
    public static final RegistryObject<Item> TACTICAL_STOCK = REGISTER.register("tactical_stock",
            () -> new StockItem(Stock.create(
                    GunModifiers.STABILISED),
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(800)
                            
                    , false
            ));
    public static final RegistryObject<Item> WEIGHTED_STOCK = REGISTER.register("weighted_stock",
            () -> new StockItem(Stock.create(
                    GunModifiers.SUPER_STABILISED),
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(1000)
            ));

    /* Barrel Attachments */
    public static final RegistryObject<Item> SILENCER = REGISTER.register("silencer",
            () -> new BarrelItem(Barrel.create(
                    0.0F,
                    GunModifiers.SILENCED),
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(500)
            ));
                            //GunModifiers.REDUCED_DAMAGE),

    public static final RegistryObject<Item> EXPLOSIVE_MUZZLE = REGISTER.register("explosive_muzzle",
            () -> new BarrelItem(Barrel.create(
                    0.0F,
                    GunModifiers.EXPLOSIVE_AMMO, GunModifiers.INCREASED_JAMMING, GunModifiers.INSCREASED_DAMAGE, GunModifiers.WORSE_CONTROL),
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(64)
                            .rarity(Rarity.UNCOMMON)
            ));

    public static final RegistryObject<Item> TRUMPET = REGISTER.register("trumpet",
            () -> new TrumpetItem(Barrel.create(
                    0.0F,
                    GunModifiers.ANNOYING),
                    new Item.Properties()
                            .rarity(Rarity.RARE)
                            .stacksTo(1)
                            .durability(64)
            ));

    /* Under Barrel Attachments */
    public static final RegistryObject<Item> LIGHT_GRIP = REGISTER.register("light_grip",
            () -> new UnderBarrelItem(UnderBarrel.create(
                    GunModifiers.LIGHT_RECOIL), new
                    Item.Properties()
                    .stacksTo(1)
                    .durability(600)
            ));
    public static final RegistryObject<Item> VERTICAL_GRIP = REGISTER.register("vertical_grip",
            () -> new UnderBarrelItem(UnderBarrel.create(
                    GunModifiers.REDUCED_RECOIL), new
                    Item.Properties()
                    .stacksTo(1)
                    .durability(800)
            ));

    public static final RegistryObject<Item> ANGLED_GRIP = REGISTER.register("angled_grip",
            () -> new UnderBarrelItem(UnderBarrel.create(
                    GunModifiers.REDUCED_RECOIL), new
                    Item.Properties()
                    .stacksTo(1)
                    .durability(800)
            ));

    /* Magazine */
    public static final RegistryObject<Item> EXTENDED_MAG = REGISTER.register("extended_mag",
            () -> new MagazineItem(Magazine.create(
                    GunModifiers.SLOW_ADS),
                    new Item.Properties()
                            .stacksTo(1)
            ));

    public static final RegistryObject<Item> DRUM_MAG = REGISTER.register("drum_mag",
            () -> new MagazineItem(Magazine.create(
                    GunModifiers.SLOWER_ADS),
                    new Item.Properties()
                            .stacksTo(1)
            ));

    /* Side */
    public static final RegistryObject<Item> FLASHLIGHT = REGISTER.register("flashlight",
            () -> new FlashlightItem(Side.create(
                    GunModifiers.FLASHLIGHT, GunModifiers.SLOW_ADS),
                    new Item.Properties()
                            .rarity(Rarity.UNCOMMON)
                            .stacksTo(1)
            ));

    public static final RegistryObject<Item> LASER_POINTER = REGISTER.register("laser_pointer",
            () -> new SpecialItem(Side.create(
                    GunModifiers.LASER_POINTER, GunModifiers.BETTER_CONTROL),
                    new Item.Properties()
                            .rarity(Rarity.UNCOMMON)
                            .stacksTo(1)
            ));

    /* Loot Drop Items */
    public static final RegistryObject<LootDropItem> AMMO_POUCH = REGISTER.register("ammo_pouch",
            () -> new LootDropItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<LootDropItem> BADGE_PACK = REGISTER.register("badge_pack",
            () -> new LootDropItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<LootDropItem> SKIN_CRATE = REGISTER.register("skin_crate",
            () -> new LootDropItem(new Item.Properties().rarity(Rarity.EPIC)));

    /* Items */
    public static final RegistryObject<Item> SCRAP = REGISTER.register("scrap",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> REPAIR_KIT = REGISTER.register("repair_kit",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> TECH_TRASH = REGISTER.register("tech_trash",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> CIRCUIT_BOARD = REGISTER.register("circuit_board",
            () -> new Item(new Item.Properties()));
    //public static final RegistryObject<Item> SPRING = REGISTER.register("spring",
    //        () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUNMETAL_GRIT = REGISTER.register("gunmetal_grit",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> GUNMETAL_INGOT = REGISTER.register("gunmetal_ingot",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> GUNMETAL_NUGGET = REGISTER.register("gunmetal_nugget",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GUNNITE_INGOT = REGISTER.register("gunnite_ingot",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> RAW_BRIMSTONE = REGISTER.register("raw_brimstone",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> BRIMSTONE_CRYSTAL = REGISTER.register("brimstone_crystal",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<Item> ECTOPLASM = REGISTER.register("ectoplasm",
            () -> new ToolTipItem(new Item.Properties()));
    public static final RegistryObject<BlueprintItem> FIREARM_BLUEPRINT = REGISTER.register("firearm_blueprint",
            () -> new BlueprintItem(new Item.Properties()));
    public static final RegistryObject<BlueprintItem> ADVANCED_FIREARM_BLUEPRINT = REGISTER.register("advanced_firearm_blueprint",
            () -> new AdvancedBlueprintItem(new Item.Properties().rarity(Rarity.RARE)));

    // Mobs
    public static final RegistryObject<Item> GHOUL_SPAWN_TALISMAN = REGISTER.register("ghoul_spawn_talisman",
            () -> new ForgeSpawnEggItem(ModEntities.GHOUL, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> BOO_SPAWN_HONEYCOMB = REGISTER.register("boo_spawn_honeycomb",
            () -> new ForgeSpawnEggItem(ModEntities.BOO, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> TERROR_PHANTOM_SPAWN_EGG = REGISTER.register("terror_phantom_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TERROR_PHANMTOM, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> PHANTOM_GUNNER_SPAWN_EGG = REGISTER.register("phantom_gunner_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PHANTOM_GUNNER, 0xFFFFFF, 0xFFFFFF, new Item.Properties()));

    public static final RegistryObject<Item> SOUL_TREAT = REGISTER.register("soul_treat",
            () -> new SoultreatItem(new Item.Properties()));

    // Gunner Mobs
    public static final RegistryObject<Item> GUNNER_ZOMBIE_SPAWN_EGG = REGISTER.register("gunner_zombie_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.ZOMBIE, 0x00AFAF, 0x799C65, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_HUSK_SPAWN_EGG = REGISTER.register("gunner_husk_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.HUSK, 0x797061, 0xE6CC94, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_DROWNED_SPAWN_EGG = REGISTER.register("gunner_drowned_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.DROWNED, 0x8FF1D7, 0x799C65, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_ZOMBIE_VILLAGER_SPAWN_EGG = REGISTER.register("gunner_zombie_villager_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.ZOMBIE_VILLAGER, 0x563C33, 0x799C65, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_SKELETON_SPAWN_EGG = REGISTER.register("gunner_skeleton_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.SKELETON, 0xC1C1C1, 0x494949, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_STRAY_SPAWN_EGG = REGISTER.register("gunner_stray_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.STRAY, 0x617677, 0xDDEAEA, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_PILLAGER_SPAWN_EGG = REGISTER.register("gunner_pillager_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.PILLAGER, 0x532F36, 0x959B9B, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_VINDICATOR_SPAWN_EGG = REGISTER.register("gunner_vindicator_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.VINDICATOR, 0x959B9B, 0x275E61, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_PIGLIN_SPAWN_EGG = REGISTER.register("gunner_piglin_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.PIGLIN, 0x995F40, 0xF9F3A4, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_PIGLIN_BRUTE_SPAWN_EGG = REGISTER.register("gunner_piglin_brute_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.PIGLIN_BRUTE, 0x592A10, 0xF9F3A4, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_ZOMBIFIED_PIGLIN_SPAWN_EGG = REGISTER.register("gunner_zombified_piglin_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.ZOMBIFIED_PIGLIN, 0xEA9393, 0x4C7129, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_WITHER_SKELETON_SPAWN_EGG = REGISTER.register("gunner_wither_skeleton_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> EntityType.WITHER_SKELETON, 0x141414, 0x474D4D, new Item.Properties()));

    public static final RegistryObject<Item> GUNNER_GHOUL_SPAWN_EGG = REGISTER.register("gunner_ghoul_spawn_egg",
            () -> new GunnerSpawnEggItem(() -> ModEntities.GHOUL.get(), 0xF9F9F9, 0x00ADFF, new Item.Properties()));

    // Fallbacks
    /*public static final RegistryObject<Item> SCAR_L_FALLBACK = REGISTER.register("scar_l",
            () -> new FallbackItem(new Item.Properties()));

    public static final RegistryObject<Item> HK_G36_FALLBACK = REGISTER.register("hk_g36",
            () -> new FallbackItem(new Item.Properties()));*/
}