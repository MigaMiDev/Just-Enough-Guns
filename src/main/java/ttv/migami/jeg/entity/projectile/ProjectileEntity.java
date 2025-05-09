package ttv.migami.jeg.entity.projectile;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.client.medal.MedalType;
import ttv.migami.jeg.common.BoundingBoxManager;
import ttv.migami.jeg.common.ChargeTracker;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.Gun.Projectile;
import ttv.migami.jeg.common.SpreadTracker;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;
import ttv.migami.jeg.event.GunProjectileHitEvent;
import ttv.migami.jeg.event.KillEffectEvent;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.interfaces.IDamageable;
import ttv.migami.jeg.interfaces.IExplosionDamageable;
import ttv.migami.jeg.interfaces.IHeadshotBox;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.*;
import ttv.migami.jeg.util.BufferUtil;
import ttv.migami.jeg.util.GunEnchantmentHelper;
import ttv.migami.jeg.util.GunModifierHelper;
import ttv.migami.jeg.util.ReflectionUtil;
import ttv.migami.jeg.util.math.ExtendedEntityRayTraceResult;
import ttv.migami.jeg.world.ProjectileExplosion;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

public class ProjectileEntity extends Entity implements IEntityAdditionalSpawnData
{
    private static final Random RANDOM = new Random();
    private static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
    private static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;

    protected int shooterId;
    protected LivingEntity shooter;
    protected Gun modifiedGun;
    protected Gun.General general;
    protected Gun.Projectile projectile;
    private ItemStack weapon = ItemStack.EMPTY;
    private ItemStack item = ItemStack.EMPTY;
    protected float additionalDamage = 0.0F;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;
    protected boolean hitWater = false;
    protected float chargeProgress;

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        this(entityType, worldIn);
        this.shooterId = shooter.getId();
        this.shooter = shooter;
        this.modifiedGun = modifiedGun;
        this.general = modifiedGun.getGeneral();
        this.projectile = modifiedGun.getProjectile();
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
        this.modifiedGravity = modifiedGun.getProjectile().isGravity() ? GunModifierHelper.getModifiedProjectileGravity(weapon, -0.04) : 0.0;
        this.life = GunModifierHelper.getModifiedProjectileLife(weapon, this.projectile.getLife());

        if (shooter instanceof ServerPlayer player) {
            this.chargeProgress = player.getPersistentData().getFloat("ChargeProgress");
        } else if (shooter instanceof Player player) {
            this.chargeProgress = ChargeTracker.getChargeProgress(player, weapon);
        } else {
            this.chargeProgress = 0.25F;
        }
        if (shooter instanceof Player player) {
            ChargeTracker.clearLastChargeProgress(player.getUUID());
        }

        /* Get speed and set motion */
        Vec3 dir = this.getDirection(shooter, weapon, item, modifiedGun);
        double speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(weapon);
        double speed = GunModifierHelper.getModifiedProjectileSpeed(weapon, this.projectile.getSpeed() * speedModifier);
        //speed = GunModifierHelper.getChargeSpeed(weapon, (float) speed, this.chargeProgress);
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();

        /* Spawn the projectile half way between the previous and current position */
        double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
        double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
        double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
        this.setPos(posX, posY, posZ);

        Item ammo = ForgeRegistries.ITEMS.getValue(this.projectile.getItem());
        if(ammo != null)
        {
            int customModelData = -1;
            if(weapon.getTag() != null)
            {
                if(weapon.getTag().contains("Model", Tag.TAG_COMPOUND))
                {
                    ItemStack model = ItemStack.of(weapon.getTag().getCompound("Model"));
                    if(model.getTag() != null && model.getTag().contains("CustomModelData"))
                    {
                        customModelData = model.getTag().getInt("CustomModelData");
                    }
                }
            }
            ItemStack ammoStack = new ItemStack(ammo);
            if(customModelData != -1)
            {
                ammoStack.getOrCreateTag().putInt("CustomModelData", customModelData);
            }
            this.item = ammoStack;
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        return this.entitySize;
    }

    public Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        float gunSpread = GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());

        if(gunSpread == 0F)
        {
            return this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        }

        if(shooter instanceof Player)
        {
            if(!modifiedGun.getGeneral().isAlwaysSpread())
            {
                gunSpread *= SpreadTracker.get((Player) shooter).getSpread(item);
            }

            if(ModSyncedDataKeys.AIMING.getValue((Player) shooter))
            {
                gunSpread *= 0.5F;
            }
        }
        else {
            gunSpread *= shooter.level().getDifficulty() != Difficulty.HARD ? 10F : 5F;
        }

        // New spread vector code provided by Poly-1810 and used with permission.
        // This fix was figured out by unze2unze4 and implemented by Poly into their CGM Refined fork.
        // Big thanks to both of them for this fix!
        gunSpread = Math.min(gunSpread, 170F) * 0.5F * Mth.DEG_TO_RAD;

        Vec3 vecforwards = this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        Vec3 vecupwards = this.getVectorFromRotation(shooter.getXRot() + 90F, shooter.getYRot());
        Vec3 vecsideways = vecforwards.cross(vecupwards);

        float theta = random.nextFloat() * 2F * (float) Math.PI;
        float r = Mth.sqrt(random.nextFloat()) * (float) Math.tan((double) gunSpread);

        float a1 = Mth.cos(theta) * r;
        float a2 = Mth.sin(theta) * r;

        return vecforwards.add(vecsideways.scale(a1)).add(vecupwards.scale(a2)).normalize();

        //return this.getVectorFromRotation(shooter.getXRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread, shooter.getYHeadRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread);
    }

    public void setWeapon(ItemStack weapon)
    {
        this.weapon = weapon.copy();
    }

    public ItemStack getWeapon()
    {
        return this.weapon;
    }

    public void setItem(ItemStack item)
    {
        this.item = item;
    }

    public ItemStack getItem()
    {
        return this.item;
    }

    public void setAdditionalDamage(float additionalDamage)
    {
        this.additionalDamage = additionalDamage;
    }

    public double getModifiedGravity()
    {
        return this.modifiedGravity;
    }

    @Override
    public void tick()
    {
        super.tick();
        this.updateHeading();
        this.onProjectileTick();

        if(!this.level().isClientSide())
        {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            HitResult result = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
            if(result.getType() != HitResult.Type.MISS)
            {
                endVec = result.getLocation();
            }

            List<EntityResult> hitEntities = null;
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COLLATERAL.get(), this.weapon);
            if(level == 0 && !this.projectile.isCollateral())
            {
                EntityResult entityResult = this.findEntityOnPath(startVec, endVec);
                if(entityResult != null)
                {
                    hitEntities = Collections.singletonList(entityResult);
                }
            }
            else
            {
                hitEntities = this.findEntitiesOnPath(startVec, endVec);
            }

            if(hitEntities != null && hitEntities.size() > 0)
            {
                for(EntityResult entityResult : hitEntities)
                {
                    result = new ExtendedEntityRayTraceResult(entityResult);
                    if(((EntityHitResult) result).getEntity() instanceof Player)
                    {
                        Player player = (Player) ((EntityHitResult) result).getEntity();

                        if(this.shooter instanceof Player && !((Player) this.shooter).canHarmPlayer(player))
                        {
                            result = null;
                        }
                    }
                    if(result != null)
                    {
                        this.onHit(result, startVec, endVec);
                    }
                }
            }
            else
            {
                this.onHit(result, startVec, endVec);
            }
        }

        double nextPosX = this.getX() + this.getDeltaMovement().x();
        double nextPosY = this.getY() + this.getDeltaMovement().y();
        double nextPosZ = this.getZ() + this.getDeltaMovement().z();
        this.setPos(nextPosX, nextPosY, nextPosZ);

        if(this.projectile.isGravity())
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        if(this.tickCount >= this.life)
        {
            if(this.isAlive())
            {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }

    /**
     * A simple method to perform logic on each tick of the projectile. This method is appropriate
     * for spawning particles. Override {@link #tick()} to make changes to physics
     */
    protected void onProjectileTick()
    {
        if (!this.level().isClientSide) {
            if (this.isUnderWater()) {
                sendParticlesToAll(
                        (ServerLevel) this.level(),
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

    /**
     * Will spawn a particle just before being deleted!
     */
    protected void impactEffect()
    {
    }

    /**
     * Called when the projectile has run out of it's life. In other words, the projectile managed
     * to not hit any blocks and instead aged. The grenade uses this to explode in the air.
     */
    protected void onExpired()
    {
    }

    /**
     * Checks if there are players nearby, if true, it will play a sound for them and add them to a
     * Hashmap, so they don't hear the same bullet twice.
     */
    private void playSoundForNearbyPlayers(Entity entity) {
        if (entity == this.shooter) {
            return;
        }

        if (this.shooter instanceof Player player) {
            this.level().playSound(player, this, ModSounds.BULLET_CLOSE.get(),  SoundSource.PLAYERS, 0.7F, 0.0F);
        }
        else {
            this.playSound(ModSounds.BULLET_CLOSE.get(), 0.7F, 1F);
        }
        //entity.playSound(ModSounds.BULLET_CLOSE.get(), 0.7F, 1F);
    }

    @Nullable
    protected EntityResult findEntityOnPath(Vec3 startVec, Vec3 endVec)
    {
        Vec3 hitVec = null;
        Entity hitEntity = null;
        boolean headshot = false;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        double closestDistance = Double.MAX_VALUE;
        for(Entity entity : entities)
        {
            if(!entity.equals(this.shooter))
            {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if(result == null)
                    continue;
                Vec3 hitPos = result.getHitPos();
                double distanceToHit = startVec.distanceTo(hitPos);
                if(distanceToHit < closestDistance)
                {
                    hitVec = hitPos;
                    hitEntity = entity;
                    closestDistance = distanceToHit;
                    headshot = result.isHeadshot();
                }
            }
        }

        List<Entity> flyByEntities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(3), PROJECTILE_TARGETS);
        for(Entity entity : flyByEntities)
        {
            {
                if (entity != this.shooter && entity instanceof Player) {
                    this.playSoundForNearbyPlayers(this);
                }
            }
        }
        return hitEntity != null ? new EntityResult(hitEntity, hitVec, headshot) : null;
    }

    @Nullable
    protected List<EntityResult> findEntitiesOnPath(Vec3 startVec, Vec3 endVec)
    {
        List<EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        for(Entity entity : entities)
        {
            if(!entity.equals(this.shooter))
            {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if(result == null)
                    continue;
                hitEntities.add(result);
            }
        }
        return hitEntities;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private EntityResult getHitResult(Entity entity, Vec3 startVec, Vec3 endVec)
    {
        double expandHeight = entity instanceof Player && !entity.isCrouching() ? 0.0625 : 0.0;
        AABB boundingBox = entity.getBoundingBox();
        if(Config.COMMON.gameplay.improvedHitboxes.get() && entity instanceof ServerPlayer && this.shooter instanceof ServerPlayer)
        {
            int ping = (int) Math.floor((((ServerPlayer) this.shooter).latency / 1000.0) * 20.0 + 0.5);
            boundingBox = BoundingBoxManager.getBoundingBox((Player) entity, ping);
        }
        boundingBox = boundingBox.expandTowards(0, expandHeight, 0);

        Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);
        Vec3 grownHitPos = boundingBox.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get()).clip(startVec, endVec).orElse(null);
        if(hitPos == null && grownHitPos != null)
        {
            HitResult raytraceresult = rayTraceBlocks(this.level(), new ClipContext(startVec, grownHitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
            if(raytraceresult.getType() == HitResult.Type.BLOCK)
            {
                return null;
            }
            hitPos = grownHitPos;
        }

        /* Check for headshot */
        boolean headshot = false;
        if(Config.COMMON.gameplay.enableHeadShots.get() && entity instanceof LivingEntity)
        {
            IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
            if(headshotBox != null)
            {
                AABB box = headshotBox.getHeadshotBox((LivingEntity) entity);
                if(box != null)
                {
                    box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
                    Optional<Vec3> headshotHitPos = box.clip(startVec, endVec);
                    if(!headshotHitPos.isPresent())
                    {
                        box = box.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get());
                        headshotHitPos = box.clip(startVec, endVec);
                    }
                    if(headshotHitPos.isPresent() && (hitPos == null || headshotHitPos.get().distanceTo(hitPos) < 0.5))
                    {
                        hitPos = headshotHitPos.get();
                        headshot = true;
                    }
                }
            }

            // Helmets against headshots!
            if (headshot && entity instanceof LivingEntity livingEntity) {
                ItemStack helmet = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.is(Items.TURTLE_HELMET) && this.random.nextFloat() < 0.2F) {
                    headshot = false;

                    // If true, remove the entities helmet!
                    if (this.random.nextBoolean()) {
                        livingEntity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        if (!helmet.isEmpty() && !livingEntity.level().isClientSide) {
                            ItemEntity flyingHelmet = new ItemEntity(
                                    livingEntity.level(),
                                    livingEntity.getX(),
                                    livingEntity.getY() + livingEntity.getBbHeight(),
                                    livingEntity.getZ(),
                                    helmet
                            );

                            flyingHelmet.setDeltaMovement(
                                    (this.random.nextDouble() - 0.5) * 0.3,
                                    0.5 + this.random.nextDouble() * 0.5,
                                    (this.random.nextDouble() - 0.5) * 0.3
                            );
                            livingEntity.level().addFreshEntity(flyingHelmet);
                            flyingHelmet.setDeltaMovement(flyingHelmet.getDeltaMovement().add(0, 1.0, 0));

                        }
                    }
                }
            }
        }

        if(hitPos == null)
        {
            return null;
        }

        return new EntityResult(entity, hitPos, headshot);
    }

    private void onHit(HitResult result, Vec3 startVec, Vec3 endVec)
    {
        if(MinecraftForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this)))
        {
            return;
        }

        if(result instanceof BlockHitResult blockHitResult)
        {
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = this.level().getBlockState(pos);
            if(blockHitResult.getType() == HitResult.Type.MISS || this.projectile.ignoresBlocks() || state.is(ModBlocks.DYNAMIC_LIGHT.get()))
            {
                return;
            }

            Vec3 hitVec = result.getLocation();
            Block block = state.getBlock();

            if (weapon.getItem() != ModItems.SUBSONIC_RIFLE.get() && weapon.getItem() != ModItems.SUPERSONIC_SHOTGUN.get() &&
                    weapon.getItem() != ModItems.HYPERSONIC_CANNON.get()) {
                this.level().gameEvent(this, GameEvent.BLOCK_DESTROY, this.getPosition(1F));
            }

            if(this.shooter instanceof Player) {
                if(Config.COMMON.gameplay.griefing.enableGlassBreaking.get() && state.is(ModTags.Blocks.FRAGILE))
                {
                    float destroySpeed = state.getDestroySpeed(this.level(), pos);
                    if(destroySpeed >= 0)
                    {
                        float chance = Config.COMMON.gameplay.griefing.fragileBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                        if(this.random.nextFloat() < chance)
                        {
                            if(this.level().getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
                                if (state.is(Blocks.MELON)) {
                                    int melonCount = RANDOM.nextInt(6) + 1;
                                    ItemStack melonStack = new ItemStack(Items.MELON_SLICE, melonCount);
                                    ItemEntity melonEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, melonStack);
                                    this.level().addFreshEntity(melonEntity);

                                    int melonSeeds = RANDOM.nextInt(2) + 1;
                                    ItemStack seedsStack = new ItemStack(Items.MELON_SEEDS, melonSeeds);
                                    ItemEntity seedsEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, seedsStack);
                                    this.level().addFreshEntity(seedsEntity);
                                }
                                if (state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN)) {
                                    int melonSeeds = RANDOM.nextInt(6) + 1;
                                    ItemStack seedsStack = new ItemStack(Items.PUMPKIN_SEEDS, melonSeeds);
                                    ItemEntity seedsEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, seedsStack);
                                    this.level().addFreshEntity(seedsEntity);
                                }
                                if (state.is(Blocks.PUMPKIN)) {
                                    int melonSeeds = RANDOM.nextInt(2) + 1;
                                    ItemStack seedsStack = new ItemStack(Items.PUMPKIN_SEEDS, melonSeeds);
                                    ItemEntity seedsEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, seedsStack);
                                    this.level().addFreshEntity(seedsEntity);

                                    ItemStack pumpkinStack = new ItemStack(Items.CARVED_PUMPKIN, 1);
                                    ItemEntity pumpkiEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, pumpkinStack);
                                    this.level().addFreshEntity(pumpkiEntity);
                                }
                                if (state.is(Blocks.COCOA)) {
                                    int melonSeeds = RANDOM.nextInt(3) + 1;
                                    ItemStack seedsStack = new ItemStack(Items.COCOA_BEANS, melonSeeds);
                                    ItemEntity seedsEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, seedsStack);
                                    this.level().addFreshEntity(seedsEntity);
                                }
                                if (state.is(Blocks.BEEHIVE) || state.is(Blocks.BEE_NEST)) {
                                    int melonSeeds = RANDOM.nextInt(3) + 1;
                                    ItemStack seedsStack = new ItemStack(Items.HONEYCOMB, melonSeeds);
                                    ItemEntity seedsEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, seedsStack);
                                    this.level().addFreshEntity(seedsEntity);
                                }
                                if (state.is(ModBlocks.BOOHIVE.get()) || state.is(ModBlocks.BOO_NEST.get())) {
                                    int melonSeeds = RANDOM.nextInt(5) + 1;
                                    ItemStack seedsStack = new ItemStack(ModItems.ECTOPLASM.get(), melonSeeds);
                                    ItemEntity seedsEntity = new ItemEntity(this.level(), pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, seedsStack);
                                    this.level().addFreshEntity(seedsEntity);
                                }
                            }
                            if (!state.is(Blocks.PUMPKIN) && !state.is(Blocks.CARVED_PUMPKIN) && !state.is(Blocks.JACK_O_LANTERN)) {
                                this.level().destroyBlock(pos, Config.COMMON.gameplay.griefing.fragileBlockDrops.get());
                            } else {
                                this.level().destroyBlock(pos, false);
                            }
                        }
                    }
                }

                boolean advantageFlag = false;
                if (Config.COMMON.gameplay.gunAdvantage.get() && this.projectile.getAdvantage().equals(ModTags.Entities.HEAVY.location()) ||
                        this.projectile.getAdvantage().equals(ModTags.Entities.VERY_HEAVY.location())) {
                    advantageFlag = true;
                } else if (!Config.COMMON.gameplay.gunAdvantage.get()) {
                    advantageFlag = true;
                }

                boolean explosiveAmmo = false;
                if (Gun.hasAttachmentEquipped(weapon, IAttachment.Type.BARREL)) {
                    if (Gun.getAttachment(IAttachment.Type.BARREL, weapon).getItem() == ModItems.EXPLOSIVE_MUZZLE.get()) {
                        explosiveAmmo = true;
                    }
                }

                boolean fireAmmo = this instanceof BlazeProjectileEntity;

                boolean breakBlocks = true;
                if (!(this.shooter instanceof Player) && !this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    breakBlocks = false;
                }

                if(breakBlocks && Config.COMMON.gameplay.griefing.enableWoodBreaking.get() && ((state.is(BlockTags.MINEABLE_WITH_AXE) && state.is(BlockTags.DRAGON_IMMUNE)) || state.is(ModTags.Blocks.WOOD)) && (fireAmmo || advantageFlag || explosiveAmmo))
                {
                    float destroySpeed = state.getDestroySpeed(this.level(), pos);
                    if(destroySpeed >= 0)
                    {
                        float chance = Config.COMMON.gameplay.griefing.woodBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                        if(this.random.nextFloat() < chance)
                        {
                            this.level().destroyBlock(pos, false);
                        }
                    }
                }

                if (explosiveAmmo) {
                    if(breakBlocks && Config.COMMON.gameplay.griefing.enableStoneBreaking.get() && ((state.is(BlockTags.MINEABLE_WITH_PICKAXE) && state.is(BlockTags.DRAGON_IMMUNE)) || state.is(ModTags.Blocks.STONE)))
                    {
                        float destroySpeed = state.getDestroySpeed(this.level(), pos);
                        if(destroySpeed >= 0)
                        {
                            float chance = Config.COMMON.gameplay.griefing.stoneBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                            if(this.random.nextFloat() < chance)
                            {
                                this.level().destroyBlock(pos, false);
                            }
                        }
                    }
                }
            }

            if(!state.canBeReplaced())
            {
                this.remove(RemovalReason.KILLED);
            }

            if(block instanceof IDamageable)
            {
                ((IDamageable) block).onBlockDamaged(this.level(), state, pos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
            }

            this.onHitBlock(state, pos, blockHitResult.getDirection(), hitVec.x, hitVec.y, hitVec.z);

            if(block instanceof TargetBlock targetBlock)
            {
                int power = ReflectionUtil.updateTargetBlock(targetBlock, this.level(), state, blockHitResult, this);
                if(this.shooter instanceof ServerPlayer serverPlayer)
                {
                    serverPlayer.awardStat(Stats.TARGET_HIT);
                    CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, this, blockHitResult.getLocation(), power);
                }
            }

            if(block instanceof BellBlock bell)
            {
                bell.attemptToRing(this.level(), pos, blockHitResult.getDirection());
            }

            // Fire
            int fireStarterLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_STARTER.get(), this.weapon);
            if(fireStarterLevel > 0 && Config.COMMON.gameplay.griefing.setFireToBlocks.get())
            {
                if(this.level().getRandom().nextFloat() > 0.70F) {
                    BlockPos offsetPos = pos.relative(blockHitResult.getDirection());
                    if(BaseFireBlock.canBePlacedAt(this.level(), offsetPos, blockHitResult.getDirection()))
                    {
                        BlockState fireState = BaseFireBlock.getState(this.level(), offsetPos);
                        this.level().setBlock(offsetPos, fireState, 11);
                        ((ServerLevel) this.level()).sendParticles(ParticleTypes.LAVA, hitVec.x - 1.0 + this.random.nextDouble() * 2.0, hitVec.y, hitVec.z - 1.0 + this.random.nextDouble() * 2.0, 4, 0, 0, 0, 0);
                    }
                }
            }
            return;
        }

        if(result instanceof ExtendedEntityRayTraceResult entityHitResult)
        {
            Entity entity = entityHitResult.getEntity();
            if(entity.getId() == this.shooterId)
            {
                return;
            }

            if (entity.hasPassenger(this.shooter))
            {
                return;
            }

            if(this.shooter instanceof Player player)
            {
                if(entity.hasIndirectPassenger(player))
                {
                    return;
                }
            }

            // Prevent Raid/Patrol mobs from hitting each other
            if (this.shooter.getTags().contains("GunnerPatroller") && entity.getTags().contains("GunnerPatroller")) {
                return;
            }
            if (entity instanceof TerrorPhantom terrorPhantom && terrorPhantom.isPlayerOwned()) {
                return;
            }
            if (this.shooter instanceof PhantomGunner phantomGunner) {
                if (phantomGunner.isPlayerOwned() && phantomGunner.getPlayer() != null) {
                    if (phantomGunner.getPlayer() == this.shooter) {
                        return;
                    }
                }
            }
            if (entity instanceof PhantomGunner phantomGunner) {
                if (phantomGunner.isPlayerOwned() && phantomGunner.getPlayer() != null) {
                    if (phantomGunner.getPlayer() == this.shooter) {
                        return;
                    }
                }
            }
            if (this.shooter.getTags().contains("PlayerOwned") && entity.getTags().contains("PlayerOwned")) {
                return;
            }
            if (this.shooter.getTags().contains("PlayerOwned") && !(entity instanceof Enemy) && !(entity instanceof Player)) {
                return;
            }

            // Entities of the same type are not allowed to hit each other
            if (this.shooter instanceof PathfinderMob pathfinderMob && pathfinderMob.getType() == entity.getType()) {
                if (this.level().random.nextFloat() < 0.9) {
                    return;
                }
            }

            // Fire
            int fireStarterLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_STARTER.get(), this.weapon);
            if(fireStarterLevel > 0)
            {
                entity.setSecondsOnFire(2);
            }

            if (Gun.hasAttachmentEquipped(weapon, IAttachment.Type.BARREL)) {
                if (Gun.getAttachment(IAttachment.Type.BARREL, weapon).getItem() == ModItems.EXPLOSIVE_MUZZLE.get()) {
                    PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageExplosiveAmmo(result.getLocation().x, result.getLocation().y, result.getLocation().z));
                }
            }

            this.onHitEntity(entity, result.getLocation(), startVec, endVec, entityHitResult.isHeadshot());

            int collateralLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COLLATERAL.get(), weapon);

            if(collateralLevel == 0 && !this.ignoreEntity(entity))
            {
                if (!(this instanceof RocketEntity) && !this.projectile.isCollateral() && !(this instanceof FlameProjectileEntity)) {
                    this.remove(RemovalReason.KILLED);
                }
            }

            if (this.shooter instanceof Player) {
                entity.invulnerableTime = 0;
            }
        }
    }

    public float advantageMultiplier(Entity entity)
    {
        ResourceLocation advantage = this.getProjectile().getAdvantage();
        float advantageMultiplier = 1F;

        if (!advantage.equals(ModTags.Entities.NONE.location()))
        {
            // Entity type disadvantage by weight!
            if (entity.getType().is(ModTags.Entities.HEAVY))
            {
                if (advantage.equals(ModTags.Entities.HEAVY.location()) ||
                        advantage.equals(ModTags.Entities.VERY_HEAVY.location()))
                {
                    advantageMultiplier = 1.25F;
                }
                else advantageMultiplier = 0.50F;
            }
            else if (entity.getType().is(ModTags.Entities.VERY_HEAVY))
            {
                if (advantage.equals(ModTags.Entities.VERY_HEAVY.location()))
                {
                    advantageMultiplier = 1.0F;
                }
                else if (advantage.equals(ModTags.Entities.HEAVY.location()))
                {
                    advantageMultiplier = 0.5F;
                }
                else advantageMultiplier = 0.25F;
            }

            // Deal extra damage and light on fire the undead!
            if (advantage.equals(ModTags.Entities.UNDEAD.location()))
            {
                if (isUndead(entity) || entity.getType().is(ModTags.Entities.GHOST))
                {
                    advantageMultiplier = 1.25F;
                    entity.setSecondsOnFire(2);
                } else
                {
                    advantageMultiplier = 0.75F;
                }
            }
        }

        return advantageMultiplier;
    }

    protected boolean ignoreEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.PLAYER_BULLET_PROTECTION.get()) && this.shooter instanceof Player) {
            return true;
        }

        if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ModEffects.BULLET_PROTECTION.get())) {
            return true;
        }

        ResourceLocation advantage = this.getProjectile().getAdvantage();
        if(entity.getType().is(ModTags.Entities.GHOST) && (!advantage.equals(ModTags.Entities.UNDEAD.location())))
        {
            if (this.getProjectile().getItem().equals(ModItems.SPECTRE_ROUND.getId())) return false;
            return true;
        }

        if (this.shooter.getMainHandItem().is(ModItems.HOLY_SHOTGUN.get()) && !isUndead(entity) && !(entity instanceof Enemy)) {
            return true;
        }

        return false;
    }

    protected boolean isUndead(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.getMobType().equals(MobType.UNDEAD) || entity.getType().is(ModTags.Entities.UNDEAD)) {
                return true;
            }
        }
        return false;
    }

    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
    {
        if (this.ignoreEntity(entity)) {
            return;
        }

        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.weapon, this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        if (Config.COMMON.gameplay.gunAdvantage.get()) {
            damage *= advantageMultiplier(entity);
        }

        if(headshot)
        {
            //damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();
            damage *= this.getProjectile().getHeadshotMultiplier();
        }

        DamageSource source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, this.shooter);
        if (entity instanceof EnderMan) {
            source = this.shooter.damageSources().mobProjectile(this, this.shooter);
        }

        if (entity instanceof LivingEntity livingEntity) {
            if (JustEnoughGuns.devilFruitsLoaded && this.projectile.hitsRubberFruit() &&
                    livingEntity.hasEffect(ttv.migami.mdf.init.ModEffects.RUBBER_FRUIT.get())) {
                entity.push(this);
                return;
            }
        }

        float damageDivisor = this.shooter.level().getDifficulty() != Difficulty.HARD ? 2 : 1.5F;
        if(!(this.shooter instanceof Player))
        {
            damage = damage / damageDivisor;
        }

        entity.hurt(source, damage);
        if (!Config.COMMON.gameplay.enableKnockback.get()) {
            entity.setDeltaMovement(0, 0, 0);
        }


        if (!entity.level().isClientSide) {
            ((ServerLevel) entity.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(), entity.getZ(), (int) damage / 2, entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 0.1);
        }

        if(this.shooter instanceof Player player && entity instanceof LivingEntity livingEntity)
        {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;

            if (headshot) {
                if(livingEntity instanceof Player player2 && player2.isCreative() || livingEntity.isSpectator()) {
                    return;
                }
                if (livingEntity.isDeadOrDying()) {
                    if (this.level() instanceof ServerLevel serverLevel && !entity.getTags().contains("JEGDying")) {
                        if (this.weapon.getTag() != null && this.weapon.getTag().getBoolean("MedalsEnabled")) {
                            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendMedal(MedalType.COMBAT_HEADSHOT.ordinal()));
                        }

                        if (Gun.hasCosmeticEquipped(this.weapon, IAttachment.Type.KILL_EFECT)) {
                            if(MinecraftForge.EVENT_BUS.post(new KillEffectEvent.Pre(player, this.weapon, this.position(), livingEntity)))
                                return;

                            MinecraftForge.EVENT_BUS.post(new KillEffectEvent.Post(player, this.weapon, this.position(), livingEntity));
                        }
                    }
                    entity.addTag("JEGDying");
                }
            }
            if (livingEntity.isDeadOrDying()) {
                //PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendHeadshot(headshot));
            }
            //if (!(livingEntity.isDeadOrDying())) {
                PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
            //}
        }

        /* Send blood particle to tracking clients. */
        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z));
    }

    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
    {
        if (!(this instanceof FlameProjectileEntity)) {
            PacketHandler.getPlayChannel().sendToTrackingChunk(() -> this.level().getChunkAt(pos), new S2CMessageProjectileHitBlock(x, y, z, pos, face));
        }
    }

    public void updateHeading()
    {
        double horizontalDistance = this.getDeltaMovement().horizontalDistance();
        this.setYRot((float) (Mth.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(this.getDeltaMovement().y(), horizontalDistance) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public Projectile getProjectile()
    {
        return this.projectile;
    }

    private Vec3 getVectorFromRotation(float pitch, float yaw)
    {
        float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    /**
     * Gets the entity who spawned the projectile
     */
    public LivingEntity getShooter()
    {
        return this.shooter;
    }

    /**
     * Gets the id of the entity who spawned the projectile
     */
    public int getShooterId()
    {
        return this.shooterId;
    }

    public float getDamage()
    {
        float initialDamage = (this.projectile.getDamage() + this.additionalDamage);
        if(this.projectile.isDamageReduceOverLife())
        {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
            initialDamage *= Math.min(modifier, 1);
        }
        float damage = initialDamage / this.general.getProjectileAmount();
        damage = GunModifierHelper.getModifiedDamage(this.weapon, this.modifiedGun, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(this.weapon, damage);
        damage = GunEnchantmentHelper.getWitheredDamage(this.weapon, damage);
        damage = GunModifierHelper.getChargeDamage(this.weapon, damage, this.chargeProgress);
        return Math.max(0F, damage);
    }

    private float getCriticalDamage(ItemStack weapon, RandomSource rand, float damage)
    {
        float chance = GunModifierHelper.getCriticalChance(weapon);
        if(rand.nextFloat() < chance)
        {
            return (float) (damage * Config.COMMON.gameplay.criticalDamageMultiplier.get());
        }
        return damage;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance)
    {
        return true;
    }

    @Override
    public void onRemovedFromWorld()
    {
        this.impactEffect();
        if(!this.level().isClientSide)
        {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(this::getDeathTargetPoint, new S2CMessageRemoveProjectile(this.getId()));
        }
    }

    private LevelLocation getDeathTargetPoint()
    {
        return LevelLocation.create(this.level(), this.getX(), this.getY(), this.getZ(), 256);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /**
     * A custom implementation of ray tracing that allows you to pass a predicate to ignore certain
     * blocks when checking for collisions.
     *
     * @param world     the world to perform the ray trace
     * @param context   the ray trace context
     * @param ignorePredicate the block state predicate
     * @return a result of the raytrace
     */
    static BlockHitResult rayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate)
    {
        return performRayTrace(context, (rayTraceContext, blockPos) -> {
            if (JustEnoughGuns.valkyrienSkiesLoaded) return RaycastUtilsKt.clipIncludeShips(world, context);
            BlockState blockState = world.getBlockState(blockPos);
            if(ignorePredicate.test(blockState)) return null;
            FluidState fluidState = world.getFluidState(blockPos);
            Vec3 startVec = rayTraceContext.getFrom();
            Vec3 endVec = rayTraceContext.getTo();
            VoxelShape blockShape = rayTraceContext.getBlockShape(blockState, world, blockPos);
            BlockHitResult blockResult = world.clipWithInteractionOverride(startVec, endVec, blockPos, blockShape, blockState);
            VoxelShape fluidShape = rayTraceContext.getFluidShape(fluidState, world, blockPos);
            BlockHitResult fluidResult = fluidShape.clip(startVec, endVec, blockPos);
            double blockDistance = blockResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(blockResult.getLocation());
            double fluidDistance = fluidResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(fluidResult.getLocation());
            return blockDistance <= fluidDistance ? blockResult : fluidResult;
        }, (rayTraceContext) -> {
            Vec3 Vector3d = rayTraceContext.getFrom().subtract(rayTraceContext.getTo());
            return BlockHitResult.miss(rayTraceContext.getTo(), Direction.getNearest(Vector3d.x, Vector3d.y, Vector3d.z), BlockPos.containing(rayTraceContext.getTo()));
        });
    }

    private static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> p_217300_2_)
    {
        Vec3 startVec = context.getFrom();
        Vec3 endVec = context.getTo();
        if(startVec.equals(endVec))
        {
            return p_217300_2_.apply(context);
        }
        else
        {
            double startX = Mth.lerp(-0.0000001, endVec.x, startVec.x);
            double startY = Mth.lerp(-0.0000001, endVec.y, startVec.y);
            double startZ = Mth.lerp(-0.0000001, endVec.z, startVec.z);
            double endX = Mth.lerp(-0.0000001, startVec.x, endVec.x);
            double endY = Mth.lerp(-0.0000001, startVec.y, endVec.y);
            double endZ = Mth.lerp(-0.0000001, startVec.z, endVec.z);
            int blockX = Mth.floor(endX);
            int blockY = Mth.floor(endY);
            int blockZ = Mth.floor(endZ);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
            T t = hitFunction.apply(context, mutablePos);
            if(t != null)
            {
                return t;
            }

            double deltaX = startX - endX;
            double deltaY = startY - endY;
            double deltaZ = startZ - endZ;
            int signX = Mth.sign(deltaX);
            int signY = Mth.sign(deltaY);
            int signZ = Mth.sign(deltaZ);
            double d9 = signX == 0 ? Double.MAX_VALUE : (double) signX / deltaX;
            double d10 = signY == 0 ? Double.MAX_VALUE : (double) signY / deltaY;
            double d11 = signZ == 0 ? Double.MAX_VALUE : (double) signZ / deltaZ;
            double d12 = d9 * (signX > 0 ? 1.0D - Mth.frac(endX) : Mth.frac(endX));
            double d13 = d10 * (signY > 0 ? 1.0D - Mth.frac(endY) : Mth.frac(endY));
            double d14 = d11 * (signZ > 0 ? 1.0D - Mth.frac(endZ) : Mth.frac(endZ));

            while(d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D)
            {
                if(d12 < d13)
                {
                    if(d12 < d14)
                    {
                        blockX += signX;
                        d12 += d9;
                    }
                    else
                    {
                        blockZ += signZ;
                        d14 += d11;
                    }
                }
                else if(d13 < d14)
                {
                    blockY += signY;
                    d13 += d10;
                }
                else
                {
                    blockZ += signZ;
                    d14 += d11;
                }

                T t1 = hitFunction.apply(context, mutablePos.set(blockX, blockY, blockZ));
                if(t1 != null)
                {
                    return t1;
                }
            }

            return p_217300_2_.apply(context);
        }
    }

    /**
     * Creates a projectile explosion for the specified entity.
     *
     * @param entity The entity to explode
     * @param radius The amount of radius the entity should deal
     * @param forceNone If true, forces the explosion mode to be NONE instead of config value
     */
    public static void createExplosion(Entity entity, float radius, boolean forceNone)
    {
        Level world = entity.level();
        if(world.isClientSide())
            return;

        DamageSource source = entity instanceof ProjectileEntity projectile ? entity.damageSources().explosion(entity, projectile.getShooter()) : null;
        Explosion.BlockInteraction mode = Config.COMMON.gameplay.griefing.enableBlockRemovalOnExplosions.get() && !forceNone ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, false, mode);

        if(net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
            return;

        // Do explosion logic
        explosion.explode();
        explosion.finalizeExplosion(true);

        // Send event to blocks that are exploded (none if mode is none)
        explosion.getToBlow().forEach(pos ->
        {
            if(world.getBlockState(pos).getBlock() instanceof IExplosionDamageable)
            {
                ((IExplosionDamageable) world.getBlockState(pos).getBlock()).onProjectileExploded(world, world.getBlockState(pos), pos, entity);
            }
        });

        // Clears the affected blocks if mode is none
        if(!explosion.interactsWithBlocks())
        {
            explosion.clearToBlow();
        }

        for(ServerPlayer player : ((ServerLevel) world).players())
        {
            if(player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 4096)
            {
                player.connection.send(new ClientboundExplodePacket(entity.getX(), entity.getY(), entity.getZ(), radius, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
            }
        }
    }

    public static void createFireExplosion(Entity entity, float radius, boolean forceNone)
    {
        Level world = entity.level();
        if(world.isClientSide())
            return;

        DamageSource source = entity instanceof ProjectileEntity projectile ? entity.damageSources().explosion(entity, projectile.getShooter()) : null;
        Explosion.BlockInteraction mode = Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, true, mode);

        if(net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
            return;

        // Do explosion logic
        explosion.explode();
        explosion.finalizeExplosion(true);

    }

    /**
     * Author: MrCrayfish
     */
    public static class EntityResult
    {
        private final Entity entity;
        private final Vec3 hitVec;
        private final boolean headshot;

        public EntityResult(Entity entity, Vec3 hitVec, boolean headshot)
        {
            this.entity = entity;
            this.hitVec = hitVec;
            this.headshot = headshot;
        }

        /**
         * Gets the entity that was hit by the projectile
         */
        public Entity getEntity()
        {
            return this.entity;
        }

        /**
         * Gets the position the projectile hit
         */
        public Vec3 getHitPos()
        {
            return this.hitVec;
        }

        /**
         * Gets if this was a headshot
         */
        public boolean isHeadshot()
        {
            return this.headshot;
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        this.projectile = new Gun.Projectile();
        this.projectile.deserializeNBT(compound.getCompound("Projectile"));
        this.general = new Gun.General();
        this.general.deserializeNBT(compound.getCompound("General"));
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.put("Projectile", this.projectile.serializeNBT());
        compound.put("General", this.general.serializeNBT());
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.projectile.serializeNBT());
        buffer.writeNbt(this.general.serializeNBT());
        buffer.writeInt(this.shooterId);
        BufferUtil.writeItemStackToBufIgnoreTag(buffer, this.item);
        buffer.writeDouble(this.modifiedGravity);
        buffer.writeVarInt(this.life);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        this.projectile = new Gun.Projectile();
        this.projectile.deserializeNBT(buffer.readNbt());
        this.general = new Gun.General();
        this.general.deserializeNBT(buffer.readNbt());
        this.shooterId = buffer.readInt();
        this.item = BufferUtil.readItemStackFromBufIgnoreTag(buffer);
        this.modifiedGravity = buffer.readDouble();
        this.life = buffer.readVarInt();
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
    }
}
