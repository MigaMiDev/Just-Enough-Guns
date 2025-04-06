package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.AnimatableIdCache;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.animations.GunAnimations;
import ttv.migami.jeg.client.render.gun.animated.AnimatedGunRenderer;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModSyncedDataKeys;

import java.util.function.Consumer;

public class AnimatedGunItem extends GunItem implements GeoAnimatable, GeoItem {
    //private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final String gunID;
    private final SoundEvent reloadSoundMagOut;
    private final SoundEvent reloadSoundMagIn;
    private final SoundEvent reloadSoundEnd;
    private final SoundEvent ejectorSoundPull;
    private final SoundEvent ejectorSoundRelease;

    private int drawTick = 0;
    private int heartBeat = 60;
    private int shootTick = 0;

    public AnimatedGunItem(Properties properties, String path,
                           SoundEvent reloadSoundMagOut, SoundEvent reloadSoundMagIn, SoundEvent reloadSoundEnd, SoundEvent ejectorSoundPull, SoundEvent ejectorSoundRelease) {
        super(properties);

        this.gunID = path;
        this.reloadSoundMagOut = reloadSoundMagOut;
        this.reloadSoundMagIn = reloadSoundMagIn;
        this.reloadSoundEnd = reloadSoundEnd;
        this.ejectorSoundPull = ejectorSoundPull;
        this.ejectorSoundRelease = ejectorSoundRelease;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(entity instanceof Player)) {
            return;
        }

        CompoundTag nbtCompound = stack.getOrCreateTag();

        if (!nbtCompound.contains(ID_NBT_KEY, Tag.TAG_ANY_NUMERIC) && world instanceof ServerLevel) {
            nbtCompound.putLong(ID_NBT_KEY, AnimatableIdCache.getFreeId((ServerLevel) world));
        }
        final long id = GeoItem.getId(stack);

        if (stack.getItem() == ModItems.FINGER_GUN.get() && !stack.getTag().getBoolean("IgnoreAmmo")){
            stack.getOrCreateTag().putBoolean("IgnoreAmmo", true);
        }

        if (entity instanceof Player player) {
            if (player.getMainHandItem().getItem() == ModItems.HYPERSONIC_CANNON.get()) {
                this.heartBeat--;
                if (this.heartBeat == 0) {
                    player.playSound(SoundEvents.WARDEN_HEARTBEAT, 0.3F, 1.0F);
                    this.heartBeat = 30;
                }
                if (this.heartBeat < 0) {
                    this.heartBeat = 30;
                }
            }

            if (id == GeoItem.getId(player.getMainHandItem())) {
                if (!nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15) {
                    nbtCompound.putBoolean("IsDrawing", true);
                }

                if (nbtCompound.getBoolean("IsShooting") && nbtCompound.getInt("ShootingTick") < 5) {
                    this.shootTick++;
                    nbtCompound.putInt("ShootingTick", this.shootTick);
                }

                if (nbtCompound.getInt("ShootingTick") >= 5) {
                    nbtCompound.remove("IsShooting");
                    this.shootTick = 0;
                }

                if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15) {
                    this.drawTick++;
                    nbtCompound.putInt("DrawnTick", this.drawTick);
                    if (stack.is(ModItems.ROCKET_LAUNCHER.get())) {
                        player.displayClientMessage(Component.translatable("chat.jeg.rocket_ride").withStyle(ChatFormatting.WHITE), true);
                    }
                    if (stack.is(ModItems.FLAMETHROWER.get())) {
                        player.displayClientMessage(Component.translatable("chat.jeg.flamethrower").withStyle(ChatFormatting.WHITE), true);
                    }
                }

                if (nbtCompound.getInt("DrawnTick") >= 15) {
                    nbtCompound.putBoolean("IsDrawing", false);
                }

                boolean isSprinting = player.isSprinting();
                boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
                updateBooleanTag(nbtCompound, "IsAiming", isAiming);
                updateBooleanTag(nbtCompound, "IsRunning", isSprinting);
            }
            else if (GeoItem.getId(player.getMainHandItem()) != id || player.isDeadOrDying()) {
                nbtCompound.putBoolean("IsDrawing", true);
                this.resetTags(nbtCompound);
                this.drawTick = 0;
                this.shootTick = 0;
            }
        }
    }

    public void resetTags(CompoundTag compoundTag) {
        compoundTag.remove("IsDrawing");
        compoundTag.remove("IsDrawn");
        compoundTag.remove("DrawnTick");
        compoundTag.remove("IsShooting");
        compoundTag.remove("IsReloading");
        compoundTag.remove("IsFinishingReloading");
        compoundTag.remove("IsInspecting");
        compoundTag.remove("IsAiming");
        compoundTag.remove("IsRunning");
    }

    private boolean isAnimationPlaying(AnimationController<GeoAnimatable> animationController, String animationName) {
        return animationController.getCurrentAnimation() != null &&
                animationController.getCurrentAnimation().animation().name().matches(animationName);
    }

    private void updateBooleanTag(CompoundTag nbt, String key, boolean value) {
        if (value) {
            nbt.putBoolean(key, true);
        } else {
            nbt.remove(key);
        }
    }

    private void soundListener(SoundKeyframeEvent<GeoAnimatable> gunItemSoundKeyframeEvent)
    {
        Player player = ClientUtils.getClientPlayer();
        if (player != null)
        {
            switch (gunItemSoundKeyframeEvent.getKeyframeData().getSound())
            {
                case "rustle" -> player.playSound(ModSounds.GUN_RUSTLE.get(), 1, 1);
                case "screw" -> player.playSound(ModSounds.GUN_SCREW.get(), 1, 1);
                case "reload_mag_out" -> player.playSound(this.reloadSoundMagOut, 1, 1);
                case "reload_mag_in" -> player.playSound(this.reloadSoundMagIn, 1, 1);
                case "reload_end" -> player.playSound(this.reloadSoundEnd, 1, 1);
                case "ejector_pull" -> player.playSound(this.ejectorSoundPull, 1, 1);
                case "ejector_release" -> player.playSound(this.ejectorSoundRelease, 1, 1);
                case "jammed" -> player.playSound(SoundEvents.ANVIL_LAND, 0.8F, 1.5F);
            }
        }
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AnimatedGunRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new AnimatedGunRenderer(new ResourceLocation(Reference.MOD_ID, gunID));

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(GunAnimations.genericIdleController(this).setSoundKeyframeHandler(this::soundListener));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
