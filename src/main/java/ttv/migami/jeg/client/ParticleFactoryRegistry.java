package ttv.migami.jeg.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.particle.*;
import ttv.migami.jeg.client.particle.phantom.PhantomGunnerParticle;
import ttv.migami.jeg.init.ModParticleTypes;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleFactoryRegistry
{
    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event)
    {
        event.registerSpecial(ModParticleTypes.BULLET_HOLE.get(), (typeIn, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) -> new BulletHoleParticle(worldIn, x, y, z, typeIn.getDirection(), typeIn.getPos()));
        event.registerSpecial(ModParticleTypes.LASER.get(), (typeIn, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) -> new LaserParticle(worldIn, x, y, z, typeIn.getDirection(), typeIn.getPos()));
        event.registerSpriteSet(ModParticleTypes.BLOOD.get(), BloodParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.TRAIL.get(), TrailParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.CASING_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SHELL_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SPECTRE_CASING_PARTICLE.get(), CasingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SCRAP.get(), ScrapParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.HEALING_GLINT.get(), GlintParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GHOST_FLAME.get(), GhostFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GHOST_GLINT.get(), GlintParticle.GhostProvider::new);
        event.registerSpriteSet(ModParticleTypes.SOUL_LAVA_PARTICLE.get(), SoulLavaParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.TYPHOONEE_BEAM.get(), TyphooneeBeamParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SONIC_RING.get(), SonicRingParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BIG_SONIC_RING.get(), SonicRingParticle.BigProvider::new);
        event.registerSpriteSet(ModParticleTypes.FLARE_SMOKE.get(), FlareSmokeParticle.SmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.FLARE.get(), FlareSmokeParticle.RedProvider::new);
        event.registerSpriteSet(ModParticleTypes.BLUE_FLARE.get(), FlareSmokeParticle.BlueProvider::new);
        event.registerSpriteSet(ModParticleTypes.FLAME.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BLUE_FLAME.get(), FlameParticle.BlueProvider::new);

        event.registerSpriteSet(ModParticleTypes.SPARK.get(), SparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BIG_EXPLOSION.get(), BigExplosion.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SMALL_EXPLOSION.get(), SmallExplosion.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SMOKE.get(), SmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE.get(), FireParticle.Provider::new);

        event.registerSpriteSet(ModParticleTypes.BUBBLE_AMMO.get(), BubbleAmmoParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ENTITY_LASER.get(), GlintParticle.EntityLaserProvider::new);

        event.registerSpriteSet(ModParticleTypes.CONFETTI.get(), ConfettiParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.HIT_MARKER.get(), GlintParticle.HitmarkerProvider::new);
        event.registerSpriteSet(ModParticleTypes.POPCORN.get(), PopcornParticle.Provider::new);

        event.registerSpriteSet(ModParticleTypes.PHANTOM_GUNNER.get(), PhantomGunnerParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.PHANTOM_GUNNER_SWARM.get(), PhantomGunnerParticle.SwarmProvider::new);
        event.registerSpriteSet(ModParticleTypes.SECOND_LAYER_PHANTOM_GUNNER_SWARM.get(), PhantomGunnerParticle.SecondLayerSwarmProvider::new);

    }
}
