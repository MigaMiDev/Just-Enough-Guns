package ttv.migami.jeg.entity.ai;

import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.ProjectileManager;
import ttv.migami.jeg.common.SpreadTracker;
import ttv.migami.jeg.entity.monster.phantom.gunner.PhantomGunner;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;
import ttv.migami.jeg.entity.projectile.ProjectileEntity;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.init.ModEffects;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.interfaces.IProjectileFactory;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.S2CMessageBulletTrail;
import ttv.migami.jeg.util.GunEnchantmentHelper;
import ttv.migami.jeg.util.GunModifierHelper;

public class AIGunEvent {
    /*public static void performTurretAttack(BasicTurretBlockEntity turret, ItemStack itemStack, Gun modifiedGun) {
        LivingEntity shooter = turret.getOwner();
        final Level level = shooter.level();
        if (level.isClientSide()) return;
        int count = modifiedGun.getGeneral().getProjectileAmount();
        Gun.Projectile projectileProps = modifiedGun.getProjectile();
        ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];
        Vec3 pos = turret.getBlockPos().getCenter();
        for (int i = 0; i < count; ++i) {
            IProjectileFactory factory = ProjectileManager.getInstance().getFactory(projectileProps.getItem());
            ProjectileEntity projectileEntity = factory.create(level, shooter, itemStack, (GunItem) itemStack.getItem(), modifiedGun);
            projectileEntity.setWeapon(itemStack);
            projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(itemStack));

            Vec3 dir = getTurretDirection(turret);
            double speed = 5;

            projectileEntity.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
            projectileEntity.updateHeading();

            double posX = pos.x;
            double posY = pos.y + 0.8;
            double posZ = pos.z;
            projectileEntity.setPos(posX, posY, posZ);

            level.addFreshEntity(projectileEntity);
            spawnedProjectiles[i] = projectileEntity;
            projectileEntity.tick();
        }
        if (!projectileProps.isVisible()) {
            int radius = (int) pos.x;
            int y1 = (int) (pos.y + 1.0);
            int z1 = (int) pos.z;
            double r = Config.COMMON.network.projectileTrackingRange.get();
            ParticleOptions data = GunEnchantmentHelper.getParticle(itemStack);
            boolean isVisible = !modifiedGun.getProjectile().hideTrail();
            S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), data, isVisible);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(level, radius, y1, z1, r),
                    messageBulletTrail
            );
        }
    }*/

    public static void performGunAttack(Mob shooter, LivingEntity target, ItemStack itemStack, Gun modifiedGun, float spreadModifier) {
        final Level level = shooter.level();
        if (level.isClientSide()) return;
        int count = modifiedGun.getGeneral().getProjectileAmount();
        Gun.Projectile projectileProps = modifiedGun.getProjectile();
        ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];

        if (shooter.hasEffect(ModEffects.SMOKED.get()) || shooter.hasEffect(ModEffects.BLINDED.get())) {
            spreadModifier *= 2;
        }
        if (target.hasEffect(ModEffects.SMOKED.get())) {
            spreadModifier *= 1.5F;
        }

        for (int i = 0; i < count; ++i) {
            IProjectileFactory factory = ProjectileManager.getInstance().getFactory(projectileProps.getItem());
            ProjectileEntity projectileEntity = factory.create(level, shooter, itemStack, (GunItem) itemStack.getItem(), modifiedGun);
            projectileEntity.setWeapon(itemStack);
            projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(itemStack));

            Vec3 dir = getDirection(shooter, itemStack, (GunItem) itemStack.getItem(), modifiedGun, spreadModifier);
            double speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(itemStack);
            double speed = GunModifierHelper.getModifiedProjectileSpeed(itemStack, projectileEntity.getProjectile().getSpeed() * speedModifier);

            if (shooter instanceof PhantomGunner || shooter instanceof TerrorPhantom) {
                speed = 6.0F;
            }

            projectileEntity.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
            projectileEntity.updateHeading();

            double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
            double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
            double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
            projectileEntity.setPos(posX, posY, posZ);

            level.addFreshEntity(projectileEntity);
            spawnedProjectiles[i] = projectileEntity;
            projectileEntity.tick();
        }
        if (!projectileProps.isVisible()) {
            int radius = (int) shooter.getX();
            int y1 = (int) (shooter.getY() + 1.0);
            int z1 = (int) shooter.getZ();
            double r = Config.COMMON.network.projectileTrackingRange.get();
            ParticleOptions data = GunEnchantmentHelper.getParticle(itemStack);
            boolean isVisible = !modifiedGun.getProjectile().hideTrail();
            S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, shooter.getId(), data, isVisible);
            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create(level, radius, y1, z1, r),
                    messageBulletTrail
            );
        }

        if (Config.COMMON.gameplay.mobDynamicLightsOnShooting.get()) {
            BlockState targetState = shooter.level().getBlockState(BlockPos.containing(shooter.getEyePosition()));
            if (targetState.getBlock() == ModBlocks.BRIGHT_DYNAMIC_LIGHT.get()) {
                if (getValue(shooter.level(), BlockPos.containing(shooter.getEyePosition()), "Delay") < 1.0) {
                    updateDelayAndNotify(shooter.level(), BlockPos.containing(shooter.getEyePosition()), targetState);
                }
            } else if (targetState.getBlock() == Blocks.AIR || targetState.getBlock() == Blocks.CAVE_AIR) {
                BlockState dynamicLightState = ModBlocks.BRIGHT_DYNAMIC_LIGHT.get().defaultBlockState();
                shooter.level().setBlock(BlockPos.containing(shooter.getEyePosition()), dynamicLightState, 3);
            }
        }
    }

    public static Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun, float spreadModifier)
    {
        float gunSpread = GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());

        if(gunSpread == 0F)
        {
            return getVectorFromRotation(shooter.getViewXRot(1F), shooter.getViewYRot(1F));
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
            //gunSpread *= shooter.level().getDifficulty() != Difficulty.HARD ? 10F : 5F;
            gunSpread *= shooter.level().getDifficulty() != Difficulty.HARD ? spreadModifier*2 : spreadModifier;
            if (gunSpread > 60) {
                gunSpread = 60;
            }
        }

        gunSpread = Math.min(gunSpread, 170F) * 0.5F * Mth.DEG_TO_RAD;

        Vec3 vecforwards = getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        Vec3 vecupwards = getVectorFromRotation(shooter.getXRot() + 90F, shooter.getYRot());
        Vec3 vecsideways = vecforwards.cross(vecupwards);

        float theta = shooter.level().random.nextFloat() * 2F * (float) Math.PI;
        float r = Mth.sqrt(shooter.level().random.nextFloat()) * (float) Math.tan((double) gunSpread);

        float a1 = Mth.cos(theta) * r;
        float a2 = Mth.sin(theta) * r;

        if (shooter instanceof TerrorPhantom terrorPhantom) {
            return getDirectionToTarget(shooter, terrorPhantom.getTarget()).add(vecsideways.scale(a1)).add(vecupwards.scale(a2)).normalize();
        }
        if (shooter instanceof PhantomGunner phantomGunner) {
            return getDirectionToTarget(shooter, phantomGunner.getTarget()).add(vecsideways.scale(a1)).add(vecupwards.scale(a2)).normalize();
        }

        return vecforwards.add(vecsideways.scale(a1)).add(vecupwards.scale(a2)).normalize();
    }

    public static Vec3 getDirectionToTarget(Entity entity, Entity target) {
        if (target == null) return Vec3.ZERO;

        Vec3 entityPos = entity.position();
        Vec3 targetPos = target.position();

        return targetPos.subtract(entityPos).normalize();
    }

    /*public static Vec3 getTurretDirection(BasicTurretBlockEntity turret)
    {
        Vec3 vecforwards = getVectorFromTurretRotation(turret.getPitch(), turret.getYaw());
        Vec3 vecupwards = getVectorFromTurretRotation(turret.getPitch() + 90F, turret.getYaw());
        Vec3 vecsideways = vecforwards.cross(vecupwards);

        vecforwards = new Vec3(vecforwards.x, -vecforwards.y, -vecforwards.z);
        vecupwards = new Vec3(vecupwards.x, -vecupwards.y, vecupwards.z);

        float theta = turret.getLevel().random.nextFloat() * 2F * (float) Math.PI;
        float r = Mth.sqrt(turret.getLevel().random.nextFloat()) * (float) Math.tan(0.05F);

        float a1 = Mth.cos(theta) * r;
        float a2 = Mth.sin(theta) * r;

        return vecforwards.add(vecsideways.scale(a1)).add(vecupwards.scale(a2)).normalize();
    }*/

    public static Vec3 getVectorFromTurretRotation(float pitch, float yaw) {
        float f = Mth.cos(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float f1 = Mth.sin(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * ((float) Math.PI / 180F));
        float f3 = Mth.sin(-pitch * ((float) Math.PI / 180F));
        return new Vec3(f1 * f2, f3, f * f2);
    }

    private static Vec3 getVectorFromRotation(float pitch, float yaw)
    {
        float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    private static void updateDelayAndNotify(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.getPersistentData().putDouble("Delay", 1.0);
        }
        if (world instanceof Level) {
            ((Level) world).sendBlockUpdated(pos, state, state, 3);
        }
    }

    public static double getValue(LevelAccessor world, BlockPos pos, String tag) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.getPersistentData().getDouble(tag) : -1.0;
    }
}