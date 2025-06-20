package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.client.KeyBinds;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.attachment.impl.Side;
import ttv.migami.jeg.item.attachment.item.SpecialItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class FlashlightItem extends SpecialItem {
    private static final String TAG_POWERED = "Powered";
    public static final String TAG_BATTERY_LIFE = "BatteryLife";
    private static final int COOLDOWN_TICKS = 20;
    public static final int MAX_BATTERY_LIFE = 600;

    public FlashlightItem(Side side, Properties properties) {
        super(side, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem() + "_help").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem() + "_help_gun", KeyBinds.KEY_MELEE.getTranslatedKeyMessage().getString().toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.GRAY));

    }

    public static void registerItemProperties() {
        ItemProperties.register(ModItems.FLASHLIGHT.get(), new ResourceLocation("powered"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {
                    return stack.hasTag() && stack.getTag().getBoolean("Powered") ? 1.0F : 0.0F;
                }
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        if (!world.isClientSide && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            boolean powered = tag.getBoolean(TAG_POWERED);
            int batteryLife = tag.getInt(TAG_BATTERY_LIFE);

            if (!tag.contains(TAG_BATTERY_LIFE)) {
                tag.putInt(TAG_BATTERY_LIFE, MAX_BATTERY_LIFE);
            }

            if (powered && (!player.isCreative() && !player.isSpectator())) {
                if (batteryLife > 0) {
                    tag.putInt(TAG_BATTERY_LIFE, batteryLife - 1);
                } else {
                    tag.putBoolean(TAG_POWERED, false);
                }
            }
            if (batteryLife <= 0) {
                if (selected) {
                    player.displayClientMessage(Component.translatable("chat.jeg.flashlight_battery_dead").withStyle(ChatFormatting.RED), true);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player != null && !world.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            boolean powered = tag.getBoolean(TAG_POWERED);
            int batteryLife = tag.getInt(TAG_BATTERY_LIFE);

            if (!Config.COMMON.gameplay.allowFlashlights.get()) {
                Component message = Component.translatable("chat.jeg.disabled_flashlights")
                        .withStyle(ChatFormatting.GRAY);
                player.displayClientMessage(message, true);
            }

            if (batteryLife <= 0) {
                if (powered) {
                    tag.putBoolean(TAG_POWERED, false);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.GOOSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS / 2);
            } else {
                if (!powered && (!player.isCreative() && !player.isSpectator())) {
                    tag.putInt(TAG_BATTERY_LIFE, batteryLife - 1);
                }
                tag.putBoolean(TAG_POWERED, !powered);
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

                world.gameEvent(player, GameEvent.BLOCK_ACTIVATE, player.getPosition(1F));
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.FLASHLIGHT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int batteryLife = tag.getInt(TAG_BATTERY_LIFE);
        return batteryLife < MAX_BATTERY_LIFE;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int batteryLife = tag.getInt(TAG_BATTERY_LIFE);
        return Math.round(13.0F * batteryLife / MAX_BATTERY_LIFE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int batteryLife = tag.getInt(TAG_BATTERY_LIFE);

        float percentage = (float) batteryLife / MAX_BATTERY_LIFE;

        if (percentage > 0.5f) {
            int red = (int) (255 * (1 - 2 * (percentage - 0.5f)));
            return (red << 16) | 0xFF00;
        } else {
            int green = (int) (255 * 2 * percentage);
            return (0xFF << 16) | (green << 8);
        }
    }
}
