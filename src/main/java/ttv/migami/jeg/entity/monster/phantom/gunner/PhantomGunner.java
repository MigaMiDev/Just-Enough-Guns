package ttv.migami.jeg.entity.monster.phantom.gunner;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.client.handler.SoundHandler;
import ttv.migami.jeg.entity.ai.EntityHurtByTargetGoal;
import ttv.migami.jeg.entity.ai.owned.NearestAttackableTargetGoal;
import ttv.migami.jeg.entity.ai.owned.PlayerHurtTargetGoal;
import ttv.migami.jeg.entity.ai.phantom.PhantomGunnerGunAttackGoal;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;
import ttv.migami.jeg.entity.throwable.GrenadeEntity;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class PhantomGunner extends Phantom implements GeoEntity {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int attackTimer = 0;
    Vec3 moveTargetPoint;
    BlockPos anchorPoint;
    PhantomGunner.AttackPhase attackPhase;

    private boolean isDying = false;
    private int deathTimer = 0;
    private static final int DEATH_ANIMATION_DURATION = 100;
    private double forwardSpeed = 0.1;

    private Player player;
    public boolean playerOwned = false;
    private int despawnTimer;

    private boolean makeSound = true;

    private static final EntityDataAccessor<Boolean> IS_DYING = SynchedEntityData.defineId(PhantomGunner.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_OWNED = SynchedEntityData.defineId(PhantomGunner.class, EntityDataSerializers.BOOLEAN);

    public PhantomGunner(EntityType<? extends Phantom> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        this.moveTargetPoint = Vec3.ZERO;
        this.anchorPoint = BlockPos.ZERO;
        this.attackPhase = PhantomGunner.AttackPhase.CIRCLE;
        this.xpReward = 100;

        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.LIGHT_MACHINE_GUN.get()));

        this.moveControl = new PhantomGunner.PhantomMoveControl(this);

        this.despawnTimer = 1200;
    }

    public void setPlayer(Player player) {
        if (player != null) {
            this.player = player;
            this.playerOwned = true;
            this.setPlayerOwned(true);
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PhantomGunnerAttackStrategyGoal());
        this.goalSelector.addGoal(2, new PhantomGunnerGunAttackGoal<>(this, 100, 5));
        this.goalSelector.addGoal(2, new PhantomGunnerSweepAttackGoal());
        this.goalSelector.addGoal(3, new PhantomGunnerCircleAroundAnchorGoal());
        this.targetSelector.addGoal(1, new PhantomGunnerAttackPlayerTargetGoal());
        this.targetSelector.addGoal(1, (new EntityHurtByTargetGoal(this)));

        // Player Owned
        this.targetSelector.addGoal(1, new PlayerHurtTargetGoal(this));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, true, (livingEntity) -> livingEntity instanceof Enemy && !(livingEntity instanceof Creeper) && !(livingEntity instanceof PhantomGunner) && !(livingEntity instanceof TerrorPhantom) && !(livingEntity instanceof EnderMan)));
    }

    private void explode() {
        GrenadeEntity.createExplosion(this, Config.COMMON.grenades.explosionRadius.get().floatValue(), true);
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

    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (this.makeSound) {
                SoundHandler.playPhantomGunnerFlySound(this);
                this.makeSound = false;
            }
            if (this.isDying()) {
                SoundHandler.playPhantomGunnerDiveSound(this);
            }
        }

        if (this.isPlayerOwned() && this.getTarget() != null) {
            if (this.getTarget().getTags().contains("PlayerOwned")) {
                this.setTarget(null);
            }
        }

        if (this.isPlayerOwned() && !this.level().isClientSide) {
            this.despawnTimer--;
            if (this.despawnTimer <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
            if (this.player != null) {
                this.anchorPoint = BlockPos.containing(this.player.position());
            } else if (this.tickCount >= 60) {
                this.remove(RemovalReason.DISCARDED);
            }
        }

        if (this.isDying()) {
            this.deathTimer++;

            this.forwardSpeed = Math.min(this.forwardSpeed + 0.05, 2.0);

            Vec3 forwardMotion = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).scale(this.forwardSpeed);
            this.setDeltaMovement(forwardMotion.x, -1, forwardMotion.z);

            this.setXRot(this.getXRot() + 2f);

            if (this.deathTimer >= DEATH_ANIMATION_DURATION || this.horizontalCollision || this.verticalCollision) {
                this.explode();
                this.remove(RemovalReason.KILLED);
            }
        }

        if (this.getPhantomSize() != 4) {
            this.setPhantomSize(4);
        }

        if (!this.isDying()) {
            if (this.attackPhase.equals(PhantomGunner.AttackPhase.SWOOP)) {
                this.horizontalCollision = false;
            }
            //this.noPhysics = this.attackPhase.equals(AttackPhase.SWOOP);
            this.noPhysics = this.player != null || (this.getTarget() != null || this.horizontalCollision);
        } else {
            this.noPhysics = false;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            this.attackTimer--;
            if (this.attackTimer <= 0) {
                this.attackPhase = PhantomGunner.AttackPhase.CIRCLE;
            }
        }

        if (this.getHealth() <= 0) {
            this.isDying = true;
            this.setDying(true);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.isDying) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean isDeadOrDying() {
        return this.isDying && this.deathTimer >= DEATH_ANIMATION_DURATION;
    }
    
    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    public static enum AttackPhase {
        CIRCLE,
        SWOOP;

        private AttackPhase() {
        }
    }

    public PhantomGunner.AttackPhase getAttackPhase() {
        return this.attackPhase;
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(PhantomGunnerAnimations.genericIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isPersistenceRequired() {
        return this.playerOwned;
    }

    class PhantomMoveControl extends MoveControl {
        private float speed = 0.4F;

        public PhantomMoveControl(Mob pMob) {
            super(pMob);
        }

        public void tick() {
            if (PhantomGunner.this.attackPhase.equals(AttackPhase.SWOOP)) {
                this.speed = 1.0F;
            } else {
                this.speed = 0.75F;
            }

            double $$0 = PhantomGunner.this.moveTargetPoint.x - PhantomGunner.this.getX();
            double $$1 = PhantomGunner.this.moveTargetPoint.y - PhantomGunner.this.getY();
            double $$2 = PhantomGunner.this.moveTargetPoint.z - PhantomGunner.this.getZ();
            double $$3 = Math.sqrt($$0 * $$0 + $$2 * $$2);
            if (Math.abs($$3) > 9.999999747378752E-6) {
                double $$4 = 1.0 - Math.abs($$1 * 0.699999988079071) / $$3;
                $$0 *= $$4;
                $$2 *= $$4;
                $$3 = Math.sqrt($$0 * $$0 + $$2 * $$2);
                double $$5 = Math.sqrt($$0 * $$0 + $$2 * $$2 + $$1 * $$1);
                float $$6 = PhantomGunner.this.getYRot();
                float $$7 = (float) Mth.atan2($$2, $$0);
                float $$8 = Mth.wrapDegrees(PhantomGunner.this.getYRot() + 90.0F);
                float $$9 = Mth.wrapDegrees($$7 * 57.295776F);
                PhantomGunner.this.setYRot(Mth.approachDegrees($$8, $$9, 4.0F) - 90.0F);
                PhantomGunner.this.yBodyRot = PhantomGunner.this.getYRot();
                if (Mth.degreesDifferenceAbs($$6, PhantomGunner.this.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 2.5F, 0.005F * (1.8F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.4F, 0.025F);
                }

                float $$10 = (float)(-(Mth.atan2(-$$1, $$3) * 57.2957763671875));
                PhantomGunner.this.setXRot($$10);
                float $$11 = PhantomGunner.this.getYRot() + 90.0F;
                double $$12 = (double)(this.speed * Mth.cos($$11 * 0.017453292F)) * Math.abs($$0 / $$5);
                double $$13 = (double)(this.speed * Mth.sin($$11 * 0.017453292F)) * Math.abs($$2 / $$5);
                double $$14 = (double)(this.speed * Mth.sin($$10 * 0.017453292F)) * Math.abs($$1 / $$5);
                Vec3 $$15 = PhantomGunner.this.getDeltaMovement();
                PhantomGunner.this.setDeltaMovement($$15.add((new Vec3($$12, $$14, $$13)).subtract($$15).scale(0.2)));
            }

        }
    }

    public class PhantomGunnerAttackStrategyGoal extends Goal {
        private int nextSweepTick;
        public PhantomGunnerAttackStrategyGoal() {
        }

        public boolean canUse() {
            if (PhantomGunner.this.getPlayer() != null) {
                double dx = PhantomGunner.this.getX() - PhantomGunner.this.getPlayer().getX();
                double dz = PhantomGunner.this.getZ() - PhantomGunner.this.getPlayer().getZ();
                return (dx * dx + dz * dz) <= (16 * 16);
            }

            LivingEntity $$0 = PhantomGunner.this.getTarget();
            return $$0 != null ? PhantomGunner.this.canAttack($$0, TargetingConditions.forCombat().ignoreLineOfSight()) : false;
        }

        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            PhantomGunner.this.attackPhase = PhantomGunner.AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        public void stop() {
            PhantomGunner.this.anchorPoint = PhantomGunner.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, PhantomGunner.this.anchorPoint).above(10 + PhantomGunner.this.random.nextInt(20));
        }

        public void tick() {
            if (PhantomGunner.this.attackPhase == PhantomGunner.AttackPhase.CIRCLE) {
                --this.nextSweepTick;
                if (this.nextSweepTick <= 0) {
                    PhantomGunner.this.attackPhase = PhantomGunner.AttackPhase.SWOOP;
                    PhantomGunner.this.attackTimer = 80;
                    this.setAnchorOvershot();
                    this.nextSweepTick = this.adjustedTickDelay((120));
                    PhantomGunner.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + PhantomGunner.this.random.nextFloat() * 0.1F);
                }
            }

        }

        private void setAnchorAboveTarget() {
            if (PhantomGunner.this.getTarget() == null) return;

            PhantomGunner.this.anchorPoint = PhantomGunner.this.getTarget().blockPosition().above(20 + PhantomGunner.this.random.nextInt(20));
            if (PhantomGunner.this.anchorPoint.getY() < PhantomGunner.this.level().getSeaLevel()) {
                PhantomGunner.this.anchorPoint = new BlockPos(PhantomGunner.this.anchorPoint.getX(), PhantomGunner.this.level().getSeaLevel() + 1, PhantomGunner.this.anchorPoint.getZ());
            }

        }

        private void setAnchorOvershot() {
            if (PhantomGunner.this.getTarget() == null) return;

            LivingEntity target = PhantomGunner.this.getTarget();
            Vec3 phantomPos = PhantomGunner.this.position();
            Vec3 targetPos = target.position();

            Vec3 direction = targetPos.subtract(phantomPos).normalize();

            double overshootDistance = 32 + PhantomGunner.this.random.nextInt(6);
            Vec3 overshotPos = targetPos.add(direction.scale(overshootDistance));

            int targetHeight = target.getBlockY();

            int maxHeight = Math.min(targetHeight + 32, PhantomGunner.this.level().getMaxBuildHeight() - 1);
            int finalHeight = Math.min(targetHeight, maxHeight);
            /*if (finalHeight < TerrorPhantom.this.level().getSeaLevel()) {
                finalHeight = TerrorPhantom.this.level().getSeaLevel() + 1;
            }*/

            PhantomGunner.this.anchorPoint = new BlockPos((int) overshotPos.x, finalHeight, (int) overshotPos.z);
        }
    }

    class PhantomGunnerSweepAttackGoal extends PhantomGunner.PhantomMoveTargetGoal {
        PhantomGunnerSweepAttackGoal() {
            super();
        }

        public boolean canUse() {
            return !PhantomGunner.this.isDying() && PhantomGunner.this.getTarget() != null && PhantomGunner.this.attackPhase == PhantomGunner.AttackPhase.SWOOP;
        }

        public boolean canContinueToUse() {
            if (PhantomGunner.this.isDying()) {
                return false;
            }

            LivingEntity $$0 = PhantomGunner.this.getTarget();
            if ($$0 == null) {
                return false;
            } else if (!$$0.isAlive()) {
                return false;
            } else {
                if ($$0 instanceof Player) {
                    Player $$1 = (Player)$$0;
                    if ($$0.isSpectator() || $$1.isCreative()) {
                        return false;
                    }
                }

                return this.canUse();
            }
        }

        public void start() {
        }

        public void stop() {
            PhantomGunner.this.setTarget((LivingEntity)null);
            PhantomGunner.this.attackPhase = PhantomGunner.AttackPhase.CIRCLE;
        }

        public void tick() {
            LivingEntity $$0 = PhantomGunner.this.getTarget();
            if ($$0 != null) {
                PhantomGunner.this.moveTargetPoint = new Vec3($$0.getX(), $$0.getY(8), $$0.getZ());
                if (PhantomGunner.this.getBoundingBox().inflate(0.20000000298023224).intersects($$0.getBoundingBox())) {
                    PhantomGunner.this.doHurtTarget($$0);
                    PhantomGunner.this.attackPhase = PhantomGunner.AttackPhase.CIRCLE;
                    if (!PhantomGunner.this.isSilent()) {
                        PhantomGunner.this.level().levelEvent(1039, PhantomGunner.this.blockPosition(), 0);
                    }
                } /*else if (PhantomGunner.this.horizontalCollision && !PhantomGunner.this.attackPhase.equals(AttackPhase.SWOOP)) {
                    PhantomGunner.this.attackPhase = PhantomGunner.AttackPhase.CIRCLE;
                }*/

            }
        }
    }

    public class PhantomGunnerCircleAroundAnchorGoal extends PhantomGunner.PhantomMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        public PhantomGunnerCircleAroundAnchorGoal() {
            super();
        }

        public boolean canUse() {
            return !PhantomGunner.this.isDying() && ((PhantomGunner.this.getTarget() == null || PhantomGunner.this.getPlayer() != null) || (PhantomGunner.this.attackPhase == PhantomGunner.AttackPhase.CIRCLE));
        }

        public void start() {
            this.distance = 5.0F + PhantomGunner.this.random.nextFloat() * 10.0F;
            this.height = -4.0F + PhantomGunner.this.random.nextFloat() * 9.0F;
            this.clockwise = PhantomGunner.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
        }

        public void tick() {
            if (PhantomGunner.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0F + PhantomGunner.this.random.nextFloat() * 9.0F;
            }

            if (PhantomGunner.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                ++this.distance;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }

            if (PhantomGunner.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = PhantomGunner.this.random.nextFloat() * 2.0F * 3.1415927F;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            if (PhantomGunner.this.moveTargetPoint.y < PhantomGunner.this.getY() && !PhantomGunner.this.level().isEmptyBlock(PhantomGunner.this.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (PhantomGunner.this.moveTargetPoint.y > PhantomGunner.this.getY() && !PhantomGunner.this.level().isEmptyBlock(PhantomGunner.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }

        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(PhantomGunner.this.anchorPoint)) {
                PhantomGunner.this.anchorPoint = new BlockPos(PhantomGunner.this.blockPosition().getX(), 40, PhantomGunner.this.blockPosition().getZ());
            }

            this.angle += this.clockwise * 15.0F * 0.017453292F;
            PhantomGunner.this.moveTargetPoint = Vec3.atLowerCornerOf(PhantomGunner.this.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), 24, (double)(this.distance * Mth.sin(this.angle)));
        }
    }

    public class PhantomGunnerAttackPlayerTargetGoal extends Goal {
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);
        private int nextScanTick = reducedTickDelay(20);

        public PhantomGunnerAttackPlayerTargetGoal() {
        }

        public boolean canUse() {
            if (PhantomGunner.this.isPlayerOwned() || PhantomGunner.this.playerOwned) {
                return false;
            }

            if (this.nextScanTick > 0) {
                --this.nextScanTick;
                return false;
            } else {
                this.nextScanTick = reducedTickDelay(60);
                List<Player> $$0 = PhantomGunner.this.level().getNearbyPlayers(this.attackTargeting, PhantomGunner.this, PhantomGunner.this.getBoundingBox().inflate(64.0, 100.0, 64.0));
                if (!$$0.isEmpty()) {
                    Iterator var2 = $$0.iterator();

                    while(var2.hasNext()) {
                        Player $$1 = (Player)var2.next();
                        if (PhantomGunner.this.canAttack($$1, TargetingConditions.forCombat().ignoreLineOfSight())) {
                            PhantomGunner.this.setTarget($$1);
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        public boolean canContinueToUse() {
            LivingEntity $$0 = PhantomGunner.this.getTarget();
            return $$0 != null ? PhantomGunner.this.canAttack($$0, TargetingConditions.forCombat().ignoreLineOfSight()) : false;
        }
    }

    public abstract class PhantomMoveTargetGoal extends Goal {
        public PhantomMoveTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return PhantomGunner.this.moveTargetPoint.distanceToSqr(PhantomGunner.this.getX(), PhantomGunner.this.getY(), PhantomGunner.this.getZ()) < 4.0;
        }
    }

    public boolean isDying() {
        return this.entityData.get(IS_DYING);
    }

    public void setDying(boolean dying) {
        this.entityData.set(IS_DYING, dying);
    }

    public boolean isPlayerOwned() {
        return this.entityData.get(IS_OWNED);
    }

    public void setPlayerOwned(boolean isPlayerOwned) {
        this.entityData.set(IS_OWNED, isPlayerOwned);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(IS_DYING, false);
        this.entityData.define(IS_OWNED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putBoolean("IsPlayerOwned", this.isPlayerOwned());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("IsPlayerOwned")) {
            this.setPlayerOwned(compound.getBoolean("IsPlayerOwned"));
        }
    }
}
