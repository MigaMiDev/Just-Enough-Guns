package ttv.migami.jeg.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.faction.raid.RaidEntity;
import ttv.migami.jeg.init.ModTags;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.item.GunItem;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

/**
 * Author: MrCrayfish
 */
public class RocketEntity extends ProjectileEntity
{
    private boolean explosionScheduled = false;
    private boolean explosionForceNone = false;
    private boolean rocketRide = false;
    public RocketEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public RocketEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        if (shooter.isCrouching()) {
            rocketRide = true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (explosionScheduled) {
            createExplosion(this, Config.COMMON.missiles.explosionRadius.get().floatValue(), explosionForceNone);
            this.remove(RemovalReason.KILLED);
            explosionScheduled = false;
        }
    }

    @Override
    protected void onProjectileTick()
    {
        if (this.level() instanceof ServerLevel serverLevel && (this.tickCount > 1 && this.tickCount < this.life)) {
            for (int i = 5; i > 0; i--)
            {
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        1,
                        0, 0, 0,
                        0
                );
            }
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

    @Override
    protected void impactEffect() {
        if (this.level() instanceof ServerLevel serverLevel) {
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
        if (this.level().isClientSide) {
            double posX = this.getX() - this.getDeltaMovement().x();
            double posY = this.getY() - this.getDeltaMovement().y();
            double posZ = this.getZ() - this.getDeltaMovement().z();

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
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
    {
        if (entity instanceof RaidEntity) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        boolean forceNone = !(this.getShooter() instanceof Player);
        if ((!entity.getType().is(ModTags.Entities.HEAVY) && !entity.getType().is(ModTags.Entities.VERY_HEAVY))) {
            if (this.tickCount <= 5 && this.getPassengers().isEmpty() && this.rocketRide) {
                entity.startRiding(this);
            }
            else if (this.getPassengers().isEmpty()) {
                createExplosion(this, Config.COMMON.missiles.explosionRadius.get().floatValue(), forceNone);
                this.remove(RemovalReason.KILLED);
            }
        }
        else {
            createExplosion(this, Config.COMMON.missiles.explosionRadius.get().floatValue(), forceNone);
            this.remove(RemovalReason.KILLED);
        }
    }

    public void setExplosionScheduled(boolean scheduled, boolean forceNone) {
        this.explosionScheduled = scheduled;
        this.explosionForceNone = forceNone;
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
    {
        boolean forceNone = !(this.getShooter() instanceof Player);
        createExplosion(this, Config.COMMON.missiles.explosionRadius.get().floatValue(), forceNone);
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public void onExpired()
    {
        boolean forceNone = !(this.getShooter() instanceof Player);
        createExplosion(this, Config.COMMON.missiles.explosionRadius.get().floatValue(), forceNone);
        this.remove(RemovalReason.KILLED);
    }
}
