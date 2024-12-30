package ttv.migami.jeg.entity.throwable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.init.ModEntities;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;

import static ttv.migami.jeg.entity.projectile.ProjectileEntity.createExplosion;

@Mod.EventBusSubscriber
public class ThrowableExplosiveChargeEntity extends ThrowableGrenadeEntity {
    private LivingEntity targetEntity;
    private boolean hasStartedRiding = false;
    private long rideStartTime;
    private int heartBeat = 20;
    private int fuse = 200;

    private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(ThrowableExplosiveChargeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_LANDED = SynchedEntityData.defineId(ThrowableExplosiveChargeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEFUSED = SynchedEntityData.defineId(ThrowableExplosiveChargeEntity.class, EntityDataSerializers.BOOLEAN);

    public void defuse() {
        this.setDefused(true);
    }

    public ThrowableExplosiveChargeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world) {
        super(entityType, world);
        this.setMaxLife(72000);
        this.setSticky(true);
    }

    public ThrowableExplosiveChargeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player) {
        super(entityType, world, player);
        this.setItem(new ItemStack(ModItems.EXPLOSIVE_CHARGE.get()));
        this.setMaxLife(72000);
        this.setSticky(true);
    }

    public ThrowableExplosiveChargeEntity(Level world, LivingEntity player, int maxCookTime) {
        super(ModEntities.THROWABLE_EXPLOSIVE_CHARGE.get(), world, player);
        this.setItem(new ItemStack(ModItems.EXPLOSIVE_CHARGE.get()));
        this.setMaxLife(maxCookTime);
        this.setSticky(true);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSticky(true);

        if (this.level().isClientSide) {
            if (this.hasLanded()) {
                this.setPos(this.getX(), this.getY(), this.getZ());
            }
        }

        if (!this.level().isClientSide) {
            this.entityData.set(FUSE, this.fuse);
            this.entityData.set(HAS_LANDED, this.hasLanded);
        } else {
            this.fuse = this.entityData.get(FUSE);
            this.hasLanded = this.entityData.get(HAS_LANDED);
        }

        if (!this.level().isClientSide) {
            if (this.hasLanded()) {
                this.heartBeat--;
                if (this.heartBeat <= 0) {
                    this.level().playSound(null, this.getOnPos(), ModSounds.BEEP.get(), SoundSource.HOSTILE, 0.6F, 1.0F);
                    this.heartBeat = 20;
                }
                this.fuse--;
            }

            if (this.fuse <= 0) {
                this.setDefused(false);
                createExplosion(this, Config.COMMON.missiles.explosionRadius.get().floatValue() / 2, false);
                this.discard();
            }
        }

        if (this.isDefused()) {
            this.discard();
        }
    }

    @Override
    public void onRemovedFromWorld() {
        if (this.level().isClientSide && !this.isDefused()) {
            double posX = this.getX() - this.getDeltaMovement().x();
            double posY = this.getY() - this.getDeltaMovement().y();
            double posZ = this.getZ() - this.getDeltaMovement().z();

            this.level().addParticle(ModParticleTypes.BIG_EXPLOSION.get(), true, posX, posY + 2, posZ, 0, 0, 0);
            for (int i = 0; i < 10; i++) {
                double xSpeed = ((random.nextDouble() - 0.5) * 0.5) * 10;
                double ySpeed = ((random.nextDouble() - 0.5) * 0.5) * 10;
                double zSpeed = ((random.nextDouble() - 0.5) * 0.5) * 10;

                this.level().addParticle(ModParticleTypes.SMOKE.get(), true, posX, posY + 2, posZ, xSpeed, ySpeed, zSpeed);
                this.level().addParticle(ModParticleTypes.FIRE.get(), true, posX, posY + 2, posZ, xSpeed, ySpeed, zSpeed);
            }
        }
    }

    @Override
    public void particleTick() {
        if (this.level().isClientSide && this.tickCount > 20) {
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.LAVA, true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
            }
        }
    }

    public boolean isDefused() {
        return this.entityData.get(DEFUSED);
    }

    public void setDefused(boolean value) {
        this.entityData.set(DEFUSED, value);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FUSE, 200);
        this.entityData.define(HAS_LANDED, false);
        this.entityData.define(DEFUSED, true);
    }
}