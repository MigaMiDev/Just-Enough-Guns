package ttv.migami.jeg.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import ttv.migami.jeg.faction.Faction;
import ttv.migami.jeg.faction.GunnerManager;
import ttv.migami.jeg.init.ModCommands;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.item.GunItem;

import java.util.function.Predicate;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class FlareProjectileEntity extends ProjectileEntity {
    private static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;

    private boolean hasRaid = false;
    private String raid = null;
    private boolean terrorRaid = false;

    public FlareProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
		super(entityType, worldIn);
	}

	public FlareProjectileEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack stack, GunItem item, Gun modifiedGun) {
		super(entityType, worldIn, shooter, stack, item, modifiedGun);
        if (stack.getTag() != null && stack.getTag().getBoolean("HasTerrorRaid")) {
            this.terrorRaid = true;
            stack.getTag().putBoolean("HasRaid", false);
        } else if (stack.getTag() != null && stack.getTag().getBoolean("HasRaid")) {
            this.hasRaid = true;
            if (stack.getTag().contains("Raid")) {
                this.raid = stack.getTag().getString("Raid");
            }
            stack.getTag().putBoolean("HasRaid", false);
            stack.getTag().remove("Raid");
        }
	}
	
	@Override
	protected void onProjectileTick() {
        if (!this.level().isClientSide) {
            if (this.tickCount > 80) {
                if (this.hasRaid) {
                    GunnerManager gunnerManager = GunnerManager.getInstance();
                    Faction faction;
                    if (this.raid != null) {
                        faction = gunnerManager.getFactionByName(this.raid);

                        ModCommands.startRaid((ServerLevel) this.level(), faction, this.shooter.position(), true);
                    } else {
                        faction = gunnerManager.getFactionByName(gunnerManager.getRandomFactionName());

                        ModCommands.startRaid((ServerLevel) this.level(), faction, this.shooter.position(), true);
                    }
                    this.hasRaid = false;
                }
                if (this.terrorRaid) {
                    ModCommands.startTerrorRaid((ServerLevel) this.level(), this.shooter.position(), true, false);
                    this.terrorRaid = false;
                }
            }
        }
        if (this.level() instanceof ServerLevel serverLevel && (this.tickCount > 1 && this.tickCount < this.life)) {
            if (this.terrorRaid) {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.BLUE_FLARE.get(),
                        true,
                        this.getX() - this.getDeltaMovement().x(),
                        this.getY() - this.getDeltaMovement().y(),
                        this.getZ() - this.getDeltaMovement().z(),
                        1,
                        0, 0, 0,
                        0
                );
            } else {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.FLARE_SMOKE.get(),
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
        }
	}

    @Override
    public void tick() {
        super.tick();

        if(!this.level().isClientSide) {

            /*if (!this.raid) {
                this.raidTimer--;
            }
            if (this.getY() > 120 && this.raid) {
                this.raid = false;
            }
            if (this.raidTimer <= 0 && this.playRaid) {
                ServerLevel level = (ServerLevel) this.level();
                int x = level.random.nextInt(-50, 50);
                int z = level.random.nextInt(-50, 50);
                level.playSound(null, BlockPos.containing(this.getPosition(1F).add(x, -50, z)), ModSounds.DARK_HORN.get(), SoundSource.HOSTILE, 1000F, 1);
                for (ServerPlayer player : level.players()) {
                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 260, 0, true, false));
                }
                this.playRaid = false;
            }*/

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