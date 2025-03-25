package ttv.migami.jeg.event;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ttv.migami.jeg.entity.monster.phantom.PhantomSwarmSpawner;
import ttv.migami.jeg.faction.patrol.GunnerPatrolSpawner;
import ttv.migami.jeg.faction.raid.GunnerRaidSpawner;

public class ServerTickHandler {
    private static final GunnerPatrolSpawner spawner = new GunnerPatrolSpawner();
    private static final GunnerRaidSpawner raid = new GunnerRaidSpawner();
    private static final PhantomSwarmSpawner swarm = new PhantomSwarmSpawner();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            spawner.tick(event.getServer().overworld(), true, false);
            raid.tick(event.getServer().overworld(), true, false);
            swarm.tick(event.getServer().overworld(), true, false);
        }
    }
}