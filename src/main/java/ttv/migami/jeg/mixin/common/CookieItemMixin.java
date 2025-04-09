package ttv.migami.jeg.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ttv.migami.jeg.init.ModItems;

@Mixin(Item.class)
public class CookieItemMixin
{
    @Inject(method = "useOn", at = @At(value = "TAIL"), cancellable = true)
    public void dropBubble(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir)
    {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        Player player = context.getPlayer();
        ItemStack itemstack = player.getItemInHand(context.getHand());
        if (itemstack.getItem() == Items.COOKIE && blockstate.is(Blocks.SOUL_CAMPFIRE)) {
            if (!level.isClientSide) {
                itemstack.shrink(1);
                player.getInventory().add(new ItemStack(ModItems.SOUL_TREAT.get()));
                level.playSound(null, player.getOnPos(), SoundEvents.BEEHIVE_EXIT, SoundSource.AMBIENT, 1F, 1F);
            }
        }
    }
}
