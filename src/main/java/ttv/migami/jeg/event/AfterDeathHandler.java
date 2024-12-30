package ttv.migami.jeg.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.init.ModEffects;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class AfterDeathHandler {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        {
            MobEffectInstance effect = new MobEffectInstance(ModEffects.BULLET_PROTECTION.get(), Config.COMMON.gameplay.bulletProtection.get() * 20, 0, false, false);
            player.addEffect(effect);
        }
    }
}