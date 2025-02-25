package ttv.migami.jeg.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.event.GunProjectileHitEvent;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.item.GunItem;

import java.util.function.Predicate;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class BlazeProjectileEntity extends ProjectileEntity {

	private static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;

	public BlazeProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
		super(entityType, worldIn);
	}

	public BlazeProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack stack, GunItem item, Gun modifiedGun) {
		super(entityType, worldIn, shooter, stack, item, modifiedGun);
	}
	
	@Override
	protected void onProjectileTick() {
        if(this.level() instanceof ServerLevel serverLevel && (this.tickCount > 1 && this.tickCount < this.life)){

            SimpleParticleType lava = ParticleTypes.LAVA;
            if (getWeapon().getItem() == ModItems.SOULHUNTER_MK2.get()) {
                if (this.tickCount % 2 == 0) {
                    sendParticlesToAll(
                            serverLevel,
                            ParticleTypes.SOUL,
                            true,
                            this.getX() - this.getDeltaMovement().x(),
                            this.getY() - this.getDeltaMovement().y(),
                            this.getZ() - this.getDeltaMovement().z(),
                            1,
                            0, 0, 0,
                            0.05
                    );
                }
                if (this.tickCount % 3 == 0) {
                    lava = ModParticleTypes.SOUL_LAVA_PARTICLE.get();
                } else {
                    lava = ParticleTypes.LAVA;
                }
            }

            for(int i = 0; i < 3; i++) {
                sendParticlesToAll(
                        serverLevel,
                        lava,
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        1,
                        0, 0, 0,
                        0
                );
            }
            sendParticlesToAll(
                    serverLevel,
                    ParticleTypes.ASH,
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
                    ParticleTypes.WHITE_ASH,
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
	
	@Override
	public void tick() {
		super.tick();

		if(!this.level().isClientSide) {

            if (this.isUnderWater()) {
                sendParticlesToAll(
                        (ServerLevel) this.level(),
                        ParticleTypes.CLOUD,
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        3,
                        0.1, 0.1, 0.1,
                        0.05
                );
                this.level().playSound(this, this.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1F, 1F);
                this.discard();
            }

			Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            
            HitResult result = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
			
            this.onHit(result, startVec, endVec);
            
		}
		
		
	}
	
	@Override
	protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
		super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
		entity.setSecondsOnFire(5);
	}
	
	/**
	 * Sets blocks on fire
	 */
	private void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
		
		if(MinecraftForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this))) {
			
            return;
            
        }

        if(result instanceof BlockHitResult) {
        	
            BlockHitResult blockRayTraceResult = (BlockHitResult) result;
            
            if(blockRayTraceResult.getType() == HitResult.Type.MISS) {
                return;
            }

            Vec3 hitVec = result.getLocation();
            BlockPos pos = blockRayTraceResult.getBlockPos();

            if(Config.COMMON.gameplay.griefing.setFireToBlocks.get()) {

                BlockPos offsetPos = pos.relative(blockRayTraceResult.getDirection());

                if(this.level().getRandom().nextFloat() > 0.50F) { // 50% chance of setting the block on fire
                    if(BaseFireBlock.canBePlacedAt(this.level(), offsetPos, blockRayTraceResult.getDirection())) {

                        BlockState fireState = BaseFireBlock.getState(this.level(), offsetPos);
                        this.level().setBlock(offsetPos, fireState, 11);
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.LAVA, hitVec.x - 1.0 + this.random.nextDouble() * 2.0, hitVec.y, hitVec.z - 1.0 + this.random.nextDouble() * 2.0, 4, 0, 0, 0, 0);

                    }
                }

            }
        }
	}


}