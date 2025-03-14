package ttv.migami.jeg.event;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntitySpawnEventHandler {
    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof Skeleton skeleton && Config.COMMON.world.trumpetSpawning.get()) {
            if (RANDOM.nextFloat() < 0.05f) {
                skeleton.addTag("TrumpetBoi");
            }
        }
    }
}