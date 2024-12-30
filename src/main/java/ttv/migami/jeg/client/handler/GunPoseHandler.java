package ttv.migami.jeg.client.handler;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.item.GunItem;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunPoseHandler {

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<LivingEntity, HumanoidModel<LivingEntity>> event) {
        /*JustEnoughGuns.LOGGER.atInfo().log("horay");
        LivingEntity entity = event.getEntity();
        EntityRenderer<? super LivingEntity> renderer = event.getRenderer();

        // Ensure the renderer is a LivingEntityRenderer with a HumanoidModel
        if (renderer instanceof LivingEntityRenderer<?, ?> livingRenderer) {
            if (livingRenderer.getModel() instanceof HumanoidModel<?> model) {
                // Apply custom arm pose logic
                if (isGunItemInHand(entity)) {
                    setCrossbowArmPose(entity, model);
                }
            }
        }*/
    }

    private static boolean isGunItemInHand(LivingEntity entity) {
        return entity.getMainHandItem().getItem() instanceof GunItem;
    }

    private static void setCrossbowArmPose(LivingEntity entity, HumanoidModel<?> model) {
        JustEnoughGuns.LOGGER.atInfo().log("we be setting");
        // Set the arm pose for the main hand
        if (entity.getMainHandItem().getItem() instanceof GunItem) {
            model.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        // Set the arm pose for the offhand
        else if (entity.getOffhandItem().getItem() instanceof GunItem) {
            model.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
    }
}