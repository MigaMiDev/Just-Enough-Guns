package ttv.migami.jeg.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.network.ServerPlayHandler;
import ttv.migami.jeg.init.ModEntities;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

/**
 * Author: MrCrayfish
 */
public class ThrowableGrenadeEntity extends ThrowableItemEntity
{
    public float rotation;
    public float prevRotation;
    public boolean terrorPhantomThrown = false;
    public boolean playerOwnedTerrorPhantom = false;

    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity entity)
    {
        super(entityType, world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(ModItems.GRENADE.get()));
        this.setMaxLife(20 * 3);
        this.setOwner(entity);
    }

    public ThrowableGrenadeEntity(Level world, LivingEntity entity, int timeLeft)
    {
        super(ModEntities.THROWABLE_GRENADE.get(), world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(ModItems.GRENADE.get()));
        this.setMaxLife(timeLeft);
        this.setOwner(entity);
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    public void tick()
    {
        super.tick();
        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        /*if (speed > 0.1)
        {
            this.rotation += speed * 50;
        }*/
        particleTick();

        if (this.getOwner() instanceof LivingEntity livingEntity) {
            ServerPlayHandler.doPanicVillagersAndHostiles(livingEntity, this.position(), 3);
        }
    }

    public void particleTick()
    {
        if (this.level().isClientSide)
        {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
        }
        if (this.level() instanceof ServerLevel serverLevel && (this.tickCount > 1)) {
            if (this.terrorPhantomThrown) {
                if (this.level().random.nextInt(2) == 0)
                {
                    sendParticlesToAll(
                            serverLevel,
                            ModParticleTypes.FIRE.get(),
                            true,
                            this.getX() - this.getDeltaMovement().x(),
                            this.getY() - this.getDeltaMovement().y(),
                            this.getZ() - this.getDeltaMovement().z(),
                            1,
                            0, 0, 0,
                            0
                    );
                    sendParticlesToAll(
                            serverLevel,
                            ParticleTypes.LAVA,
                            true,
                            this.getX() - this.getDeltaMovement().x(),
                            this.getY() - this.getDeltaMovement().y(),
                            this.getZ() - this.getDeltaMovement().z(),
                            1,
                            0, 0, 0,
                            0
                    );
                    sendParticlesToAll(
                            serverLevel,
                            ParticleTypes.FLAME,
                            true,
                            this.getX() - this.getDeltaMovement().x(),
                            this.getY() - this.getDeltaMovement().y(),
                            this.getZ() - this.getDeltaMovement().z(),
                            1,
                            0, 0, 0,
                            0
                    );
                }
            }
        }
    }

    @Override
    public void onDeath()
    {
        if (this.playerOwnedTerrorPhantom) {
            if (this.level() instanceof ServerLevel serverLevel) {
                double radius = 10.0;
                double maxDamage = 80.0;
                AABB area = new AABB(this.blockPosition()).inflate(radius);
                serverLevel.playSound(this, this.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5.0F, 1.0F);
                for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
                    if (!(entity instanceof Enemy) && !(entity instanceof Player)) {
                        continue;
                    }

                    double distance = this.position().distanceTo(entity.position());
                    if (distance <= radius) {
                        double damage = maxDamage * (1.0 - (distance / radius));
                        entity.hurt(entity.damageSources().explosion(this.getOwner(), this.getOwner()), (float) damage);
                    }
                }

                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.BIG_EXPLOSION.get(),
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        1,
                        0, 0, 0,
                        0
                );
            }
        } else {
            if (this.terrorPhantomThrown && Config.COMMON.gunnerMobs.terrorPhantomDestroyBlocks.get())
            {
                GrenadeEntity.createExplosion(this, Config.COMMON.grenades.explosionRadius.get().floatValue(), false);
            }
            else {
                GrenadeEntity.createExplosion(this, Config.COMMON.grenades.explosionRadius.get().floatValue(), true);
            }
        }
    }
}
