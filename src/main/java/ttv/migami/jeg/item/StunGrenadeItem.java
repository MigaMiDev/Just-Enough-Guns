package ttv.migami.jeg.item;

import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.entity.throwable.ThrowableGrenadeEntity;
import ttv.migami.jeg.entity.throwable.ThrowableStunGrenadeEntity;
import ttv.migami.jeg.init.ModSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class StunGrenadeItem extends GrenadeItem
{
    public StunGrenadeItem(Item.Properties properties, int maxCookTime)
    {
        super(properties, maxCookTime);
    }

    @Override
    public ThrowableGrenadeEntity create(ItemStack stack, Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableStunGrenadeEntity(world, entity, 20 * 1);
    }

    @Override
    public boolean canCook()
    {
        return false;
    }

    @Override
    protected void onThrown(Level world, ThrowableGrenadeEntity entity)
    {
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
