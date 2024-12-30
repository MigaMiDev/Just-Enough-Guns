package ttv.migami.jeg.entity.throwable;

import net.minecraft.server.level.ServerLevel;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.entity.projectile.ProjectileEntity;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.item.GunItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

/**
 * Author: MrCrayfish
 */
public class GrenadeEntity extends ProjectileEntity
{
    public GrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    public GrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        super(entityType, world, shooter, weapon, item, modifiedGun);
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
        createExplosion(this, this.getDamage() / 5F, true);
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
    {
        createExplosion(this, this.getDamage() / 5F, true);
    }

    @Override
    public void onExpired()
    {
        createExplosion(this, this.getDamage() / 5F, true);
    }
}
