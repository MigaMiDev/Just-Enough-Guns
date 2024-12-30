package ttv.migami.jeg.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.init.ModEffects;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class KillEffectEffect extends MobEffect
{
    public KillEffectEffect(MobEffectCategory typeIn, int liquidColorIn)
    {
        super(typeIn, liquidColorIn);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level instanceof ServerLevel serverLevel) {
            if (entity.hasEffect(ModEffects.POPPED.get())) {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.POPCORN.get(),
                        true,
                        entity.getX(),
                        entity.getY() + entity.getEyeHeight(),
                        entity.getZ(),
                        1,
                        entity.getBbWidth() / 1.5, entity.getBbHeight() - entity.getEyeHeight(), entity.getBbWidth() / 1.5,
                        0
                );
                if (level.random.nextBoolean()) {
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.BEEHIVE_EXIT, SoundSource.PLAYERS, 5.0F, 0.5F + level.random.nextFloat());
                }
            }
            if (entity.hasEffect(ModEffects.TRICKSHOTTED.get())) {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.HIT_MARKER.get(),
                        true,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        1,
                        entity.getBbWidth() / 1.2, entity.getBbHeight(), entity.getBbWidth() / 1.2,
                        0
                );
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSounds.HIT_MARKER.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                if (level.random.nextFloat() < 0.2) {
                    if (level.random.nextFloat() < 0.1) {
                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                ModSounds.GOOSE.get(), SoundSource.PLAYERS, 5.0F, 1.0F);
                    } else {
                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                ModSounds.AIR_HORN.get(), SoundSource.PLAYERS, 5.0F, 1.0F);
                    }
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
