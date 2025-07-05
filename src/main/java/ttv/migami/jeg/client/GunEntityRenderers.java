package ttv.migami.jeg.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.render.entity.*;
import ttv.migami.jeg.init.ModEntities;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunEntityRenderers
{
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.ARROW_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.FLAME_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BEAM.get(), BeamRenderer::new);
        event.registerEntityRenderer(ModEntities.PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SPECTRE_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.WATER_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BLAZE_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SONIC_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.FLARE_PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.WATER_BOMB.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.POCKET_BUBBLE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.ROCKET.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_MOLOTOV_COCKTAIL.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_SMOKE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_WATER_BOMB.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_POCKET_BUBBLE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_FLARE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_EXPLOSIVE_CHARGE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_PHANTOM_GUNNER_BAIT.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_TERROR_PHANTOM_FLARE.get(), ThrowableGrenadeRenderer::new);
    }
}
