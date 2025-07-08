package ttv.migami.jeg.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MOD_ID);

    public static final RegistryObject<SoundEvent> ENCHANTED_GUN_FIRE = register("item.gun_generic.enchanted_fire");
    public static final RegistryObject<SoundEvent> FINGER_GUN_FIRE = register("item.finger_gun");
    public static final RegistryObject<SoundEvent> DOOT = register("item.doot");
    public static final RegistryObject<SoundEvent> BEEP = register("item.beep");
    public static final RegistryObject<SoundEvent> GOOSE = register("item.goose");
    public static final RegistryObject<SoundEvent> FLASHLIGHT = register("item.flashlight");
    public static final RegistryObject<SoundEvent> FLASHLIGHT_CHARGE = register("item.flashlight_charge");

    public static final RegistryObject<SoundEvent> COMBAT_RIFLE_FIRE = register("item.combat_rifle.fire");
    public static final RegistryObject<SoundEvent> COMBAT_RIFLE_SILENCED_FIRE = register("item.combat_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> COMBAT_RIFLE_ENCHANTED_FIRE = register("item.combat_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_FIRE = register("item.assault_rifle.fire");
    public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_SILENCED_FIRE = register("item.assault_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_ENCHANTED_FIRE = register("item.assault_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> REVOLVER_FIRE = register("item.revolver.fire");
    public static final RegistryObject<SoundEvent> REVOLVER_SILENCED_FIRE = register("item.revolver.silenced_fire");
    public static final RegistryObject<SoundEvent> REVOLVER_ENCHANTED_FIRE = register("item.revolver.enchanted_fire");
    public static final RegistryObject<SoundEvent> WATERPIPE_SHOTGUN_FIRE = register("item.waterpipe_shotgun.fire");
    public static final RegistryObject<SoundEvent> WATERPIPE_SHOTGUN_ENCHANTED_FIRE = register("item.waterpipe_shotgun.enchanted_fire");
    public static final RegistryObject<SoundEvent> SEMI_AUTO_RIFLE_FIRE = register("item.semi_auto_rifle.fire");
    public static final RegistryObject<SoundEvent> SEMI_AUTO_RIFLE_SILENCED_FIRE = register("item.semi_auto_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> SEMI_AUTO_RIFLE_ENCHANTED_FIRE = register("item.semi_auto_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> BURST_RIFLE_FIRE = register("item.burst_rifle.fire");
    public static final RegistryObject<SoundEvent> BURST_RIFLE_SILENCED_FIRE = register("item.burst_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> BURST_RIFLE_ENCHANTED_FIRE = register("item.burst_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> PUMP_SHOTGUN_FIRE = register("item.pump_shotgun.fire");
    public static final RegistryObject<SoundEvent> PUMP_SHOTGUN_SILENCED_FIRE = register("item.pump_shotgun.silenced_fire");
    public static final RegistryObject<SoundEvent> PUMP_SHOTGUN_ENCHANTED_FIRE = register("item.pump_shotgun.enchanted_fire");
    public static final RegistryObject<SoundEvent> BOLT_ACTION_RIFLE_FIRE = register("item.bolt_action_rifle.fire");
    public static final RegistryObject<SoundEvent> BOLT_ACTION_RIFLE_SILENCED_FIRE = register("item.bolt_action_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> BOLT_ACTION_RIFLE_ENCHANTED_FIRE = register("item.bolt_action_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> CUSTOM_SMG_FIRE = register("item.custom_smg.fire");
    public static final RegistryObject<SoundEvent> CUSTOM_SMG_SILENCED_FIRE = register("item.custom_smg.silenced_fire");
    public static final RegistryObject<SoundEvent> CUSTOM_SMG_ENCHANTED_FIRE = register("item.custom_smg.enchanted_fire");
    public static final RegistryObject<SoundEvent> BLOSSOM_RIFLE_FIRE = register("item.blossom_rifle.fire");
    public static final RegistryObject<SoundEvent> BLOSSOM_RIFLE_SILENCED_FIRE = register("item.blossom_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> BLOSSOM_RIFLE_ENCHANTED_FIRE = register("item.blossom_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> DOUBLE_BARREL_SHOTGUN_FIRE = register("item.double_barrel_shotgun.fire");
    public static final RegistryObject<SoundEvent> DOUBLE_BARREL_SHOTGUN_ENCHANTED_FIRE = register("item.double_barrel_shotgun.enchanted_fire");
    public static final RegistryObject<SoundEvent> HOLY_SHOTGUN_FIRE = register("item.holy_shotgun.fire");
    public static final RegistryObject<SoundEvent> HOLY_SHOTGUN_SILENCED_FIRE = register("item.holy_shotgun.silenced_fire");
    public static final RegistryObject<SoundEvent> HOLY_SHOTGUN_ENCHANTED_FIRE = register("item.holy_shotgun.enchanted_fire");
    public static final RegistryObject<SoundEvent> TYPHOONEE_FIRE = register("item.typhoonee.fire");
    public static final RegistryObject<SoundEvent> TYPHOONEE_PREFIRE = register("item.typhoonee.prefire");
    public static final RegistryObject<SoundEvent> REPEATING_SHOTGUN_FIRE = register("item.repeating_shotgun.fire");
    public static final RegistryObject<SoundEvent> REPEATING_SHOTGUN_ENCHANTED_FIRE = register("item.repeating_shotgun.enchanted_fire");
    public static final RegistryObject<SoundEvent> INFANTRY_RIFLE_FIRE = register("item.infantry_rifle.fire");
    public static final RegistryObject<SoundEvent> INFANTRY_RIFLE_SILENCED_FIRE = register("item.infantry_rifle.silenced_fire");
    public static final RegistryObject<SoundEvent> INFANTRY_RIFLE_ENCHANTED_FIRE = register("item.infantry_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> INFANTRY_RIFLE_PING = register("item.infantry_rifle.ping");
    public static final RegistryObject<SoundEvent> SERVICE_RIFLE_FIRE = register("item.service_rifle.fire");
    public static final RegistryObject<SoundEvent> SERVICE_RIFLE_ENCHANTED_FIRE = register("item.service_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> HOLLENFIRE_MK2_FIRE = register("item.hollenfire_mk2.fire");
    public static final RegistryObject<SoundEvent> HOLLENFIRE_MK2_ENCHANTED_FIRE = register("item.hollenfire_mk2.enchanted_fire");
    public static final RegistryObject<SoundEvent> SOULHUNTER_MK2_FIRE = register("item.soulhunter_mk2.fire");
    public static final RegistryObject<SoundEvent> SOULHUNTER_MK2_ENCHANTED_FIRE = register("item.soulhunter_mk2.enchanted_fire");
    public static final RegistryObject<SoundEvent> SUBSONIC_RIFLE_FIRE = register("item.subsonic_rifle.fire");
    public static final RegistryObject<SoundEvent> SUBSONIC_RIFLE_ENCHANTED_FIRE = register("item.subsonic_rifle.enchanted_fire");
    public static final RegistryObject<SoundEvent> SUPERSONIC_FIRE = register("item.supersonic_shotgun.fire");
    public static final RegistryObject<SoundEvent> HYPERSONIC_CANNON_CHARGE = register("item.hypersonic_cannon.charge");
    public static final RegistryObject<SoundEvent> ROCKET_LAUNCHER_FIRE = register("item.rocket_launcher.fire");
    public static final RegistryObject<SoundEvent> FLARE_GUN_FIRE = register("item.flare_gun.fire");
    public static final RegistryObject<SoundEvent> GRENADE_LAUNCHER_FIRE = register("item.grenade_launcher.fire");
    public static final RegistryObject<SoundEvent> COMPOUND_BOW_FIRE = register("item.compound_bow.fire");
    public static final RegistryObject<SoundEvent> LIGHT_MACHINE_GUN_FIRE = register("item.light_machine_gun.fire");
    public static final RegistryObject<SoundEvent> LIGHT_MACHINE_GUN_SILENCED_FIRE = register("item.light_machine_gun.silenced_fire");
    public static final RegistryObject<SoundEvent> LIGHT_MACHINE_GUN_ENCHANTED_FIRE = register("item.light_machine_gun.enchanted_fire");
    public static final RegistryObject<SoundEvent> SEMI_AUTO_PISTOL_FIRE = register("item.semi_auto_pistol.fire");
    public static final RegistryObject<SoundEvent> SEMI_AUTO_PISTOL_SILENCED_FIRE = register("item.semi_auto_pistol.silenced_fire");
    public static final RegistryObject<SoundEvent> SEMI_AUTO_PISTOL_ENCHANTED_FIRE = register("item.semi_auto_pistol.enchanted_fire");
    public static final RegistryObject<SoundEvent> COMBAT_PISTOL_FIRE = register("item.combat_pistol.fire");
    public static final RegistryObject<SoundEvent> COMBAT_PISTOL_SILENCED_FIRE = register("item.combat_pistol.silenced_fire");
    public static final RegistryObject<SoundEvent> COMBAT_PISTOL_ENCHANTED_FIRE = register("item.combat_pistol.enchanted_fire");

    // Animation Sounds
    public static final RegistryObject<SoundEvent> GUN_RUSTLE = register("item.gun_rustle");
    public static final RegistryObject<SoundEvent> GUN_SCREW = register("item.gun_screw");

    public static final RegistryObject<SoundEvent> CR_RELOAD_MAGAZINE_OUT = register("item.combat_rifle.reload_magazine_out");
    public static final RegistryObject<SoundEvent> CR_RELOAD_MAGAZINE_IN = register("item.combat_rifle.reload_magazine_in");
    public static final RegistryObject<SoundEvent> CR_RELOAD_EJECTOR = register("item.combat_rifle.reload_ejector");
    public static final RegistryObject<SoundEvent> CR_EJECTOR_PULL = register("item.combat_rifle.ejector_pull");
    public static final RegistryObject<SoundEvent> CR_EJECTOR_RELEASE = register("item.combat_rifle.ejector_release");
    public static final RegistryObject<SoundEvent> PS_PUMP = register("item.pump_shotgun.pump");
    public static final RegistryObject<SoundEvent> PS_PUMP_PULL = register("item.pump_shotgun.pump_pull");
    public static final RegistryObject<SoundEvent> PS_PUMP_RELEASE = register("item.pump_shotgun.pump_release");
    public static final RegistryObject<SoundEvent> PS_SHELL = register("item.pump_shotgun.shell");

    public static final RegistryObject<SoundEvent> AR_RELOAD_MAGAZINE_OUT = register("item.assault_rifle.reload_magazine_out");
    public static final RegistryObject<SoundEvent> AR_RELOAD_MAGAZINE_IN = register("item.assault_rifle.reload_magazine_in");
    public static final RegistryObject<SoundEvent> AR_RELOAD_EJECTOR = register("item.assault_rifle.reload_ejector");
    public static final RegistryObject<SoundEvent> AR_EJECTOR_PULL = register("item.assault_rifle.ejector_pull");
    public static final RegistryObject<SoundEvent> AR_EJECTOR_RELEASE = register("item.assault_rifle.ejector_release");

    public static final RegistryObject<SoundEvent> IR_CLIP_IN = register("item.infantry_rifle.clip_in");
    public static final RegistryObject<SoundEvent> IR_EJECTOR = register("item.infantry_rifle.ejector");
    public static final RegistryObject<SoundEvent> IR_EJECTOR_PULL = register("item.infantry_rifle.ejector_pull");
    public static final RegistryObject<SoundEvent> IR_EJECTOR_RELEASE = register("item.infantry_rifle.ejector_release");

    public static final RegistryObject<SoundEvent> SR_RELOAD_MAGAZINE_OUT = register("item.service_rifle.reload_magazine_out");
    public static final RegistryObject<SoundEvent> SR_RELOAD_MAGAZINE_IN = register("item.service_rifle.reload_magazine_in");

    public static final RegistryObject<SoundEvent> REV_RELOAD_BULLETS_OUT = register("item.revolver.reload_bullets_out");
    public static final RegistryObject<SoundEvent> REV_RELOAD_BULLET_IN = register("item.revolver.reload_bullet_in");
    public static final RegistryObject<SoundEvent> REV_CHAMBER_SPIN = register("item.revolver.chamber_spin");

    public static final RegistryObject<SoundEvent> BOLT_PULL = register("item.bolt_action_rifle.bolt_pull");
    public static final RegistryObject<SoundEvent> BOLT_RELEASE = register("item.bolt_action_rifle.bolt_release");
    public static final RegistryObject<SoundEvent> BULLET_IN = register("item.bolt_action_rifle.bullet_in");

    public static final RegistryObject<SoundEvent> LID_OPEN = register("item.rocket_launcher.lid_open");
    public static final RegistryObject<SoundEvent> LID_CLOSE = register("item.rocket_launcher.lid_close");
    public static final RegistryObject<SoundEvent> ROCKET_IN = register("item.rocket_launcher.rocket_in");

    public static final RegistryObject<SoundEvent> BOW_PULL = register("item.compound_bow.pull");
    public static final RegistryObject<SoundEvent> BOW_PLACE_ARROW = register("item.compound_bow.place_arrow");
    public static final RegistryObject<SoundEvent> BOW_STRING = register("item.compound_bow.charge");

    public static final RegistryObject<SoundEvent> PISTOL_MAG_OUT = register("item.combat_pistol.reload_mag_out");
    public static final RegistryObject<SoundEvent> PISTOL_MAG_IN = register("item.combat_pistol.reload_mag_in");
    public static final RegistryObject<SoundEvent> PISTOL_EJECTOR_PULL = register("item.combat_pistol.ejector_pull");
    public static final RegistryObject<SoundEvent> PISTOL_EJECTOR_RELEASE = register("item.combat_pistol.ejector_release");

    /* Misc. Special Sounds */
    public static final RegistryObject<SoundEvent> BULLET_CLOSE = register("entity.bullet.close");
    public static final RegistryObject<SoundEvent> HIT_MARKER = register("entity.bullet.hit");
    public static final RegistryObject<SoundEvent> MEDAL_GENERIC = register("ui.medal.generic");
    public static final RegistryObject<SoundEvent> MEDAL_HEADSHOT = register("ui.medal.headshot");
    public static final RegistryObject<SoundEvent> WATER_DROP = register("item.water_drop");

    public static final RegistryObject<SoundEvent> BIRTHDAY_PARTY = register("item.kill_effect.birthday_party");
    public static final RegistryObject<SoundEvent> AIR_HORN = register("item.kill_effect.air_horn");

    public static final RegistryObject<SoundEvent> TERROR_HORN = register("event.raid.terror_horn");

    public static final RegistryObject<SoundEvent> AMMO_POUCH = register("item.loot_drop.item.ammo_pouch");
    public static final RegistryObject<SoundEvent> BADGE_PACK = register("item.loot_drop.item.badge_pack");

    /* Back to normal sounds */
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_RELOAD = register("item.pistol.reload");
    public static final RegistryObject<SoundEvent> ITEM_PISTOL_COCK = register("item.pistol.cock");
    public static final RegistryObject<SoundEvent> ITEM_GRENADE_PIN = register("item.grenade.pin");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_EXPLOSION = register("entity.stun_grenade.explosion");
    public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_RING = register("entity.stun_grenade.ring");
    public static final RegistryObject<SoundEvent> ENTITY_MOLOTOV_EXPLOSION = register("entity.molotov.explosion");
    public static final RegistryObject<SoundEvent> ENTITY_SMOKE_GRENADE_EXPLOSION = register("entity.smoke_grenade.explosion");
    public static final RegistryObject<SoundEvent> UI_WEAPON_ATTACH = register("ui.weapon.attach");
    public static final RegistryObject<SoundEvent> RECYCLER_LOOP = register("block.recycler_loop");
    public static final RegistryObject<SoundEvent> RECYCLER_SHREDDING = register("block.recycler_shredding");

    /* Mob Sounds */
    public static final RegistryObject<SoundEvent> ENTITY_GHOUL_AMBIENT = register("entity.ghoul.ambient");
    public static final RegistryObject<SoundEvent> ENTITY_GHOUL_DEATH = register("entity.ghoul.death");
    public static final RegistryObject<SoundEvent> ENTITY_GHOUL_HURT = register("entity.ghoul.hurt");
    public static final RegistryObject<SoundEvent> ENTITY_PHANTOM_FLY = register("entity.phantom.fly");
    public static final RegistryObject<SoundEvent> ENTITY_PHANTOM_DIVE = register("entity.phantom.dive");
    public static final RegistryObject<SoundEvent> ENTITY_PHANTOM_BOOST = register("entity.phantom.boost");

    /* Event Sounds */
    public static final RegistryObject<SoundEvent> EVENT_PHANTOM_SWARM = register("event.phantom.distant");

    /* Impact Sounds */
    public static final RegistryObject<SoundEvent> METAL_HIT = register("block.hit.metal");
    public static final RegistryObject<SoundEvent> STONE_HIT = register("block.hit.stone");
    public static final RegistryObject<SoundEvent> WOOD_HIT = register("block.hit.wood");
    public static final RegistryObject<SoundEvent> SQUISHY_BREAK = register("block.hit.squishy");

    private static RegistryObject<SoundEvent> register(String key) {
        return REGISTER.register(key, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Reference.MOD_ID, key)));
    }
}
