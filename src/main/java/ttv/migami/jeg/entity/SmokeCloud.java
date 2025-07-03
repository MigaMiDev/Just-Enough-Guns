package ttv.migami.jeg.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import ttv.migami.jeg.init.ModEffects;

public class SmokeCloud extends AreaEffectCloud
{
    private static final int fireCheckInterval = 5;
    private int checkTimer = 0;

    public SmokeCloud(Level world, double x, double y, double z, SimpleParticleType particle, float radius, int duration)
    {
        super(world, x, y, z);
        this.setParticle(particle);
        this.setRadius(radius);
        this.setDuration(duration);
        this.addEffect(new MobEffectInstance(ModEffects.SMOKED.get(), 30, 0, false, false, true));
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!this.level().isClientSide)
        {
            this.checkTimer++;
            if (this.checkTimer >= fireCheckInterval)
            {
                this.checkTimer = 0;
                if (this.extinguishNearbyFire())
                {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
    }

    private boolean extinguishNearbyFire()
    {
        boolean extinguishedAny = false;
        float radius = this.getRadius() * 2F;
        int radiusCeil = Mth.ceil(radius);
        int centerX = Mth.floor(this.getX());
        int centerY = Mth.floor(this.getY());
        int centerZ = Mth.floor(this.getZ());
        double radiusSq = radius * radius;

        BlockPos minPos = new BlockPos(
                centerX - radiusCeil,
                centerY - 1,
                centerZ - radiusCeil
        );
        BlockPos maxPos = new BlockPos(
                centerX + radiusCeil,
                centerY + 2,
                centerZ + radiusCeil
        );

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos))
        {
            double dx = pos.getX() - centerX;
            double dy = pos.getY() - centerY;
            double dz = pos.getZ() - centerZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= radiusSq && this.level().getBlockState(pos).is(Blocks.FIRE))
            {
                this.level().removeBlock(pos, false);
                extinguishedAny = true;
            }
        }

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius)))
        {
            double dx = entity.getX() - this.getX();
            double dy = entity.getY() - this.getY();
            double dz = entity.getZ() - this.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= radiusSq && entity.getRemainingFireTicks() > 0)
            {
                entity.setRemainingFireTicks(0);
                extinguishedAny = true;
            }
        }

        return extinguishedAny;
    }
}