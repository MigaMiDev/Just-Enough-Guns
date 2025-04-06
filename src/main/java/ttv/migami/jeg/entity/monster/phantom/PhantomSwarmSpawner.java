package ttv.migami.jeg.entity.monster.phantom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.init.ModCommands;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;

import java.util.ArrayList;
import java.util.List;

public class PhantomSwarmSpawner implements CustomSpawner {
    //private int nextTick = (24000 * 5) + 12000;
    private int nextTick;

    private final int MAX_SWARM_PARTICLE_TICK = 1200 * 5;
    private int swarmParticleTick = MAX_SWARM_PARTICLE_TICK;

    public PhantomSwarmSpawner() {
    }

    @Override
    public int tick(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        if (!Config.COMMON.gunnerMobs.phantomSwarm.get()) {
            return 0;
        }

        if (level.getDifficulty().equals(Difficulty.PEACEFUL)) {
            return 0;
        }

        if (!spawnEnemies || !level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        }

        PhantomSwarmData raidData = PhantomSwarmData.get(level);
        this.nextTick = raidData.getNextTick();
        if (!raidData.hasPhantomSwarm()) {
            return 0;
        }

        RandomSource random = level.random;
        --this.nextTick;
        raidData.setNextTick(this.nextTick);

        int playerCount2 = level.players().size();
        if (playerCount2 < 1) {
            return 0;
        }

        Player swarmPlayer = level.players().get(random.nextInt(playerCount2));
        if (swarmPlayer.isSpectator()) {
            return 0;
        }

        if (--this.swarmParticleTick == 0 && swarmPlayer instanceof ServerPlayer serverPlayer) {
            spawnPhantomGunnerFormation(level, serverPlayer);
            this.swarmParticleTick = MAX_SWARM_PARTICLE_TICK;
        }

        if (this.nextTick > 0) {
            return 0;
        }

        int fixedDaysInterval = Config.COMMON.gunnerMobs.patrolIntervalDays.get();
        int randomIntervalMin = Config.COMMON.gunnerMobs.randomIntervalMinTicks.get();
        int randomIntervalMax = Config.COMMON.gunnerMobs.randomIntervalMaxTicks.get();

        if (fixedDaysInterval > 0) {
            this.nextTick += fixedDaysInterval * 24000;
        } else {
            this.nextTick += randomIntervalMin + random.nextInt(randomIntervalMax - randomIntervalMin + 1);
        }

        // Adds a chance for the Patrol to spawn in either Daytime or Nighttime
        this.nextTick += random.nextInt(12000);
        raidData.setNextTick(this.nextTick);

        long dayTime = level.getDayTime() / 24000L;

        int minimumDays = Config.COMMON.gunnerMobs.minimumDaysForPatrols.get();
        if (dayTime < minimumDays) {
            return 0;
        }

        int playerCount = level.players().size();
        if (playerCount < 1) {
            return 0;
        }

        Player randomPlayer = level.players().get(random.nextInt(playerCount));
        if (randomPlayer.isSpectator() || level.isCloseToVillage(randomPlayer.blockPosition(), 2)) {
            return 0;
        }

        BlockPos.MutableBlockPos spawnPos = randomPlayer.blockPosition().mutable()
            .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                  0,
                  (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));

        if (!level.hasChunksAt(spawnPos.getX() - 10, spawnPos.getZ() - 10, spawnPos.getX() + 10, spawnPos.getZ() + 10)) {
            return 0;
        }

        Holder<Biome> biome = level.getBiome(spawnPos);
        if (biome.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return 0;
        }

        int patrolCount = 4 + random.nextInt(4);
        return ModCommands.spawnPhantomSwarm(level, patrolCount, randomPlayer, spawnPos);
    }

    public static void spawnPhantomGunnerFormation(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();

        boolean randomX = random.nextBoolean();
        boolean randomZ = random.nextBoolean();

        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable()
                .move((48 + random.nextInt(48)) * (randomX ? -1 : 1),
                        0,
                        (48 + random.nextInt(48)) * (randomZ ? -1 : 1));

        Vec3 betterPos = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        Vec3 spawnCenter = betterPos.add(0, 170, 0);

        level.playSound(null, BlockPos.containing(spawnCenter), ModSounds.EVENT_PHANTOM_SWARM.get(), SoundSource.HOSTILE, 100F, 1.0F);

        int numParticles = 2 + random.nextInt(2);
        double spacing = 2.5;
        List<Vec3> formationPositions = new ArrayList<>();

        for (int i = 0; i < numParticles; i++) {
            int row = i / 2;
            int side = (i % 2 == 0) ? 1 : -1;

            double xOffset = row * spacing * side;
            double zOffset = row * spacing;

            formationPositions.add(spawnCenter.add(xOffset, 0, zOffset));
        }

        for (Vec3 pos : formationPositions) {
            double speedX = (betterPos.x - pos.x) * 0.1;
            double speedZ = (betterPos.z - pos.z) * 0.1;

            for (ServerPlayer players : level.players()) {
                level.sendParticles(
                        players,
                        ModParticleTypes.PHANTOM_GUNNER.get(),
                        true,
                        pos.x, pos.y, pos.z,
                        1,
                        speedX, 0, speedZ,
                        1
            );}
        }
    }

    public static void spawnPhantomGunnerSwarm(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();

        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable()
                .move((0 + random.nextInt(175)) * (random.nextBoolean() ? -1 : 1),
                        0,
                        (100 + random.nextInt(64)) * (-1));

        Vec3 betterPos = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        Vec3 spawnCenter = betterPos.add(0, 170, 0);


        int numParticles = 2 + random.nextInt(2);
        double spacing = 10;
        List<Vec3> formationPositions = new ArrayList<>();

        for (int i = 0; i < numParticles; i++) {
            int row = i / 2;
            int side = (i % 2 == 0) ? 1 : -1;

            double xOffset = row * spacing * side;
            double zOffset = row * spacing;

            formationPositions.add(spawnCenter.add(xOffset, 0, zOffset));
        }

        for (Vec3 pos : formationPositions) {
            double speedX = (betterPos.x - pos.x) * 0.1;
            double speedZ = (betterPos.z - pos.z) * 0.1;

            level.sendParticles(
                    player,
                    ModParticleTypes.PHANTOM_GUNNER_SWARM.get(),
                    true,
                    pos.x, pos.y, pos.z,
                    1,
                    speedX, 0, speedZ,
                    1
            );
        }
    }

    public static void spawnSecondLayerPhantomGunnerSwarm(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();

        BlockPos.MutableBlockPos spawnPos = player.blockPosition().mutable()
                .move((0 + random.nextInt(175)) * (random.nextBoolean() ? -1 : 1),
                        0,
                        (100 + random.nextInt(64)) * (-1));

        Vec3 betterPos = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());

        Vec3 spawnCenter = betterPos.add(0, 170, 0);


        int numParticles = 2 + random.nextInt(2);
        double spacing = 10;
        List<Vec3> formationPositions = new ArrayList<>();

        for (int i = 0; i < numParticles; i++) {
            int row = i / 2;
            int side = (i % 2 == 0) ? 1 : -1;

            double xOffset = row * spacing * side;
            double zOffset = row * spacing;

            formationPositions.add(spawnCenter.add(xOffset, 0, zOffset));
        }

        for (Vec3 pos : formationPositions) {
            double speedX = (betterPos.x - pos.x) * 0.1;
            double speedZ = (betterPos.z - pos.z) * 0.1;

            level.sendParticles(
                    player,
                    ModParticleTypes.SECOND_LAYER_PHANTOM_GUNNER_SWARM.get(),
                    true,
                    pos.x, pos.y, pos.z,
                    1,
                    speedX, 0, speedZ,
                    1
            );
        }
    }
}