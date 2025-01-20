package ttv.migami.jeg.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.FireMode;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModTags;
import ttv.migami.jeg.common.network.ServerPlayHandler;
import ttv.migami.jeg.entity.throwable.ThrowableExplosiveChargeEntity;
import ttv.migami.jeg.init.*;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.util.GunEnchantmentHelper;
import ttv.migami.jeg.util.GunModifierHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEventBus {
    static final Map<UUID, Long> runningPlayers = new HashMap<>();
    private static final long RUN_DURATION_THRESHOLD = 2000;

    // Bayonet charge handler
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID playerId = player.getUUID();

            if (player.isSprinting() && player.getMainHandItem().getItem() instanceof GunItem gunItem) {
                long currentTime = System.currentTimeMillis();
                runningPlayers.putIfAbsent(playerId, currentTime);

                ItemCooldowns tracker = player.getCooldowns();
                if (runningPlayers.get(playerId) != null && currentTime - runningPlayers.get(playerId) >= RUN_DURATION_THRESHOLD
                        && !tracker.isOnCooldown(gunItem)
                        && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)
                        && (Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem swordItem)) {

                    ItemStack bayonet = Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem());

                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 0, false, false));
                    float damage = swordItem.getDamage();

                    int maxDamage = bayonet.getMaxDamage();
                    int currentDamage = bayonet.getDamageValue();
                    if (currentDamage >= maxDamage / 1.5) {
                        damage = 0;
                    }

                    damage = damage + bayonet.getEnchantmentLevel(Enchantments.SHARPNESS);
                    AABB boundingBox = player.getBoundingBox().inflate(1.5);

                    if (runningPlayers.containsKey(playerId)) {
                        float finalDamage = damage;
                        player.level().getEntities(player, boundingBox).forEach(entity -> {
                            ServerPlayHandler.handleCharge(player, entity, finalDamage, currentDamage, maxDamage, bayonet);
                        });

                        Vec3 start = player.getEyePosition(1.0F);
                        Vec3 look = player.getLookAngle();
                        Vec3 end = start.add(look.scale(0.5));

                        HitResult hitResult = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                        if (hitResult.getType() == HitResult.Type.BLOCK) {
                            Vec3 pushBackDirection = player.getLookAngle().normalize().scale(-1);

                            BlockPos blockPos = ServerPlayHandler.rayTrace(player, 4.0);
                            BlockState blockState = player.level().getBlockState(blockPos);

                            double pushBackForce = 1.0 + (double) GunModifierHelper.getSwordKnockBack(player) / 4;

                            /*if (blockState.getBlock() instanceof SlimeBlock) {
                                pushBackForce = (1.0 + (double) GunModifierHelper.getSwordKnockBack(player) / 4) * 2;
                            }*/

                            player.level().playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 3.0F, 1.0F);
                            player.push(pushBackDirection.x * pushBackForce, 0.5, pushBackDirection.z * pushBackForce);
                        }
                    }
                }
            } else {
                runningPlayers.remove(playerId);
            }
        }
    }

    @SubscribeEvent
    public static void preShoot(GunFireEvent.Pre event)
    {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getTag();

        if(heldItem.getItem() instanceof GunItem gunItem)
        {
            Gun gun = gunItem.getModifiedGun(heldItem);
            if (!gun.getGeneral().canFireUnderwater() && player.isUnderWater() && !Config.COMMON.gameplay.underwaterFiring.get() && heldItem.getEnchantmentLevel(ModEnchantments.ATLANTIC_SHOOTER.get()) == 0)
            {
                event.setCanceled(true);
            }

            ItemCooldowns tracker = player.getCooldowns();
            if(tracker.isOnCooldown(heldItem.getItem()) && gun.getGeneral().getFireMode() == FireMode.PULSE)
            {
                event.setCanceled(true);
            }

            if (heldItem.getItem() instanceof AnimatedGunItem animatedGunItem) {
                final long id = GeoItem.getId(heldItem);
                AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");

                if (heldItem.getTag() != null && animationController.getCurrentAnimation() != null) {
                    if (animationController.getCurrentAnimation().animation().name().matches("draw")) {
                        event.setCanceled(true);
                    }
                }
            }

            boolean explosiveAmmo = false;
            double chance = 0.975;
            if (Gun.hasAttachmentEquipped(heldItem, IAttachment.Type.BARREL)) {
                if (Gun.getAttachment(IAttachment.Type.BARREL, heldItem).getItem() == ModItems.EXPLOSIVE_MUZZLE.get()) {
                    explosiveAmmo = true;
                }
            }

            int damageAmount = explosiveAmmo ? 5 : 1;
            if (heldItem.isDamageableItem() && tag != null) {
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() - damageAmount)) {
                    level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(), gun.getGeneral().getRate());
                    event.setCanceled(true);
                }

                //This is the Jam function
                /*CompoundTag nbtCompound = heldItem.getOrCreateTag();
                if (gunItem instanceof AnimatedGunItem animatedGunItem) {
                    final long id = GeoItem.getId(player.getMainHandItem());
                    AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");

                    if (animationController.getCurrentAnimation().animation().name().matches("jam")) {
                        event.setCanceled(true);
                    }
                }*/

                int maxDamage = heldItem.getMaxDamage();
                int currentDamage = heldItem.getDamageValue();

                double explosiveJam = 1.75;
                /*if (explosiveAmmo) {
                    explosiveJam = Double.MAX_VALUE;
                }*/

                //if (currentDamage >= maxDamage / explosiveJam && Config.COMMON.gameplay.gunJamming.get() && !nbtCompound.getBoolean("IsJammed")) {
                if (currentDamage >= maxDamage / explosiveJam && Config.COMMON.gameplay.gunJamming.get()) {
                    if (Math.random() >= chance) {
                        event.getEntity().playSound(ModSounds.ITEM_PISTOL_COCK.get(), 1.0F, 1.0F);
                        int coolDown = gun.getGeneral().getRate() * 10;
                        if (coolDown > 60) {
                            coolDown = 60;
                        }
                        Component message = Component.translatable("chat.jeg.jam")
                                .withStyle(ChatFormatting.GRAY);
                        player.displayClientMessage(message, true);
                        /*if (gunItem instanceof AnimatedGunItem animatedGunItem) {
                            final long id = GeoItem.getId(player.getMainHandItem());
                            AnimationController<GeoAnimatable> animationController = animatedGunItem.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");
                            nbtCompound.putBoolean("IsJammed", true);
                            animationController.tryTriggerAnimation("jam");
                        } else {*/
                            event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(), (coolDown));
                        //}
                        /*if (player.level().isClientSide) {
                            Minecraft mc = Minecraft.getInstance();
                            mc.options.keyAttack.setDown(false);
                        }*/
                        event.setCanceled(true);
                    }
                } else if (tag.getInt("AmmoCount") >= 1) {
                    broken(heldItem, level, player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event)
    {
        Player player = event.getEntity();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getTag();
        if(heldItem.getItem() instanceof GunItem gunItem)
        {
            Gun gun = gunItem.getModifiedGun(heldItem);

            if (gunItem != ModItems.SUBSONIC_RIFLE.get() && gunItem != ModItems.SUPERSONIC_SHOTGUN.get() &&
                    gunItem != ModItems.HYPERSONIC_CANNON.get() && !GunModifierHelper.isSilencedFire(heldItem)) {
                level.gameEvent(player, GameEvent.EXPLODE, player.getPosition(1F));
            }

            specialEffects(level, player);
            if (gun.getProjectile().ejectsCasing() && tag != null)
            {
                if (tag.getInt("AmmoCount") >= 1 || player.getAbilities().instabuild) {
                    if (!(gunItem instanceof AnimatedGunItem)) {
                        ejectCasing(level, player);
                    }
                    if (gunItem == ModItems.ROCKET_LAUNCHER.get()) {
                        firingSmoke(level, player);
                    }
                }
            }

            boolean explosiveAmmo = false;
            if (Gun.hasAttachmentEquipped(heldItem, IAttachment.Type.BARREL)) {
                if (Gun.getAttachment(IAttachment.Type.BARREL, heldItem).getItem() == ModItems.EXPLOSIVE_MUZZLE.get()) {
                    explosiveAmmo = true;
                }
            }

            if (heldItem.isDamageableItem() && tag != null) {
                if (tag.getInt("AmmoCount") >= 1 && Config.COMMON.gameplay.gunDurability.get()) {
                    if (player instanceof Player ){
                        damageGun(heldItem, level, player, explosiveAmmo);
                        if (heldItem.getEnchantmentLevel(Enchantments.MENDING) == 0) {
                            damageAttachments(heldItem, level, player);
                        }
                    }
                }
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() / 1.5)) {
                    float randomPitch = 1.5F + (player.getRandom().nextFloat() * (1.75F - 1.5F));
                    level.playSound(player, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.3F, randomPitch);
                }
            }

            // Pushback
            float pushBack = gun.getGeneral().getShooterPushback();
            if (player.isCrouching()) {
                    pushBack = pushBack / 2;
            }
            recoil(player, pushBack);

            float damage = gun.getProjectile().getDamage();
            damage = GunModifierHelper.getModifiedDamage(heldItem, gun, damage);
            damage = GunEnchantmentHelper.getAcceleratorDamage(heldItem, damage);
            damage = GunEnchantmentHelper.getWitheredDamage(heldItem, damage);
            if (gunItem == ModItems.TYPHOONEE.get()) {
                typhooneeBlast(level, player, damage);
            }
            if (gunItem == ModItems.SUPERSONIC_SHOTGUN.get()) {
                soundwaveBlast(level, player, damage, gun);
            }
            if (gunItem == ModItems.HYPERSONIC_CANNON.get()) {
                hypersonicBlast(level, player, damage);
            }
            if (Gun.getAttachment(IAttachment.Type.BARREL, heldItem).getItem() == ModItems.TRUMPET.get() &&
                gunItem.getGun().getGeneral().getProjectileAmount() > 3) {
                trumpetBlast(level, player);
            }

            //if (heldItem.getItem() == ModItems.INFANTRY_RIFLE.get() && tag != null) {
            if (heldItem.getItem() == ModItems.INFANTRY_RIFLE.get() && tag != null && !Gun.hasAttachmentEquipped(heldItem, IAttachment.Type.MAGAZINE)) {
                if (tag.getInt("AmmoCount") == 1)
                    event.getEntity().level().playSound(player, player.blockPosition(), ModSounds.INFANTRY_RIFLE_PING.get(), SoundSource.MASTER, 3.0F, 1.0F);
            }
        }
    }

    public static void broken(ItemStack stack, Level level, Player player) {
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        if (currentDamage >= (maxDamage - 2)) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    public static void damageGun(ItemStack stack, Level level, Player player, boolean explosiveAmmo) {
        if (!player.getAbilities().instabuild) {
            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int currentDamage = stack.getDamageValue();
                int damageAmount = explosiveAmmo ? 5 : 1;
                if (currentDamage >= (maxDamage - damageAmount)) {
                    if (currentDamage >= (maxDamage - 2)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                        //stack.shrink(1);
                    }
                } else {
                    stack.hurtAndBreak(damageAmount, player, null);
                }
            }
        }
    }

    public static void damageAttachments(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild) {
            if (stack.getItem() instanceof GunItem) {

                //Scope
                ItemStack scopeStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
                if (scopeStack.getEnchantmentLevel(Enchantments.MENDING) == 0) {
                    if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.SCOPE) && scopeStack.isDamageableItem()) {
                        int maxDamage = scopeStack.getMaxDamage();
                        int currentDamage = scopeStack.getDamageValue();
                        if (currentDamage == (maxDamage - 1)) {
                            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                            Gun.removeAttachment(stack, "Scope");
                            Component message = Component.translatable("chat.jeg.attachment_broke")
                                    .withStyle(ChatFormatting.GRAY);
                            player.displayClientMessage(message, true);
                        } else {
                            scopeStack.hurtAndBreak(1, player, null);
                        }
                    }
                }

                //Barrel
                ItemStack barrelStack = Gun.getAttachment(IAttachment.Type.BARREL, stack);
                if (barrelStack.getEnchantmentLevel(Enchantments.MENDING) == 0 || barrelStack.getItem() == ModItems.EXPLOSIVE_MUZZLE.get()) {
                    if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.BARREL) && barrelStack.isDamageableItem() && !(barrelStack.getItem() instanceof SwordItem)) {
                        int maxDamage = barrelStack.getMaxDamage();
                        int currentDamage = barrelStack.getDamageValue();
                        if (currentDamage == (maxDamage - 1)) {
                            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                            Gun.removeAttachment(stack, "Barrel");
                            Component message = Component.translatable("chat.jeg.attachment_broke")
                                    .withStyle(ChatFormatting.GRAY);
                            player.displayClientMessage(message, true);
                        } else {
                            barrelStack.hurtAndBreak(1, player, null);
                        }
                    }
                }

                //Stock
                ItemStack stockStack = Gun.getAttachment(IAttachment.Type.STOCK, stack);
                if (stockStack.getEnchantmentLevel(Enchantments.MENDING) == 0) {
                    if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK) && stockStack.isDamageableItem()) {
                        int maxDamage = stockStack.getMaxDamage();
                        int currentDamage = stockStack.getDamageValue();
                        if (currentDamage == (maxDamage - 1)) {
                            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                            Gun.removeAttachment(stack, "Stock");
                            Component message = Component.translatable("chat.jeg.attachment_broke")
                                    .withStyle(ChatFormatting.GRAY);
                            player.displayClientMessage(message, true);
                        } else {
                            stockStack.hurtAndBreak(1, player, null);
                        }
                    }
                }

                //Under Barrel
                ItemStack underBarrelStack = Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack);
                if (underBarrelStack.getEnchantmentLevel(Enchantments.MENDING) == 0) {
                    if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.UNDER_BARREL) && underBarrelStack.isDamageableItem()) {
                        int maxDamage = underBarrelStack.getMaxDamage();
                        int currentDamage = underBarrelStack.getDamageValue();
                        if (currentDamage == (maxDamage - 1)) {
                            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                            Gun.removeAttachment(stack, "Under_Barrel");
                            Component message = Component.translatable("chat.jeg.attachment_broke")
                                    .withStyle(ChatFormatting.GRAY);
                            player.displayClientMessage(message, true);
                        } else {
                            underBarrelStack.hurtAndBreak(1, player, null);
                        }
                    }
                }
            }
        }
    }

    public static void typhooneeBlast(Level level, LivingEntity shooter, float damage) {
        HitResult result = shooter.pick(64, 0, false);
        Vec3 userPos = shooter.getEyePosition();
        Vec3 targetPos = result.getLocation();
        Vec3 distanceTo = targetPos.subtract(userPos);
        Vec3 normal = distanceTo.normalize();

        if(!level.isClientSide()) {

            for(int i = 3; i < Mth.floor(distanceTo.length()); ++i) {
                Vec3 vec33 = userPos.add(normal.scale((double)i));
                if (level instanceof ServerLevel serverLevel) {
                    sendParticlesToAll(
                            serverLevel,
                            ModParticleTypes.TYPHOONEE_BEAM.get(),
                            true,
                            vec33.x(),
                            vec33.y(),
                            vec33.z(),
                            1,
                            0, 0, 0,
                            0
                    );
                    if (!shooter.isUnderWater()) {
                        sendParticlesToAll(
                                serverLevel,
                                ParticleTypes.CLOUD,
                                true,
                                vec33.x(),
                                vec33.y(),
                                vec33.z(),
                                1,
                                0, 0, 0,
                                0
                        );
                    } else {
                        sendParticlesToAll(
                                serverLevel,
                                ParticleTypes.BUBBLE,
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
                if (!shooter.isUnderWater()) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.SPLASH, vec33.x, vec33.y, vec33.z, 3, 0.3D, 0.3D, 0.3D, 1.0D);
                    ((ServerLevel) level).sendParticles(ParticleTypes.FALLING_WATER, vec33.x, vec33.y, vec33.z, 1, 0.3D, 0.3D, 0.3D, 0.0D);
                }
            }

            // Extinguish fire in a 5 diameter (2 blocks radius)
            int radius = 2;
            BlockPos blockPos = BlockPos.containing(result.getLocation());
            if (Config.COMMON.gameplay.griefing.extinguishFire.get()) {
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos blockPos2 = blockPos.offset(x, y, z);
                            BlockState blockState = level.getBlockState(blockPos2);
                            if (blockState.getBlock() instanceof FireBlock) {
                                level.setBlockAndUpdate(blockPos2, Blocks.AIR.defaultBlockState());
                                level.playSound(null, BlockPos.containing(result.getLocation()), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.2F, 1F);
                            }
                        }
                    }
                }
            }

            // Defuse Explosive Charges
            AABB area = new AABB(blockPos).inflate(radius);
            List<Entity> entities = level.getEntities((Entity) null, area, entity -> entity instanceof ThrowableExplosiveChargeEntity);

            for (Entity entity : entities) {
                if (entity instanceof ThrowableExplosiveChargeEntity explosiveChargeEntity) {
                    Item item = ModItems.EXPLOSIVE_CHARGE.get();
                    ItemStack itemStack = new ItemStack(item, 1);

                    ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), itemStack);
                    entity.level().addFreshEntity(itemEntity);
                    explosiveChargeEntity.defuse();

                    sendParticlesToAll(
                            (ServerLevel) entity.level(),
                            ParticleTypes.CLOUD,
                            true,
                            entity.getX() - entity.getDeltaMovement().x(),
                            (entity.getEyeY() - 0.1) - entity.getDeltaMovement().y(),
                            entity.getZ() - entity.getDeltaMovement().z(),
                            5,
                            0.1, 0.1, 0.1,
                            0.01
                    );
                    entity.level().playSound(null, entity.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1F, 1F);
                }
            }

            EntityHitResult e = ProjectileUtil.getEntityHitResult(level, shooter, userPos, targetPos, new AABB(userPos, targetPos), ServerPlayHandler::canDamage);

            if(e != null && e.getEntity() instanceof LivingEntity entity) {

                float advantageMultiplier = 1F;
                if (entity.getType().is(ModTags.Entities.FIRE))
                {
                    advantageMultiplier = 2.0F;
                }

                if (entity.isOnFire()) {
                    entity.extinguishFire();
                    ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 1, entity.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.0D);
                }
                ((ServerLevel) level).sendParticles(ParticleTypes.FALLING_WATER, entity.getX(), entity.getY() + 1, entity.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.0D);

                entity.hurt(shooter.damageSources().sonicBoom(shooter), damage * advantageMultiplier);
                double d1 = 0.5D * (1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                double d0 = 2.5D * (1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                entity.push(normal.x() * d0, normal.y() * d1, normal.z() * d0);

                entity.addEffect(new MobEffectInstance(ModEffects.DEAFENED.get(), 100, 0, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            }
        }
    }

    public static void soundwaveBlast(Level level, LivingEntity shooter, float maxDamage, Gun gun) {
        Vec3 lookVec = shooter.getLookAngle();
        double offsetX = lookVec.x * 1.8;
        double offsetY = lookVec.y * 1.8;
        double offsetZ = lookVec.z * 1.8;
        Vec3 playerPos = shooter.getPosition(1F).add(offsetX, offsetY + shooter.getEyeHeight(), offsetZ);

        double attackRange = 8.0;
        double sweepAngle = Math.toRadians(gun.getGeneral().getSpread());
        double maxDistance = 10.0;

        Vec3 playerPos2 = shooter.position();

        BlockPos playerBlockPos = shooter.blockPosition();
        int fireExtinguishRange = 10;

        for (BlockPos pos : BlockPos.betweenClosed(
                playerBlockPos.offset(-fireExtinguishRange, -fireExtinguishRange, -fireExtinguishRange),
                playerBlockPos.offset(fireExtinguishRange, fireExtinguishRange, fireExtinguishRange))) {

            BlockState blockState = level.getBlockState(pos);

            if (blockState.is(Blocks.FIRE)) {
                Vec3 firePos = Vec3.atCenterOf(pos).subtract(playerPos2);
                double distance = firePos.length();
                double angle = Math.acos(firePos.normalize().dot(lookVec.normalize()));

                if (angle < sweepAngle / 2 && distance <= maxDistance) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        }

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, shooter.getBoundingBox().inflate(attackRange));

        level.playSound(null, shooter.getOnPos(), SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.PLAYERS, 2F, 1F);
        for (LivingEntity entity : entities) {
            if (entity == shooter) continue;

            Vec3 entityPos = entity.position().subtract(playerPos2);
            double distance = entityPos.length();
            double angle = Math.acos(entityPos.normalize().dot(lookVec.normalize()));

            if (angle < sweepAngle / 2) {
                double distanceMultiplier = 1.0 - Math.min(distance / maxDistance, 1.0);
                float adjustedDamage = (float) (maxDamage * distanceMultiplier);

                if (adjustedDamage > 0) {
                    entity.hurt(shooter.damageSources().sonicBoom(shooter), adjustedDamage);
                    entity.invulnerableTime = 0;

                    if (!shooter.level().isClientSide) {
                        ServerLevel serverLevel = (ServerLevel) shooter.level();
                        serverLevel.sendParticles(ParticleTypes.SCULK_CHARGE_POP, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 12, 0.2, 0.0, 0.3, 0.1);
                    }
                }
            }
        }

        level.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 2, offsetY / 2, offsetZ / 2);
        level.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 4, offsetY / 4, offsetZ / 4);
        level.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 1.5, offsetY / 1.5, offsetZ / 1.5);

        if (level instanceof ServerLevel serverLevel) {
            sendParticlesToAll(
                    serverLevel,
                    ModParticleTypes.SONIC_RING.get(),
                    true,
                    playerPos.x,
                    playerPos.y,
                    playerPos.z,
                    5,
                    0, 0, 0,
                    0.2
            );
            sendParticlesToAll(
                    serverLevel,
                    ModParticleTypes.BIG_SONIC_RING.get(),
                    true,
                    playerPos.x,
                    playerPos.y,
                    playerPos.z,
                    2,
                    0, 0, 0,
                    0.1
            );
        }
    }

    public static void hypersonicBlast(Level level, LivingEntity shooter, float damage) {
        Vec3 userPos = shooter.getEyePosition();
        Vec3 targetPos = userPos.add(shooter.getLookAngle().scale(100));
        Vec3 distanceTo = targetPos.subtract(userPos);
        Vec3 normal = distanceTo.normalize();

        if (!level.isClientSide()) {
            EntityHitResult e = ProjectileUtil.getEntityHitResult(level, shooter, userPos, targetPos,
                    new AABB(userPos, targetPos),
                    entity -> entity instanceof LivingEntity && entity != shooter);

            if (e != null && e.getEntity() instanceof LivingEntity entity) {
                float advantageMultiplier = 1F;
                if (entity.isOnFire()) {
                    entity.extinguishFire();
                    ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 1, entity.getZ(), 6, 0.3D, 0.3D, 0.3D, 0.0D);
                }
                ((ServerLevel) level).sendParticles(ParticleTypes.SCULK_CHARGE_POP, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 12, 0.2, 0.0, 0.3, 0.1);
                entity.hurt(shooter.damageSources().sonicBoom(shooter), damage * advantageMultiplier);
                entity.addEffect(new MobEffectInstance(ModEffects.DEAFENED.get(), 100, 0, false, false));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50));
                entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 120));
            }

            for (int i = 3; i < Mth.floor(distanceTo.length()); ++i) {
                Vec3 vec33 = userPos.add(normal.scale(i));
                if (level instanceof ServerLevel serverLevel) {
                    sendParticlesToAll(
                            serverLevel,
                            ParticleTypes.SONIC_BOOM,
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
            if (level instanceof ServerLevel serverLevel) {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.SONIC_RING.get(),
                        true,
                        userPos.x,
                        userPos.y,
                        userPos.z,
                        5,
                        0, 0, 0,
                        0.2
                );
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.BIG_SONIC_RING.get(),
                        true,
                        userPos.x,
                        userPos.y,
                        userPos.z,
                        2,
                        0, 0, 0,
                        0.1
                );
            }
        }
    }

    public static void trumpetBlast(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        double offsetX = lookVec.x * 1.8;
        double offsetY = lookVec.y * 1.8;
        double offsetZ = lookVec.z * 1.8;
        Vec3 playerPos = player.getPosition(1F).add(offsetX, offsetY + player.getEyeHeight(), offsetZ);

        double attackRange = 8.0;
        double sweepAngle = Math.toRadians(100);
        double maxDistance = 10.0;

        Vec3 playerPos2 = player.position();

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(attackRange));

        double pushStrength = 2;
        double opposite = -1;
        player.push(lookVec.x * opposite, lookVec.y * opposite, lookVec.z * opposite);
        player.fallDistance = 0;

        for (LivingEntity entity : entities) {
            if (entity == player) continue;

            Vec3 entityPos = entity.position().subtract(playerPos2);
            double distance = entityPos.length();
            double angle = Math.acos(entityPos.normalize().dot(lookVec.normalize()));

            if (angle < sweepAngle / 2) {
                double distanceMultiplier = 1.0 - Math.min(distance / maxDistance, 1.0);
                if (entity instanceof Player player1) {
                    player1.push(lookVec.x * (pushStrength * distanceMultiplier), lookVec.y * (pushStrength * distanceMultiplier), lookVec.z * (pushStrength * distanceMultiplier));
                } else {
                    entity.push(lookVec.x * (pushStrength * distanceMultiplier), lookVec.y * (pushStrength * distanceMultiplier), lookVec.z * (pushStrength * distanceMultiplier));
                }
            }
        }

        if (level.isClientSide) {
            level.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 2, offsetY / 2, offsetZ / 2);
            level.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 4, offsetY / 4, offsetZ / 4);
            level.addAlwaysVisibleParticle(ModParticleTypes.BIG_SONIC_RING.get(), playerPos.x, playerPos.y, playerPos.z, offsetX / 1.5, offsetY / 1.5, offsetZ / 1.5);
        }
    }

    public static void recoil(Player player, double force) {
        Vec3 lookVec = player.getLookAngle();
        double opposite = force;
        player.push(lookVec.x * opposite, lookVec.y * opposite, lookVec.z * opposite);
        player.fallDistance = 0;
    }

    public static void firingSmoke(Level level, Player player) {
        Vec3 lookVec = player.getLookAngle();
        double offsetX = lookVec.x * 1.8;
        double offsetY = lookVec.y * 1.8 + player.getEyeHeight();
        double offsetZ = lookVec.z * 1.8;
        Vec3 playerPos = player.getPosition(1F).add(offsetX, offsetY, offsetZ);


        if (!level.isClientSide) {
            ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE, playerPos.x, playerPos.y, playerPos.z, 6, 0, 0, 0, 0.2);
        }
    }
    public static void ejectPosedCasing(Level level, Vec3 pos) {
        if (level instanceof ServerLevel serverLevel)
        {
            serverLevel.sendParticles(ModParticleTypes.CASING_PARTICLE.get(),
                    pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    public static void ejectCasing(Level level, LivingEntity livingEntity)
    {
        ItemStack heldItem = livingEntity.getMainHandItem();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        Vec3 lookVec = livingEntity.getLookAngle();
        Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        Vec3 forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        double divisor = 0.5;
        if (livingEntity instanceof Player player) {
            boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
            divisor = isAiming ? 0.4 : 0.5;
        }

        double offsetX = rightVec.x * divisor + forwardVec.x * divisor; //Move the particle 0.5 blocks to the right and 0.5 blocks forward
        double offsetY = livingEntity.getEyeHeight() - 0.4; //Move the particle slightly below the player's head
        double offsetZ = rightVec.z * divisor + forwardVec.z * divisor; //Move the particle 0.5 blocks to the right and 0.5 blocks forward

        Vec3 particlePos = livingEntity.getPosition(1).add(offsetX, offsetY, offsetZ); //Add the offsets to the player's position

        ResourceLocation pistolAmmoLocation = ModItems.PISTOL_AMMO.getId();
        ResourceLocation rifleAmmoLocation = ModItems.RIFLE_AMMO.getId();
        ResourceLocation shotgunShellLocation = ModItems.SHOTGUN_SHELL.getId();
        ResourceLocation spectreAmmoLocation = ModItems.SPECTRE_ROUND.getId();
        ResourceLocation blazeAmmoLocation = ModItems.BLAZE_ROUND.getId();
        ResourceLocation projectileLocation = gun.getProjectile().getItem();

        SimpleParticleType casingType = ModParticleTypes.CASING_PARTICLE.get();

        if (projectileLocation != null) {
            if (projectileLocation.equals(pistolAmmoLocation) || projectileLocation.equals(rifleAmmoLocation)) {
                casingType = ModParticleTypes.CASING_PARTICLE.get();
            } else if (projectileLocation.equals(shotgunShellLocation)) {
                casingType = ModParticleTypes.SHELL_PARTICLE.get();
            }
            else if (projectileLocation.equals(spectreAmmoLocation)) {
                casingType = ModParticleTypes.SPECTRE_CASING_PARTICLE.get();
            }
        }

        if (level instanceof ServerLevel serverLevel)
        {
            serverLevel.sendParticles(casingType,
                    particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
        }
    }

    public static void specialEffects(Level level, LivingEntity livingEntity) {
        ItemStack heldItem = livingEntity.getMainHandItem();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        Vec3 lookVec = livingEntity.getLookAngle();
        Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        Vec3 forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        double divisor = 0.5;
        if (livingEntity instanceof Player player) {
            boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);
            divisor = isAiming ? 0.4 : 0.5;
        }

        double offsetX = rightVec.x * divisor + forwardVec.x * divisor; //Move the particle 0.5 blocks to the right and 0.5 blocks forward
        double offsetY = livingEntity.getEyeHeight() - 0.4; //Move the particle slightly below the player's head
        double offsetZ = rightVec.z * divisor + forwardVec.z * divisor; //Move the particle 0.5 blocks to the right and 0.5 blocks forward

        Vec3 particlePos = livingEntity.getPosition(1).add(offsetX, offsetY, offsetZ); //Add the offsets to the player's position

        if (level instanceof ServerLevel serverLevel)
        {
            if (livingEntity.getMainHandItem().is(ModItems.FLAMETHROWER.get())) {
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.MYCELIUM,
                        true,
                        particlePos.x,
                        particlePos.y + 2,
                        particlePos.z,
                        50,
                        10.0, 5, 10.0,
                        0
                );
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.WHITE_ASH,
                        true,
                        particlePos.x,
                        particlePos.y + 2,
                        particlePos.z,
                        50,
                        10.0, 5, 10.0,
                        0
                );
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.ASH,
                        true,
                        particlePos.x,
                        particlePos.y + 2,
                        particlePos.z,
                        50,
                        10.0, 5, 10.0,
                        0
                );
            }
            if (livingEntity.getMainHandItem().is(ModItems.BLOSSOM_RIFLE.get())) {
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.CHERRY_LEAVES,
                        true,
                        particlePos.x,
                        particlePos.y,
                        particlePos.z,
                        1,
                        0.3, 0.2, 0.3,
                        0
                );
            }
        }
    }

}