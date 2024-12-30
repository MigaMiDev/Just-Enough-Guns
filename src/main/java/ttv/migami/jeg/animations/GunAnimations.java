package ttv.migami.jeg.animations;

import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.RawAnimation;

public final class GunAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation HOLD_FIRE = RawAnimation.begin().then("hold_fire", Animation.LoopType.HOLD_ON_LAST_FRAME);
    public static final RawAnimation SHOOT = RawAnimation.begin().then("shoot", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
    public static final RawAnimation AIM_SHOOT = RawAnimation.begin().then("aim_shoot", Animation.LoopType.PLAY_ONCE).thenLoop("idle");
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

}
