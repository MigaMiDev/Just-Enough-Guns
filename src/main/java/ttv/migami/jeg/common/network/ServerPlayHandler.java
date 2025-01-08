package ttv.migami.jeg.common.network;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.common.*;
import ttv.migami.jeg.common.container.*;
import ttv.migami.jeg.crafting.workbench.*;
import ttv.migami.jeg.entity.projectile.ProjectileEntity;
import ttv.migami.jeg.event.BurstFireEvent;
import ttv.migami.jeg.event.GunFireEvent;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.interfaces.IProjectileFactory;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.*;
import ttv.migami.jeg.util.GunEnchantmentHelper;
import ttv.migami.jeg.util.GunModifierHelper;

import java.util.List;
import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ServerPlayHandler
{
    //private static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity -> entity.getSoundSource() == SoundSource.HOSTILE && !(entity instanceof NeutralMob) && !Config.COMMON.aggroMobs.exemptEntities.get().contains(EntityType.getKey(entity.getType()).toString());
    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity ->
            (entity.getSoundSource() == SoundSource.HOSTILE || entity.getType() == EntityType.PIGLIN || entity.getType() == EntityType.ZOMBIFIED_PIGLIN) &&
                    !Config.COMMON.aggroMobs.exemptEntities.get().contains(EntityType.getKey(entity.getType()).toString());
    private static final Predicate<LivingEntity> FLEEING_ENTITIES = entity ->
            Config.COMMON.fleeingMobs.fleeingEntities.get().contains(EntityType.getKey(entity.getType()).toString());

    public static BlockPos rayTrace(Player pPlayer, double distance) {
        HitResult rayTraceResult = pPlayer.pick(distance, 1.0F, false);
        return BlockPos.containing(rayTraceResult.getLocation());
    }

    public static EntityHitResult hitEntity(Level pLevel, Player pPlayer, BlockPos blockPos) {
        return ProjectileUtil.getEntityHitResult(pLevel, pPlayer, pPlayer.getEyePosition(), blockPos.getCenter(), new AABB(pPlayer.getEyePosition(), blockPos.getCenter()), ServerPlayHandler::canDamage);
    }

    public static boolean canDamage(Entity entity) {
        return entity instanceof LivingEntity;
    }

    public static void toggleMedals(Player player) {
        ItemStack item = player.getMainHandItem();
        if (item.getItem() instanceof GunItem && item.getTag() != null) {
            item.getTag().putBoolean("MedalsEnabled", !item.getTag().getBoolean("MedalsEnabled"));
        }
    }

    public static void unjam(Player player) {
        ItemStack itemStack = player.getMainHandItem();
        CompoundTag nbtCompound = itemStack.getOrCreateTag();
        if (itemStack.getItem() instanceof AnimatedGunItem) {
            nbtCompound.remove("IsJammed");
        }
    }

    public static void overheat(Player player) {
        sendParticlesToAll(
                (ServerLevel) player.level(),
                ParticleTypes.CLOUD,
                true,
                player.getX() - player.getDeltaMovement().x() + player.getLookAngle().multiply(1, 1, 1).x,
                (player.getEyeY() - 0.1) - player.getDeltaMovement().y() + player.getLookAngle().multiply(1, 1, 1).y,
                player.getZ() - player.getDeltaMovement().z() + player.getLookAngle().multiply(1, 1, 1).z,
                5,
                0.1, 0.1, 0.1,
                0.01
        );
        player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1F, 1F);
    }

    /**
     * Fires the weapon the player is currently holding.
     * This is only intended for use on the logical server.
     *
     * @param player the player for who's weapon to fire
     */
    public static void handleShoot(C2SMessageShoot message, ServerPlayer player)
    {
        if(player.isSpectator())
            return;

        if(player.getUseItem().getItem() instanceof ShieldItem)
            return;

        Level world = player.level();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.getItem() instanceof GunItem item && (Gun.hasAmmo(heldItem) || player.isCreative()))
        {
            Gun modifiedGun = item.getModifiedGun(heldItem);
            if(modifiedGun != null)
            {
                if(MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                    return;

                /* Updates the yaw and pitch with the clients current yaw and pitch */
                player.setYRot(Mth.wrapDegrees(message.getRotationYaw()));
                player.setXRot(Mth.clamp(message.getRotationPitch(), -90F, 90F));

                // Bingo bango.

                ShootTracker tracker = ShootTracker.getShootTracker(player);
                if(tracker.hasCooldown(item) && tracker.getRemaining(item) > Config.SERVER.cooldownThreshold.get())
                {
                    JustEnoughGuns.LOGGER.warn(player.getName().getContents() + "(" + player.getUUID() + ") tried to fire before cooldown finished or server is lagging? Remaining milliseconds: " + tracker.getRemaining(item));
                    return;
                }
                tracker.putCooldown(heldItem, item, modifiedGun);

                if(ModSyncedDataKeys.RELOADING.getValue(player))
                {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                }

                if(!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F)
                {
                    SpreadTracker.get(player).update(player, item);
                }

                if (modifiedGun.getProjectile().hasProjectile())
                {
                    int count = modifiedGun.getGeneral().getProjectileAmount();
                    Gun.Projectile projectileProps = modifiedGun.getProjectile();
                    ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];
                    for(int i = 0; i < count; i++)
                    {
                        IProjectileFactory factory = ProjectileManager.getInstance().getFactory(projectileProps.getItem());
                        ProjectileEntity projectileEntity = factory.create(world, player, heldItem, item, modifiedGun);
                        projectileEntity.setWeapon(heldItem);
                        projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
                        world.addFreshEntity(projectileEntity);
                        spawnedProjectiles[i] = projectileEntity;
                        projectileEntity.tick();
                    }
                    if (!modifiedGun.getProjectile().hideTrail()) {
                        if(!projectileProps.isVisible())
                        {
                            double spawnX = player.getX();
                            double spawnY = player.getY() + 1.0;
                            double spawnZ = player.getZ();
                            double radius = Config.COMMON.network.projectileTrackingRange.get();
                            ParticleOptions data = GunEnchantmentHelper.getParticle(heldItem);
                            boolean isVisible = !modifiedGun.getProjectile().hideTrail();
                            S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, player.getId(), data, isVisible);
                            PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), spawnX, spawnY, spawnZ, radius), messageBulletTrail);
                        }
                    }
                }

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));

                ResourceLocation fireSound = getFireSound(player, heldItem, modifiedGun);

                if(fireSound != null)
                {
                    double posX = player.getX();
                    double posY = player.getY() + player.getEyeHeight();
                    double posZ = player.getZ();
                    float volume = Config.COMMON.world.playerGunfireVolume.get();
                    float selfVolume = 0.5F;
                    float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                    if (player.isUnderWater()) {
                        pitch = 0.7F + world.random.nextFloat() * 0.2F;
                        volume = volume / 2;
                    }
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
                    boolean muzzle = modifiedGun.getDisplay().getFlash() != null;
                    if (player.isUnderWater()) {
                        player.level().playSound(null, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.PLAYERS, 10, 1.7f);
                    }
                    player.level().playSound(player, posX, posY, posZ, SoundEvent.createVariableRangeEvent(fireSound), SoundSource.PLAYERS, volume - 0.5F, pitch);
                    S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, selfVolume, pitch, player.getId(), muzzle, false);
                    PacketHandler.getPlayChannel().sendToPlayer(() -> player, messageSound);
                    //PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), posX, posY, posZ, radius), messageSound);
                    //PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageClientFireSound());
                }

                if(Gun.getAttachment(IAttachment.Type.BARREL, heldItem).getItem() == ModItems.TRUMPET.get()) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.DOOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                if(Gun.getAttachment(IAttachment.Type.BARREL, heldItem).getItem() == ModItems.EXPLOSIVE_MUZZLE.get()) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                if(!player.isCreative())
                {
                    CompoundTag tag = heldItem.getOrCreateTag();
                    if(!tag.getBoolean("IgnoreAmmo"))
                    {
                            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.RECLAIMED.get(), heldItem);
                            if(level == 0 || player.level().random.nextInt(4 - Mth.clamp(level, 1, 2)) != 0)
                            {
                                tag.putInt("AmmoCount", Math.max(0, tag.getInt("AmmoCount") - 1));
                                if (modifiedGun.getReloads().getReloadType().equals(ReloadType.INVENTORY_FED)) {
                                    ReloadTracker.inventoryFeed(player, modifiedGun);
                                }
                        }
                    }
                }

                player.awardStat(Stats.ITEM_USED.get(item));

                boolean isSilenced = Gun.getAttachment(IAttachment.Type.BARREL, heldItem).getItem() == ModItems.SILENCER.get() || modifiedGun.getGeneral().isSilenced();
                if (player.level().random.nextFloat() <= 0.1f && (!isSilenced && (Config.COMMON.aggroMobs.enabled.get() || Config.COMMON.fleeingMobs.enabled.get()))) {
                    tryToAlertEntities(player, heldItem);
                }
            }
        }
        else
        {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.8F);
        }
    }

    private static void tryToAlertEntities(Player player, ItemStack heldItem) {
        double aggroRadius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.COMMON.aggroMobs.unsilencedRange.get());
        double fleeRadius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.COMMON.fleeingMobs.unsilencedRange.get());

        AABB playerBox = player.getBoundingBox();
        AABB inflatedBox = playerBox.inflate(Math.max(aggroRadius, fleeRadius), Math.max(aggroRadius, fleeRadius), Math.max(aggroRadius, fleeRadius));
        List<LivingEntity> allEntities = player.level().getEntitiesOfClass(LivingEntity.class, inflatedBox);

        for (LivingEntity entity : allEntities) {
            if (Config.COMMON.aggroMobs.enabled.get() && HOSTILE_ENTITIES.test(entity)) {
                if (entity instanceof ZombifiedPiglin zombifiedPiglin) {
                    zombifiedPiglin.setPersistentAngerTarget(player.getUUID());
                    zombifiedPiglin.setRemainingPersistentAngerTime(400 + player.level().random.nextInt(400));
                } else if (entity instanceof Piglin piglin) {
                    piglin.setTarget(player);
                    piglin.setAggressive(true);
                } else {
                    entity.setLastHurtByMob(player);
                }
            }

            if (Config.COMMON.fleeingMobs.enabled.get() && FLEEING_ENTITIES.test(entity)) {
                if (entity instanceof PathfinderMob mob) {
                    if (mob instanceof Wolf wolf && (wolf.isTame() || wolf.isInSittingPose())) {
                        continue;
                    }
                    mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
                    mob.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
                    mob.getNavigation().stop();

                    double speedMultiplier = (entity.getType() == EntityType.VILLAGER) ? 1.0 : 1.5;

                    Vec3 randomPos = LandRandomPos.getPos(mob, 16, 7);
                    if (randomPos != null) {
                        mob.getNavigation().moveTo(randomPos.x, randomPos.y, randomPos.z, speedMultiplier);
                    }
                }
            }
        }
    }

    public static void handlePreFireSound(C2SMessagePreFireSound message, ServerPlayer player) {
        Level world = player.level();
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.getItem() instanceof GunItem item && (Gun.hasAmmo(heldItem) || player.isCreative()))
        {
            Gun modifiedGun = item.getModifiedGun(heldItem);
            ResourceLocation fireSound = getPreFireSound(heldItem, modifiedGun);
            if(fireSound != null)
            {
                double posX = player.getX();
                double posY = player.getY() + player.getEyeHeight();
                double posZ = player.getZ();
                float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotMaxDistance.get());
                S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, player.getId(), false, false);
                PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level(), posX, posY, posZ, radius), messageSound);
            }
        }
    }

    public static void handleBurst(ServerPlayer player) {

        ItemStack heldItem = player.getMainHandItem();
        if(heldItem.getItem() instanceof GunItem gunItem)
        {

            Gun gun = gunItem.getModifiedGun(heldItem);
            if (gun.getGeneral().getFireMode() == FireMode.BURST)
            {
                BurstFireEvent.resetBurst(heldItem);
            }

        }
    }

    public static ResourceLocation getFireSound(Player player, ItemStack stack, Gun modifiedGun)
    {
        ResourceLocation fireSound = null;
        if(GunModifierHelper.isSilencedFire(stack))
        {
            fireSound = modifiedGun.getSounds().getSilencedFire();
        }
        else if(!player.isUnderWater() && stack.isEnchanted() && Config.CLIENT.sounds.enchantSound.get())
        {
            fireSound = modifiedGun.getSounds().getEnchantedFire();
        }
        if(fireSound != null)
        {
            return fireSound;
        }
        return modifiedGun.getSounds().getFire();
    }

    public static ResourceLocation getPreFireSound(ItemStack stack, Gun modifiedGun)
    {
        return modifiedGun.getSounds().getPreFire();
    }

    /**
     * Sends particles to all players in the ServerLevel using the specified parameters.
     *
     * @param serverLevel   The ServerLevel where the particles will be shown.
     * @param particleType  The particle type to be displayed.
     * @param longDistance  Whether the particles are visible from far away.
     * @param posX          The X-coordinate of the particles.
     * @param posY          The Y-coordinate of the particles.
     * @param posZ          The Z-coordinate of the particles.
     * @param particleCount Number of particles to spawn.
     * @param offsetX       Spread of the particles along the X-axis.
     * @param offsetY       Spread of the particles along the Y-axis.
     * @param offsetZ       Spread of the particles along the Z-axis.
     * @param speed         Speed of the particles.
     */
    public static <T extends ParticleOptions> void sendParticlesToAll(
            ServerLevel serverLevel,
            T particleType,
            boolean longDistance,
            double posX, double posY, double posZ,
            int particleCount,
            double offsetX, double offsetY, double offsetZ,
            double speed
    ) {
        for (ServerPlayer player : serverLevel.players()) {
            serverLevel.sendParticles(
                    player,
                    particleType,
                    longDistance,
                    posX, posY, posZ,
                    particleCount,
                    offsetX, offsetY, offsetZ,
                    speed
            );
        }
    }

    /**
     * Crafts the specified item at the workstation the player is currently using.
     * This is only intended for use on the logical server.
     *
     * @param player the player who is crafting
     * @param id     the id of an item which is registered as a valid workstation recipe
     * @param pos    the block position of the workstation the player is using
     */
    public static void handleCraft(ServerPlayer player, ResourceLocation id, BlockPos pos) {
        Level world = player.level();

        if (player.containerMenu instanceof ScrapWorkbenchContainer workbench) {
            if (workbench.getPos().equals(pos)) {
                ScrapWorkbenchRecipe recipe = WorkbenchRecipes.getRecipeById(world, id, ModRecipeTypes.SCRAP_WORKBENCH.get());
                if (recipe == null || !recipe.hasMaterials(player))
                    return;

                recipe.consumeMaterials(player);

                ItemStack stack = recipe.getItem();
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack);
            }
        }

        if (player.containerMenu instanceof GunmetalWorkbenchContainer workbench) {
            if (workbench.getPos().equals(pos)) {
                GunmetalWorkbenchRecipe recipe = WorkbenchRecipes.getRecipeById(world, id, ModRecipeTypes.GUNMETAL_WORKBENCH.get());
                if (recipe == null || !recipe.hasMaterials(player))
                    return;

                recipe.consumeMaterials(player);

                ItemStack stack = recipe.getItem();
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack);
            }
        }

        if (player.containerMenu instanceof GunniteWorkbenchContainer workbench) {
            if (workbench.getPos().equals(pos)) {
                GunniteWorkbenchRecipe recipe = WorkbenchRecipes.getRecipeById(world, id, ModRecipeTypes.GUNNITE_WORKBENCH.get());
                if (recipe == null || !recipe.hasMaterials(player))
                    return;

                recipe.consumeMaterials(player);

                ItemStack stack = recipe.getItem();
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack);
            }
        }

        if (player.containerMenu instanceof BlueprintWorkbenchContainer workbench) {
            if (workbench.getPos().equals(pos)) {
                BlueprintWorkbenchRecipe recipe = WorkbenchRecipes.getRecipeById(world, id, ModRecipeTypes.BLUEPRINT_WORKBENCH.get());
                if (recipe == null || !recipe.hasMaterials(player))
                    return;

                recipe.consumeMaterials(player);

                ItemStack stack = recipe.getItem();
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack);
            }
        }
    }

    /*
    public static void handleRecycle(ServerPlayer player, ResourceLocation id, BlockPos pos)
    {
        Level world = player.level;

        if(player.containerMenu instanceof RecyclerContainer recycler)
        {
            if(recycler.getPos().equals(pos))
            {
                RecyclerRecipe recipe = RecyclerRecipes.getRecipeById(world, id);
                if(recipe == null)
                    return;

                ItemStack stack = recipe.getItem();
                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack);
            }
        }
    }
    */

    /**
     * @param player
     */
    public static void stopSprinting(Player player) {
        player.setSprinting(false);

        if(player.level().isClientSide) {
            Minecraft.getInstance().options.keySprint.setDown(false);
        }
    }

    public static void handleCharge(Player player, Entity entity, float damage, float currentDamage, float maxDamage, ItemStack bayonet) {
        float finalDamage = damage;

        double sweepAngle = Math.toRadians(100);

        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();

        Vec3 entityPos = entity.position().subtract(playerPos);
        double angle = Math.acos(entityPos.normalize().dot(lookVec.normalize()));

        if (angle < sweepAngle / 3 && entity != player) {
            if (entity instanceof LivingEntity target && target.invulnerableTime == 0) {
                Vec3 direction = entity.position().subtract(player.position()).normalize();
                player.invulnerableTime = 40;

                if (GunModifierHelper.getSwordSweepingEdge(player) < 2) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false));
                }

                target.push(direction.x * GunModifierHelper.getSwordKnockBack(player), 0.5, direction.z * GunModifierHelper.getSwordKnockBack(player));

                if (currentDamage <= maxDamage / 1.5) {
                    target.hurt(player.damageSources().playerAttack(player), GunModifierHelper.getSwordDamage(player) / 1.5F);
                    player.level().playSound(null, player.getOnPos(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2F, 1F);

                    if (GunModifierHelper.getSwordFireAspect(player) > 0) {
                        entity.setSecondsOnFire(2 * GunModifierHelper.getSwordFireAspect(player));
                    }

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, entity.getX(), entity.getY(), entity.getZ(), (int) finalDamage, 0.3, entity.getBbHeight(), 0.3, 0.2);
                    }
                } else {
                    player.level().playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 3.0F, 1.0F);
                }

                Vec3 pushBackDirection = player.position().subtract(target.position()).normalize();
                double pushBackForce = 1.0;
                if (GunModifierHelper.getSwordSweepingEdge(player) < 3) {
                    player.push(pushBackDirection.x * pushBackForce, 0.5, pushBackDirection.z * pushBackForce);
                }

                if (!player.getAbilities().instabuild && Config.COMMON.gameplay.gunDurability.get() && currentDamage <= maxDamage / 1.5) {
                    if (bayonet.getEnchantmentLevel(Enchantments.MENDING) == 0) {
                        bayonet.hurtAndBreak(15, player, null);
                    }
                }
            }
        }
    }

    /**
     * @param player
     */
    public static void handleMelee(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        ItemCooldowns tracker = player.getCooldowns();
        double offsetX;
        double offsetY;
        double offsetZ;

        if (stack.getItem() instanceof GunItem gunItem && !tracker.isOnCooldown(stack.getItem())) {

            Level level = player.level();

            if (gunItem instanceof AnimatedGunItem animatedGunItem) {
                final long id = GeoItem.getId(stack);
                AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
                if (animationController.getCurrentAnimation() != null) {
                    if (animationController.getCurrentAnimation().animation().name().matches("draw")) {
                        return;
                    }
                }
                animationController.forceAnimationReset();
                if ((Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem)) {
                    animationController.tryTriggerAnimation("bayonet");
                }
                else {
                    animationController.tryTriggerAnimation("melee");
                }
            }

            if (Gun.hasAttachmentEquipped(player.getMainHandItem(), IAttachment.Type.SPECIAL)) {
                if (Gun.getAttachment(IAttachment.Type.SPECIAL, player.getMainHandItem()).getItem() == ModItems.FLASHLIGHT.get()) {
                    ItemStack flashlight = Gun.getAttachment(IAttachment.Type.SPECIAL, player.getMainHandItem());
                    if (flashlight.getTag() != null) {
                        flashlight.getTag().putBoolean("Powered", !flashlight.getTag().getBoolean("Powered"));
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.FLASHLIGHT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }

            ItemStack bayonet = Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem());
            int maxDamage = bayonet.getMaxDamage();
            int currentDamage = bayonet.getDamageValue();

            double attackRange = 2.0;
            double sweepAngle = Math.toRadians(100);

            Vec3 playerPos = player.position();
            Vec3 lookVec = player.getLookAngle();

            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(attackRange));

            level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2F, 1F);
            int cooldown = 15;
            if (player.isSprinting())
                cooldown = 40;
            tracker.addCooldown(stack.getItem(), cooldown);

            for (LivingEntity entity : entities) {
                Vec3 entityPos = entity.position().subtract(playerPos);
                double angle = Math.acos(entityPos.normalize().dot(lookVec.normalize()));

                if (angle < sweepAngle / 2 && entity != player) {

                    if (!player.level().isClientSide) {

                        Vec3 direction = entity.position().subtract(player.position()).normalize();

                        entity.push(direction.x * GunModifierHelper.getSwordKnockBack(player), 0.5, direction.z * GunModifierHelper.getSwordKnockBack(player));

                        if (currentDamage <= maxDamage / 1.5) {
                            entity.hurt(player.damageSources().playerAttack(player), GunModifierHelper.getSwordDamage(player) / 1.5F);

                            if (GunModifierHelper.getSwordFireAspect(player) > 0) {
                                entity.setSecondsOnFire(2 * GunModifierHelper.getSwordFireAspect(player));
                            }
                        } else {
                            level.playSound(player, player.getOnPos(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 3.0F, 1.0F);
                        }

                        if (!player.getAbilities().instabuild && Config.COMMON.gameplay.gunDurability.get() && currentDamage <= maxDamage / 1.5) {
                            if (bayonet.getEnchantmentLevel(Enchantments.MENDING) == 0) {
                                bayonet.hurtAndBreak(8, player, null);
                            }
                        }
                    }
                }
            }

            offsetX = lookVec.x * 1.8;
            offsetY = lookVec.y * 1.8 + player.getEyeHeight();
            offsetZ = lookVec.z * 1.8;
            playerPos = player.getPosition(1F).add(offsetX, offsetY, offsetZ);

            if (!level.isClientSide) {
                ((ServerLevel) level).sendParticles(ParticleTypes.SWEEP_ATTACK, playerPos.x, playerPos.y, playerPos.z, 1, 0, 0, 0, 0.0);
            }

        }
    }

    /**
     * @param player
     */
    public static void handleUnload(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = stack.getTag();

            if (stack.is(ModItems.FLARE_GUN.get()) && stack.getTag() != null) {
                stack.getTag().putBoolean("HasRaid", false);
                stack.getTag().remove("Raid");
            }

            if (gun.getReloads().getReloadType() != ReloadType.SINGLE_ITEM && gun.getReloads().getReloadType() != ReloadType.INVENTORY_FED && !gun.getReloads().getReloadType().equals(ReloadType.INVENTORY_FED)) {
                if (tag != null && tag.contains("AmmoCount", Tag.TAG_INT)) {
                    int count = tag.getInt("AmmoCount");
                    tag.putInt("AmmoCount", 0);

                    ResourceLocation id = gun.getProjectile().getItem();

                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    if (item == null) {
                        return;
                    }

                    int maxStackSize = item.getMaxStackSize();
                    int stacks = count / maxStackSize;

                    if (player.getMainHandItem().getEnchantmentLevel(ModEnchantments.INFINITY.get()) == 0) {
                        for (int i = 0; i < stacks; i++) {
                            spawnAmmo(player, new ItemStack(item, maxStackSize));
                        }

                        int remaining = count % maxStackSize;
                        if (remaining > 0) {
                            spawnAmmo(player, new ItemStack(item, remaining));
                        }
                    }
                }
            }
        }
    }

    /**
     * @param player
     */
    public static void handleExtraAmmo(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(stack);
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("AmmoCount", Tag.TAG_INT)) {
                int currentAmmo = tag.getInt("AmmoCount");

                if (currentAmmo > GunModifierHelper.getModifiedAmmoCapacity(stack, gun)) {

                    ResourceLocation id = gun.getProjectile().getItem();
                    Item item = ForgeRegistries.ITEMS.getValue(id);
                    if (item == null) {
                        return;
                    }
                    int residue = currentAmmo - gun.getReloads().getMaxAmmo();

                    tag.putInt("AmmoCount", currentAmmo - residue);

                    spawnAmmo(player, new ItemStack(item, residue));
                }
            }
        }
    }

    /**
     * @param player
     * @param stack
     */
    private static void spawnAmmo(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (stack.getCount() > 0) {
            player.level().addFreshEntity(new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    /**
     * @param player
     */
    public static void handleAttachments(ServerPlayer player) {
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof GunItem) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, player1) -> new AttachmentContainer(windowId, playerInventory, heldItem), Component.translatable("container.jeg.attachments")));
        }
    }

    public static void burnPlayer(ServerPlayer player) {
        player.setSecondsOnFire(5);
    }
}
