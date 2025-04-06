package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ScoreStreakItem extends ToolTipItem {
    private int maxPoints = 2000;
    private static final int TICK_INTERVAL = 20 * 10;

    public ScoreStreakItem(Properties properties, int maxPoints) {
        super(properties);
        this.maxPoints = maxPoints;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player) {
            int points = getPoints(stack);
            int seconds = getSeconds(stack);

            if (seconds <= 0 && points < this.maxPoints) {
                setPoints(stack, points + TICK_INTERVAL);
                setSeconds(stack, TICK_INTERVAL);
            }

            if (seconds > 0 && points < this.maxPoints) {
                setSeconds(stack, seconds - 1);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tagCompound = stack.getTag();

        if (tagCompound != null) {
            tooltip.add(Component.translatable("info.jeg.score_streak_points").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(tagCompound.getInt("Points"))).withStyle(ChatFormatting.GOLD)
                            .append(Component.literal("/")).withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(String.valueOf(this.maxPoints)).withStyle(ChatFormatting.GOLD))));
        }

        tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.jeg.tooltip_item.score_streak_item").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (getPoints(stack) >= this.maxPoints || player.isCreative()) {
            useScoreStreak(player);
            if (!player.isCreative())
                setPoints(stack, 0);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    public void useScoreStreak(Player player) {
    }

    public int getPoints(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Points");
    }

    public void setPoints(ItemStack stack, int points) {
        stack.getOrCreateTag().putInt("Points", Math.min(points, maxPoints));
    }

    public int getSeconds(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Seconds");
    }

    public void setSeconds(ItemStack stack, int seconds) {
        stack.getOrCreateTag().putInt("Seconds", seconds);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getPoints(stack) < this.maxPoints;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) (13.0 * getPoints(stack) / this.maxPoints);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, (float) getPoints(stack) / this.maxPoints) * 0.33F, 1.0F, 1.0F);
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
