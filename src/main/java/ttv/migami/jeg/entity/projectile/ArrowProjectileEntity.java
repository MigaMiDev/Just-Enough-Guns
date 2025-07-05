package ttv.migami.jeg.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
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
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.item.GunItem;

import java.util.function.Predicate;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class ArrowProjectileEntity extends ProjectileEntity {
    private static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;

    private boolean flaming = false;
    private boolean charged = false;

	public ArrowProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
		super(entityType, worldIn);
	}

	public ArrowProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack stack, GunItem item, Gun modifiedGun) {
		super(entityType, worldIn, shooter, stack, item, modifiedGun);
        if (stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) != 0 || stack.getEnchantmentLevel(ModEnchantments.FIRE_STARTER.get()) != 0) {
            this.flaming = true;
        }
        if (this.chargeProgress == 1F) {
            this.charged = true;
        }
	}
	
	@Override
	protected void onProjectileTick() {
        if(this.level() instanceof ServerLevel serverLevel && (this.tickCount > 1 && this.tickCount < this.life)){

            SimpleParticleType lava = ParticleTypes.LAVA;
            if (this.flaming) {
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
            if (this.charged) {
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.CRIT,
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        1,
                        0, 0, 0,
                        0
                );
            }
            if (this.isUnderWater()) {
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.BUBBLE,
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        2,
                        0.1, 0.1, 0.1,
                        0
                );
            }
        }
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if(!this.level().isClientSide) {
			
			Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            
            HitResult result = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
			
            this.onHit(result, startVec, endVec);
            
		}
		
		
	}
	
	@Override
	protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
		super.onHitEntity(entity, hitVec, startVec, endVec, headshot);

        if (this.flaming) {
            entity.setSecondsOnFire(5);
        }
		
	}
	
	/**
     * Sets blocks on fire
     */
    private void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
		
		if(MinecraftForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this))) {
			
            return;
            
        }

        if(result instanceof BlockHitResult && this.flaming) {
        	
            BlockHitResult blockRayTraceResult = (BlockHitResult) result;
            
            if(blockRayTraceResult.getType() == HitResult.Type.MISS) {
                return;
            }

            Vec3 hitVec = result.getLocation();
            BlockPos pos = blockRayTraceResult.getBlockPos();

            if(Config.COMMON.gameplay.griefing.setFireToBlocks.get()) {

                BlockPos offsetPos = pos.relative(blockRayTraceResult.getDirection());

                if(BaseFireBlock.canBePlacedAt(this.level(), offsetPos, blockRayTraceResult.getDirection())) {

                    BlockState fireState = BaseFireBlock.getState(this.level(), offsetPos);
                    this.level().setBlock(offsetPos, fireState, 11);
                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.LAVA, hitVec.x - 1.0 + this.random.nextDouble() * 2.0, hitVec.y, hitVec.z - 1.0 + this.random.nextDouble() * 2.0, 4, 0, 0, 0, 0);

                }

            }
        }
	}
}