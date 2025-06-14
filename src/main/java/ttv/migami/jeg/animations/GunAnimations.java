package ttv.migami.jeg.animations;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.ClientUtils;
import ttv.migami.jeg.common.FireMode;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.ReloadType;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.util.GunEnchantmentHelper;

public final class GunAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation SHOOT = RawAnimation.begin().then("shoot", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final RawAnimation AIM_SHOOT = RawAnimation.begin().then("aim_shoot", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final RawAnimation HOLD_FIRE = RawAnimation.begin().then("hold_fire", Animation.LoopType.PLAY_ONCE).thenLoop("hold");
    public static final RawAnimation HOLD = RawAnimation.begin().then("hold", Animation.LoopType.LOOP);
    public static final RawAnimation RELOAD = RawAnimation.begin().then("reload", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation RELOAD_ALT = RawAnimation.begin().then("reload_alt", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation RELOAD_START = RawAnimation.begin().then("reload_start", Animation.LoopType.PLAY_ONCE).thenLoop("reload_loop");
    public static final RawAnimation RELOAD_LOOP = RawAnimation.begin().then("reload_loop", Animation.LoopType.LOOP);
    public static final RawAnimation RELOAD_STOP = RawAnimation.begin().then("reload_stop", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation MELEE = RawAnimation.begin().then("melee", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation BAYONET = RawAnimation.begin().then("bayonet", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation SPRINT = RawAnimation.begin().then("sprint", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final RawAnimation INSPECT = RawAnimation.begin().then("inspect", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation DRAW = RawAnimation.begin().then("draw", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation JAM = RawAnimation.begin().then("jam", Animation.LoopType.PLAY_ONCE).thenLoop("idle");

    public static <T extends GeoAnimatable> AnimationController<GeoAnimatable> genericIdleController(AnimatedGunItem animatable) {
        return new AnimationController<>(animatable, "Controller", 0, state -> {
            Player player = ClientUtils.getClientPlayer();
            ItemStack gunStack = player.getMainHandItem();

            state.setControllerSpeed(1F);

            if (gunStack.getItem() != animatable)
                return state.setAndContinue(IDLE);

            if (!(gunStack.getItem() instanceof AnimatedGunItem animatedGunItem))
                return state.setAndContinue(IDLE);

            if (state.getController().getCurrentAnimation() == null)
                return state.setAndContinue(IDLE);

            if (gunStack.getTag() != null) {
                CompoundTag tag = gunStack.getTag();

                if (ModSyncedDataKeys.SHOOTING.getValue(player) &&
                        animatedGunItem.getModifiedGun(gunStack).getGeneral().getFireMode().equals(FireMode.RELEASE_FIRE)) {
                    return state.setAndContinue(HOLD_FIRE);
                }
                if (tag.getBoolean("IsShooting") ||
                        (GunAnimations.isAnimationPlaying(state.getController(), "shoot") ||
                                GunAnimations.isAnimationPlaying(state.getController(), "aim_shoot")) &&
                                !state.getController().getAnimationState().equals(AnimationController.State.PAUSED)) {
                    state.setControllerSpeed(GunEnchantmentHelper.getReloadAnimationSpeed(gunStack));
                    if (tag.getBoolean("IsAiming")) {
                        return state.setAndContinue(AIM_SHOOT);
                    } else {
                        return state.setAndContinue(SHOOT);
                    }
                } else {
                    if (tag.getBoolean("IsMeleeing") ||
                            (GunAnimations.isAnimationPlaying(state.getController(), "melee") ||
                                    GunAnimations.isAnimationPlaying(state.getController(), "bayonet")) &&
                                    !state.getController().getAnimationState().equals(AnimationController.State.PAUSED)) {

                        if ((Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem)) {
                            return state.setAndContinue(BAYONET);
                        } else {
                            return state.setAndContinue(MELEE);
                        }
                    }

                    if ((!ModSyncedDataKeys.RELOADING.getValue(player) &&
                            GunAnimations.isAnimationPlaying(state.getController(), "reload_loop")) ||
                            GunAnimations.isAnimationPlaying(state.getController(), "reload_stop")) {
                        state.setControllerSpeed(GunEnchantmentHelper.getReloadAnimationSpeed(gunStack));
                        return  state.setAndContinue(RELOAD_STOP);
                    }
                    if ((ModSyncedDataKeys.RELOADING.getValue(player) ||
                            ((GunAnimations.isAnimationPlaying(state.getController(), "reload") ||
                                    GunAnimations.isAnimationPlaying(state.getController(), "reload_alt") ||
                                    GunAnimations.isAnimationPlaying(state.getController(), "reload_start")) &&
                                    !state.getController().hasAnimationFinished()))) {
                        if (animatedGunItem.getModifiedGun(gunStack).getReloads().getReloadType().equals(ReloadType.MANUAL)) {
                            state.setControllerSpeed(GunEnchantmentHelper.getReloadAnimationSpeed(gunStack));
                            return state.setAndContinue(RELOAD_START);
                        }
                        state.setControllerSpeed(GunEnchantmentHelper.getReloadAnimationSpeed(gunStack));
                        if (gunStack.is(ModItems.INFANTRY_RIFLE.get())) {
                            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, gunStack).getItem() == ModItems.EXTENDED_MAG.get() ||
                                    Gun.getAttachment(IAttachment.Type.MAGAZINE, gunStack).getItem() == ModItems.DRUM_MAG.get()) {
                                return state.setAndContinue(RELOAD_ALT);
                            }
                        }
                        return state.setAndContinue(RELOAD);
                    }

                    if (tag.getBoolean("IsInspecting") ||
                            (GunAnimations.isAnimationPlaying(state.getController(), "inspect") &&
                                    !state.getController().hasAnimationFinished())) {
                        if (tag.getBoolean("IsAiming")) {
                            return state.setAndContinue(IDLE);
                        }
                        return state.setAndContinue(INSPECT);
                    }

                    if (tag.getBoolean("IsDrawing") ||
                            (GunAnimations.isAnimationPlaying(state.getController(), "draw") &&
                                    !state.getController().hasAnimationFinished())) {
                        state.setControllerSpeed(GunEnchantmentHelper.getReloadAnimationSpeed(gunStack));
                        return state.setAndContinue(DRAW);
                    } else {
                        if (tag.getBoolean("IsRunning") && !tag.getBoolean("IsAiming")) {
                            if (!(Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem)) {
                                return state.setAndContinue(SPRINT);
                            }
                        }
                    }
                }
            }

            state.setAndContinue(IDLE);

            return PlayState.CONTINUE;
        });
    }

    public static boolean isAnimationPlaying(AnimationController<GeoAnimatable> animationController, String animation) {
        return animationController.getCurrentAnimation().animation().name().equals(animation);
    }
}
