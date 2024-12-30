package ttv.migami.jeg.event;

import net.minecraft.world.entity.monster.Skeleton;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntitySpawnEventHandler {

    @SubscribeEvent
    public static void onSpecialSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof Skeleton skeleton && Config.COMMON.world.trumpetSpawning.get()) {
            // 5% chance to equip the trumpet instead of a bow
            if (skeleton.level().random.nextFloat() < 0.05) {
                skeleton.addTag("TrumpetBoi");
            }
        }
    }
}