package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.entity.throwable.ThrowableExplosiveChargeEntity;
import ttv.migami.jeg.entity.throwable.ThrowableGrenadeEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class ExplosiveChargeItem extends GrenadeItem
{
    public ExplosiveChargeItem(Properties properties, int maxCookTime)
    {
        super(properties, maxCookTime);
    }

    @Override
    public ThrowableGrenadeEntity create(ItemStack stack, Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableExplosiveChargeEntity(world, entity, 72000);
    }

    @Override
    public boolean canCook()
    {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("info.jeg.tooltip_item.wither_blueprint").withStyle(ChatFormatting.GRAY));

    }


    @Override
    protected void onThrown(Level world, ThrowableGrenadeEntity entity)
    {
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
