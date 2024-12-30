package ttv.migami.jeg.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import ttv.migami.jeg.client.KeyBinds;
import ttv.migami.jeg.item.AnimatedGunItem;

/**
 * Author: MrCrayfish
 */
public class InspectHandler
{
    private static InspectHandler instance;

    public static InspectHandler get()
    {
        if(instance == null)
        {
            instance = new InspectHandler();
        }
        return instance;
    }

    private InspectHandler()
    {
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event)
    {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        if(player.getMainHandItem().getItem() instanceof AnimatedGunItem gunItem) {
            if(KeyBinds.KEY_INSPECT.isDown() && event.getAction() == GLFW.GLFW_PRESS)
            {
                final long id = GeoItem.getId(player.getMainHandItem());
                AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");

                if (animationController != null && animationController.getCurrentAnimation() != null && !animationController.getCurrentAnimation().animation().name().matches("draw")
                        && !animationController.getCurrentAnimation().animation().name().matches("reload") &&
                    !AimingHandler.get().isAiming()) {
                    animationController.setAnimationSpeed(1.0D);
                    animationController.forceAnimationReset();
                    animationController.tryTriggerAnimation("inspect");
                }
            }
        }

    }

}
