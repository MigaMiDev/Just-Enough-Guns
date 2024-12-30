package ttv.migami.jeg.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import ttv.migami.jeg.client.KeyBinds;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.item.AnimatedBowItem;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.C2SMessageMelee;

/**
 * Author: MrCrayfish
 */
public class MeleeHandler
{
    private static MeleeHandler instance;

    public static MeleeHandler get()
    {
        if(instance == null)
        {
            instance = new MeleeHandler();
        }
        return instance;
    }

    private MeleeHandler()
    {
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event)
    {
        Player player = Minecraft.getInstance().player;
        if(player == null)
            return;

        if(KeyBinds.KEY_MELEE.isDown() && event.getAction() == GLFW.GLFW_PRESS)
        {
            if (player.getMainHandItem().getItem() instanceof AnimatedBowItem) {
                return;
            }

            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMelee());

            if (player.getMainHandItem().getItem() instanceof AnimatedGunItem gunItem) {
                final long id = GeoItem.getId(player.getMainHandItem());
                AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");

                ItemCooldowns tracker = player.getCooldowns();
                if (!tracker.isOnCooldown(player.getMainHandItem().getItem()) &&
                        !animationController.getCurrentAnimation().animation().name().matches("draw")) {
                    //animationController.forceAnimationReset();

                    if ((Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem)) {
                        animationController.tryTriggerAnimation("bayonet");
                        if (player.isSprinting()) {
                            Vec3 lookVec = player.getLookAngle();
                            double pushStrength = 1;

                            player.push(lookVec.x * pushStrength, lookVec.y * pushStrength, lookVec.z * pushStrength);
                        }
                    }
                    else {
                        animationController.tryTriggerAnimation("melee");
                    }
                }
            }
        }
    }
}
