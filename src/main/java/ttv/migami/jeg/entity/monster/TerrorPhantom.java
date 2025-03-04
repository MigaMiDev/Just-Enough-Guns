package ttv.migami.jeg.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.entity.ai.EntityHurtByTargetGoal;
import ttv.migami.jeg.entity.ai.TerrorPhantomGunAttackGoal;
import ttv.migami.jeg.init.ModItems;

import java.util.EnumSet;

public class TerrorPhantom extends Phantom {
    private int swoopTimer = 0;
    Vec3 moveTargetPoint;
    BlockPos anchorPoint;
    AttackPhase attackPhase;

    public TerrorPhantom(EntityType<? extends Phantom> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveTargetPoint = Vec3.ZERO;
        this.anchorPoint = BlockPos.ZERO;
        this.attackPhase = TerrorPhantom.AttackPhase.CIRCLE;
        this.xpReward = 25;

        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.LIGHT_MACHINE_GUN.get()));

        /*if (this.level() instanceof ServerLevel serverLevel) {
            if (this.getPassengers().isEmpty()) {
                GunnerManager gunnerManager = GunnerManager.getInstance();
                Faction faction = gunnerManager.getFactionByName(gunnerManager.getRandomFactionName());
                Mob mob = ModCommands.getFactionMob(serverLevel, faction, this.position(), true, 0);
                if (mob != null) {
                    this.addPassenger(mob);
                }
            }
        }*/

        this.moveControl = new TerrorPhantom.PhantomMoveControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PhantomAttackStrategyGoal());
        this.goalSelector.addGoal(2, new TerrorPhantomGunAttackGoal<>(this, 100, 3));
        this.goalSelector.addGoal(2, new PhantomSweepAttackGoal());
        this.goalSelector.addGoal(3, new PhantomCircleAroundAnchorGoal());
        this.targetSelector.addGoal(1, new PhantomAttackPlayerTargetGoal());
        this.targetSelector.addGoal(1, (new EntityHurtByTargetGoal(this)));
    }

    public void tick() {
        super.tick();

        if (this.getPhantomSize() != 32) {
            this.setPhantomSize(32);
        }

        if (this.attackPhase.equals(AttackPhase.SWOOP)) {
            this.horizontalCollision = false;
        }
        //this.noPhysics = this.attackPhase.equals(AttackPhase.SWOOP);
        this.noPhysics = this.getTarget() != null;

        /*if (this.level() instanceof ServerLevel serverLevel) {
            if (this.tickCount % 20 == 0 && this.attackPhase.equals(AttackPhase.SWOOP)) {
                BlockPos pos = this.blockPosition();
                ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(this.level(), this, 60);
                serverLevel.playSound(this, this.blockPosition(), ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.HOSTILE, 30F, 1F);
                grenade.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
                serverLevel.addFreshEntity(grenade);
            }
        }*/
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.1F;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.FOLLOW_RANGE, 256.0D)
                .add(Attributes.ARMOR, 35.0D);
    }

    public static enum AttackPhase {
        CIRCLE,
        SWOOP;

        private AttackPhase() {
        }
    }

    public AttackPhase getAttackPhase() {
        return this.attackPhase;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    class PhantomMoveControl extends MoveControl {
        private float speed = 0.4F;

        public PhantomMoveControl(Mob pMob) {
            super(pMob);
        }

        public void tick() {
            /*if (TerrorPhantom.this.horizontalCollision && !TerrorPhantom.this.attackPhase.equals(AttackPhase.SWOOP)) {
                TerrorPhantom.this.setYRot(TerrorPhantom.this.getYRot() + 180.0F);
                this.speed = 0.4F;
            }*/

            double $$0 = TerrorPhantom.this.moveTargetPoint.x - TerrorPhantom.this.getX();
            double $$1 = TerrorPhantom.this.moveTargetPoint.y - TerrorPhantom.this.getY();
            double $$2 = TerrorPhantom.this.moveTargetPoint.z - TerrorPhantom.this.getZ();
            double $$3 = Math.sqrt($$0 * $$0 + $$2 * $$2);
            if (Math.abs($$3) > 9.999999747378752E-6) {
                double $$4 = 1.0 - Math.abs($$1 * 0.699999988079071) / $$3;
                $$0 *= $$4;
                $$2 *= $$4;
                $$3 = Math.sqrt($$0 * $$0 + $$2 * $$2);
                double $$5 = Math.sqrt($$0 * $$0 + $$2 * $$2 + $$1 * $$1);
                float $$6 = TerrorPhantom.this.getYRot();
                float $$7 = (float)Mth.atan2($$2, $$0);
                float $$8 = Mth.wrapDegrees(TerrorPhantom.this.getYRot() + 90.0F);
                float $$9 = Mth.wrapDegrees($$7 * 57.295776F);
                TerrorPhantom.this.setYRot(Mth.approachDegrees($$8, $$9, 4.0F) - 90.0F);
                TerrorPhantom.this.yBodyRot = TerrorPhantom.this.getYRot();
                if (Mth.degreesDifferenceAbs($$6, TerrorPhantom.this.getYRot()) < 3.0F) {
                    this.speed = Mth.approach(this.speed, 2.5F, 0.005F * (1.8F / this.speed));
                } else {
                    this.speed = Mth.approach(this.speed, 0.4F, 0.025F);
                }

                float $$10 = (float)(-(Mth.atan2(-$$1, $$3) * 57.2957763671875));
                TerrorPhantom.this.setXRot($$10);
                float $$11 = TerrorPhantom.this.getYRot() + 90.0F;
                double $$12 = (double)(this.speed * Mth.cos($$11 * 0.017453292F)) * Math.abs($$0 / $$5);
                double $$13 = (double)(this.speed * Mth.sin($$11 * 0.017453292F)) * Math.abs($$2 / $$5);
                double $$14 = (double)(this.speed * Mth.sin($$10 * 0.017453292F)) * Math.abs($$1 / $$5);
                Vec3 $$15 = TerrorPhantom.this.getDeltaMovement();
                TerrorPhantom.this.setDeltaMovement($$15.add((new Vec3($$12, $$14, $$13)).subtract($$15).scale(0.2)));
            }

        }
    }

    public class PhantomAttackStrategyGoal extends Goal {
        private int nextSweepTick;
        private final int MAX_SWOOP_COOLDOWN = 20;
        private int swoopCooldown = MAX_SWOOP_COOLDOWN;

        public PhantomAttackStrategyGoal() {
        }

        public boolean canUse() {
            LivingEntity $$0 = TerrorPhantom.this.getTarget();
            return $$0 != null ? TerrorPhantom.this.canAttack($$0, TargetingConditions.DEFAULT) : false;
        }

        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            TerrorPhantom.this.attackPhase = TerrorPhantom.AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        public void stop() {
            TerrorPhantom.this.anchorPoint = TerrorPhantom.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, TerrorPhantom.this.anchorPoint).above(10 + TerrorPhantom.this.random.nextInt(20));
        }

        public void tick() {
            if (TerrorPhantom.this.attackPhase == TerrorPhantom.AttackPhase.CIRCLE) {
                --this.nextSweepTick;
                --this.swoopCooldown;
                if (this.nextSweepTick <= 0) {
                    TerrorPhantom.this.attackPhase = TerrorPhantom.AttackPhase.SWOOP;
                    TerrorPhantom.this.swoopTimer = 40;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((16 + TerrorPhantom.this.random.nextInt(8)) * 20);
                    this.swoopCooldown = this.MAX_SWOOP_COOLDOWN;
                    TerrorPhantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 30.0F, 0.45F + TerrorPhantom.this.random.nextFloat() * 0.1F);
                }
            }

        }

        private void setAnchorAboveTarget() {
            TerrorPhantom.this.anchorPoint = TerrorPhantom.this.getTarget().blockPosition().above(20 + TerrorPhantom.this.random.nextInt(20));
            if (TerrorPhantom.this.anchorPoint.getY() < TerrorPhantom.this.level().getSeaLevel()) {
                TerrorPhantom.this.anchorPoint = new BlockPos(TerrorPhantom.this.anchorPoint.getX(), TerrorPhantom.this.level().getSeaLevel() + 1, TerrorPhantom.this.anchorPoint.getZ());
            }

        }
    }

    class PhantomSweepAttackGoal extends PhantomMoveTargetGoal {
        PhantomSweepAttackGoal() {
            super();
        }

        public boolean canUse() {
            return TerrorPhantom.this.getTarget() != null && TerrorPhantom.this.attackPhase == TerrorPhantom.AttackPhase.SWOOP;
        }

        public boolean canContinueToUse() {
            LivingEntity $$0 = TerrorPhantom.this.getTarget();
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
            TerrorPhantom.this.setTarget((LivingEntity)null);
            TerrorPhantom.this.attackPhase = TerrorPhantom.AttackPhase.CIRCLE;
        }

        public void tick() {
            LivingEntity $$0 = TerrorPhantom.this.getTarget();
            if ($$0 != null) {
                TerrorPhantom.this.moveTargetPoint = new Vec3($$0.getX(), $$0.getY(8), $$0.getZ());
                if (TerrorPhantom.this.getBoundingBox().inflate(0.20000000298023224).intersects($$0.getBoundingBox())) {
                    TerrorPhantom.this.doHurtTarget($$0);
                    TerrorPhantom.this.attackPhase = TerrorPhantom.AttackPhase.CIRCLE;
                    if (!TerrorPhantom.this.isSilent()) {
                        TerrorPhantom.this.level().levelEvent(1039, TerrorPhantom.this.blockPosition(), 0);
                    }
                } /*else if (TerrorPhantom.this.horizontalCollision && !TerrorPhantom.this.attackPhase.equals(AttackPhase.SWOOP)) {
                    TerrorPhantom.this.attackPhase = TerrorPhantom.AttackPhase.CIRCLE;
                }*/

            }

            if (--TerrorPhantom.this.swoopTimer <= 0) {
                TerrorPhantom.this.attackPhase = TerrorPhantom.AttackPhase.CIRCLE;
            }
        }
    }

    public class PhantomCircleAroundAnchorGoal extends PhantomMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        public PhantomCircleAroundAnchorGoal() {
            super();
        }

        public boolean canUse() {
            return TerrorPhantom.this.getTarget() == null || TerrorPhantom.this.attackPhase == TerrorPhantom.AttackPhase.CIRCLE;
        }

        public void start() {
            this.distance = 5.0F + TerrorPhantom.this.random.nextFloat() * 10.0F;
            this.height = -4.0F + TerrorPhantom.this.random.nextFloat() * 9.0F;
            this.clockwise = TerrorPhantom.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
        }

        public void tick() {
            if (TerrorPhantom.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                this.height = -4.0F + TerrorPhantom.this.random.nextFloat() * 9.0F;
            }

            if (TerrorPhantom.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                ++this.distance;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }

            if (TerrorPhantom.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = TerrorPhantom.this.random.nextFloat() * 2.0F * 3.1415927F;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            if (TerrorPhantom.this.moveTargetPoint.y < TerrorPhantom.this.getY() && !TerrorPhantom.this.level().isEmptyBlock(TerrorPhantom.this.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (TerrorPhantom.this.moveTargetPoint.y > TerrorPhantom.this.getY() && !TerrorPhantom.this.level().isEmptyBlock(TerrorPhantom.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }

        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(TerrorPhantom.this.anchorPoint)) {
                TerrorPhantom.this.anchorPoint = TerrorPhantom.this.blockPosition();
            }

            this.angle += this.clockwise * 15.0F * 0.017453292F;
            TerrorPhantom.this.moveTargetPoint = Vec3.atLowerCornerOf(TerrorPhantom.this.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), (double)(-4.0F + this.height), (double)(this.distance * Mth.sin(this.angle)));
        }
    }

    public abstract class PhantomMoveTargetGoal extends Goal {
        public PhantomMoveTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return TerrorPhantom.this.moveTargetPoint.distanceToSqr(TerrorPhantom.this.getX(), TerrorPhantom.this.getY(), TerrorPhantom.this.getZ()) < 4.0;
        }
    }
}
