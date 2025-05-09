package ttv.migami.jeg.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.entity.monster.phantom.PhantomSwarmData;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;
import ttv.migami.jeg.init.ModEntities;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntitySpawnEventHandler {
    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();

        if (mob instanceof Skeleton skeleton && Config.COMMON.world.trumpetSpawning.get()) {
            if (RANDOM.nextFloat() < 0.05f) {
                skeleton.addTag("TrumpetBoi");
            }
        }

        if (mob.level() instanceof ServerLevel serverLevel) {
            PhantomSwarmData raidData = PhantomSwarmData.get(serverLevel);
            if (event.getSpawnType().equals(MobSpawnType.NATURAL) && mob.getRandom().nextFloat() < 0.2F && mob instanceof Phantom && Config.COMMON.gunnerMobs.phantomGunnersReplacePhantoms.get() && raidData.hasPhantomSwarm()) {
                mob.discard();
                PhantomGunner gunner = new PhantomGunner(ModEntities.PHANTOM_GUNNER.get(), serverLevel);
                gunner.setPos(mob.getX(), mob.getY(), mob.getZ());
                mob.level().addFreshEntity(gunner);
            }
        }
    }
}