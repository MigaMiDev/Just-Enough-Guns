package ttv.migami.jeg.modifier.type;

import net.minecraft.world.effect.MobEffect;

public class PotionEffectModifier implements IModifierEffect {
    private final MobEffect effect;
    private final int duration; // in ticks
    private final int amplifier;

    public PotionEffectModifier(MobEffect effect, int duration, int amplifier) {
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public MobEffect getEffect() {
        return effect;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }
}