package ttv.migami.jeg.entity.monster.phantom.terror;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.handler.SoundHandler;
import ttv.migami.jeg.entity.ai.EntityHurtByTargetGoal;
import ttv.migami.jeg.entity.ai.phantom.TerrorPhantomGunAttackGoal;
import ttv.migami.jeg.entity.monster.phantom.PhantomSwarmData;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;
import ttv.migami.jeg.entity.throwable.*;
import ttv.migami.jeg.init.*;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static ttv.migami.jeg.common.network.ServerPlayHandler.getFireworkStack;
import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;
import static ttv.migami.jeg.entity.monster.phantom.PhantomSwarmSpawner.spawnPhantomGunnerSwarm;
import static ttv.migami.jeg.entity.monster.phantom.PhantomSwarmSpawner.spawnSecondLayerPhantomGunnerSwarm;

public class TerrorPhantom extends Phantom implements GeoEntity {
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int attackTimer = 0;
    Vec3 moveTargetPoint;
    BlockPos anchorPoint;
    AttackPhase attackPhase;
    private boolean isHalfHealth = false;
    private boolean isAngry = false;

    private boolean isDying = false;
    private int deathTimer = 0;
    private static final int DEATH_ANIMATION_DURATION = 200;
    private double forwardSpeed = 0.1;
    private boolean looted = false;

    private Player player;
    public boolean playerOwned = false;
    private int despawnTimer;
    private int bombingTimer = 0;
    private final static int MAX_BOMBING_TIMER = 20;
    private float vertical = 0;
    private float speed = 5F;

    private final int MAX_SWARM_PARTICLE_TICK = 7;
    private int swarmParticleTick = MAX_SWARM_PARTICLE_TICK;

    private final int MAX_SWARM_SPAWN_TICK = 300;
    private int swarmSpawnTick = MAX_SWARM_SPAWN_TICK;

    private boolean makeSound = true;

    private ResourceLocation lootTable1 = new ResourceLocation(Reference.MOD_ID, "entities/phantom/terror/terror_phantom_supply");
    private ResourceLocation lootTable2 = new ResourceLocation(Reference.MOD_ID, "entities/phantom/terror/terror_phantom_reward");

    private static final EntityDataAccessor<Boolean> IS_ROLLING = SynchedEntityData.defineId(TerrorPhantom.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_DYING = SynchedEntityData.defineId(TerrorPhantom.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_OWNED = SynchedEntityData.defineId(TerrorPhantom.class, EntityDataSerializers.BOOLEAN);

    public TerrorPhantom(EntityType<? extends Phantom> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

        this.moveTargetPoint = Vec3.ZERO;
        this.anchorPoint = BlockPos.ZERO;
        this.attackPhase = AttackPhase.CIRCLE;
        this.xpReward = 100;

        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.LIGHT_MACHINE_GUN.get()));

        this.moveControl = new PhantomMoveControl(this);

        if (!this.isPlayerOwned()) {
            this.bossEvent.setProgress(1.0F);
        } else {
            this.bossEvent.removeAllPlayers();
        }

        this.despawnTimer = 200;
        this.bombingTimer = MAX_BOMBING_TIMER;
    }

    public void setPlayer(Player player) {
        if (player != null) {
            this.player = player;
            this.playerOwned = true;
            this.setPlayerOwned(true);
            this.addTag("   PlayerOwned");
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TerrorPhantomAttackStrategyGoal());
        this.goalSelector.addGoal(2, new TerrorPhantomGunAttackGoal<>(this, 100, 5));
        this.goalSelector.addGoal(2, new TerrorPhantomSweepAttackGoal());
        this.goalSelector.addGoal(2, new TerrorPhantomRollAttackGoal());
        this.goalSelector.addGoal(2, new TerrorPhantomSwarmAttackGoal(TerrorPhantom.this, 10));
        this.goalSelector.addGoal(2, new TerrorPhantomBombingAttackGoal());
        this.goalSelector.addGoal(3, new TerrorPhantomCircleAroundAnchorGoal());
        this.targetSelector.addGoal(1, new TerrorPhantomAttackPlayerTargetGoal());
        this.targetSelector.addGoal(1, (new EntityHurtByTargetGoal(this)));
    }

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.jeg.terror_phantom"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
    );

    private void releaseSwarm(ServerLevel serverLevel) {
        if (!Config.COMMON.gunnerMobs.phantomSwarm.get()) {
            return;
        }

        PhantomSwarmData raidData = PhantomSwarmData.get(serverLevel);

        if (!raidData.hasPhantomSwarm()) {
            raidData.setPhantomSwarm(true);
            Component message = Component.translatable("broadcast.jeg.terror_phantom_defeat")
                    .withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.BOLD);
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    private void explode() {
        this.endGrenades(this);
        GrenadeEntity.createExplosion(this, Config.COMMON.grenades.explosionRadius.get().floatValue(), true);
        if (this.level() instanceof ServerLevel serverLevel) {
            if (!this.looted) {
                this.spawnLootBarrels(serverLevel, this.getOnPos(), lootTable1, lootTable2);
                this.looted = true;
            }
            ThrowableFlareEntity flare = new ThrowableFlareEntity(serverLevel, this);
            serverLevel.addFreshEntity(flare);
            ModCommands.startTerrorRaid(serverLevel, this.position(), true, true);

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

    private void dropGrenades(TerrorPhantom terrorPhantom) {
        if (terrorPhantom.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = terrorPhantom.blockPosition();
            //serverLevel.playSound(terrorPhantom, pos, ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.HOSTILE, 30F, 1F);

            RandomSource random = terrorPhantom.getRandom();
            int grenadeCount = 1 + random.nextInt(1);

            for (int i = 0; i < grenadeCount; i++) {
                ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(terrorPhantom.level(), terrorPhantom, 50 + terrorPhantom.level().random.nextInt(9));
                grenade.setPos(pos.getX(), pos.getY() + 1, pos.getZ());

                double xVelocity = (random.nextDouble() - 0.5) * 1.5;
                double yVelocity = (0.4 + random.nextDouble() * 0.6) * -1;
                double zVelocity = (random.nextDouble() - 0.5) * 1.5;

                grenade.setDeltaMovement(xVelocity, yVelocity, zVelocity);
                grenade.terrorPhantomThrown = true;

                serverLevel.addFreshEntity(grenade);
            }
        }
    }

    private void dropGrenade(TerrorPhantom terrorPhantom) {
        if (terrorPhantom.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = terrorPhantom.blockPosition();
            //serverLevel.playSound(terrorPhantom, pos, ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.HOSTILE, 30F, 1F);

            RandomSource random = terrorPhantom.getRandom();
            int grenadeCount = 2;

            for (int i = 0; i < grenadeCount; i++) {
                ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(terrorPhantom.level(), terrorPhantom, 100);

                grenade.setPos(pos.getX(), pos.getY() + 1, pos.getZ());

                double xVelocity = 0;
                double yVelocity = -1;
                double zVelocity = 0;

                grenade.setDeltaMovement(xVelocity, yVelocity, zVelocity);
                grenade.terrorPhantomThrown = true;
                grenade.playerOwnedTerrorPhantom = true;

                serverLevel.addFreshEntity(grenade);

                grenade.setShouldBounce(false);
            }
        }
    }

    private void dropGrenadesSideways(TerrorPhantom terrorPhantom) {
        if (terrorPhantom.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = terrorPhantom.blockPosition();

            Vec3 lookVec = terrorPhantom.getLookAngle();

            Vec3 leftVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize().scale(0.5);
            Vec3 rightVec = new Vec3(lookVec.z, 0, -lookVec.x).normalize().scale(0.5);

            throwGrenade(serverLevel, terrorPhantom, pos, leftVec);
            throwGrenade(serverLevel, terrorPhantom, pos, rightVec);
        }
    }

    private void throwGrenade(ServerLevel serverLevel, TerrorPhantom terrorPhantom, BlockPos pos, Vec3 offset) {
        ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(terrorPhantom.level(), terrorPhantom, 100);

        grenade.setPos(pos.getX() + offset.x, pos.getY() + 1, pos.getZ() + offset.z);

        grenade.setDeltaMovement(offset.x, -0.5, offset.z);

        grenade.terrorPhantomThrown = true;
        grenade.playerOwnedTerrorPhantom = true;
        grenade.setShouldBounce(false);

        serverLevel.addFreshEntity(grenade);
    }

    private void endGrenades(TerrorPhantom terrorPhantom) {
        if (terrorPhantom.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = terrorPhantom.blockPosition();
            serverLevel.playSound(terrorPhantom, pos, ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.HOSTILE, 30F, 1F);

            RandomSource random = terrorPhantom.getRandom();
            int grenadeCount = 15 + random.nextInt(5);

            for (int i = 0; i < grenadeCount; i++) {
                ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(terrorPhantom.level(), terrorPhantom, 90 + terrorPhantom.level().random.nextInt(9));
                grenade.setPos(pos.getX(), pos.getY() + 1, pos.getZ());

                double xVelocity = (random.nextDouble() - 0.5) * 1.5;
                double yVelocity = (0.4 + random.nextDouble() * 0.6) * 2;
                double zVelocity = (random.nextDouble() - 0.5) * 1.5;

                grenade.setDeltaMovement(xVelocity, yVelocity, zVelocity);
                grenade.terrorPhantomThrown = true;

                serverLevel.addFreshEntity(grenade);
            }
        }
    }

    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (this.makeSound) {
                if (this.isPlayerOwned()) {
                    SoundHandler.playTerrorPhantomBoostSound(this);
                } else {
                    SoundHandler.playTerrorPhantomFlySound(this);
                }
                this.makeSound = false;
            }
            if (this.isDying()) {
                SoundHandler.playTerrorPhantomDiveSound(this);
            }
        }

        if (this.isPlayerOwned()) {
            this.bossEvent.removeAllPlayers();
            this.despawnTimer--;

            /*if (this.despawnTimer < 100) {
                if (this.vertical < 1.0F) {
                    this.vertical = this.vertical + 0.1F;
                }
                if (this.speed < 3.0F) {
                    this.speed = this.speed + 0.1F;
                }
            }*/

            Vec3 forwardMotion = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).scale(speed);
            this.setDeltaMovement(forwardMotion.x, this.vertical, forwardMotion.z);
            if (this.tickCount > 18) {
                this.bombingTimer--;
                if (this.tickCount % 2 == 0 && this.bombingTimer >= 0) {
                    dropGrenade(this);
                    dropGrenadesSideways(this);
                }
            }
            if (this.despawnTimer <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }

        if (this.isDying()) {
            this.deathTimer++;

            this.forwardSpeed = Math.min(this.forwardSpeed + 0.05, 2.0);

            Vec3 forwardMotion = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).scale(this.forwardSpeed);
            this.setDeltaMovement(forwardMotion.x, -0.75, forwardMotion.z);

            this.setXRot(this.getXRot() + 2f);

            if (this.deathTimer >= DEATH_ANIMATION_DURATION || this.horizontalCollision || this.verticalCollision) {
                this.explode();
                if (this.level() instanceof ServerLevel serverLevel) {
                    this.releaseSwarm(serverLevel);
                    for (int i = 0; i < 25; i++) {
                        ExperienceOrb.award(serverLevel, this.position().add(0, 100, 0), 100);
                    }
                }
                this.remove(RemovalReason.KILLED);
            }

            if (this.tickCount % 10 == 0) {
                dropGrenades(this);
            }

            if (this.tickCount % 5 == 0) {
                FireworkRocketEntity firework = new FireworkRocketEntity(this.level(), getFireworkStack(this.random.nextBoolean(), false, this.random.nextInt(0, 3), this.random.nextInt(0, 3)), this.getX() + this.random.nextInt(-32, 32), this.getY() + this.random.nextInt(0, 3 ), this.getZ() + this.random.nextInt(-10, 10), false);
                this.level().addFreshEntity(firework);
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                if (this.tickCount % 15 == 0) {
                    serverLevel.playSound(this, this.getOnPos(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 10.0F, 0.8F + this.random.nextFloat());
                }
                if (this.tickCount % 8 == 0) {
                    sendParticlesToAll(
                            serverLevel,
                            ModParticleTypes.SMALL_EXPLOSION.get(),
                            true,
                            this.getX() - this.getDeltaMovement().x(),
                            this.getY() - this.getDeltaMovement().y(),
                            this.getZ() - this.getDeltaMovement().z(),
                            1,
                            3, 3, 3,
                            0
                    );
                    serverLevel.playSound(this, this.getOnPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 10.0F, 1.0F);
                }
            }
        }

        this.isHalfHealth = TerrorPhantom.this.getHealth() <= TerrorPhantom.this.getMaxHealth() / 2;
        this.isAngry = TerrorPhantom.this.getHealth() <= (TerrorPhantom.this.getMaxHealth() / 2) + 50;

        if (this.getPhantomSize() != 6) {
            this.setPhantomSize(6);
        }

        if (!this.isDying()) {
            if (this.attackPhase.equals(AttackPhase.SWOOP)) {
                this.horizontalCollision = false;
            }
            //this.noPhysics = this.attackPhase.equals(AttackPhase.SWOOP);
            this.noPhysics = (this.getTarget() != null || this.horizontalCollision);
        } else {
            this.noPhysics = false;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            this.attackTimer--;
            if (this.attackTimer <= 0) {
                this.attackPhase = AttackPhase.CIRCLE;
            }
            if (this.isAngry && --this.swarmParticleTick <= 0) {
                for (ServerPlayer players : serverLevel.players()) {
                    spawnPhantomGunnerSwarm(serverLevel, players);
                    spawnSecondLayerPhantomGunnerSwarm(serverLevel, players);
                }
                this.swarmParticleTick = MAX_SWARM_PARTICLE_TICK;
            }
            if (this.isHalfHealth && --this.swarmSpawnTick <= 0 && this.getTarget() instanceof Player player) {
                BlockPos.MutableBlockPos spawnPos = this.getTarget().blockPosition().mutable()
                        .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                                0,
                                (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));
                ModCommands.spawnPhantomSwarm(serverLevel, 1 + serverLevel.random.nextInt(1), player, spawnPos);
                this.swarmSpawnTick = MAX_SWARM_SPAWN_TICK;
            }
        }

        if (this.getHealth() <= 0) {
            this.isDying = true;
            this.setDying(true);
        }
    }

    public void spawnLootBarrels(ServerLevel world, BlockPos pos, ResourceLocation lootTable1, ResourceLocation lootTable2) {
        BlockPos chestPos1 = findGroundPosition(world, pos);
        BlockPos chestPos2 = findGroundPosition(world, pos.offset(2, 0, 2));

        this.placeBarrelWithLoot(world, chestPos1, lootTable1);
        this.placeBarrelWithLoot(world, chestPos2, lootTable2);
    }

    private static BlockPos findGroundPosition(Level world, BlockPos pos) {
        while (!world.getBlockState(pos).isSolid() && pos.getY() > world.getMinBuildHeight()) {
            pos = pos.below();
        }
        return pos.above();
    }

    private void placeBarrelWithLoot(ServerLevel world, BlockPos pos, ResourceLocation lootTable) {
        world.setBlock(pos, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.getRandom(this.random)), 3);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BarrelBlockEntity chest) {
            chest.setLootTable(lootTable, world.getRandom().nextLong());
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

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide && !this.isPlayerOwned() && this.tickCount > 20) {
            updateBossBar();
        }
    }

    private void updateBossBar() {
        float healthPercentage = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(healthPercentage);

        List<ServerPlayer> nearbyPlayers = getNearbyPlayers(128);
        this.bossEvent.removeAllPlayers();

        for (ServerPlayer player : nearbyPlayers) {
            this.bossEvent.addPlayer(player);
        }
    }

    private List<ServerPlayer> getNearbyPlayers(double radius) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return List.of();
        }

        return serverLevel.getPlayers(player -> player.distanceTo(this) <= radius);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (player.distanceTo(this) <= 128 && !this.isPlayerOwned() && this.tickCount > 20) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    @Override
    protected void playHurtSound(DamageSource pSource) {
        SoundEvent soundevent = this.getHurtSound(pSource);
        if (soundevent != null && this.tickCount % 40 == 0) {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.1F;
    }

    @Override
    protected float getSoundVolume() {
        return 1000F;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.FOLLOW_RANGE, 256.0D)
                .add(Attributes.ARMOR, 35.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    public static enum AttackPhase {
        CIRCLE,
        SWOOP,
        ROLL,
        SWARM,
        BOMBING;

        private AttackPhase() {
        }
    }

    public AttackPhase getAttackPhase() {
        return this.attackPhase;
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(TerrorPhantomAnimations.genericIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isPersistenceRequired() {
        return !this.isPlayerOwned();
    }

    class PhantomMoveControl extends MoveControl {
        private float speed = 0.4F;

        public PhantomMoveControl(Mob pMob) {
            super(pMob);
        }

        public void tick() {
            if (TerrorPhantom.this.isPlayerOwned()) {
                return;
            }

            if (TerrorPhantom.this.isDying()) {
                this.speed = 0.5F;
            } else {
                if (TerrorPhantom.this.attackPhase == AttackPhase.SWARM) {
                    this.speed = 3.0F;
                } else if (TerrorPhantom.this.attackPhase == AttackPhase.ROLL) {
                    this.speed = 1.5F;
                } else if (TerrorPhantom.this.attackPhase == AttackPhase.BOMBING) {
                    this.speed = 2.0F;
                } else {
                    this.speed = 0.5F;
                }
            }

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

    public class TerrorPhantomAttackStrategyGoal extends Goal {
        private int nextSweepTick;
        private boolean lastRolled = false;
        private int turnsUntilSwarm = 0;
        private int turnsUntilBombing = 0;
        private boolean isBombing = false;
        private int bombingTurns = 3;
        public TerrorPhantomAttackStrategyGoal() {
        }

        public boolean canUse() {
            if (TerrorPhantom.this.isPlayerOwned()) {
                return false;
            }
            LivingEntity target = TerrorPhantom.this.getTarget();
            return target != null ? TerrorPhantom.this.canAttack(target, TargetingConditions.forCombat().ignoreLineOfSight()) : false;
        }

        public void start() {
            this.nextSweepTick = this.adjustedTickDelay(10);
            TerrorPhantom.this.attackPhase = AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
            this.turnsUntilBombing = 0;
            this.bombingTurns = 3;
        }

        public void stop() {
            TerrorPhantom.this.anchorPoint = TerrorPhantom.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, TerrorPhantom.this.anchorPoint).above(10 + TerrorPhantom.this.random.nextInt(20));
        }

        public void tick() {
            if (TerrorPhantom.this.attackPhase == AttackPhase.CIRCLE) {
                --this.nextSweepTick;

                if (this.bombingTurns <= 0) {
                    this.isBombing = false;
                }

                if (this.nextSweepTick <= 0) {
                    if (this.isBombing) {
                        this.doBombing();
                        this.bombingTurns--;
                    } else if (this.turnsUntilSwarm <= 0 && TerrorPhantom.this.isHalfHealth) {
                        this.doSwarm();
                        this.turnsUntilSwarm = 5;
                    } else if (this.turnsUntilBombing <= 0 && TerrorPhantom.this.isHalfHealth) {
                        this.isBombing = true;
                        this.turnsUntilBombing = 2;
                    } else {
                        // Do Swooping if true, do Rolling if false
                        if (TerrorPhantom.this.random.nextBoolean()) {
                            this.doSwoop();
                            this.lastRolled = false;
                        } else {
                            if (!this.lastRolled) {
                                this.doRoll();
                                if (TerrorPhantom.this.getHealth() >= TerrorPhantom.this.getMaxHealth() / 2) {
                                    this.lastRolled = true;
                                }
                            } else {
                                this.doSwoop();
                                this.lastRolled = false;
                            }
                        }
                        this.turnsUntilSwarm--;
                        this.turnsUntilBombing--;
                    }
                }
            }
        }

        private void doSwoop() {
            TerrorPhantom.this.attackPhase = AttackPhase.SWOOP;
            TerrorPhantom.this.attackTimer = 80;
            this.setAnchorAboveTarget();
            this.nextSweepTick = this.adjustedTickDelay((100));
            TerrorPhantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 30.0F, 0.45F + TerrorPhantom.this.random.nextFloat() * 0.1F);
        }

        private void doRoll() {
            TerrorPhantom.this.setRolling(true);
            TerrorPhantom.this.attackPhase = AttackPhase.ROLL;
            TerrorPhantom.this.attackTimer = 100;
            this.setAnchorOvershot();
            this.nextSweepTick = this.adjustedTickDelay((200));
            TerrorPhantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 30.0F, 0.45F + TerrorPhantom.this.random.nextFloat() * 0.1F);
        }

        private void doSwarm() {
            TerrorPhantom.this.attackPhase = AttackPhase.SWARM;
            TerrorPhantom.this.attackTimer = 240;
            this.setAnchorAboveTarget();
            this.nextSweepTick = this.adjustedTickDelay((300));
            TerrorPhantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 30.0F, 0.45F + TerrorPhantom.this.random.nextFloat() * 0.1F);
        }

        private void doBombing() {
            TerrorPhantom.this.attackPhase = AttackPhase.BOMBING;
            TerrorPhantom.this.attackTimer = 70;
            if (TerrorPhantom.this.getTarget() != null) {
                TerrorPhantom.this.lookAt(TerrorPhantom.this.getTarget(), 255.0F, 255.0F);
            }
            this.setAnchorOvershot();
            this.nextSweepTick = this.adjustedTickDelay((60));
            TerrorPhantom.this.playSound(SoundEvents.PHANTOM_SWOOP, 30.0F, 0.45F + TerrorPhantom.this.random.nextFloat() * 0.1F);
        }

        private void setAnchorAboveTarget() {
            if (TerrorPhantom.this.getTarget() == null) return;

            int maxHeight = Math.min(TerrorPhantom.this.getTarget().getBlockY() + 32, TerrorPhantom.this.level().getMaxBuildHeight() - 1);
            int targetHeight = TerrorPhantom.this.getTarget().getBlockY() + 20 + TerrorPhantom.this.random.nextInt(12);
            TerrorPhantom.this.anchorPoint = new BlockPos(
                    TerrorPhantom.this.getTarget().getBlockX(),
                    Math.min(targetHeight, maxHeight),
                    TerrorPhantom.this.getTarget().getBlockZ()
            );
            /*if (TerrorPhantom.this.anchorPoint.getY() < TerrorPhantom.this.level().getSeaLevel()) {
                TerrorPhantom.this.anchorPoint = new BlockPos(TerrorPhantom.this.anchorPoint.getX(), TerrorPhantom.this.level().getSeaLevel() + 1, TerrorPhantom.this.anchorPoint.getZ());
            }*/

        }

        private void setAnchorOvershot() {
            if (TerrorPhantom.this.getTarget() == null) return;

            LivingEntity target = TerrorPhantom.this.getTarget();
            Vec3 phantomPos = TerrorPhantom.this.position();
            Vec3 targetPos = target.position();

            Vec3 direction = targetPos.subtract(phantomPos).normalize();

            double overshootDistance = 32 + TerrorPhantom.this.random.nextInt(6);
            Vec3 overshotPos = targetPos.add(direction.scale(overshootDistance));

            int targetHeight = target.getBlockY();

            int maxHeight = Math.min(targetHeight + 32, TerrorPhantom.this.level().getMaxBuildHeight() - 1);
            int finalHeight = Math.min(targetHeight, maxHeight);
            /*if (finalHeight < TerrorPhantom.this.level().getSeaLevel()) {
                finalHeight = TerrorPhantom.this.level().getSeaLevel() + 1;
            }*/

            TerrorPhantom.this.anchorPoint = new BlockPos((int) overshotPos.x, finalHeight, (int) overshotPos.z);
        }
    }

    class TerrorPhantomSweepAttackGoal extends PhantomMoveTargetGoal {
        TerrorPhantomSweepAttackGoal() {
            super();
        }

        public boolean canUse() {
            return !TerrorPhantom.this.isDying() && TerrorPhantom.this.getTarget() != null && TerrorPhantom.this.attackPhase == AttackPhase.SWOOP;
        }

        public boolean canContinueToUse() {
            if (TerrorPhantom.this.isDying()) {
                return false;
            }

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
            TerrorPhantom.this.attackPhase = AttackPhase.CIRCLE;
        }

        public void tick() {
            LivingEntity $$0 = TerrorPhantom.this.getTarget();
            if ($$0 != null) {
                TerrorPhantom.this.moveTargetPoint = new Vec3($$0.getX(), $$0.getY(8), $$0.getZ());
                if (TerrorPhantom.this.getBoundingBox().inflate(0.20000000298023224).intersects($$0.getBoundingBox())) {
                    TerrorPhantom.this.doHurtTarget($$0);
                    TerrorPhantom.this.attackPhase = AttackPhase.CIRCLE;
                    if (!TerrorPhantom.this.isSilent()) {
                        TerrorPhantom.this.level().levelEvent(1039, TerrorPhantom.this.blockPosition(), 0);
                    }
                } /*else if (TerrorPhantom.this.horizontalCollision && !TerrorPhantom.this.attackPhase.equals(AttackPhase.SWOOP)) {
                    TerrorPhantom.this.attackPhase = TerrorPhantom.AttackPhase.CIRCLE;
                }*/

            }
        }
    }

    class TerrorPhantomRollAttackGoal extends PhantomMoveTargetGoal {
        private int grenadeRollsLeft = 2;
        private int grenadeTick = 20;

        TerrorPhantomRollAttackGoal() {
            super();
        }

        public boolean canUse() {
            return !TerrorPhantom.this.isDying() && TerrorPhantom.this.getTarget() != null && TerrorPhantom.this.attackPhase == AttackPhase.ROLL;
        }

        public boolean canContinueToUse() {
            if (TerrorPhantom.this.isDying()) {
                return false;
            }

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
            this.grenadeRollsLeft = 2;
            this.grenadeTick = 20;
        }

        public void stop() {
            TerrorPhantom.this.setRolling(false);
            TerrorPhantom.this.attackPhase = AttackPhase.CIRCLE;
        }

        public void tick() {
            BlockPos $$0 = TerrorPhantom.this.anchorPoint;
            TerrorPhantom.this.moveTargetPoint = new Vec3($$0.getX(), TerrorPhantom.this.getTarget().getY(24), $$0.getZ());

            TerrorPhantom.this.invulnerableTime = 20;
            TerrorPhantom.this.addEffect(new MobEffectInstance(ModEffects.BULLET_PROTECTION.get(), 20));

            //if (TerrorPhantom.this.attackTimer == 140 - 25 || TerrorPhantom.this.attackTimer == 140 - 85) {
            if (--this.grenadeTick == 0 && this.grenadeRollsLeft > 0) {
                this.dropGrenades(TerrorPhantom.this);
                this.grenadeTick = 20;
                this.grenadeRollsLeft--;
            }

            /*if (this.grenadeRollsLeft <= 0) {
                TerrorPhantom.this.attackTimer = 40;
            }*/

            if (TerrorPhantom.this.attackTimer <= 0) {
                TerrorPhantom.this.setRolling(false);
            }
        }

        private void dropGrenades(TerrorPhantom terrorPhantom) {
            if (terrorPhantom.level() instanceof ServerLevel serverLevel) {
                BlockPos pos = terrorPhantom.blockPosition();
                serverLevel.playSound(terrorPhantom, pos, ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.HOSTILE, 30F, 1F);

                RandomSource random = terrorPhantom.getRandom();
                int grenadeCount = 6 + random.nextInt(3);

                for (int i = 0; i < grenadeCount; i++) {
                    ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(terrorPhantom.level(), terrorPhantom, 50 + terrorPhantom.level().random.nextInt(9));

                    if (i == 1 && TerrorPhantom.this.isHalfHealth && TerrorPhantom.this.random.nextInt(1, 5) == 1) {
                        grenade = new ThrowableMolotovCocktailEntity(terrorPhantom.level(), terrorPhantom, 60 + terrorPhantom.level().random.nextInt(9));
                    }

                    grenade.setPos(pos.getX(), pos.getY() + 1, pos.getZ());

                    double xVelocity = (random.nextDouble() - 0.5) * 1.5;
                    double yVelocity = (0.4 + random.nextDouble() * 0.6) * -1;
                    double zVelocity = (random.nextDouble() - 0.5) * 1.5;

                    grenade.setDeltaMovement(xVelocity, yVelocity, zVelocity);
                    grenade.terrorPhantomThrown = true;

                    serverLevel.addFreshEntity(grenade);
                }
            }
        }
    }

    public class TerrorPhantomSwarmAttackGoal extends PhantomMoveTargetGoal {
        private final TerrorPhantom terrorPhantom;
        private final int summonInterval;
        private int tickCounter;

        public TerrorPhantomSwarmAttackGoal(TerrorPhantom terrorPhantom, int summonInterval) {
            super();
            this.terrorPhantom = terrorPhantom;
            this.summonInterval = summonInterval;
            this.tickCounter = 0;
        }

        public boolean canUse() {
            return !TerrorPhantom.this.isDying() && TerrorPhantom.this.getTarget() != null && TerrorPhantom.this.attackPhase == AttackPhase.SWARM;
        }

        public void tick() {
            this.tickCounter++;

            if (TerrorPhantom.this.isHalfHealth) {
                TerrorPhantom.this.setHealth(TerrorPhantom.this.getHealth() + 1);
            }

            TerrorPhantom.this.invulnerableTime = 20;
            TerrorPhantom.this.addEffect(new MobEffectInstance(ModEffects.BULLET_PROTECTION.get(), 20));

            if (this.tickCounter >= this.summonInterval) {
                this.tickCounter = 0;
                summonPhantoms();
            }
        }

        private void summonPhantoms() {
            int numPhantoms = 1 + TerrorPhantom.this.random.nextInt(1);
            for (int i = 0; i < numPhantoms; i++) {
                Phantom newPhantom = EntityType.PHANTOM.create(this.terrorPhantom.level());
                if (newPhantom != null) {
                    if (this.terrorPhantom.random.nextInt(1, 5) == 1) {
                        newPhantom = new PhantomGunner(ModEntities.PHANTOM_GUNNER.get(), this.terrorPhantom.level());
                    }

                    Vec3 spawnPos = this.terrorPhantom.position().add(this.terrorPhantom.getRandom().nextGaussian() * 2, 2, this.terrorPhantom.getRandom().nextGaussian() * 2);
                    newPhantom.moveTo(spawnPos.x, spawnPos.y, spawnPos.z);
                    newPhantom.finalizeSpawn((ServerLevelAccessor) this.terrorPhantom.level(), this.terrorPhantom.level().getCurrentDifficultyAt(this.terrorPhantom.blockPosition()), MobSpawnType.EVENT, null, null);
                    newPhantom.setTarget(this.terrorPhantom.getTarget());

                    this.terrorPhantom.level().addFreshEntity(newPhantom);
                    newPhantom.addTag("GunnerPatroller");

                    if (this.terrorPhantom.level().isDay() && newPhantom.getType().is(ModTags.Entities.UNDEAD)) {
                        newPhantom.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0, false, true));
                        newPhantom.extinguishFire();
                    }

                    if (this.terrorPhantom.random.nextInt(1, 4) == 1 && (!(newPhantom instanceof PhantomGunner))) {
                        ThrowableExplosiveChargeEntity explosiveChargeEntity = new ThrowableExplosiveChargeEntity(ModEntities.THROWABLE_EXPLOSIVE_CHARGE.get(), this.terrorPhantom.level());
                        this.terrorPhantom.level().addFreshEntity(explosiveChargeEntity);
                        explosiveChargeEntity.startRiding(newPhantom);
                    }
                }
            }
        }
    }

    class TerrorPhantomBombingAttackGoal extends PhantomMoveTargetGoal {
        private int grenadeTick = 0;

        TerrorPhantomBombingAttackGoal() {
            super();
        }

        public boolean canUse() {
            return !TerrorPhantom.this.isDying() && TerrorPhantom.this.getTarget() != null && TerrorPhantom.this.attackPhase == AttackPhase.BOMBING;
        }

        public boolean canContinueToUse() {
            if (TerrorPhantom.this.isDying()) {
                return false;
            }

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
            this.grenadeTick = 2;
        }

        public void stop() {
            TerrorPhantom.this.attackPhase = AttackPhase.CIRCLE;
        }

        public void tick() {
            BlockPos $$0 = TerrorPhantom.this.anchorPoint;
            TerrorPhantom.this.moveTargetPoint = new Vec3($$0.getX(), TerrorPhantom.this.getTarget().getY(12), $$0.getZ());

            //this.setAnchorOvershot();

            TerrorPhantom.this.invulnerableTime = 20;
            TerrorPhantom.this.addEffect(new MobEffectInstance(ModEffects.BULLET_PROTECTION.get(), 10));

            //if (TerrorPhantom.this.attackTimer == 140 - 25 || TerrorPhantom.this.attackTimer == 140 - 85) {
            if (--this.grenadeTick == 0) {
                this.dropGrenade(TerrorPhantom.this);
                this.grenadeTick = 2;
            }

            /*if (this.grenadeRollsLeft <= 0) {
                TerrorPhantom.this.attackTimer = 40;
            }*/

            if (TerrorPhantom.this.attackTimer <= 0) {
                TerrorPhantom.this.setRolling(false);
            }
        }

        private void dropGrenade(TerrorPhantom terrorPhantom) {
            if (terrorPhantom.level() instanceof ServerLevel serverLevel) {
                BlockPos pos = terrorPhantom.blockPosition();
                serverLevel.playSound(terrorPhantom, pos, ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.HOSTILE, 30F, 1F);

                RandomSource random = terrorPhantom.getRandom();
                int grenadeCount = 1;

                for (int i = 0; i < grenadeCount; i++) {
                    ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(terrorPhantom.level(), terrorPhantom, 100);

                    grenade.setPos(pos.getX(), pos.getY() + 1, pos.getZ());

                    double xVelocity = 0;
                    double yVelocity = -1;
                    double zVelocity = 0;

                    grenade.setDeltaMovement(xVelocity, yVelocity, zVelocity);
                    grenade.terrorPhantomThrown = true;

                    serverLevel.addFreshEntity(grenade);

                    grenade.setShouldBounce(false);
                }
            }
        }
    }

    public class TerrorPhantomCircleAroundAnchorGoal extends PhantomMoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        public TerrorPhantomCircleAroundAnchorGoal() {
            super();
        }

        public boolean canUse() {
            return !TerrorPhantom.this.isDying() && (TerrorPhantom.this.getTarget() == null || (TerrorPhantom.this.attackPhase == AttackPhase.CIRCLE || TerrorPhantom.this.attackPhase == AttackPhase.SWARM));
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
            TerrorPhantom.this.moveTargetPoint = Vec3.atLowerCornerOf(TerrorPhantom.this.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), 24, (double)(this.distance * Mth.sin(this.angle)));
        }
    }

    public class TerrorPhantomAttackPlayerTargetGoal extends Goal {
        private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);
        private int nextScanTick = reducedTickDelay(20);

        public TerrorPhantomAttackPlayerTargetGoal() {
        }

        public boolean canUse() {
            if (this.nextScanTick > 0) {
                --this.nextScanTick;
                return false;
            } else {
                this.nextScanTick = reducedTickDelay(60);
                List<Player> players = TerrorPhantom.this.level().getNearbyPlayers(this.attackTargeting, TerrorPhantom.this, TerrorPhantom.this.getBoundingBox().inflate(64.0, 100.0, 64.0));
                if (!players.isEmpty()) {
                    Iterator var2 = players.iterator();

                    while(var2.hasNext()) {
                        Player player = (Player)var2.next();
                        if (TerrorPhantom.this.canAttack(player, TargetingConditions.forCombat().ignoreLineOfSight())) {
                            TerrorPhantom.this.setTarget(player);
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        public boolean canContinueToUse() {
            LivingEntity $$0 = TerrorPhantom.this.getTarget();
            return $$0 != null ? TerrorPhantom.this.canAttack($$0, TargetingConditions.forCombat().ignoreLineOfSight()) : false;
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

    public boolean isRolling() {
        return this.entityData.get(IS_ROLLING);
    }

    public void setRolling(boolean rolling) {
        this.entityData.set(IS_ROLLING, rolling);
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

        this.entityData.define(IS_ROLLING, false);
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
