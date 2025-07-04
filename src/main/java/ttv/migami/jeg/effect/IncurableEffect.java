package ttv.migami.jeg.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.init.ModEffects;

import java.util.Collections;
import java.util.List;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

/**
 * Author: MrCrayfish
 */
public class IncurableEffect extends MobEffect
{
    public IncurableEffect(MobEffectCategory typeIn, int liquidColorIn)
    {
        super(typeIn, liquidColorIn);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();
        if (level instanceof ServerLevel serverLevel && !entity.isDeadOrDying()) {
            if (entity.hasEffect(ModEffects.RESONANCE.get())) {
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.ELECTRIC_SPARK,
                        true,
                        entity.getX(),
                        entity.getY() + entity.getEyeHeight(),
                        entity.getZ(),
                        1 + (1 * entity.getEffect(ModEffects.RESONANCE.get()).getAmplifier()),
                        entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2,
                        0
                );
                if (level.random.nextFloat() < 0.2F) {
                    serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.NOTE_BLOCK_CHIME.get(), SoundSource.PLAYERS, 2.0F, 0.7F + level.random.nextFloat());
                }
            }
        }
    }

    @Override
    public List<ItemStack> getCurativeItems()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
