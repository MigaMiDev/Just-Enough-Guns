package ttv.migami.jeg.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModEffects;
import ttv.migami.jeg.item.GunItem;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class ResonanceProjectileEntity extends ProjectileEntity {
	public ResonanceProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
		super(entityType, worldIn);
	}

	public ResonanceProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack stack, GunItem item, Gun modifiedGun) {
		super(entityType, worldIn, shooter, stack, item, modifiedGun);
	}

	@Override
	protected void onProjectileTick() {
		if(this.level() instanceof ServerLevel serverLevel && (this.tickCount > 1 && this.tickCount < this.life)){
			sendParticlesToAll(
					serverLevel,
					ParticleTypes.ELECTRIC_SPARK,
					true,
					this.getX() - this.getDeltaMovement().x(),
					this.getY() - this.getDeltaMovement().y(),
					this.getZ() - this.getDeltaMovement().z(),
					2,
					0.1, 0.1, 0.1,
					0.05
			);
		}
	}
	
	@Override
	protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
		super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
		if (entity instanceof LivingEntity livingEntity) {
            int resonanceLevel = 0;
            if (livingEntity.hasEffect(ModEffects.RESONANCE.get())) {
                resonanceLevel = livingEntity.getEffect(ModEffects.RESONANCE.get()).getAmplifier() + 1;
				if (resonanceLevel > Config.COMMON.gameplay.maxResonanceLevel.get()) resonanceLevel = Config.COMMON.gameplay.maxResonanceLevel.get();
            }
            livingEntity.addEffect(new MobEffectInstance(ModEffects.RESONANCE.get(), 20 + (20 * resonanceLevel), resonanceLevel, false, false));
        }
	}
}