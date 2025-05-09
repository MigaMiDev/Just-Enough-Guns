package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.ClientUtils;
import software.bernie.geckolib.util.GeckoLibUtil;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.animations.GunAnimations;
import ttv.migami.jeg.client.render.gun.animated.AnimatedGunRenderer;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.C2SMessageCasing;
import ttv.migami.jeg.util.GunEnchantmentHelper;

import java.util.function.Consumer;

public class AnimatedGunItem extends GunItem implements GeoAnimatable, GeoItem {
    //private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final String gunID;

    private int heartBeat = 60;

    public AnimatedGunItem(Properties properties, String path) {
        super(properties);

        this.gunID = path;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(entity instanceof Player)) {
            return;
        }

        if (stack.getItem() == ModItems.FINGER_GUN.get() && stack.getTag() != null && !stack.getTag().getBoolean("IgnoreAmmo")){
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

            if (stack == player.getMainHandItem()) {
                CompoundTag nbtCompound = stack.getOrCreateTag();

                if (nbtCompound.getBoolean("IsShooting") && nbtCompound.getInt("AnimationTick") < 5) {
                    nbtCompound.putInt("AnimationTick", nbtCompound.getInt("AnimationTick") + 1);
                }
                if (nbtCompound.getInt("AnimationTick") >= 5) {
                    nbtCompound.remove("IsShooting");
                    nbtCompound.remove("AnimationTick");
                }

                if (nbtCompound.getBoolean("IsMeleeing") && nbtCompound.getInt("AnimationTick") < 5) {
                    nbtCompound.putInt("AnimationTick", nbtCompound.getInt("AnimationTick") + 1);
                }
                if (nbtCompound.getInt("AnimationTick") >= 5) {
                    nbtCompound.remove("IsMeleeing");
                    nbtCompound.remove("AnimationTick");
                }

                if (nbtCompound.getBoolean("IsInspecting") && nbtCompound.getInt("AnimationTick") < 5) {
                    nbtCompound.putInt("AnimationTick", nbtCompound.getInt("AnimationTick") + 1);
                }
                if (nbtCompound.getInt("AnimationTick") >= 5) {
                    nbtCompound.remove("IsInspecting");
                    nbtCompound.remove("AnimationTick");
                }

                int drawTick = GunEnchantmentHelper.getModifiedDrawTick(player.getMainHandItem(), getModifiedGun(player.getMainHandItem()).getGeneral().getDrawTimer());

                if (nbtCompound.getInt("DrawnTick") >= drawTick) {
                    nbtCompound.putBoolean("IsDrawing", false);
                }

                if (!nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < drawTick) {
                    nbtCompound.putBoolean("IsDrawing", true);
                }

                if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < drawTick) {
                    nbtCompound.putInt("DrawnTick", nbtCompound.getInt("DrawnTick") + 1);
                    if (stack.is(ModItems.ROCKET_LAUNCHER.get())) {
                        player.displayClientMessage(Component.translatable("chat.jeg.rocket_ride").withStyle(ChatFormatting.WHITE), true);
                    }
                    if (stack.is(ModItems.FLAMETHROWER.get())) {
                        player.displayClientMessage(Component.translatable("chat.jeg.flamethrower").withStyle(ChatFormatting.WHITE), true);
                    }
                }

                boolean isSprinting = player.isSprinting();
                boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
                updateBooleanTag(nbtCompound, "IsAiming", isAiming);
                updateBooleanTag(nbtCompound, "IsRunning", isSprinting);
            }
            else {
                if (stack.getTag() == null) {
                    return;
                }
                if (stack == player.getMainHandItem()) {
                    return;
                }
                this.resetTags(stack);
            }
        }
    }

    public void resetTags(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();

        if (compoundTag == null) {
            return;
        }

        compoundTag.remove("IsDrawing");
        compoundTag.remove("DrawnTick");
        compoundTag.remove("IsShooting");
        compoundTag.remove("IsReloading");
        compoundTag.remove("IsInspecting");
        compoundTag.remove("IsRunning");
        compoundTag.remove("IsMeleeing");
        compoundTag.remove("IsFirstPersonReload");
    }

    public void resetTags(CompoundTag compoundTag) {
        compoundTag.remove("IsDrawing");
        compoundTag.remove("IsShooting");
        compoundTag.remove("IsReloading");
        compoundTag.remove("IsInspecting");
        compoundTag.remove("IsRunning");
        compoundTag.remove("IsMeleeing");
        compoundTag.remove("IsFirstPersonReload");
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

            if (!(player.getMainHandItem().getItem() instanceof GunItem)) {
                return;
            }

            Gun gun = getModifiedGun(player.getMainHandItem());
            SoundEvent sound;

            switch (gunItemSoundKeyframeEvent.getKeyframeData().getSound())
            {
                case "rustle" -> player.playSound(ModSounds.GUN_RUSTLE.get(), 1, 1);
                case "screw" -> player.playSound(ModSounds.GUN_SCREW.get(), 1, 1);
                case "reload_mag_out" ->  {
                    sound = BuiltInRegistries.SOUND_EVENT.get(gun.getSounds().getReloadStart());
                    if (sound != null) {
                        player.playSound(sound, 1, 1);
                    }
                }
                case "reload_mag_in" -> {
                    sound = BuiltInRegistries.SOUND_EVENT.get(gun.getSounds().getReloadLoad());
                    if (sound != null) {
                        player.playSound(sound, 1, 1);
                    }
                }
                case "reload_end" -> {
                    sound = BuiltInRegistries.SOUND_EVENT.get(gun.getSounds().getReloadEnd());
                    if (sound != null) {
                        player.playSound(sound, 1, 1);
                    }
                }
                case "ejector_pull" -> {
                    sound = BuiltInRegistries.SOUND_EVENT.get(gun.getSounds().getEjectorPull());
                    if (sound != null) {
                        player.playSound(sound, 1, 1);
                    }
                }
                case "ejector_release" -> {
                    sound = BuiltInRegistries.SOUND_EVENT.get(gun.getSounds().getEjectorRelease());
                    if (sound != null) {
                        player.playSound(sound, 1, 1);
                    }
                }
                case "jammed" -> player.playSound(SoundEvents.ANVIL_LAND, 0.8F, 1.5F);
            }
        }
    }

    private void particleListener(ParticleKeyframeEvent<GeoAnimatable> gunItemParticleKeyframeEvent)
    {
        Player player = ClientUtils.getClientPlayer();

        if (player != null)
        {
            if (player.getMainHandItem().getItem() instanceof AnimatedGunItem) {
                ItemStack itemStack = player.getMainHandItem();

                switch (gunItemParticleKeyframeEvent.getKeyframeData().getEffect())
                {
                    case "eject_casing" -> PacketHandler.getPlayChannel().sendToServer(new C2SMessageCasing());
                }
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
        controllers.add(GunAnimations.genericIdleController(this).setSoundKeyframeHandler(this::soundListener).setParticleKeyframeHandler(this::particleListener));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
