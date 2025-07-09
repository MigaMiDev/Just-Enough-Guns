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
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModTags;

import java.util.concurrent.CompletableFuture;

/**
 * Author: MrCrayfish
 */
public class CFGGunGen extends CFGGunProvider
{
    public CFGGunGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }

    @Override
    protected void registerGuns()
    {
        this.addGun(new ResourceLocation(Reference.MOD_ID, "vindicator_smg"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(2)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(0.33F)
                .setRecoilAngle(1.0F)
                .setAlwaysSpread(false)
                .setSpread(4.0F)
                .setDrawTimer(20)

                // Reloads
                .setMaxAmmo(30)
                .setReloadType(ReloadType.SINGLE_ITEM)
                .setReloadTimer(35)
                .setAdditionalReloadTimer(10)

                // Projectile
                .setAmmo(Items.EMERALD)
                .setReloadItem(Items.EMERALD_BLOCK)
                .setEjectsCasing(false)
                .setProjectileVisible(false)
                .setDamage(2.0F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(6F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0x00FF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setReduceDamageOverLife(true)

                // Sounds
                .setFireSound(ModSounds.BLOSSOM_RIFLE_FIRE.get())
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.SR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())
                .setEnchantedFireSound(ModSounds.BLOSSOM_RIFLE_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0, 4.45, -5)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0,  4.65, -1.75))
                .setScope(1.0F, 0.0, 5.1, 1)
                .setUnderBarrel(1.0F, 0.0, 2.7, -1.5)

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "primitive_blowpipe"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.SEMI_AUTO)
                .setFireRate(40)
                .setGripType(GripType.ONE_HANDED)
                .setRecoilKick(0.33F)
                .setRecoilAngle(1.0F)
                .setAlwaysSpread(false)
                .setSpread(0.0F)
                .setDrawTimer(10)

                // Reloads
                .setMaxAmmo(1)
                .setReloadType(ReloadType.INVENTORY_FED)
                .setReloadTimer(20)
                .setAdditionalReloadTimer(0)

                // Projectile
                .setAmmo(Items.ARROW)
                .setEjectsCasing(false)
                .setProjectileVisible(false)
                .setDamage(4.0F)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(24F)
                .setProjectileLife(60)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFFFF | 0xFF000000)
                .setProjectileAffectedByGravity(true)

                // Mob Effect
                .setPotionEffect(BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.POISON))
                .setPotionEffectStrength(0)
                .setPotionEffectDuration(100)
                .setSelfPotionEffect(false)

                // Sounds
                .setFireSound(SoundEvents.PLAYER_ATTACK_SWEEP)
                .setReloadStart(ModSounds.CR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.SR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.CR_RELOAD_EJECTOR.get())
                .setEjectorPullSound(ModSounds.CR_EJECTOR_PULL.get())
                .setEjectorReleaseSound(ModSounds.CR_EJECTOR_RELEASE.get())

                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "fire_sweeper"), Gun.Builder.create()

                // General
                .setFireMode(FireMode.AUTOMATIC)
                .setFireRate(4)
                .setGripType(GripType.TWO_HANDED)
                .setRecoilKick(2.0F)
                .setRecoilAngle(5.0F)
                .setAlwaysSpread(true)
                .setSpread(16.0F)
                .setProjectileAmount(8)
                .setDrawTimer(30)

                // Reloads
                .setMaxAmmo(8)
                .setReloadType(ReloadType.MAG_FED)
                .setReloadTimer(90)
                .setAdditionalReloadTimer(30)

                // Projectile
                .setAmmo(Items.FIRE_CHARGE)
                .setEjectsCasing(true)
                .setProjectileVisible(false)
                .setDamage(17F)
                .setAdvantage(ModTags.Entities.VERY_HEAVY.location())
                .setReduceDamageOverLife(true)
                .setProjectileSize(0.05F)
                .setProjectileSpeed(2.0F)
                .setProjectileLife(7)
                .setProjectileTrailLengthMultiplier(2)
                .setProjectileTrailColor(0xFFFF00 | 0xFF000000)
                .setProjectileAffectedByGravity(false)
                .setHideTrail(true)
                .setCollateral(true)

                // Sounds
                .setFireSound(ModSounds.REPEATING_SHOTGUN_FIRE.get())
                .setReloadStart(ModSounds.AR_RELOAD_MAGAZINE_OUT.get())
                .setReloadLoadSound(ModSounds.AR_RELOAD_MAGAZINE_IN.get())
                .setReloadEndSound(ModSounds.PS_PUMP.get())
                .setEjectorPullSound(SoundEvents.FIRECHARGE_USE)
                .setEjectorReleaseSound(ModSounds.PS_PUMP_RELEASE.get())
                .setSilencedFireSound(ModSounds.PUMP_SHOTGUN_SILENCED_FIRE.get())
                .setEnchantedFireSound(ModSounds.REPEATING_SHOTGUN_ENCHANTED_FIRE.get())

                // Attachments
                .setMuzzleFlash(0.8, 0.0, 4.645, -10.635)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.4, -1.75))
                .setMagazine(0.0F, 0.0, 0.0, 0.0)

                .build());
    }
}
