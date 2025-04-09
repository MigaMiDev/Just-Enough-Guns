package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import ttv.migami.jeg.common.network.ServerPlayHandler;
import ttv.migami.jeg.entity.animal.Boo;
import ttv.migami.jeg.init.ModEntities;
import ttv.migami.jeg.init.ModParticleTypes;

import javax.annotation.Nullable;
import java.util.List;

import static ttv.migami.jeg.common.network.ServerPlayHandler.rayTrace;

public class SoultreatItem extends ToolTipItem {
    public SoultreatItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            Entity target = getTargetEntity(player);

            if (target instanceof Bee && !(target instanceof Boo)) {
                target.remove(Entity.RemovalReason.DISCARDED);

                ServerLevel serverLevel = (ServerLevel) world;
                Boo boo = new Boo(ModEntities.BOO.get(), serverLevel);
                if (((Bee) target).getAge() < 0) {
                    boo.setAge(-1000);
                }
                boo.setPos(target.getX(), target.getY(), target.getZ());
                serverLevel.addFreshEntity(boo);

                serverLevel.playSound(null, boo, SoundEvents.BEEHIVE_EXIT, SoundSource.AMBIENT, 1F, 1F);
                ((ServerLevel) boo.level()).sendParticles(ModParticleTypes.GHOST_FLAME.get(), boo.getX(), boo.getY(), boo.getZ(), 10, boo.getBbWidth() / 2, boo.getBbHeight() / 2, boo.getBbWidth() / 2, 0.1);


                itemStack.shrink(1);
                return InteractionResultHolder.success(itemStack);
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

        tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.jeg.tooltip_item.soul_treat.obtainment").withStyle(ChatFormatting.GRAY));


    }

}
