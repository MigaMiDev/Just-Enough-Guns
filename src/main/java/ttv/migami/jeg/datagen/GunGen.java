package ttv.migami.jeg.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.FireMode;
import ttv.migami.jeg.common.GripType;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.ReloadType;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModTags;

import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public class GunGen extends GunProvider
{
    public GunGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    protected void registerGuns()
    {
        this.addGun(new ResourceLocation(Reference.MOD_ID, "abstract_gun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(3)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.22F)
                .setRecoilAngle(2.7F)
                .setAlwaysSpread(true)
                .setSpread(4.0F)
                .setDrawTimer(28)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(35)
                .setAdditionalReloadTimer(15)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(6F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(14F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.ASSAULT_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_IN.get())
                .setReloadLoadSound(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadEndSound(ModSounds.AR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.ASSAULT_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.ASSAULT_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 3.96, -4.785)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.75, -1.75))
                .setScope(1.0F, 0.0, 4.715, 5.0)
                .setBarrel(1.0F, 0.0, 3.97, -4.5)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(1.0F, 0.0, 2.7, 0.3)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1, 4, 0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "finger_gun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(1)
                .setGripType(GripType.ONE_HANDED)
                .setRecoilKick(0)
                .setRecoilAngle(0)
                .setAlwaysSpread(false)
                .setSpread(0)
                .setWitheredDisabled(true)
                .setInfinityDisabled(true)
                .setDrawTimer(0)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.INVENTORY_FED)
                .setReloadTimer(20)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(Items.AIR)
                .setProjectileVisible(false)
                .setDamage(0.0F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(20F)
                .setProjectileLife(80)
                .setProjectileTrailLengthMultiplier(0)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setEjectsCasing(false)
                .setHideTrail(true)

                // Sounds
                .setFireSound(ModSounds.FINGER_GUN_FIRE.get())
                .setReloadStart(ModSounds.ITEM_PISTOL_RELOAD.get())
                .setReloadLoadSound(ModSounds.ITEM_PISTOL_COCK.get())
                .setSilencedFireSound(ModSounds.FINGER_GUN_FIRE.get())
                .setEnchantedFireSound(ModSounds.FINGER_GUN_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.0, 0.0, 3.7, -4.7)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3, -1.75))

                .build());

        /* Scrap Tier */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "revolver"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(3)
                .setGripType(GripType.ONE_HANDED)
                .setRecoilKick(0.33F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(false)
                .setSpread(3.0F)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(8)
                .setReloadType(ReloadType.MANUAL)
                .setReloadAmount(1)
                .setReloadTimer(20)
                .setAdditionalReloadTimer(40)

                // Projectile
                .setAmmo(ModItems.PISTOL_AMMO.get())
                .setProjectileVisible(false)
                .setDamage(5F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(10F)
                .setProjectileLife(40)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)
                .setEjectsCasing(false)

                // Sounds
                .setFireSound(ModSounds.REVOLVER_FIRE.get())
                .setReloadStart(ModSounds.REV_RELOAD_BULLETS_OUT.get())
                .setReloadLoadSound(ModSounds.REV_RELOAD_BULLET_IN.get())
                .setReloadEndSound(ModSounds.REV_CHAMBER_SPIN.get())
                .setEjectorPullSound(SoundEvents.IRON_TRAPDOOR_OPEN)
                .setEjectorReleaseSound(SoundEvents.IRON_TRAPDOOR_CLOSE)
                .setSilencedFireSound(ModSounds.REVOLVER_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.REVOLVER_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.695, -2.785)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.85, -1.75))
                .setBarrel(1.0F, 0.0, 4.69, -3.2)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                //.setSpecial(1.0F, 0.0, 3.0, 0.0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "waterpipe_shotgun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(8)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.5F)
                .setRecoilAngle(10.0F)
                .setAlwaysSpread(true)
                .setSpread(15.0F)
                .setProjectileAmount(18)
                .setShooterPushback(-0.4F)
                .setDrawTimer(22)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(30)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(ModItems.HANDMADE_SHELL.get())
                .setProjectileVisible(false)
                .setDamage(20F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(6F)
                .setProjectileLife(10)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)

                // Sounds
                .setFireSound(ModSounds.WATERPIPE_SHOTGUN_FIRE.get())
                .setReloadStart(SoundEvents.ITEM_FRAME_REMOVE_ITEM)
                .setReloadLoadSound(ModSounds.PS_SHELL.get())
                .setReloadEndSound(ModSounds.PS_PUMP.get())
                .setEjectorPullSound(ModSounds.PS_PUMP_PULL.get())
                .setEjectorReleaseSound(ModSounds.PS_PUMP_RELEASE.get())
                .setEnchantedFireSound(ModSounds.WATERPIPE_SHOTGUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 3.89, -7.89)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.7F)
                        .setOffset(0.0, 3.65, 0.75))
                .setSpecial(1.0F, -0, 3, -5)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "custom_smg"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.33F)
                .setRecoilAngle(1.0F)
                .setAlwaysSpread(true)
                .setSpread(4.0F)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(24)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(35)
                .setAdditionalReloadTimer(10)

                // Projectile
                .setAmmo(ModItems.PISTOL_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(2.8F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(12F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.CUSTOM_SMG_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.SR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.CUSTOM_SMG_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.CUSTOM_SMG_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.45, -2.205)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.85, -1.75))
                .setBarrel(1.0F, 0.0, 4.475, -1.2)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -0.8, 3.8, 0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "double_barrel_shotgun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(8)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.5F)
                .setRecoilAngle(10.0F)
                .setAlwaysSpread(true)
                .setSpread(25.0F)
                .setProjectileAmount(22)
                .setDrawTimer(15)

                // Reloads
                .setMaxAmmo(2)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(37)
                .setAdditionalReloadTimer(13)

                // Projectile
                .setAmmo(ModItems.HANDMADE_SHELL.get())
                .setProjectileVisible(false)
                .setDamage(24F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(6F)
                .setProjectileLife(6)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setEjectsCasing(false)

                // Sounds
                .setFireSound(ModSounds.DOUBLE_BARREL_SHOTGUN_FIRE.get())
                .setReloadStart(SoundEvents.ITEM_FRAME_REMOVE_ITEM)
                .setReloadLoadSound(ModSounds.PS_SHELL.get())
                .setReloadEndSound(ModSounds.PS_PUMP.get())
                .setEjectorPullSound(ModSounds.PS_PUMP_PULL.get())
                .setEjectorReleaseSound(ModSounds.PS_PUMP_RELEASE.get())
                .setEnchantedFireSound(ModSounds.DOUBLE_BARREL_SHOTGUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(1.3, 0, 5.6, -9.255)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.7F)
                        .setOffset(0.0, 5.05, 0.75))
                .setStock(0.0F, 0.0, 0.0, 0.0)

                .build());

        /* Gunmetal Tier */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "semi_auto_pistol"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(2)
                .setGripType(GripType.ONE_HANDED)
                .setRecoilKick(0.33F)
                .setRecoilAngle(2.0F)
                .setAlwaysSpread(false)
                .setSpread(2.5F)
                .setDrawTimer(10)

                // Reloads
                .setMaxAmmo(10)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(37)
                .setAdditionalReloadTimer(13)

                // Projectile
                .setAmmo(ModItems.PISTOL_AMMO.get())
                .setProjectileVisible(false)
                .setDamage(5.5F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(15F)
                .setProjectileLife(40)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setEjectsCasing(true)

                // Sounds
                .setFireSound(ModSounds.SEMI_AUTO_PISTOL_FIRE.get())
                .setReloadStart(ModSounds.PISTOL_MAG_OUT.get())
                .setReloadLoadSound(ModSounds.PISTOL_MAG_IN.get())
                .setReloadEndSound(ModSounds.REV_CHAMBER_SPIN.get())
                .setEjectorPullSound(ModSounds.PISTOL_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.PISTOL_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.SEMI_AUTO_PISTOL_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.SEMI_AUTO_PISTOL_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 5.645, -2.2)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.965, -1.75))
                .setSpecial(1.0F, -0.8, 5.2, 0)
                .setBarrel(1.0F, 0.0, 5.4, -3.2)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "semi_auto_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(4)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.15F)
                .setRecoilAngle(3.0F)
                .setAlwaysSpread(false)
                .setSpread(3.0F)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(16)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(40)
                .setAdditionalReloadTimer(7)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(6.5F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(12F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.SEMI_AUTO_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.SR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.AR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.SEMI_AUTO_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.SEMI_AUTO_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.43, -7.305)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.6, -1.25))
                .setScope(1.0F, 0.0, 4.5, 5.4)
                .setBarrel(1.0F, 0.0, 4.425, -5.5)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(1.0F, 0.0, 3.6, -2.8)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -0.8, 4, 2)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "assault_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(3)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.22F)
                .setRecoilAngle(2.7F)
                .setAlwaysSpread(true)
                .setSpread(4.0F)
                .setDrawTimer(28)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(35)
                .setAdditionalReloadTimer(15)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(6F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(14F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.ASSAULT_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_IN.get())
                .setReloadLoadSound(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadEndSound(ModSounds.AR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.ASSAULT_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.ASSAULT_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 3.96, -4.785)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.75, -1.75))
                .setScope(1.0F, 0.0, 4.715, 5.0)
                .setBarrel(1.0F, 0.0, 3.97, -4.5)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(1.0F, 0.0, 2.7, 0.3)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1, 4, 0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "pump_shotgun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(22)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.5F)
                .setRecoilAngle(10.0F)
                .setAlwaysSpread(true)
                .setSpread(10.0F)
                .setProjectileAmount(12)
                .setDrawTimer(22)

                // Reloads
                .setMaxAmmo(6)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(18)
                .setAdditionalReloadTimer(6)

                // Projectile
                .setAmmo(ModItems.SHOTGUN_SHELL.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(20F)
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setReduceDamageOverLife(true)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(6F)
                .setProjectileLife(10)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)

                // Sounds
                .setFireSound(ModSounds.PUMP_SHOTGUN_FIRE.get())
                .setReloadStart(ModSounds.PS_SHELL.get())
                .setReloadLoadSound(ModSounds.PS_SHELL.get())
                .setReloadEndSound(ModSounds.PS_PUMP.get())
                .setEjectorPullSound(ModSounds.PS_PUMP_PULL.get())
                .setEjectorReleaseSound(ModSounds.PS_PUMP_RELEASE.get())
                .setSilencedFireSound(ModSounds.PUMP_SHOTGUN_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.PUMP_SHOTGUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.075, -5.785)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.3, -1.25))
                .setScope(1.0F, 0.0, 4.265, 0.8)
                .setBarrel(1.0F, 0.0, 4.125, -5.51)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1, 4, -2)

                .build());

        /* Gunnite Tier */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "combat_pistol"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(2)
                .setGripType(GripType.ONE_HANDED)
                .setRecoilKick(0.33F)
                .setRecoilAngle(2.0F)
                .setAlwaysSpread(false)
                .setSpread(1.5F)
                .setDrawTimer(10)

                // Reloads
                .setMaxAmmo(15)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(37)
                .setAdditionalReloadTimer(13)

                // Projectile
                .setAmmo(ModItems.PISTOL_AMMO.get())
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setProjectileVisible(false)
                .setDamage(6.5F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(15F)
                .setProjectileLife(40)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setEjectsCasing(true)

                // Sounds
                .setFireSound(ModSounds.COMBAT_PISTOL_FIRE.get())
                .setReloadStart(ModSounds.PISTOL_MAG_OUT.get())
                .setReloadLoadSound(ModSounds.PISTOL_MAG_IN.get())
                .setReloadEndSound(ModSounds.REV_CHAMBER_SPIN.get())
                .setEjectorPullSound(ModSounds.PISTOL_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.PISTOL_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.COMBAT_PISTOL_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.COMBAT_PISTOL_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 5.645, -2.2)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.135, -1.75))
                .setBarrel(1.0F, 0.0, 5.5, -3.2)
                .setSpecial(1.0F, -0.8, 5.2, 0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "burst_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.BURST)
                .setBurstAmount(3)
                .setBurstDelay(6)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.15F)
                .setRecoilAngle(2.0F)
                .setAlwaysSpread(true)
                .setSpread(2.0F)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(32)
                .setAdditionalReloadTimer(8)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(7.0F)
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(16F)
                .setProjectileLife(80)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)

                // Sounds
                .setFireSound(ModSounds.BURST_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.CR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.BURST_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.BURST_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.34, -5.865)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.65, -1.75))
                .setScope(1.0F, 0.0, 5.73, 5.4)
                .setBarrel(1.0F, 0.0, 4.34, -5.5)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(1.0F, 0.0, 2.975, -0.78)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1.2, 4.335, -2)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "combat_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(3)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.25F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(true)
                .setSpread(3.0F)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(32)
                .setAdditionalReloadTimer(8)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(8.0F)
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(16F)
                .setProjectileLife(80)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)

                // Sounds
                .setFireSound(ModSounds.COMBAT_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.CR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.COMBAT_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.COMBAT_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.59, -7.955)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.71, -1.75))
                .setScope(1.0F, 0.0, 5.885, 4.8)
                .setBarrel(1.0F, 0.0, 4.585, -7.855)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(0.0F, 0.0, 0.0, 0.0)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1.2, 4.63, -1.8)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "bolt_action_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(28)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.25F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(true)
                .setSpread(0.25F)
                .setDrawTimer(30)

                // Reloads
                .setMaxAmmo(4)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(20)
                .setAdditionalReloadTimer(10)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(21F)
                .setHeadshotMultiplier(1.5F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(24F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)
                .setHitsRubberFruit(true)

                // Sounds
                .setFireSound(ModSounds.BOLT_ACTION_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.BULLET_IN.get())
                .setReloadLoadSound(ModSounds.BULLET_IN.get())
                .setReloadEndSound(ModSounds.BULLET_IN.get())
                .setEjectorPullSound(ModSounds.BOLT_PULL.get())
                .setEjectorReleaseSound(ModSounds.BOLT_RELEASE.get())
                .setSilencedFireSound(ModSounds.BOLT_ACTION_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.BOLT_ACTION_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.915, -15.155)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.35, -1.25))
                .setBarrel(1.0F, 0.0, 4.905, -14.5)
                .setScope(1.0F, 0.0, 5.0, -4.40)
                .setSpecial(1.0F, -1, 4, -7)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "flare_gun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(20)
                .setGripType(GripType.ONE_HANDED)
                .setRecoilKick(0.33F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(false)
                .setSpread(3.0F)
                .setInfinityDisabled(true)
                .setDrawTimer(10)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(20)
                .setAdditionalReloadTimer(20)

                // Projectile
                .setAmmo(ModItems.FLARE.get())
                .setProjectileVisible(false)
                .setDamage(5F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(3F)
                .setProjectileLife(100)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileAffectedByGravity(true)
                .setEjectsCasing(false)
                .setHideTrail(true)

                // Sounds
                .setFireSound(ModSounds.FLARE_GUN_FIRE.get())
                .setReloadStart(ModSounds.PS_SHELL.get())
                .setReloadLoadSound(ModSounds.PS_SHELL.get())
                .setReloadEndSound(ModSounds.REV_CHAMBER_SPIN.get())
                .setEjectorPullSound(SoundEvents.IRON_TRAPDOOR_OPEN)
                .setEjectorReleaseSound(SoundEvents.IRON_TRAPDOOR_CLOSE)
                .setSilencedFireSound(ModSounds.FLARE_GUN_FIRE.get())
                .setEnchantedFireSound(ModSounds.FLARE_GUN_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.695, -2.04)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.85, -1.75))

                .build());

        /* Spectre Tier */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "blossom_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.45F)
                .setRecoilAngle(2.0F)
                .setAlwaysSpread(true)
                .setSpread(3.0F)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(80)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(ModItems.SPECTRE_ROUND.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(6.75F)
                .setAdvantage(ModTags.Entities.UNDEAD.location())
                .setProjectileSize(0.1F)
                .setProjectileSpeed(8F)
                .setProjectileLife(80)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0x00DCFF | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(false)

                // Sounds
                .setFireSound(ModSounds.BLOSSOM_RIFLE_FIRE.get())

                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.WATER_DROP.get())
                .setReloadEndSound(SoundEvents.BEEHIVE_EXIT)

                .setSilencedFireSound(ModSounds.BLOSSOM_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.BLOSSOM_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.4, -9.7)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.75, -1.50))
                .setScope(1.0F, 0.0, 6.0, 3.0)
                .setBarrel(1.0F, 0.0, 5.1, -9.4)
                        .setSpecial(1.0F, -1.1, 5.0, -6.2)
                .setUnderBarrel(1.0F, 0.0, 3.9, 0.0)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "holy_shotgun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(14)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.5F)
                .setRecoilAngle(8.0F)
                .setAlwaysSpread(true)
                .setSpread(8.0F)
                .setProjectileAmount(10)
                .setCollateral(true)

                // Reloads
                .setMaxAmmo(8)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(16)
                .setAdditionalReloadTimer(10)

                // Projectile
                .setAmmo(ModItems.SPECTRE_ROUND.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(23F)
                .setAdvantage(ModTags.Entities.UNDEAD.location())
                .setReduceDamageOverLife(true)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(8F)
                .setProjectileLife(10)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0x00DCFF | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(false)

                // Sounds
                .setFireSound(ModSounds.HOLY_SHOTGUN_FIRE.get())
                .setReloadStart(ModSounds.ITEM_PISTOL_RELOAD.get())
                .setReloadLoadSound(ModSounds.ITEM_PISTOL_COCK.get())
                .setSilencedFireSound(ModSounds.HOLY_SHOTGUN_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.HOLY_SHOTGUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 3.05, -3.03)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.14, -1.25))
                .setScope(1.0F, 0.0, 2.97, 5.0)
                .setBarrel(1.0F, 0.0, 2.804, -5.5)
                .setUnderBarrel(0.0F, 0.0, 1.77, 2.125)

                .build());

        /* Water Tier */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "atlantean_spear"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(32)
                .setFireTimer(15)
                .setCustomFiring(true)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.5F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(false)
                .setSpread(0.0F)
                .setCanFireUnderwater(true)

                // Reloads
                .setMaxAmmo(6)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(16)
                .setAdditionalReloadTimer(10)

                // Projectile
                .setAmmo(ModItems.WATER_BOMB.get())
                .setEjectsCasing(false)
                .setProjectileVisible(false)
                .setDamage(10F)
                .setAdvantage(ModTags.Entities.FIRE.location())
                .setReduceDamageOverLife(true)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(3F)
                .setProjectileLife(40)
                .setProjectileTrailLengthMultiplier(0)
                .setProjectileTrailColor(0x00DCFF)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(true)
                .setHitsRubberFruit(true)

                // Sounds
                .setFireSound(ModSounds.TYPHOONEE_FIRE.get())
                .setReloadStart(ModSounds.ITEM_PISTOL_RELOAD.get())
                .setReloadLoadSound(ModSounds.ITEM_PISTOL_COCK.get())
                .setEnchantedFireSound(ModSounds.TYPHOONEE_FIRE.get())
                .setPreFireSound(ModSounds.TYPHOONEE_PREFIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 2.05, -4.03)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.825, -1.25))

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "typhoonee"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.PULSE)
                .setFireRate(32)
                .setFireTimer(15)
                .setCustomFiring(true)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.5F)
                .setRecoilAngle(8.0F)
                .setAlwaysSpread(false)
                .setSpread(0.0F)
                .setCanFireUnderwater(true)

                // Reloads
                .setReloadItem(Items.WATER_BUCKET)
                .setMaxAmmo(8)
                .setReloadType(ReloadType.SINGLE_ITEM)
                .setReloadTimer(60)
                .setAdditionalReloadTimer(20)

                // Projectile
                .setAmmo(ModItems.POCKET_BUBBLE.get())
                .setEjectsCasing(false)
                .setProjectileVisible(false)
                .setDamage(10F)
                .setAdvantage(ModTags.Entities.FIRE.location())
                .setReduceDamageOverLife(true)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(10F)
                .setProjectileLife(3)
                .setProjectileTrailLengthMultiplier(0)
                .setProjectileTrailColor(0x00DCFF)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(true)
                .setHitsRubberFruit(true)
                .setNoProjectile(true)

                // Sounds
                .setFireSound(ModSounds.TYPHOONEE_FIRE.get())
                .setReloadStart(ModSounds.ITEM_PISTOL_RELOAD.get())
                .setReloadLoadSound(ModSounds.ITEM_PISTOL_COCK.get())
                .setEnchantedFireSound(ModSounds.TYPHOONEE_FIRE.get())
                .setPreFireSound(ModSounds.TYPHOONEE_PREFIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 2.5, -3.03)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.45, -1.25))

                .build());

        /* TODO: Revisit this one boi!
        this.addGun(new ResourceLocation(Reference.MOD_ID, "bubble_cannon"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.BURST)
                .setBurstAmount(3)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.15F)
                .setRecoilAngle(3.0F)
                .setAlwaysSpread(true)
                .setSpread(10.0F)

                // Reloads
                .setMaxAmmo(3)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(30)
                .setEmptyMagTimer(10)

                // Projectile
                .setAmmo(ModItems.POCKET_BUBBLE.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(8.0F)
                .setAdvantage(ModTags.Entities.FIRE.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(2F)
                .setProjectileLife(200)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.TYPHOONEE_FIRE.get())
                .setReloadSound(ModSounds.ITEM_PISTOL_RELOAD.get())
                .setCockSound(ModSounds.ITEM_PISTOL_COCK.get())
                .setEnchantedFireSound(ModSounds.TYPHOONEE_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 2.5, -3.03)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.45, -1.25))

                .build());*/

        /* Blue print */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "repeating_shotgun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(22)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.5F)
                .setRecoilAngle(10.0F)
                .setAlwaysSpread(true)
                .setSpread(8.0F)
                .setProjectileAmount(12)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(8)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(16)
                .setAdditionalReloadTimer(10)

                // Projectile
                .setAmmo(ModItems.SHOTGUN_SHELL.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(25F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setReduceDamageOverLife(true)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(6F)
                .setProjectileLife(10)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)

                // Sounds
                .setFireSound(ModSounds.REPEATING_SHOTGUN_FIRE.get())
                .setReloadStart(ModSounds.PS_PUMP_PULL.get())
                .setReloadLoadSound(ModSounds.PS_SHELL.get())
                .setReloadEndSound(ModSounds.PS_PUMP.get())
                .setEjectorPullSound(ModSounds.PS_PUMP_PULL.get())
                .setEjectorReleaseSound(ModSounds.PS_PUMP_RELEASE.get())
                .setSilencedFireSound(ModSounds.PUMP_SHOTGUN_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.REPEATING_SHOTGUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.645, -10.635)
                /*.setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.92, -1.25))*/
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.75, -1.75))
                .setScope(1.0F, 0.0, 4.9, 0)
                .setBarrel(1.0F, 0.0, 4.65, -10)
                .setUnderBarrel(1.0F, 0.0, 3, 0.45)
                .setSpecial(1.0F, -0.8, 4, -5)

                .build());


        this.addGun(new ResourceLocation(Reference.MOD_ID, "infantry_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(3)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.15F)
                .setRecoilAngle(3.0F)
                .setAlwaysSpread(false)
                .setSpread(3.0F)
                .setDrawTimer(17)

                // Reloads
                .setMaxAmmo(8)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(30)
                .setAdditionalReloadTimer(10)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(10.5F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(12F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.INFANTRY_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.IR_CLIP_IN.get())
                .setReloadEndSound(ModSounds.IR_EJECTOR.get())
                .setEjectorPullSound(ModSounds.IR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.IR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.INFANTRY_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.INFANTRY_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.495, -9.655)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.8, 1.75))
                .setScope(1.0F, 0.0, 4.97, -0.715)
                .setBarrel(1.0F, 0.0, 4.5, -9.6)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1, 4.3, -6)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "service_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(3)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.25F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(true)
                .setSpread(3.0F)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(32)
                .setAdditionalReloadTimer(8)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(7.25F)
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(16F)
                .setProjectileLife(80)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)

                // Sounds
                .setFireSound(ModSounds.SERVICE_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.SR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.COMBAT_RIFLE_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.SERVICE_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.68, -9.145)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.2, -1.75))
                .setScope(1.0F, 0.0, 5.305, 4.5)
                .setBarrel(1.0F, 0.0, 4.685, -8.5)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(1.0F, 0.0, 3.3, -1.0)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1.2, 4.7, -3.5)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "hollenfire_mk2"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.25F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(true)
                .setSpread(4.0F)
                .setCanBeBlueprinted(false)
                .setOverheatTimer(100)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(40)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(80)
                .setAdditionalReloadTimer(20)

                // Projectile
                .setAmmo(ModItems.BLAZE_ROUND.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(8.0F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(7F)
                .setProjectileLife(100)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(false)
                .setHitsRubberFruit(true)

                // Sounds
                .setFireSound(ModSounds.HOLLENFIRE_MK2_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.SR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())
                .setEnchantedFireSound(ModSounds.HOLLENFIRE_MK2_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.68, -7.645)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.2, -1.75))
                .setScope(1.0F, 0.0, 5.305, 4.5)
                .setStock(0.0F, 0.0, 0.0, 0.0)
                .setUnderBarrel(1.0F, 0.0, 2.725, -1.0)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)
                .setSpecial(1.0F, -1.9, 4.7, -4)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "soulhunter_mk2"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.BURST)
                .setFireRate(1)
                .setBurstAmount(3)
                .setBurstDelay(6)
                .setFireTimer(0)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.25F)
                .setRecoilAngle(2.0F)
                .setAlwaysSpread(false)
                .setSpread(2.0F)
                .setCanBeBlueprinted(false)
                .setOverheatTimer(100)
                .setDrawTimer(25)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(80)
                .setAdditionalReloadTimer(20)

                // Projectile
                .setAmmo(ModItems.BLAZE_ROUND.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(9.0F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(7F)
                .setProjectileLife(100)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0x00DCFF | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(false)
                .setHitsRubberFruit(true)

                // Sounds
                .setFireSound(ModSounds.SOULHUNTER_MK2_FIRE.get())
                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.AR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.AR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setEnchantedFireSound(ModSounds.SOULHUNTER_MK2_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.52, -10.66)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.415, 2.0))
                .setScope(1.0F, 0.0, 5.305, 1.5)
                .setUnderBarrel(1.0F, 0.0, 3.3, -4.33)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "subsonic_rifle"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(3)
                .setSilenced(true)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.09F)
                .setRecoilAngle(1.3F)
                .setAlwaysSpread(false)
                .setSpread(4.0F)
                .setCanBeBlueprinted(false)
                .setCanFireUnderwater(true)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(20)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(28)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(Items.ECHO_SHARD)
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setIgnoresBlocks(true)
                .setDamage(9F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.5F)
                .setProjectileSpeed(6F)
                .setProjectileLife(50)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00)
                .setHideTrail(true)
                .setHitsRubberFruit(true)

                // Sounds
                .setFireSound(ModSounds.SUBSONIC_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_IN.get())
                .setReloadLoadSound(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadEndSound(ModSounds.AR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.SUBSONIC_RIFLE_FIRE.get())
                .setEnchantedFireSound(ModSounds.SUBSONIC_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.95, -0.75))
                .setScope(1.0F, 0.0, 4.9, 5.0)
                .setBarrel(1.0F, 0.0, 4.515, -8.25)
                .setUnderBarrel(1.0F, 0.0, 3.15, -1)
                .setMagazine(0.0F, 0.0, 0.0, 0.0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "supersonic_shotgun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.PULSE)
                .setFireRate(22)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.2F)
                .setRecoilAngle(7.0F)
                .setAlwaysSpread(true)
                .setSpread(100.0F)
                .setProjectileAmount(6)
                .setCanBeBlueprinted(false)
                .setCanFireUnderwater(true)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(6)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(18)
                .setAdditionalReloadTimer(7)

                // Projectile
                .setAmmo(Items.ECHO_SHARD)
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(22F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setReduceDamageOverLife(true)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(3F)
                .setProjectileLife(2)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(true)
                .setNoProjectile(true)

                // Sounds
                .setFireSound(ModSounds.SUPERSONIC_FIRE.get())
                .setReloadStart(ModSounds.PS_SHELL.get())
                .setReloadLoadSound(ModSounds.PS_SHELL.get())
                .setReloadEndSound(ModSounds.PS_PUMP.get())
                .setEjectorPullSound(ModSounds.PS_PUMP_PULL.get())
                .setEjectorReleaseSound(ModSounds.PS_PUMP_RELEASE.get())
                .setSilencedFireSound(ModSounds.SUPERSONIC_FIRE.get())
                .setEnchantedFireSound(ModSounds.SUPERSONIC_FIRE.get())

                // Attachments
                .setMuzzleFlash(1.0, 0.0, 4.315, -5.855)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.2, -3.75))
                .setScope(1.0F,0.0, 5.195, 4.5)
                .setBarrel(1.0F, 0.0, 4.125, -5.51)
                .setUnderBarrel(0.0F, 0.0, 0.0, 0.0)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "hypersonic_cannon"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.PULSE)
                .setFireRate(10)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.55F)
                .setRecoilAngle(4.0F)
                .setAlwaysSpread(false)
                .setSpread(3.0F)
                .setFireTimer(20)
                .setCanBeBlueprinted(false)
                .setInfinityDisabled(true)
                .setCanFireUnderwater(true)
                .setShooterPushback(-1F)
                .setDrawTimer(10)

                // Reloads
                .setMaxAmmo(15)
                .setReloadType(ReloadType.SINGLE_ITEM)
                .setReloadTimer(60)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(Items.SCULK_CATALYST)
                .setReloadItem(Items.SCULK_CATALYST)
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(13F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(12F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00)
                .setProjectileAffectedByGravity(true)
                .setHideTrail(true)
                .setNoProjectile(true)

                // Sounds
                .setFireSound(SoundEvents.WARDEN_SONIC_BOOM)
                .setPreFireSound(ModSounds.HYPERSONIC_CANNON_CHARGE.get())
                .setReloadStart(ModSounds.HYPERSONIC_CANNON_CHARGE.get())
                .setReloadLoadSound(SoundEvents.ITEM_PICKUP)
                .setReloadEndSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM)
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(SoundEvents.WARDEN_SONIC_BOOM)
                .setEnchantedFireSound(SoundEvents.WARDEN_SONIC_BOOM)

                // Attachments
                .setMuzzleFlash(0.8, 0, 3.935, -2.285)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.9, -2.25))

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "rocket_launcher"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(100)
                .setGripType(GripType.BAZOOKA)
                .setRecoilKick(0.25F)
                .setRecoilAngle(7.0F)
                .setAlwaysSpread(true)
                .setSpread(1.0F)
                .setFireTimer(7)
                .setInfinityDisabled(true)
                .setCanBeBlueprinted(false)
                .setShooterPushback(-1F)
                .setDrawTimer(40)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.MANUAL)
                .setReloadTimer(45)
                .setAdditionalReloadTimer(14)

                // Projectile
                .setAmmo(ModItems.ROCKET.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(28.0F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(5F)
                .setProjectileLife(600)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00)
                .setProjectileAffectedByGravity(true)
                .setHideTrail(true)
                
                // Sounds
                .setFireSound(ModSounds.ROCKET_LAUNCHER_FIRE.get())
                .setReloadStart(ModSounds.LID_OPEN.get())
                .setReloadLoadSound(ModSounds.ROCKET_IN.get())
                .setReloadEndSound(ModSounds.LID_CLOSE.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.ROCKET_LAUNCHER_FIRE.get())
                .setEnchantedFireSound(ModSounds.ROCKET_LAUNCHER_FIRE.get())

                // Attachments
                .setMuzzleFlash(1.3, 0.0, 4.695, -8.015)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(2.8, 3.4, -1.75))

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "compound_bow"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.RELEASE_FIRE)
                .setFireRate(10)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.1F)
                .setRecoilAngle(2.0F)
                .setAlwaysSpread(false)
                .setSpread(0.0F)
                .setMaxHoldFire(20)
                .setSilenced(true)
                .setCanBeBlueprinted(false)
                .setDrawTimer(10)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.INVENTORY_FED)
                .setReloadTimer(16)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(Items.ARROW)
                .setEjectsCasing(false)
                .setProjectileVisible(false)
                .setDamage(10.0F)
                .setHeadshotMultiplier(2.5F)
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(8F)
                .setProjectileAffectedByGravity(true)
                .setProjectileLife(1000)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFFFF | 0xFF000000)

                // Sounds
                .setFireSound(ModSounds.COMPOUND_BOW_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.BOW_PLACE_ARROW.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.BOW_PULL.get())
                .setEjectorReleaseSound(ModSounds.BOW_STRING.get())

                // Attachments
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(1.05, 3.5, 0))

                .build());


        this.addGun(new ResourceLocation(Reference.MOD_ID, "primitive_bow"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.RELEASE_FIRE)
                .setFireRate(10)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.1F)
                .setRecoilAngle(2.0F)
                .setAlwaysSpread(false)
                .setSpread(0.0F)
                .setMaxHoldFire(20)
                .setSilenced(true)
                .setCanBeBlueprinted(false)
                .setDrawTimer(10)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.INVENTORY_FED)
                .setReloadTimer(16)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(Items.ARROW)
                .setEjectsCasing(false)
                .setProjectileVisible(false)
                .setDamage(7.0F)
                .setHeadshotMultiplier(1.8F)
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(8F)
                .setProjectileAffectedByGravity(true)
                .setProjectileLife(1000)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFFFF | 0xFF000000)

                // Sounds
                .setFireSound(ModSounds.COMPOUND_BOW_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.BOW_PLACE_ARROW.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.BOW_PULL.get())
                .setEjectorReleaseSound(ModSounds.BOW_STRING.get())

                // Mob Effect
                .setPotionEffect(BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.POISON))
                .setPotionEffectStrength(2)
                .setPotionEffectDuration(100)
                .setSelfPotionEffect(false)

                // Attachments
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(1.50, 4.5, 0))

                .build());


        this.addGun(new ResourceLocation(Reference.MOD_ID, "grenade_launcher"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(20)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.25F)
                .setRecoilAngle(3.0F)
                .setAlwaysSpread(true)
                .setSpread(2.0F)
                .setCanBeBlueprinted(false)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(42)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(ModItems.GRENADE.get())
                .setEjectsCasing(false)
                .setProjectileVisible(true)
                .setDamage(25.0F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(3F)
                .setProjectileLife(200)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(true)

                // Sounds
                .setFireSound(ModSounds.GRENADE_LAUNCHER_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_IN.get())
                .setReloadLoadSound(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())

                // Attachments
                .setMuzzleFlash(1.0, 0.0, 4.86, -7)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 3.61, 2))
                .setScope(1.0F, 0.0, 4.5, 0)
                .setUnderBarrel(1.0F, 0.0, 2.5, -2.5)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "light_machine_gun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.15F)
                .setRecoilAngle(1.0F)
                .setAlwaysSpread(true)
                .setSpread(3.0F)
                .setShooterPushback(-0.1F)
                .setOverheatTimer(100)
                .setCanBeBlueprinted(false)
                .setDrawTimer(40)

                // Reloads
                .setMaxAmmo(100)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(90)
                .setAdditionalReloadTimer(15)

                // Projectile
                .setAmmo(ModItems.RIFLE_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(7.0F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(16F)
                .setProjectileLife(80)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)

                // Sounds
                .setFireSound(ModSounds.LIGHT_MACHINE_GUN_FIRE.get())
                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_IN.get())
                .setReloadLoadSound(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadEndSound(ModSounds.AR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.LIGHT_MACHINE_GUN_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.LIGHT_MACHINE_GUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.88, -10)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.56, -1.75))
                .setScope(1.0F, 0.0, 5.33, 0)
                .setBarrel(1.0F, 0.0, 4.855, -7)
                .setUnderBarrel(1.0F, 0.0, 2.5, 0.3)
                .setSpecial(1.0F, -1.2, 3.63, -0.1)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "flamethrower"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(1)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.15F)
                .setRecoilAngle(0.5F)
                .setAlwaysSpread(true)
                .setSpread(20.0F)
                .setOverheatTimer(150)
                .setCanBeBlueprinted(false)
                .setProjectileAmount(3)
                .setWitheredDisabled(true)
                .setDrawTimer(25)

                // Reloads
                .setMaxAmmo(200)
                .setReloadType(ReloadType.SINGLE_ITEM)
                .setReloadTimer(120)
                .setAdditionalReloadTimer(30)
                .setReloadItem(Items.LAVA_BUCKET)

                // Projectile
                .setAmmo(Items.FIRE_CHARGE)
                .setEjectsCasing(false)
                .setProjectileVisible(false)
                .setDamage(3.0F)
                .setAdvantage(ModTags.Entities.NONE.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(2.0F)
                .setProjectileLife(7)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(true)
                .setCollateral(true)

                // Sounds
                .setFireSound(SoundEvents.FIRECHARGE_USE)
                .setReloadStart(SoundEvents.ITEM_FRAME_REMOVE_ITEM)
                .setReloadLoadSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM)
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(SoundEvents.IRON_TRAPDOOR_OPEN)
                .setEjectorReleaseSound(SoundEvents.IRON_TRAPDOOR_CLOSE)

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.1, -11.8)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.71, -1.75))

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "minigun"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(1)
                .setGripType(GripType.MINI_GUN)
                .setRecoilKick(0.15F)
                .setRecoilAngle(0.8F)
                .setAlwaysSpread(false)
                .setSpread(10.0F)
                .setShooterPushback(-0.075F)
                .setOverheatTimer(200)
                .setCanBeBlueprinted(false)
                .setProjectileAmount(3)
                .setInfinityDisabled(true)
                .setWitheredDisabled(true)
                .setDrawTimer(30)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.INVENTORY_FED)
                .setReloadTimer(18)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(ModItems.PISTOL_AMMO.get())
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(4.0F)
                .setAdvantage(ModTags.Entities.HEAVY.location())
                .setProjectileSize(0.05F)
                .setProjectileSpeed(13F)
                .setProjectileLife(80)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setReduceDamageOverLife(true)

                // Sounds
                .setFireSound(ModSounds.LIGHT_MACHINE_GUN_FIRE.get())
                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_IN.get())
                .setReloadLoadSound(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadEndSound(ModSounds.AR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.AR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.AR_EJECTOR_RELEASE.get())
                .setSilencedFireSound(ModSounds.LIGHT_MACHINE_GUN_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.LIGHT_MACHINE_GUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(1.0, 0.0, -1.1, -13)
                .setSpecial(1.0F, 0, 1.2, 4)

                .build());

    }
}
