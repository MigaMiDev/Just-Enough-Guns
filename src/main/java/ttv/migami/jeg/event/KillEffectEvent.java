package ttv.migami.jeg.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class KillEffectEvent extends PlayerEvent
{
    private final ItemStack stack;
    private final Vec3 pos;
    private final LivingEntity target;

    public KillEffectEvent(Player player, ItemStack stack, Vec3 pos, LivingEntity target)
    {
        super(player);
        this.stack = stack;
        this.pos = pos;
        this.target = target;
    }

    /**
     * @return The stack the player was holding when firing the gun
     */
    public ItemStack getStack()
    {
        return stack;
    }

    /**
     * @return The stack the last position of the bullet
     */
    public Vec3 getPos()
    {
        return pos;
    }

    /**
     * @return The killed entity
     */
    public LivingEntity getTarget()
    {
        return target;
    }

    /**
     * @return Whether or not this event was fired on the client side
     */
    public boolean isClient()
    {
        return this.getEntity().getCommandSenderWorld().isClientSide();
    }

    /**
     * <p>Fired when a player is about to kill a target.</p>
     */
    @Cancelable
    public static class Pre extends KillEffectEvent
    {
        public Pre(Player player, ItemStack stack, Vec3 pos, LivingEntity target)
        {
            super(player, stack, pos, target);
        }
    }

    /**
     * <p>Fired after a player has killed an target.</p>
     */
    public static class Post extends KillEffectEvent
    {
        public Post(Player player, ItemStack stack, Vec3 pos, LivingEntity target)
        {
            super(player, stack, pos, target);
        }
    }
}
