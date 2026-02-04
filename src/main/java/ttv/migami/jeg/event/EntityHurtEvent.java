package ttv.migami.jeg.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;

@Mod.EventBusSubscriber
public class EntityHurtEvent {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingHurtEvent event) {
        LivingEntity hurtBoi = event.getEntity();

        if (hurtBoi.getRandom().nextFloat() > 0.3F) {
            if (hurtBoi.isPassenger() && hurtBoi.getVehicle() instanceof TerrorPhantom terrorPhantom) {
                terrorPhantom.hurt(event.getSource(), event.getAmount());
                event.setAmount(0);
                event.setCanceled(true);
            }
            if (hurtBoi instanceof TerrorPhantom terrorPhantom && terrorPhantom.getFirstPassenger() != null) {
                terrorPhantom.getFirstPassenger().hurt(event.getSource(), event.getAmount());
                event.setAmount(0);
                event.setCanceled(true);
            }
        }

    }
}
