package ttv.migami.jeg.world.loot;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reference.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SCRAP_SIMPLE_DUNGEON =
            LOOT_MODIFIER_SERIALIZERS.register("scrap_on_simple_dungeon", ScrapOnSimpleDungeonModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> TECH_TRASH_SIMPLE_DUNGEON =
            LOOT_MODIFIER_SERIALIZERS.register("tech_trash_on_simple_dungeon", TechTrashOnSimpleDungeonModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ATLANTEAN_SPEAR_SHIPWRECK_TREASURE =
            LOOT_MODIFIER_SERIALIZERS.register("atlantean_spear_on_shipwreck_treasure", AtlanteanSpearOnShipwreckTreasureModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> TYPHOONE_ELDER_GUARDIAN =
            LOOT_MODIFIER_SERIALIZERS.register("typhoonee_on_elder_guardian", TyphooneeOnElderGuardianModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> GRENADE_CREEPER =
            LOOT_MODIFIER_SERIALIZERS.register("grenade_on_creeper", GrenadeOnCreeperModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> REPEATING_SHOTGUN_MINESHAFT =
            LOOT_MODIFIER_SERIALIZERS.register("repeating_shotgun_on_abandoned_mineshaft", RepeatingShotgunOnMineshaftModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> REPEATING_SHOTGUN_FISHING =
            LOOT_MODIFIER_SERIALIZERS.register("repeating_shotgun_bp_on_fishing", RepeatingShotgunBPOnFishingModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> INFANTRY_RIFLE_MINESHAFT =
            LOOT_MODIFIER_SERIALIZERS.register("infantry_rifle_on_abandoned_mineshaft", InfantryRifleOnMineshaftModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> INFANTRY_RIFLE_FISHING =
            LOOT_MODIFIER_SERIALIZERS.register("infantry_rifle_bp_on_fishing", InfantryRifleBPOnFishingModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SERVICE_RIFLE_MINESHAFT =
            LOOT_MODIFIER_SERIALIZERS.register("service_rifle_on_abandoned_mineshaft", ServiceRifleOnMineshaftModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SERVICE_RIFLE_FISHING =
            LOOT_MODIFIER_SERIALIZERS.register("service_rifle_bp_on_fishing", ServiceRifleBPOnFishingModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> HOLLENFIRE_MK2_BP_BARTERING =
            LOOT_MODIFIER_SERIALIZERS.register("hollenfire_mk2_bp_on_bartering", HollenfireMK2BPOnBarteringModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> SOULHUNTER_MK2_BP_BARTERING =
            LOOT_MODIFIER_SERIALIZERS.register("soulhunter_mk2_bp_on_bartering", SoulhunterMK2BPOnBarteringModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> FIRE_STARTER_BARTERING =
            LOOT_MODIFIER_SERIALIZERS.register("fire_starter_on_bartering", FireStarterOnBarteringModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> FLARE_GUN_MINESHAFT =
            LOOT_MODIFIER_SERIALIZERS.register("flare_gun_on_abandoned_mineshaft", FlareGunOnMineshaftModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> BLUEPRINT_WITHER =
            LOOT_MODIFIER_SERIALIZERS.register("blueprint_on_wither", BlueprintOnWither.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> BLUEPRINT_WARDEN =
            LOOT_MODIFIER_SERIALIZERS.register("blueprint_on_warden", BlueprintOnWarden.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ATLANTIC_SHOOTER_ON_SHIPWRECK =
            LOOT_MODIFIER_SERIALIZERS.register("atlantic_shooter_on_shipwreck", AtlanticShooterOnShipwreck.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> PRIMITIVE_BOW_JUNGLE_TEMPLE =
            LOOT_MODIFIER_SERIALIZERS.register("primitive_bow_on_jungle_temple", PrimitiveBowOnJungleTempleModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> PRIMITIVE_BLOWPIPE_JUNGLE_TEMPLE =
            LOOT_MODIFIER_SERIALIZERS.register("primitive_blowpipe_on_jungle_temple", PrimitiveBlowpipeOnJungleTempleModifier.CODEC);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> FIRE_SWEEPER_BASTION =
            LOOT_MODIFIER_SERIALIZERS.register("fire_sweeper_bastion", FireSweeperBastionModifier.CODEC);

    public static void register(IEventBus bus) {
        LOOT_MODIFIER_SERIALIZERS.register(bus);
    }
}