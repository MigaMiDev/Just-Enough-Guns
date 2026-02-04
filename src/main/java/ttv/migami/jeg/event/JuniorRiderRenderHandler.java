package ttv.migami.jeg.event;

import com.mojang.math.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class JuniorRiderRenderHandler {

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();

        if (player.getVehicle() instanceof TerrorPhantom phantom) {
            float partialTick = event.getPartialTick();

            float interpolatedYaw = phantom.getYRot();
            float interpolatedPitch = phantom.getXRot();

            //event.getPoseStack().mulPose(Axis.YP.rotationDegrees(interpolatedYaw));
            event.getPoseStack().mulPose(Axis.XP.rotationDegrees(interpolatedPitch));
        }
    }
}