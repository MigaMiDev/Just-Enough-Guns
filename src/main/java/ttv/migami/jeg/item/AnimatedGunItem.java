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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.AnimatableIdCache;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.animations.GunAnimations;
import ttv.migami.jeg.client.render.gun.animated.AnimatedGunRenderer;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.ReloadType;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.C2SMessageCasing;
import ttv.migami.jeg.network.message.C2SMessageGunLoaded;
import ttv.migami.jeg.network.message.C2SMessageMayStopReloadAnimation;
import ttv.migami.jeg.network.message.C2SMessageStopReloading;
import ttv.migami.jeg.util.GunEnchantmentHelper;

import java.util.function.Consumer;

public class AnimatedGunItem extends GunItem implements GeoAnimatable, GeoItem {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    //private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final String gunID;
    private final SoundEvent reloadSoundMagOut;
    private final SoundEvent reloadSoundMagIn;
    private final SoundEvent reloadSoundEnd;
    private final SoundEvent ejectorSoundPull;
    private final SoundEvent ejectorSoundRelease;

    private int drawTick = 0;
    private boolean drawn = false;
    private int heartBeat = 60;

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

    public int getDrawTick() {
        return this.drawTick;
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
        AnimationController<GeoAnimatable> animationController = getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");

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

            updateBooleanTag(nbtCompound, "IsDrawing", nbtCompound.getBoolean("IsDrawn"));

            if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15) {
                this.drawTick++;
                nbtCompound.putInt("DrawnTick", this.drawTick);
                if (stack.is(ModItems.ROCKET_LAUNCHER.get())) {
                    player.displayClientMessage(Component.translatable("chat.jeg.rocket_ride").withStyle(ChatFormatting.WHITE), true);
                }
            }

            //if (stack == player.getMainHandItem()) {
            if (id == GeoItem.getId(player.getMainHandItem())) {

                if (!nbtCompound.getBoolean("IsDrawn")) {
                    nbtCompound.putBoolean("IsDrawn", true);
                }

                boolean isSprinting = player.isSprinting();

                boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
                updateBooleanTag(nbtCompound, "IsAiming", isAiming);

                updateBooleanTag(nbtCompound, "IsRunning", isSprinting);

                if (nbtCompound.getBoolean("IsDrawing") && nbtCompound.getInt("DrawnTick") < 15
                && Config.COMMON.gameplay.drawAnimation.get()) {
                    handleDrawingState(nbtCompound, animationController, stack);
                }

                // Jam Animations
                /*if (nbtCompound.getBoolean("IsJammed") && !isAnimationPlaying(animationController, "draw")
                        && Config.COMMON.gameplay.gunJamming.get()) {
                    handleJammedState(nbtCompound, animationController, stack);
                }*/

                if (nbtCompound.getInt("DrawnTick") >= 15 &&
                        !isAnimationPlaying(animationController, "draw") &&
                        !isAnimationPlaying(animationController, "jam") &&
                        !isAnimationPlaying(animationController, "melee") &&
                        !isAnimationPlaying(animationController, "bayonet") &&
                        !isAnimationPlaying(animationController, "hold_fire") &&
                        !isAnimationPlaying(animationController, "shoot") &&
                        !isAnimationPlaying(animationController, "aim_shoot")) {
                    if ((nbtCompound.getBoolean("IsReloading") || nbtCompound.getBoolean("IsFinishingReloading")) &&
                            !isAnimationPlaying(animationController, "reload_stop")) {
                        handleReloadingState(nbtCompound, animationController, stack);
                    } else if (nbtCompound.getBoolean("IsAiming")) {
                        handleAimingState(nbtCompound, animationController);
                    } else if (nbtCompound.getBoolean("IsRunning") &&
                            !isAnimationPlaying(animationController, "inspect") &&
                            !isAnimationPlaying(animationController, "reload_stop")) {
                        handleRunningState(animationController, isAiming, player);
                    } else if (!isAnimationPlaying(animationController, "inspect") &&
                            !isAnimationPlaying(animationController, "melee") &&
                            !isAnimationPlaying(animationController, "bayonet") &&
                            !isAnimationPlaying(animationController, "shoot") &&
                            !isAnimationPlaying(animationController, "aim_shoot") &&
                            //!isAnimationPlaying(animationController, "reload") &&
                            //!isAnimationPlaying(animationController, "reload_start") &&
                            //!isAnimationPlaying(animationController, "reload_loop") &&
                            !isAnimationPlaying(animationController, "reload_stop")) {
                        animationController.tryTriggerAnimation("idle");
                    }

                    if (!selected) {
                        animationController.tryTriggerAnimation("idle");
                    }
                }
            }
            else if (GeoItem.getId(player.getMainHandItem()) != id) {
                if (nbtCompound.getBoolean("IsDrawn")) {
                    nbtCompound.remove("IsDrawn");
                    nbtCompound.remove("DrawnTick");
                    nbtCompound.remove("IsReloading");
                    nbtCompound.remove("IsFinishingReloading");
                    this.drawTick = 0;
                }
                animationController.tryTriggerAnimation("idle");
            }
        }
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

    private void handleJammedState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        if (GunEnchantmentHelper.getQuickHands(stack) == 2) {
            animationController.setAnimationSpeed(1.75F);
        }
        else if (GunEnchantmentHelper.getQuickHands(stack) == 1) {
            animationController.setAnimationSpeed(1.25F);
        }
        if (GunEnchantmentHelper.getQuickHands(stack) == 0) {
            animationController.setAnimationSpeed(1.0D);
        }
        if (nbt.getBoolean("IsJammed") && !isAnimationPlaying(animationController, "draw")) {
            animationController.tryTriggerAnimation("jam");
        }

        nbt.remove("IsShooting");
        nbt.remove("IsInspecting");
    }

    private void handleDrawingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        if (GunEnchantmentHelper.getQuickHands(stack) == 2) {
            animationController.setAnimationSpeed(1.75F);
        }
        else if (GunEnchantmentHelper.getQuickHands(stack) == 1) {
            animationController.setAnimationSpeed(1.25F);
        }
        if (GunEnchantmentHelper.getQuickHands(stack) == 0) {
            animationController.setAnimationSpeed(1.0D);
        }
        if (nbt.getInt("DrawnTick") < 15) {
            animationController.tryTriggerAnimation("draw");
        }

        nbt.remove("IsShooting");
        nbt.remove("IsInspecting");
    }

    private void handleReloadingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController, ItemStack stack) {
        Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
        if (GunEnchantmentHelper.getQuickHands(stack) == 2) {
            animationController.setAnimationSpeed(1.75F);
        }
        else if (GunEnchantmentHelper.getQuickHands(stack) == 1) {
            animationController.setAnimationSpeed(1.25F);
        }
        if (GunEnchantmentHelper.getQuickHands(stack) == 0) {
            animationController.setAnimationSpeed(1.0D);
        }

        if (modifiedGun.getReloads().getReloadType() == ReloadType.MANUAL) {
            animationController.tryTriggerAnimation("reload_start");
        }
        else {
            if (stack.getItem() == ModItems.INFANTRY_RIFLE.get() && Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE)) {
                animationController.tryTriggerAnimation("reload_alt");
            }
            else {
                animationController.tryTriggerAnimation("reload");
            }
        }
        nbt.remove("IsShooting");
        nbt.remove("IsInspecting");
    }

    private void handleAimingState(CompoundTag nbt, AnimationController<GeoAnimatable> animationController) {
        animationController.setAnimationSpeed(1.0D);
        nbt.remove("IsInspecting");
        animationController.tryTriggerAnimation("idle");
    }

    private void handleRunningState(AnimationController<GeoAnimatable> animationController, boolean isAiming, Player player) {
        animationController.setAnimationSpeed(1.0D);
        if (!isAiming) {
            if (!(Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem))
                animationController.tryTriggerAnimation("sprint");
            else
                animationController.tryTriggerAnimation("idle");
        }
    }

    private void soundListener(SoundKeyframeEvent<AnimatedGunItem> gunItemSoundKeyframeEvent)
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

    private void particleListener(ParticleKeyframeEvent<AnimatedGunItem> gunItemParticleKeyframeEvent)
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

    private PlayState predicate(AnimationState<AnimatedGunItem> event)
    {
        if (event.getController().getCurrentAnimation() == null || event.getController().getAnimationState() == AnimationController.State.STOPPED)
        {
            event.getController().tryTriggerAnimation("idle");
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<AnimatedGunItem> controller = new AnimationController<>(this, "controller", 0, this::predicate)
                .setSoundKeyframeHandler(this::soundListener)
                .setParticleKeyframeHandler(this::particleListener)
                .setCustomInstructionKeyframeHandler((customInstructionKeyframeEvent -> {
                    String instruction = customInstructionKeyframeEvent.getKeyframeData().getInstructions();
                    switch(instruction) {
                        case "reloaded;" -> PacketHandler.getPlayChannel().sendToServer(new C2SMessageGunLoaded());
                        case "mayEndReload;" -> PacketHandler.getPlayChannel().sendToServer(new C2SMessageMayStopReloadAnimation());
                        case "finishReloading;" -> PacketHandler.getPlayChannel().sendToServer(new C2SMessageStopReloading());

                        //case "unjam;" -> PacketHandler.INSTANCE.sendToServer(new SpecialAttackPacket(this.getId()));

                        //case "finishUnjamming;" -> PacketHandler.INSTANCE.sendToServer(new UltimateAttackPacket(this.getId()));
                    }
                }))

                .triggerableAnim("idle", GunAnimations.IDLE)
                .triggerableAnim("hold_fire", GunAnimations.HOLD_FIRE)
                .triggerableAnim("shoot", GunAnimations.SHOOT)
                .triggerableAnim("aim_shoot", GunAnimations.AIM_SHOOT)
                .triggerableAnim("reload", GunAnimations.RELOAD)
                .triggerableAnim("reload_alt", GunAnimations.RELOAD_ALT)
                .triggerableAnim("reload_start", GunAnimations.RELOAD_START)
                .triggerableAnim("reload_loop", GunAnimations.RELOAD_LOOP)
                .triggerableAnim("reload_stop", GunAnimations.RELOAD_STOP)
                .triggerableAnim("sprint", GunAnimations.SPRINT)
                .triggerableAnim("melee", GunAnimations.MELEE)
                .triggerableAnim("bayonet", GunAnimations.BAYONET)
                .triggerableAnim("draw", GunAnimations.DRAW)
                .triggerableAnim("inspect", GunAnimations.INSPECT)
                .triggerableAnim("jam", GunAnimations.JAM);

        controllers.add(controller);

        /*controllers.add(new AnimationController<>(this, "controller", 1, state -> {
            ItemDisplayContext context = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

            if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
                return PlayState.CONTINUE;
            }
            else {
                return PlayState.STOP;
            }

        })
                .triggerableAnim("idle", GunAnimations.IDLE)
                .triggerableAnim("shoot", GunAnimations.SHOOT)
                .triggerableAnim("reload", GunAnimations.RELOAD)
                .triggerableAnim("sprint", GunAnimations.SPRINT)
        );*/
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
