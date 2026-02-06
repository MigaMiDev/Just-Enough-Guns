package ttv.migami.jeg.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.client.BulletTrail;
import ttv.migami.jeg.client.CustomGunManager;
import ttv.migami.jeg.client.audio.GunShotSound;
import ttv.migami.jeg.client.handler.BulletTrailRenderingHandler;
import ttv.migami.jeg.client.handler.GunRenderingHandler;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.NetworkGunManager;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModTags;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.network.message.*;
import ttv.migami.jeg.particles.BulletHoleData;
import ttv.migami.jeg.particles.LaserData;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Author: MrCrayfish
 */
public class ClientPlayHandler
{
    private static final Random RANDOM = new Random();
    private static final AtomicLong lastSoundTime = new AtomicLong(0);
    private static final long SOUND_COOLDOWN_MS = 50;

    public static void handleMessageGunSound(S2CMessageGunSound message)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.level == null)
            return;

        if(message.showMuzzleFlash())
        {
            GunRenderingHandler.get().showMuzzleFlashForPlayer(message.getShooterId());
        }

        if(message.getShooterId() == mc.player.getId())
        {
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(message.getId(), SoundSource.PLAYERS, message.getVolume(), message.getPitch(), mc.level.getRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
        else
        {
            Minecraft.getInstance().getSoundManager().play(new GunShotSound(message.getId(), SoundSource.PLAYERS, message.getX(), message.getY(), message.getZ(), message.getVolume(), message.getPitch(), message.isReload(), mc.level.getRandom()));
        }
    }

    public static void handleMessageBlood(S2CMessageBlood message)
    {
        if(!Config.CLIENT.particle.enableBlood.get())
        {
            return;
        }
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            for(int i = 0; i < 10; i++)
            {
                world.addParticle(ModParticleTypes.BLOOD.get(), true, message.getX(), message.getY(), message.getZ(), 0.5, 0, 0.5);
            }
        }
    }

    public static void handleExplosiveAmmo(S2CMessageExplosiveAmmo message)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            world.addParticle(ModParticleTypes.SMALL_EXPLOSION.get(), true, message.getX(), message.getY(), message.getZ(), 0.0, 0, 0.0);
        }
    }

    public static void handleMessageBulletTrail(S2CMessageBulletTrail message)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            int[] entityIds = message.getEntityIds();
            Vec3[] positions = message.getPositions();
            Vec3[] motions = message.getMotions();
            ItemStack item = message.getItem();
            int trailColor = message.getTrailColor();
            double trailLengthMultiplier = message.getTrailLengthMultiplier();
            int life = message.getLife();
            double gravity = message.getGravity();
            int shooterId = message.getShooterId();
            boolean enchanted = message.isEnchanted();
            ParticleOptions data = message.getParticleData();
            boolean isVisible = message.isVisible();
            for(int i = 0; i < message.getCount(); i++)
            {
                BulletTrailRenderingHandler.get().add(new BulletTrail(entityIds[i], positions[i], motions[i], item, trailColor, trailLengthMultiplier, life, gravity, shooterId, enchanted, data, isVisible));
            }
        }
    }

    public static void handleExplosionStunGrenade(S2CMessageStunGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        ParticleEngine particleManager = mc.particleEngine;
        Level world = Objects.requireNonNull(mc.level);
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        for(int i = 0; i < 30; i++)
        {
            spawnParticle(particleManager, ParticleTypes.CLOUD, x, y, z, world.random, 0.2);
        }

        for(int i = 0; i < 30; i++)
        {
            Particle smoke = spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, world.random, 4.0);
            smoke.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.4)) * 0.5));
            spawnParticle(particleManager, ParticleTypes.CRIT, x, y, z, world.random, 4.0);
        }
    }

    public static void handleExplosionSmokeGrenade(S2CMessageSmokeGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level level = Objects.requireNonNull(mc.level);
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();
        double diameter = Config.COMMON.smokeGrenades.smokeGrenadeCloudDiameter.get();
        double vel = 0.004;
        int amount = (int) (diameter * 15);

        for(int i = 0; i < amount; i++)
        {
            level.addAlwaysVisibleParticle(ModParticleTypes.SMOKE_CLOUD.get(),
                    true,
                    x+((Math.random()-0.5) * diameter),
                    y+(Math.random() * (diameter * 0.5)),
                    z+((Math.random()-0.5) * diameter),
                    (Math.random()-0.5) * vel,
                    Math.random() * (vel * 0.5),
                    (Math.random()-0.5) * vel);
        }
    }

    private static Particle spawnParticle(ParticleEngine manager, ParticleOptions data, double x, double y, double z, RandomSource rand, double velocityMultiplier)
    {
        return manager.createParticle(data, x, y, z, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier);
    }

    // Laser
    public static void handleLaser(S2CMessageLaser message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world != null)
        {
            double holeX = message.getX() + 0.005 * message.getFace().getStepX();
            double holeY = message.getY() + 0.005 * message.getFace().getStepY();
            double holeZ = message.getZ() + 0.005 * message.getFace().getStepZ();
            world.addParticle(new LaserData(message.getFace(), message.getPos()), false, holeX, holeY, holeZ, 0, 0, 0);
            Vec3i normal = message.getFace().getNormal();
            Vec3 motion = new Vec3(normal.getX(), normal.getY(), normal.getZ());
            motion.add(getRandomDir(world.random), getRandomDir(world.random), getRandomDir(world.random));
        }
    }

    // Bullet Sparks
    public static void handleProjectileHitBlock(S2CMessageProjectileHitBlock message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world != null)
        {
            BlockState state = world.getBlockState(message.getPos());
            double holeX = message.getX() + 0.005 * message.getFace().getStepX();
            double holeY = message.getY() + 0.005 * message.getFace().getStepY();
            double holeZ = message.getZ() + 0.005 * message.getFace().getStepZ();
            double distance = Math.sqrt(mc.player.distanceToSqr(message.getX(), message.getY(), message.getZ()));
            world.addParticle(new BulletHoleData(message.getFace(), message.getPos()), false, holeX, holeY, holeZ, 0, 0, 0);
            Vec3i normal = message.getFace().getNormal();
            Vec3 motion = new Vec3(normal.getX(), normal.getY(), normal.getZ());
            motion.add(getRandomDir(world.random), getRandomDir(world.random), getRandomDir(world.random));

            SoundType soundType = state.getSoundType();
            if (state.is(ModTags.Blocks.METAL) || soundType.equals(SoundType.METAL)) {
                world.playLocalSound(message.getX(), message.getY(), message.getZ(), ModSounds.METAL_HIT.get(), SoundSource.BLOCKS, 0.5F, 1.0F, false);
                for(int i = 0; i < 2; i++)
                {
                    world.addParticle(ModParticleTypes.SPARK.get(), false, message.getX(), message.getY(), message.getZ(), motion.x, motion.y, motion.z);
                }
            }
            if (state.is(ModTags.Blocks.STONE) || soundType.equals(SoundType.STONE)) {
                world.playLocalSound(message.getX(), message.getY(), message.getZ(), ModSounds.STONE_HIT.get(), SoundSource.BLOCKS, 0.4F, 1.0F, false);
                world.addParticle(ParticleTypes.CLOUD, true, message.getX(), message.getY(), message.getZ(), motion.x * RANDOM.nextFloat() / 10, motion.y * RANDOM.nextFloat() / 10, motion.z * RANDOM.nextFloat() / 10);
                for(int i = 0; i < 2; i++)
                {
                    world.addParticle(ModParticleTypes.SPARK.get(), false, message.getX(), message.getY(), message.getZ(), motion.x, motion.y, motion.z);
                }
            }
            if (((state.is(BlockTags.MINEABLE_WITH_AXE)) || state.is(ModTags.Blocks.WOOD)) || soundType.equals(SoundType.WOOD)) {
                world.playLocalSound(message.getX(), message.getY(), message.getZ(), ModSounds.WOOD_HIT.get(), SoundSource.BLOCKS, 0.4F, 1.0F, false);
                world.addParticle(ParticleTypes.CLOUD, false, message.getX(), message.getY(), message.getZ(), motion.x * RANDOM.nextFloat() / 10, motion.y * RANDOM.nextFloat() / 10, motion.z * RANDOM.nextFloat() / 10);
            }
            if (state.is(ModTags.Blocks.SQUISHY)) {
                world.playLocalSound(message.getX(), message.getY(), message.getZ(), ModSounds.SQUISHY_BREAK.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            if (Gun.hasAttachmentEquipped(mc.player.getMainHandItem(), IAttachment.Type.BARREL)) {
                if (Gun.getAttachment(IAttachment.Type.BARREL, mc.player.getMainHandItem()).getItem() == ModItems.EXPLOSIVE_MUZZLE.get()) {
                    world.addParticle(ModParticleTypes.SMALL_EXPLOSION.get(), true, message.getX(), message.getY(), message.getZ(), motion.x  / 12, motion.y  / 12, motion.z / 12);
                }
            }

            if(distance < Config.CLIENT.particle.impactParticleDistance.get())
            {
                for(int i = 0; i < 4; i++)
                {
                    motion.add(getRandomDir(world.random), getRandomDir(world.random), getRandomDir(world.random));
                    world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), false, message.getX(), message.getY(), message.getZ(), motion.x, motion.y, motion.z);
                }
            }
            if(distance <= Config.CLIENT.sounds.impactSoundDistance.get())
            {
                world.playLocalSound(message.getX(), message.getY(), message.getZ(), state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 2.0F, false);
            }
        }
    }

    private static double getRandomDir(RandomSource random)
    {
        return -0.25 + random.nextDouble() * 0.5;
    }

    public static void handleProjectileHitEntity(S2CMessageProjectileHitEntity message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world == null)
            return;

        if (Config.CLIENT.display.hitmarker.get()) {
            GunRenderingHandler.get().playHitMarker(message.isCritical() || message.isHeadshot());
        }

        if (message.isHeadshot()) {

        }

        SoundEvent event = getHitSound(message.isCritical(), message.isHeadshot(), message.isPlayer());
        if(event == null)
            return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSoundTime.get() > SOUND_COOLDOWN_MS) {
            lastSoundTime.set(currentTime);
            mc.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F, 1.0F + world.random.nextFloat() * 0.2F));
        }
    }

    @Nullable
    private static SoundEvent getHitSound(boolean critical, boolean headshot, boolean player)
    {
        if(critical)
        {
            if(Config.CLIENT.sounds.playSoundWhenCritical.get())
            {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.sounds.criticalSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_CRIT;
            }
        }
        else if(headshot)
        {
            if(Config.CLIENT.sounds.playSoundWhenHeadshot.get())
            {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.sounds.headshotSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_KNOCKBACK;
            }
        }
        else if(player)
        {
            return SoundEvents.PLAYER_HURT;
        }
        // Hit Marker Sound
        else if (Config.CLIENT.sounds.playHitMarkerSound.get()) {
            return ModSounds.HIT_MARKER.get();
        }
        return null;
    }


    public static void handleRemoveProjectile(S2CMessageRemoveProjectile message)
    {
        BulletTrailRenderingHandler.get().remove(message.getEntityId());
    }

    public static void handleUpdateGuns(S2CMessageUpdateGuns message)
    {
        NetworkGunManager.updateRegisteredGuns(message);
        CustomGunManager.updateCustomGuns(message);
    }
}
