package ttv.migami.jeg.entity.throwable;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.entity.SmokeCloud;
import ttv.migami.jeg.init.ModEntities;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.S2CMessageSmokeGrenade;

/**
 * Author: An0m3l1
 */
public class ThrowableSmokeGrenadeEntity extends ThrowableGrenadeEntity
{
    public ThrowableSmokeGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    public ThrowableSmokeGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player)
    {
        super(entityType, world, player);
        this.setItem(new ItemStack(ModItems.SMOKE_GRENADE.get()));
    }

    public ThrowableSmokeGrenadeEntity(Level world, LivingEntity player, int timeLeft)
    {
        super(ModEntities.THROWABLE_SMOKE_GRENADE.get(), world, player);
        this.setItem(new ItemStack(ModItems.SMOKE_GRENADE.get()));
        this.setMaxLife(timeLeft);
    }

    @Override
    public void tick()
    {
        super.tick();
        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        if (speed > 0.1)
        {
            this.rotation += (speed * 50);
        }
        if (this.level().isClientSide)
        {
            this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), (Math.random()-0.5) * 0.1, 0.1, (Math.random()-0.5) * 0.1);
        }
    }

    @Override
    protected void onHit(HitResult result)
    {
        if (result.getType() == HitResult.Type.BLOCK && !this.level().isClientSide)
        {
            double radius = Config.COMMON.smokeGrenades.smokeGrenadeCloudDiameter.get() * 0.75;
            if (radius > 0)
            {
                BlockPos grenadePos = new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
                int radiusInt = (int) Math.ceil(radius);
                boolean foundFire = false;

                for (int x = -radiusInt; x <= radiusInt; x++)
                {
                    for (int z = -radiusInt; z <= radiusInt; z++)
                    {
                        double distanceSq = x * x + z * z;
                        if (distanceSq <= radius * radius)
                        {
                            for (int y = -2; y <= 2; y++)
                            {
                                BlockPos checkPos = grenadePos.offset(x, y, z);
                                if (this.level().getBlockState(checkPos).is(BlockTags.FIRE))
                                {
                                    foundFire = true;
                                    break;
                                }
                            }
                            if (foundFire) break;
                        }
                    }
                    if (foundFire) break;
                }

                if (foundFire)
                {
                    this.remove(Entity.RemovalReason.KILLED);
                    this.onDeath();
                    return;
                }
            }
        }
        super.onHit(result);
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        double radius = Config.COMMON.smokeGrenades.smokeGrenadeCloudDiameter.get() * 0.5;
        double duration = ((Config.COMMON.smokeGrenades.smokeGrenadeCloudDuration.get() - 3) * 20);
        @NotNull SimpleParticleType particle = ModParticleTypes.SMOKE_EFFECT.get();
        this.level().playSound(null, this.getX(), y, this.getZ(), ModSounds.ENTITY_SMOKE_GRENADE_EXPLOSION.get(), SoundSource.BLOCKS, 4, 1);
        if(!this.level().isClientSide)
        {
            SmokeCloud cloudLow = new SmokeCloud(this.level(), this.getX(), this.getY() - 0.5, this.getZ(), particle, (float) radius, (int) duration);
            this.level().addFreshEntity(cloudLow);

            SmokeCloud cloudMid = new SmokeCloud(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), particle, (float) radius, (int) duration);
            this.level().addFreshEntity(cloudMid);

            SmokeCloud cloudHigh = new SmokeCloud(this.level(), this.getX(), this.getY() + 1.5, this.getZ(), particle, (float) radius, (int) duration);
            this.level().addFreshEntity(cloudHigh);

            SmokeCloud cloudHighest = new SmokeCloud(this.level(), this.getX(), this.getY() + 2.5, this.getZ(), particle, (float) radius, (int) duration);
            this.level().addFreshEntity(cloudHighest);
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.level(), this.getX(), y, this.getZ(), 256), new S2CMessageSmokeGrenade(this.getX(), y, this.getZ()));
    }
}