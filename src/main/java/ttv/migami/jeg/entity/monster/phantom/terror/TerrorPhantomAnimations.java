package ttv.migami.jeg.entity.monster.phantom.terror;

import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class TerrorPhantomAnimations {
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    public static final RawAnimation ROLL = RawAnimation.begin().thenPlay("roll").thenLoop("idle");
    public static final RawAnimation DYING = RawAnimation.begin().thenPlay("dying");

    /**
     * Generic {@link DefaultAnimations#WALK walk} + {@link DefaultAnimations#IDLE idle} controller.<br>
     * Will play the walk animation if the animatable is considered moving, or idle if not
     */
    public static <T extends GeoAnimatable> AnimationController<TerrorPhantom> genericIdleController(TerrorPhantom animatable) {
        return new AnimationController<>(animatable, "Idle", 0, state -> {
            if (animatable.isDying()) {
                return state.setAndContinue(DYING);
            }
            else if (animatable.isRolling()) {
                return state.setAndContinue(ROLL);
            }

            state.setAndContinue(IDLE);

            return PlayState.CONTINUE;
        });
    }
}