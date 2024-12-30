package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import ttv.migami.jeg.common.network.ServerPlayHandler;
import ttv.migami.jeg.init.ModItems;

import javax.annotation.Nullable;
import java.util.List;

import static ttv.migami.jeg.common.network.ServerPlayHandler.rayTrace;

/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 *
 * Author: MrCrayfish
 */
public class AmmoItem extends Item implements IAmmo
{
    public AmmoItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.getItem() == ModItems.RIFLE_AMMO.get()) {
            if (!world.isClientSide) {
                Entity target = getTargetEntity(player);

                if (target instanceof Blaze) {

                    itemStack.shrink(1);
                    player.getInventory().add(new ItemStack(ModItems.BLAZE_ROUND.get()));
                    return InteractionResultHolder.success(itemStack);
                }

            }

        }

        return InteractionResultHolder.pass(itemStack);

    }

    private Entity getTargetEntity(Player player) {
        BlockPos blockPos = rayTrace(player, 4.0D);
        EntityHitResult entityHitResult = ServerPlayHandler.hitEntity(player.level(), player, blockPos);

        return entityHitResult.getEntity();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        if (this == ModItems.BLAZE_ROUND.get())
            tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem()).withStyle(ChatFormatting.GRAY));
    }
}
