package ttv.migami.jeg.entity.ai.trumpet;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.TrumpetItem;

import java.util.EnumSet;
import java.util.List;

public class TrumpetRangedAttackGoal<T extends Mob & RangedAttackMob> extends Goal {

    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime;

    public TrumpetRangedAttackGoal(T mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.attackTime = -1;
        this.strafingTime = -1;
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() != null && this.isHoldingTrumpet();
    }

    protected boolean isHoldingTrumpet() {
        return this.mob.isHolding((itemStack) -> itemStack.getItem() instanceof TrumpetItem);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingTrumpet();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            double distanceToTarget = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(target);
            boolean sawTargetPreviously = this.seeTime > 0;

            if (canSeeTarget != sawTargetPreviously) {
                this.seeTime = 0;
            }

            if (canSeeTarget) {
                ++this.seeTime;
            } else {
                --this.seeTime;
            }

            if (distanceToTarget <= this.attackRadiusSqr && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
                ++this.strafingTime;
            } else {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if (this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if (this.mob.getRandom().nextFloat() < 0.3) {
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
            }

            if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.performTrumpetAttack(target);
                this.attackTime = this.attackIntervalMin;
            }

            double targetEyeY = target.getEyeY();
            this.mob.getLookControl().setLookAt(target.getX(), targetEyeY, target.getZ());
            this.mob.lookAt(EntityAnchorArgument.Anchor.FEET, target.getBoundingBox().getCenter());
        }
    }

    private void performTrumpetAttack(LivingEntity target) {
        this.mob.level().playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(), ModSounds.DOOT.get(), this.mob.getSoundSource(), 1.0F, 1.0F);
        double knockbackStrength = 2.0;
        target.knockback(knockbackStrength, Mth.sin(this.mob.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(this.mob.getYRot() * ((float) Math.PI / 180F)));
        pushEntitiesAway(this.mob, 7, 1);
        //this.mob.kill();
        //target.hurt(this.mob.damageSources().generic(), 0.1F);

        Vec3 mobPosition = mob.position();


        Vec3 lookVec = this.mob.getLookAngle();
        double opposite = -1;
        this.mob.push(lookVec.x * opposite, lookVec.y * opposite, lookVec.z * opposite);

        List<Entity> nearbyEntities = mob.level().getEntities(mob, mob.getBoundingBox().inflate(5), e -> e != mob && e instanceof LivingEntity);

        for (Entity entity : nearbyEntities) {
            Vec3 direction = entity.position().subtract(mobPosition).normalize();

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.hurtMarked = true;
                livingEntity.push(direction.x * 1, direction.y * 1, direction.z * 1);
            }
        }
        this.mob.push(0, 0.1, 0);

        double offsetX = lookVec.x * 1.8;
        double offsetY = lookVec.y * 1.8;
        double offsetZ = lookVec.z * 1.8;
        Vec3 skeletonPos = this.mob.getPosition(1F).add(offsetX, offsetY + this.mob.getEyeHeight(), offsetZ);

        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticleTypes.BIG_SONIC_RING.get(), skeletonPos.x, skeletonPos.y, skeletonPos.z, 1, offsetX / 2, offsetY / 2, offsetZ / 2, 0.1);
            serverLevel.sendParticles(ModParticleTypes.BIG_SONIC_RING.get(), skeletonPos.x, skeletonPos.y, skeletonPos.z, 1, offsetX / 4, offsetY / 4, offsetZ / 4, 0.1);
            serverLevel.sendParticles(ModParticleTypes.BIG_SONIC_RING.get(), skeletonPos.x, skeletonPos.y, skeletonPos.z, 1, offsetX / 1.5, offsetY / 1.5, offsetZ / 1.5, 0.1);
        }
    }

    public void pushEntitiesAway(Mob mob, double radius, double force) {
        Vec3 mobPosition = mob.position();

        List<Entity> nearbyEntities = mob.level().getEntities(mob, mob.getBoundingBox().inflate(radius), e -> e != mob && e instanceof LivingEntity);

        for (Entity entity : nearbyEntities) {
            Vec3 direction = entity.position().subtract(mobPosition).normalize();

            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity instanceof Player player) {
                    //player.hurt(player.damageSources().generic(), 0.1F);
                }
                else {
                    livingEntity.push(direction.x * force, direction.y * force, direction.z * force);
                }
            }
        }
    }
}