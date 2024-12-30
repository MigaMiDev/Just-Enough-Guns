package ttv.migami.jeg.entity.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.item.GunItem;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class SpectreProjectileEntity extends ProjectileEntity {

    public SpectreProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public SpectreProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick()
    {
        if (this.level() instanceof ServerLevel serverLevel && (this.tickCount > 1 && this.tickCount < this.life)) {
            sendParticlesToAll(
                    serverLevel,
                    ModParticleTypes.GHOST_GLINT.get(),
                    true,
                    this.getX() - this.getDeltaMovement().x(),
                    this.getY() - this.getDeltaMovement().y(),
                    this.getZ() - this.getDeltaMovement().z(),
                    2,
                    0.1, 0.1, 0.1,
                    0.5
            );
        }
    }

}
