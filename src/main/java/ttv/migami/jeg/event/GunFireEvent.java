package ttv.migami.jeg.event;

import net.mcreator.deathangels.init.DeathAngelsModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.item.AnimatedBowItem;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.util.GunModifierHelper;

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
                if (stack.getItem() instanceof AnimatedGunItem animatedGunItem) {
                    animatedGunItem.resetTags(stack.getTag());
                    stack.getTag().putBoolean("IsShooting", true);
                    final long id = GeoItem.getId(stack);
                    AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("Controller");
                    animationController.forceAnimationReset();
                }
            }

            if (stack.getItem() instanceof GunItem) {
                if (!GunModifierHelper.isSilencedFire(stack)) {
                    if (JustEnoughGuns.aQuietPlaceLoaded) {
                        player.addEffect(new MobEffectInstance(DeathAngelsModMobEffects.SOUND_EMITTING.get(), 40, 1));
                    }
                }
            }

            boolean doesNotLight = stack.is(ModItems.FINGER_GUN.get())
                    || stack.is(ModItems.TYPHOONEE.get())
                    || stack.is(ModItems.ATLANTEAN_SPEAR.get())
                    || stack.getItem() instanceof AnimatedBowItem
                    || stack.getOrCreateTag().getString("GunId").endsWith("bow");

            if (Config.COMMON.gameplay.dynamicLightsOnShooting.get() && !doesNotLight) {
                BlockState targetState = player.level().getBlockState(BlockPos.containing(player.getEyePosition()));
                if (targetState.getBlock() == ModBlocks.BRIGHT_DYNAMIC_LIGHT.get()) {
                    if (getValue(player.level(), BlockPos.containing(player.getEyePosition()), "Delay") < 1.0) {
                        updateDelayAndNotify(player.level(), BlockPos.containing(player.getEyePosition()), targetState);
                    }
                } else if (targetState.getBlock() == Blocks.AIR || targetState.getBlock() == Blocks.CAVE_AIR) {
                    BlockState dynamicLightState = ModBlocks.BRIGHT_DYNAMIC_LIGHT.get().defaultBlockState();
                    player.level().setBlock(BlockPos.containing(player.getEyePosition()), dynamicLightState, 3);
                }
            }
        }
    }

    private static void updateDelayAndNotify(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.getPersistentData().putDouble("Delay", 1.0);
        }
        if (world instanceof Level) {
            ((Level) world).sendBlockUpdated(pos, state, state, 3);
        }
    }

    public static double getValue(LevelAccessor world, BlockPos pos, String tag) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.getPersistentData().getDouble(tag) : -1.0;
    }
}
