package ttv.migami.jeg.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.AnimatedGunItem;

/**
 * <p>Fired when a player shoots a gun.</p>
 *
 * @author Ocelot
 */
public class GunFireEvent extends PlayerEvent
{
    private final ItemStack stack;

    public GunFireEvent(Player player, ItemStack stack)
    {
        super(player);
        this.stack = stack;
    }

    /**
     * @return The stack the player was holding when firing the gun
     */
    public ItemStack getStack()
    {
        return stack;
    }

    /**
     * @return Whether or not this event was fired on the client side
     */
    public boolean isClient()
    {
        return this.getEntity().getCommandSenderWorld().isClientSide();
    }

    /**
     * <p>Fired when a player is about to shoot a bullet.</p>
     *
     * @author Ocelot
     */
    @Cancelable
    public static class Pre extends GunFireEvent
    {
        public Pre(Player player, ItemStack stack)
        {
            super(player, stack);

            /*if(stack.getItem() instanceof AnimatedGunItem animatedGunItem) {
                if (animatedGunItem.getDrawTick() < 1) {
                    this.setCanceled(true);
                }
            }*/
        }
    }

    /**
     * <p>Fired after a player has shot a bullet.</p>
     *
     * @author Ocelot
     */
    public static class Post extends GunFireEvent
    {
        public Post(Player player, ItemStack stack)
        {
            super(player, stack);

            if (stack.getTag() != null) {
                if (stack.getItem() instanceof AnimatedGunItem gunItem) {
                    //stack.getTag().putBoolean("IsShooting", true);
                    stack.getTag().remove("IsReloading");
                    stack.getTag().remove("IsFinishingReloading");
                    final long id = GeoItem.getId(stack);
                    AnimationController<GeoAnimatable> animationController = gunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
                    animationController.forceAnimationReset();
                    if (ModSyncedDataKeys.AIMING.getValue(player)) {
                        animationController.tryTriggerAnimation("aim_shoot");
                    }
                    else {
                        animationController.tryTriggerAnimation("shoot");
                    }
                }
            }
        }
    }
}
