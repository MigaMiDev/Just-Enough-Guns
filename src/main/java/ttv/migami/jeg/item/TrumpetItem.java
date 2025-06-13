package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.attachment.impl.Barrel;
import ttv.migami.jeg.item.attachment.item.BarrelItem;

import javax.annotation.Nullable;
import java.util.List;

public class TrumpetItem extends BarrelItem {
    public static final int COOLDOWN_TICKS = 40;

    public TrumpetItem(Barrel barrel, Properties properties) {
        super(barrel, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem()).withStyle(ChatFormatting.GRAY));

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        world.gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, player.getPosition(1F));

        if (!world.isClientSide()) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.DOOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        Vec3 lookVec = player.getLookAngle();
        double offsetX = lookVec.x * 1.8;
        double offsetY = lookVec.y * 1.8;
        double offsetZ = lookVec.z * 1.8;
        Vec3 playerPos = player.getPosition(1F).add(offsetX, offsetY + player.getEyeHeight(), offsetZ);

        double pushStrength = 2;
        double opposite = -1;

        player.push(lookVec.x * opposite, lookVec.y * opposite, lookVec.z * opposite);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        player.fallDistance = 0;

        // Push entities
        double attackRange = 8.0;
        double sweepAngle = Math.toRadians(100);
        double maxDistance = 10.0;

        Vec3 playerPos2 = player.position();

        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(attackRange));

        for (LivingEntity entity : entities) {
            if (entity == player) continue;

            Vec3 entityPos = entity.position().subtract(playerPos2);
            double distance = entityPos.length();
            double angle = Math.acos(entityPos.normalize().dot(lookVec.normalize()));

            if (angle < sweepAngle / 2) {
                double distanceMultiplier = 1.0 - Math.min(distance / maxDistance, 1.0);
                entity.push(lookVec.x * (pushStrength * distanceMultiplier), lookVec.y * (pushStrength * distanceMultiplier), lookVec.z * (pushStrength * distanceMultiplier));
                entity.hurtMarked = true;
            }
        }

        if (world.isClientSide) {
            world.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 2, offsetY / 2, offsetZ / 2);
            world.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 4, offsetY / 4, offsetZ / 4);
            world.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 1.5, offsetY / 1.5, offsetZ / 1.5);
        }

        int maxDamage = itemStack.getMaxDamage();
        int currentDamage = itemStack.getDamageValue();
        if (currentDamage >= (maxDamage - 1)) {
            world.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
            itemStack.shrink(1);
        } else {
            itemStack.hurtAndBreak(1, player, e -> {});
        }

        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }

}
