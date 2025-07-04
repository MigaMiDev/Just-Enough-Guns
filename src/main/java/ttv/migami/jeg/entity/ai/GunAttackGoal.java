package ttv.migami.jeg.entity.ai;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.event.GunEventBus;
import ttv.migami.jeg.init.ModEffects;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.GunItem;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;
import static ttv.migami.jeg.event.GunEventBus.ejectCasing;

public class GunAttackGoal<T extends PathfinderMob> extends Goal {
    protected final T shooter;
    protected final double speedModifier;
    protected int seeTime;
    protected int attackTime;
    protected final float attackRadiusSqr;
    protected boolean strafingClockwise;
    protected boolean strafingBackwards;
    protected int strafingTime;

    protected int burstIntervalTimer = 0;
    protected int remainingBursts = 0;
    protected int burstResetTimer = 0;

    protected int reloadTick = 0;
    protected boolean isReloading = false;

    protected boolean isPanicked = false;
    protected int panickTimer = 0;

    protected AIType aiType = AIType.TACTICAL;

    protected Vec3 lastKnownPosition;

    protected float spreadModifier = 10;
    protected int burstAmount = 3;
    protected int burstTimer = 20;

    public GunAttackGoal(T shooter, double stopRange, float speedModifier, AIType aiType, int difficulty) {
        this.shooter = shooter;
        this.speedModifier = speedModifier;
        this.attackTime = -1;
        this.attackRadiusSqr = (float) (stopRange * stopRange);
        this.aiType = aiType;
        if (this.shooter.getTarget() != null) {
            this.lastKnownPosition = this.shooter.getTarget().position();
        }

        this.spreadModifier /= difficulty;
        this.burstAmount *= difficulty;
        this.burstTimer /= difficulty;
    }

    @Override
    public boolean canUse() {
        return this.shooter.getTarget() != null && this.isHoldingGun() && !this.shooter.getTarget().isDeadOrDying();
    }

    protected boolean isHoldingGun() {
        return this.shooter.isHolding((itemStack) -> itemStack.getItem() instanceof GunItem);
    }

    @Override
    public void start() {
        super.start();
        this.shooter.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.shooter.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.shooter.stopUsingItem();
        this.reloadTick = 0;
        this.isReloading = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.shooter.getTarget();
        ItemStack heldItem = this.shooter.getMainHandItem();

        if (this.shooter.hasEffect(ModEffects.BLINDED.get()) || this.shooter.hasEffect(ModEffects.DEAFENED.get()))
            this.isPanicked = true;

        if (target != null && heldItem.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(heldItem);

            double distanceToTarget = this.shooter.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean canSeeTarget = this.shooter.getSensing().hasLineOfSight(target);
            boolean sawTargetPreviously = this.seeTime > 0;

            if (canSeeTarget != sawTargetPreviously) {
                this.seeTime = 0;
            }

            if (this.isReloading) {
                ++this.seeTime;
            }
            else if (canSeeTarget) {
                this.lastKnownPosition = new Vec3(target.getX(), target.getY(), target.getZ());
                ++this.seeTime;
            } else {
                if (this.aiType == AIType.TACTICAL) {
                    if (this.lastKnownPosition != null) {
                        Vec3 flankingPosition = findFlankingPosition(this.lastKnownPosition, target);
                        if (flankingPosition != null) {
                            this.shooter.getNavigation().moveTo(flankingPosition.x, flankingPosition.y, flankingPosition.z, this.speedModifier);
                        }
                    }
                }
                --this.seeTime;
            }

            // Panic State
            if (this.aiType == AIType.COWARD &&
                    (this.shooter.getHealth() < (this.shooter.getMaxHealth() / 3) || this.shooter.invulnerableTime != 0) ||
                    this.shooter.hasEffect(ModEffects.BLINDED.get())) {
                this.isPanicked = true;
                this.panickTimer = 20;
            }
            if (this.isPanicked) {
                Vec3 vec3 = DefaultRandomPos.getPos(this.shooter, 5, 4);
                if (vec3 != null) {
                    this.shooter.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
                }
                this.panickTimer--;
            }
            if (this.panickTimer <= 0) {
                this.isPanicked = false;
            }

            // Reloading
            if (heldItem.getTag().getInt("AmmoCount") <= 0) {
                if (!this.isReloading) {
                    if (this.aiType != AIType.RECKLESS) {
                        Vec3 coverLocation = findCoverLocation();
                        this.shooter.getNavigation().moveTo(coverLocation.x, coverLocation.y, coverLocation.z, 1.2D);
                    }
                    this.isReloading = true;
                    this.reloadTick = gun.getReloads().getReloadTimer();
                    this.shooter.level().playSound(null, this.shooter.getX(), this.shooter.getY(), this.shooter.getZ(),
                            ModSounds.ITEM_PISTOL_RELOAD.get(), SoundSource.HOSTILE, 1.0F, 1F);
                } else if (this.reloadTick == 0) {
                    heldItem.getTag().putInt("AmmoCount", gun.getReloads().getMaxAmmo());
                    this.shooter.level().playSound(null, this.shooter.getX(), this.shooter.getY(), this.shooter.getZ(),
                            ModSounds.ITEM_PISTOL_COCK.get(), SoundSource.HOSTILE, 1.0F, 1F);
                    this.isReloading = false;
                } else {
                    --this.reloadTick;
                }
            }

            // Flanking
            if (this.shooter.level().random.nextFloat() < 0.1 && this.aiType == AIType.TACTICAL) {
                Vec3 flankingPosition = findFlankingPosition(this.lastKnownPosition, target);
                if (flankingPosition != null) {
                    this.shooter.getNavigation().moveTo(flankingPosition.x, flankingPosition.y, flankingPosition.z, this.speedModifier);
                }
            }

            // Shooting & Strafing
            if (!this.isReloading && !this.isPanicked) {

                if (distanceToTarget <= this.attackRadiusSqr && this.seeTime >= 20) {
                    this.shooter.getNavigation().stop();
                    ++this.strafingTime;
                } else if (this.aiType == AIType.RECKLESS) {
                    this.shooter.getNavigation().moveTo(target, this.speedModifier);
                    this.strafingTime = -1;
                } else  {
                    this.shooter.getNavigation().moveTo(target, 1.0F);
                    this.strafingTime = -1;
                }

                if (this.strafingTime >= 20) {
                    if (this.shooter.getRandom().nextFloat() < 0.3) {
                        this.strafingClockwise = !this.strafingClockwise;
                    }

                    if (this.shooter.getRandom().nextFloat() < 0.3) {
                        this.strafingBackwards = !this.strafingBackwards;
                    }

                    this.strafingTime = 0;
                }

                if (this.strafingTime > -1) {
                    if (distanceToTarget > (double)(this.attackRadiusSqr * 0.75F)) {
                        this.strafingBackwards = false;
                    } else if (distanceToTarget < (double)(this.attackRadiusSqr * 0.25F)) {
                        this.strafingBackwards = true;
                    }

                    this.shooter.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                }

                // Shooting
                if (this.shooter.getMainHandItem().getTag().getInt("AmmoCount") > 0) {
                    if (--this.attackTime <= 0 && this.seeTime >= -20 && this.seeTime >= 10) {
                        if (remainingBursts <= 0 && burstResetTimer <= 0) {
                            remainingBursts = 1 + this.shooter.level().random.nextInt(this.burstAmount);
                            burstIntervalTimer = 1 + this.shooter.level().random.nextInt(this.burstTimer); // Random interval between 1 and 21 ticks
                            burstResetTimer = 20 + this.shooter.level().random.nextInt(40); // Wait 20-60 ticks before next burst sequence
                        }

                        if (this.shooter.hasEffect(ModEffects.BLINDED.get()) && (!this.aiType.equals(AIType.DEFAULT)) && !this.aiType.equals(AIType.TACTICAL)) {
                            burstResetTimer = 0;
                        }

                        if (remainingBursts > 0 && --burstIntervalTimer <= 0) {
                            if (this.shooter.getMainHandItem().getItem() instanceof GunItem) {
                                shoot(target, gun);
                            }
                            remainingBursts--;
                            burstIntervalTimer = 1 + this.shooter.level().random.nextInt(10); // Reset interval for the next burst shot
                        }

                        if (remainingBursts <= 0) {
                            burstResetTimer--;
                        }
                    }
                }

                double targetEyeY = target.getEyeY();
                this.shooter.getLookControl().setLookAt(target.getX(), targetEyeY, target.getZ());
                this.shooter.lookAt(EntityAnchorArgument.Anchor.FEET, target.getBoundingBox().getCenter());

                if (this.shooter.getMainHandItem().getItem() == ModItems.BOLT_ACTION_RIFLE.get() && this.shooter.getTarget() != null && canSeeTarget) {
                    Vec3 userPos = this.shooter.getPosition(1F).add(0, this.shooter.getTarget().getEyeHeight() / 1.25, 0);
                    //Vec3 targetPos = this.shooter.getTarget().getPosition(1F).add(0, this.shooter.getTarget().getEyeHeight(), 0);
                    Vec3 targetPos = this.shooter.getTarget().getPosition(1F).add(0, this.shooter.getTarget().getEyeHeight() / 2, 0);
                    Vec3 distanceTo = targetPos.subtract(userPos);
                    Vec3 normal = distanceTo.normalize();

                    if(!this.shooter.level().isClientSide()) {
                        for(float i = 1; i < Mth.floor(distanceTo.length()); i += 0.2F) {
                            Vec3 vec33 = userPos.add(normal.scale((double)i));
                            if (this.shooter.level() instanceof ServerLevel serverLevel) {
                                sendParticlesToAll(
                                        serverLevel,
                                        ModParticleTypes.ENTITY_LASER.get(),
                                        true,
                                        vec33.x(),
                                        vec33.y(),
                                        vec33.z(),
                                        1,
                                        0, 0, 0,
                                        0
                                );
                            }
                        }
                    }
                }
            }

            // Stare if Tactical
            if (this.aiType == AIType.TACTICAL && !this.isPanicked && !this.isReloading) {
                double targetEyeY = target.getEyeY();
                this.shooter.getLookControl().setLookAt(target.getX(), targetEyeY, target.getZ());
                this.shooter.lookAt(EntityAnchorArgument.Anchor.FEET, target.getBoundingBox().getCenter());
            }

            // Emote Bubbles
            if((this.isReloading && heldItem.getTag().getInt("AmmoCount") <= 0) && this.shooter.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticleTypes.BUBBLE_AMMO.get(), this.shooter.getX(), this.shooter.getY() + (this.shooter.getEyeHeight() + 0.9), this.shooter.getZ(), 1, 0, 0, 0, 0);
            }
        }
    }

    private void shoot(LivingEntity target, Gun gun) {
        if (this.shooter.hasEffect(ModEffects.SMOKED.get()) || target.hasEffect(ModEffects.SMOKED.get())) {
            if (this.shooter.getRandom().nextBoolean()) {
                return;
            }
        }

        ItemStack heldItem = this.shooter.getMainHandItem();
        if (heldItem.getItem() == ModItems.SUPERSONIC_SHOTGUN.get()) {
            GunEventBus.soundwaveBlast(this.shooter.level(), this.shooter, Gun.getAdditionalDamage(heldItem), gun);
        } else {
            AIGunEvent.performGunAttack(this.shooter, target, heldItem, gun, this.spreadModifier);
        }
        this.attackTime = gun.getGeneral().getRate();
        consumeAmmo(heldItem);
        if (this.shooter.getMainHandItem().getItem() instanceof GunItem) {
            ejectCasing(this.shooter.level(), this.shooter);
        }
        ResourceLocation fireSound = gun.getSounds().getFire();
        if(fireSound != null) {
            double posX = this.shooter.getX();
            double posY = this.shooter.getY() + this.shooter.getEyeHeight();
            double posZ = this.shooter.getZ();
            float volume = Config.COMMON.world.mobGunfireVolume.get();
            float pitch = 0.9F + this.shooter.level().random.nextFloat() * 0.2F;
            double radius = Config.SERVER.gunShotMaxDistance.get();
            boolean muzzle = gun.getDisplay().getFlash() != null;
            //S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.HOSTILE, (float) posX, (float) posY, (float) posZ, volume - 0.5F, pitch, this.shooter.getId(), muzzle, false);
            //PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.shooter.level(), posX, posY, posZ, radius), messageSound);
            this.shooter.level().playSound(null, posX, posY, posZ, SoundEvent.createVariableRangeEvent(fireSound), SoundSource.HOSTILE, volume - 0.5F, pitch);
        }
    }

    private void consumeAmmo(ItemStack itemStack) {
        itemStack.getTag().putInt("AmmoCount", itemStack.getTag().getInt("AmmoCount") - 1);
    }

    /**
     * Finds a flanking position near the last known position of the target.
     * Attempts to find a position with line of sight to the target.
     */
    private Vec3 findFlankingPosition(Vec3 lastKnownPosition, LivingEntity target) {
        Vec3 mobPos = this.shooter.position();

        if (lastKnownPosition == null) {
            return null;
        }

        Vec3 directionToTarget = lastKnownPosition.subtract(mobPos).normalize();

        for (int i = 0; i < 5; i++) {
            Vec3 offset = directionToTarget.cross(Vec3.atLowerCornerOf(Direction.UP.getNormal())).scale(3).add(randomOffset());
            Vec3 flankingPosition = mobPos.add(offset);

            if (this.shooter.level().getBlockState(BlockPos.containing(flankingPosition)).isAir() &&
                    this.shooter.getSensing().hasLineOfSight(target)) {
                return flankingPosition;
            }
        }
        return null;
    }

    /**
     * Generates a random offset for slight variations in position.
     */
    private Vec3 randomOffset() {
        return new Vec3(
                this.shooter.getRandom().nextDouble() - 0.5,
                0,
                this.shooter.getRandom().nextDouble() - 0.5
        ).scale(1.5);
    }

    /**
     *  Logic to find a cover location relative to the mob and the target
     *  For example, get a position to the side of the mob that is not directly facing the target
     *  Return null if no suitable location is found
     *  Has a chance of failure which looks like the mob getting into a panicked state
     */
    private Vec3 findCoverLocation() {
        Vec3 targetPos = new Vec3(this.shooter.getTarget().getX(), this.shooter.getTarget().getY(), this.shooter.getTarget().getZ());
        Vec3 mobPos = this.shooter.position();
        return mobPos.add(mobPos.subtract(targetPos).normalize().scale(3));
    }
}