package ttv.migami.jeg.faction.raid;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.entity.monster.phantom.terror.TerrorPhantom;
import ttv.migami.jeg.entity.throwable.ThrowableExplosiveChargeEntity;
import ttv.migami.jeg.faction.jeg.FactionData;
import ttv.migami.jeg.faction.jeg.FactionDataManager;
import ttv.migami.jeg.init.*;

import java.util.HashSet;
import java.util.List;

import static ttv.migami.jeg.entity.monster.phantom.PhantomSwarmSpawner.spawnPhantomGunnerSwarm;
import static ttv.migami.jeg.entity.monster.phantom.PhantomSwarmSpawner.spawnSecondLayerPhantomGunnerSwarm;

public class TerrorRaidEntity extends Entity {
    private final ServerBossEvent bossBar;
    private final HashSet<LivingEntity> activeMobs = new HashSet<>();
    private final HashSet<LivingEntity> spawnedMobs = new HashSet<>();
    private final HashSet<Player> activePlayers = new HashSet<>();
    private boolean forceGuns = true;
    private int totalWaves = 3;
    private int maxCooldown = 200;
    private int maxBreakTime = 100;

    private boolean inWave = false;
    private boolean justFinishedWave = false;
    private boolean spawningWave = false;
    private boolean isSpawningMobs = false;
    private int currentWave = 0;
    private boolean isFinished = false;
    private int cooldown = maxCooldown;
    private int breakTime = maxBreakTime;
    private int spawnInterval = 60;
    private int spawnCooldown = spawnInterval;
    private boolean victory = false;
    private boolean defeat = false;
    private int despawnTicks = MAX_DESPAWN_TICKS;
    private boolean result = true;
    private int resultPrize = 1;
    private boolean summonBoss = true;

    private static final int MAX_ACTIVE_MOBS = 10;
    private int totalWaveMobs = 20;
    private static final int MAX_DESPAWN_TICKS = 200;

    private static final int ACTIVE_RADIUS = 64;

    private final int MAX_SWARM_PARTICLE_TICK = 7;
    private int swarmParticleTick = MAX_SWARM_PARTICLE_TICK;

    public TerrorRaidEntity(EntityType<? extends Entity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);

        MutableComponent factionLang = Component.translatable("faction.jeg.terror_armada");
        this.bossBar = new ServerBossEvent(
                Component.translatable(factionLang.getString() + " active raid!"),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossBar.setVisible(true);
    }

    public TerrorRaidEntity(EntityType<? extends Entity> type, Level level, boolean forceGuns, int waves, int waveSize) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);

        this.forceGuns = forceGuns;
        this.totalWaves = waves;

        MutableComponent factionLang = Component.translatable("faction.jeg.terror_armada");
        MutableComponent raidLang = Component.translatable("raid.jeg");
        this.bossBar = new ServerBossEvent(
                Component.translatable(factionLang.getString() + " " + raidLang.getString()),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossBar.setVisible(true);
    }

    private static ItemStack getRandomHorseArmor(RandomSource random) {
        Item[] horseArmors = {
                Items.IRON_HORSE_ARMOR,
                Items.GOLDEN_HORSE_ARMOR,
                Items.DIAMOND_HORSE_ARMOR
        };
        return new ItemStack(horseArmors[random.nextInt(horseArmors.length)]);
    }

    public void spawnMobs(ServerLevel level, Vec3 startPos, boolean forceGuns, int spread) {
        BlockPos.MutableBlockPos spawnPos = this.blockPosition().mutable()
                .move((12 + random.nextInt(12)) * (random.nextBoolean() ? -1 : 1),
                        0,
                        (12 + random.nextInt(12)) * (random.nextBoolean() ? -1 : 1));

        if (level.hasChunksAt(spawnPos.getX() - 10, spawnPos.getZ() - 10, spawnPos.getX() + 10, spawnPos.getZ() + 10)) {
            if (activeMobs.size() < MAX_ACTIVE_MOBS && spawnedMobs.size() < this.totalWaveMobs) {
                int mobsToSpawn = this.random.nextInt(2, 3);
                for (int i = 0; i < mobsToSpawn; i++) {
                    if (activeMobs.size() >= MAX_ACTIVE_MOBS) {
                        break;
                    }

                    FactionData factionData = null;
                    if (this.currentWave == 1) {
                        factionData = FactionDataManager.getTerrorArmadaWave1();
                    } else if (this.currentWave == 2) {
                        factionData = FactionDataManager.getTerrorArmadaWave2();
                    } else if (this.currentWave == 3) {
                        factionData = FactionDataManager.getTerrorArmadaWave3();
                    }

                    if (factionData == null) {
                        return;
                    }

                    if (!this.activePlayers.isEmpty() && this.currentWave == 2 && this.random.nextFloat() < 0.4F) {
                        for (int flock = 0; flock < 1; flock++) {
                            Player swarmPlayer = this.activePlayers.stream().findAny().get();
                            BlockPos.MutableBlockPos phantomPos = swarmPlayer.blockPosition().mutable()
                                    .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                                            0,
                                            (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));

                            ModCommands.spawnPhantomSwarm(level, 1, swarmPlayer, phantomPos);
                        }
                    }

                    if (!this.activePlayers.isEmpty() && this.currentWave == 3 && this.random.nextFloat() < 0.3F) {
                        for (int flock = 0; flock < 1; flock++) {
                            Player swarmPlayer = this.activePlayers.stream().findAny().get();
                            BlockPos.MutableBlockPos phantomPos = swarmPlayer.blockPosition().mutable()
                                    .move((24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1),
                                            0,
                                            (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1));

                            ModCommands.spawnPhantomGunnerSquad(level, 2, swarmPlayer, phantomPos);
                        }
                    }

                    LivingEntity mob = ModCommands.getFactionMob(level, factionData, startPos, forceGuns, spread);
                    if (mob != null) {
                        if (ModCommands.spawnRaider(level, mob, null, spawnPos, this.position(), true)) {
                            activeMobs.add(mob);
                            spawnedMobs.add(mob);
                            if (!this.activePlayers.isEmpty() && mob instanceof PathfinderMob pathfinderMob) {
                                pathfinderMob.setTarget(this.activePlayers.stream().findAny().get());
                            }

                            // Horsemen!
                            if (mob.getTags().contains("EliteGunner") && level.random.nextBoolean() && Config.COMMON.gunnerMobs.horsemen.get()) {
                                if (mob instanceof Zombie) {
                                    ZombieHorse zombieHorse = new ZombieHorse(EntityType.ZOMBIE_HORSE, this.level());
                                    zombieHorse.setPos(mob.position());
                                    zombieHorse.addTag("GunnerPatroller");

                                    if (level.random.nextInt(3) == 0) {
                                        ItemStack randomHorseArmor = getRandomHorseArmor(level.random);
                                        zombieHorse.setItemSlot(EquipmentSlot.CHEST, randomHorseArmor);
                                    }

                                    this.level().addFreshEntity(zombieHorse);
                                    zombieHorse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400));
                                    mob.startRiding(zombieHorse);
                                } else if (mob instanceof AbstractSkeleton) {
                                    SkeletonHorse skeletonHorse = new SkeletonHorse(EntityType.SKELETON_HORSE, this.level());
                                    skeletonHorse.setPos(mob.position());
                                    skeletonHorse.addTag("GunnerPatroller");

                                    if (level.random.nextInt(3) == 0) {
                                        ItemStack randomHorseArmor = getRandomHorseArmor(level.random);
                                        skeletonHorse.setItemSlot(EquipmentSlot.CHEST, randomHorseArmor);
                                    }

                                    this.level().addFreshEntity(skeletonHorse);
                                    skeletonHorse.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400));
                                    mob.startRiding(skeletonHorse);
                                }
                            }

                            // Reinforcements!
                            if (Config.COMMON.gunnerMobs.explosiveMobs.get()) {
                                if (this.level().random.nextFloat() < 0.2) {
                                    if (this.level().random.nextBoolean()) {
                                        Creeper annoyingBoy = new Creeper(EntityType.CREEPER, this.level());
                                        annoyingBoy.setPos(mob.position());
                                        activeMobs.add(annoyingBoy);
                                        spawnedMobs.add(annoyingBoy);
                                        annoyingBoy.addTag("GunnerPatroller");
                                        this.level().addFreshEntity(annoyingBoy);
                                        if (!this.activePlayers.isEmpty()) {
                                            annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                        }
                                    } else {
                                        Phantom annoyingBoy = new Phantom(EntityType.PHANTOM, this.level());
                                        annoyingBoy.setPos(mob.position());
                                        activeMobs.add(annoyingBoy);
                                        spawnedMobs.add(annoyingBoy);
                                        annoyingBoy.addTag("GunnerPatroller");
                                        this.level().addFreshEntity(annoyingBoy);

                                        ThrowableExplosiveChargeEntity explosiveChargeEntity = new ThrowableExplosiveChargeEntity(ModEntities.THROWABLE_EXPLOSIVE_CHARGE.get(), level);
                                        this.level().addFreshEntity(explosiveChargeEntity);
                                        explosiveChargeEntity.startRiding(annoyingBoy);

                                        if (!this.activePlayers.isEmpty()) {
                                            annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                        }
                                    }
                                }
                            }
                            if (this.level().random.nextFloat() < 0.5) {
                                if (mob.getType().is(ModTags.Entities.UNDEAD)) {
                                    Phantom annoyingBoy = new Phantom(EntityType.PHANTOM, this.level());
                                    annoyingBoy.setPos(mob.position().add(0, 10, 0));
                                    activeMobs.add(annoyingBoy);
                                    spawnedMobs.add(annoyingBoy);
                                    annoyingBoy.addTag("GunnerPatroller");
                                    if (level.isDay() && annoyingBoy.getType().is(ModTags.Entities.UNDEAD)) {
                                        annoyingBoy.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0, false, true));
                                        annoyingBoy.extinguishFire();
                                    }
                                    this.level().addFreshEntity(annoyingBoy);
                                    if (!this.activePlayers.isEmpty()) {
                                        annoyingBoy.setTarget(this.activePlayers.stream().findAny().get());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (activeMobs.size() >= MAX_ACTIVE_MOBS || spawnedMobs.size() >= this.totalWaveMobs) {
            this.isSpawningMobs = false;
        }
    }

    public void waveIterator() {
        if (this.currentWave < this.totalWaves) {
            for (Player player : this.activePlayers) {
                this.bossBar.addPlayer((ServerPlayer) player);
            }

            //startWave((ServerLevel) this.level(), faction, this.position(), true, 10, 5);
            this.inWave = true;
            this.isSpawningMobs = true;
            this.currentWave++;
            this.playHorn();
        }
    }

    public void playHorn () {
        int x = this.level().random.nextInt(-25, 25);
        int z = this.level().random.nextInt(-25, 25);
        this.level().playSound(null, BlockPos.containing(this.position().add(x, 32, z)), ModSounds.DARK_HORN.get(), SoundSource.HOSTILE, 1000F, 1);
        for (LivingEntity entity : getActivePlayers()) {
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
        }
    }

    public boolean isWaveComplete() {
        if (this.spawningWave) {
            return false;
        }

        return activeMobs.isEmpty() && spawnedMobs.size() >= this.totalWaveMobs;
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean isInWave() {
        return this.inWave;
    }

    public int getCurrentWave() {
        return this.currentWave;
    }

    public HashSet<LivingEntity> getActiveMobs() {
        return activeMobs;
    }

    public HashSet<Player> getActivePlayers() {
        return activePlayers;
    }

    private void updateBossBar() {
        if (this.isFinished) {
            this.bossBar.setVisible(false);
            this.bossBar.removeAllPlayers();
            return;
        }

        int remainingMobs = this.totalWaveMobs - spawnedMobs.size();

        float progress = (float) remainingMobs / this.totalWaveMobs;
        this.bossBar.setProgress(progress);
        MutableComponent factionLang = Component.translatable("faction.jeg.terror_armada");
        MutableComponent raidLang = Component.translatable("raid.jeg");
        MutableComponent waveLang = Component.translatable("raid.jeg.wave");
        this.bossBar.setName(Component.translatable(factionLang.getString() + " " + raidLang.getString() + " | " + waveLang.getString() + " : " + this.currentWave + "/" + this.totalWaves));
    }

    private void updatePlayers() {
        AABB playerBox = this.getBoundingBox();
        AABB inflatedBox = playerBox.inflate(ACTIVE_RADIUS, ACTIVE_RADIUS, ACTIVE_RADIUS);
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, inflatedBox);

        for (Player player : nearbyPlayers) {
            if (!activePlayers.contains(player)) {
                activePlayers.add(player);
                System.out.println("Player entered: " + player.getName().getString());
            }
        }

        activePlayers.removeIf(player -> !nearbyPlayers.contains(player) || player.isDeadOrDying());
    }

    private void waveReward(Player player, ServerLevel serverLevel) {
        ResourceLocation lootTableID;
        lootTableID = new ResourceLocation(Reference.MOD_ID, "factions/raids/generic_raid_wave_reward");
        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableID);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(LootContextParamSets.CHEST);

        // Roll the loot table 1 time
        for (int i = 0; i < 1; i++) {
            List<ItemStack> loot = lootTable.getRandomItems(lootParams);

            for (ItemStack itemStack : loot) {
                if (!player.getInventory().add(itemStack)) {
                    player.drop(itemStack, false);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (!this.isFinished && this.summonBoss) {
                summonParticleRing();
            }
        }

        if (!this.level().isClientSide) {
            activeMobs.removeIf(mob -> mob.isRemoved() || !mob.isAlive() || mob.isDeadOrDying());
            updateBossBar();
            updatePlayers();

            if (this.getActivePlayers().isEmpty() || this.level().getDifficulty().equals(Difficulty.PEACEFUL)) {
                this.isFinished = true;
                this.defeat = true;
            }

            if (this.currentWave >= this.totalWaves && this.isWaveComplete()) {
                this.isFinished = true;
            }

            if (this.justFinishedWave) {
                for (Player player : this.activePlayers) {
                    Component message = Component.translatable("broadcast.jeg.raid.next_wave");
                    player.sendSystemMessage(message);
                    this.waveReward(player, (ServerLevel) player.level());
                }
                this.justFinishedWave = false;
            }

            if (!this.isFinished) {
                if (this.isSpawningMobs) {
                    this.spawnCooldown--;
                    if (this.spawnCooldown <= 0) {
                        spawnMobs((ServerLevel) this.level(), this.position(), this.forceGuns, 10);
                        this.spawnCooldown = spawnInterval;
                    }
                }

                if (!this.inWave) {
                    if (this.spawningWave) {
                        float progress = 1.0f - ((float) this.cooldown / this.maxCooldown);
                        this.bossBar.setProgress(progress);

                        if (this.currentWave != 0 && this.breakTime == this.maxBreakTime) {
                            this.justFinishedWave = true;
                        }

                        this.breakTime--;
                        if (this.breakTime <= 0) {
                            this.cooldown--;
                            if (this.cooldown <= 0) {
                                waveIterator();
                                this.inWave = true;
                                this.spawningWave = false;
                            }
                        }
                    } else {
                        if (this.currentWave < this.totalWaves) {
                            this.spawningWave = true;
                        }
                        this.cooldown = this.maxCooldown;
                        this.breakTime = this.maxBreakTime;
                    }
                } else {
                    if (activeMobs.size() < MAX_ACTIVE_MOBS && spawnedMobs.size() < this.totalWaveMobs) {
                        this.isSpawningMobs = true;
                    }
                }

                if (getActiveMobs().size() < 10 && spawnedMobs.size() > this.totalWaveMobs - (this.totalWaveMobs / 5) && this.summonBoss) {
                    for (LivingEntity entity : getActiveMobs()) {
                        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 180, 0, false, false));
                    }
                }
            }

            if (!this.summonBoss && this.level() instanceof ServerLevel serverLevel) {
                if (--this.swarmParticleTick <= 0 && this.tickCount <= 400) {
                    for (ServerPlayer players : serverLevel.players()) {
                        spawnPhantomGunnerSwarm(serverLevel, players);
                        spawnSecondLayerPhantomGunnerSwarm(serverLevel, players);
                    }
                    this.swarmParticleTick = MAX_SWARM_PARTICLE_TICK;
                }
            }

            if (!this.summonBoss && this.tickCount >= 800) {
                this.bossBar.setVisible(false);
                this.bossBar.removeAllPlayers();
                this.discard();
            }

            if (this.isWaveComplete()) {
                if (this.currentWave >= this.totalWaves) {
                    this.victory = true;
                }
                this.inWave = false;
                this.cooldown = this.maxCooldown;
                this.breakTime = this.maxBreakTime;
                this.spawnedMobs.clear();
                this.activeMobs.clear();
            }
            if (this.isFinished) {
                if (this.resultPrize > 0 && this.summonBoss) {
                    if (this.victory) {
                        Component message = Component.translatable("broadcast.jeg.terror_armada.victory").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.BLUE);
                        ((ServerLevel) this.level()).getServer().getPlayerList().broadcastSystemMessage(message, false);
                        this.resultPrize--;
                    }
                    if (this.defeat) {
                        for (LivingEntity entity : getActiveMobs()) {
                            entity.removeEffect(MobEffects.GLOWING);
                        }
                        if (this.summonBoss) {
                            Component message2 = Component.translatable("broadcast.jeg.raid.no_players", Component.translatable("faction.jeg.terror_armada")).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
                            ((ServerLevel) this.level()).getServer().getPlayerList().broadcastSystemMessage(message2, false);
                            Component message = Component.translatable("broadcast.jeg.raid.defeat", Component.translatable("faction.jeg.terror_armada")).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
                            ((ServerLevel) this.level()).getServer().getPlayerList().broadcastSystemMessage(message, false);
                        }
                        this.resultPrize--;
                    }
                }

                this.despawnTicks--;
                if (this.despawnTicks == 100 && this.summonBoss) {
                    this.playHorn();
                }
                if (this.despawnTicks < 0) {
                    if (this.summonBoss) {
                        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, this.level());
                        lightningBolt.setPos(this.getPosition(1F).add(0, 64, 32));
                        this.level().addFreshEntity(lightningBolt);

                        ((ServerLevel) this.level()).setWeatherParameters(12000, 0, false, false);

                        TerrorPhantom boss = new TerrorPhantom(ModEntities.TERROR_PHANMTOM.get(), this.level());
                        boss.setPos(this.getPosition(1F).add(0, 32, 32));
                        this.level().addFreshEntity(boss);
                        /*if (!this.activePlayers.isEmpty()) {
                            boss.setTarget(this.activePlayers.stream().findAny().get());
                        }*/
                    }

                    this.bossBar.setVisible(false);
                    this.bossBar.removeAllPlayers();
                    this.discard();
                }
            }
        }

    }

    @Override
    public void onRemovedFromWorld() {
        if (this.summonBoss && !this.victory && !this.defeat && this.level() instanceof ServerLevel serverLevel) {
            Component message2 = Component.translatable("broadcast.jeg.raid.no_players", Component.translatable("faction.jeg.terror_armada")).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message2, false);
            Component message = Component.translatable("broadcast.jeg.raid.defeat", Component.translatable("faction.jeg.terror_armada")).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);
            serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    public static void summonTerrorRaidEntity(ServerLevel level, Vec3 startPos, boolean forceGuns, boolean defeat) {
        TerrorRaidEntity raidEntity = new TerrorRaidEntity(ModEntities.TERROR_RAID_ENTITY.get(), level);
        raidEntity.setPos(startPos);
        level.addFreshEntity(raidEntity);
        if (!defeat) {
            Component message = Component.translatable("broadcast.jeg.raid", Component.translatable("faction.jeg.terror_armada"), BlockPos.containing(startPos)).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
            level.getServer().getPlayerList().broadcastSystemMessage(message, false);
            level.setWeatherParameters(0, 12000, true, true);
        }
        if (defeat) {
            raidEntity.totalWaves = 1;
            raidEntity.summonBoss = false;
            raidEntity.totalWaveMobs = 20;
            raidEntity.maxCooldown = 20;
        }
    }

    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    private void summonParticleRing() {
        for (int i = 0; i < 360; i += 1) {
            double angle = Math.toRadians(i);
            double xOffset = Math.cos(angle) * ACTIVE_RADIUS;
            double zOffset = Math.sin(angle) * ACTIVE_RADIUS;
            this.level().addParticle(ModParticleTypes.ENTITY_LASER.get(),
                    this.getX() + xOffset, this.getY() + 5, this.getZ() + zOffset, 0.0, 0.0, 0.0);
        }
    }
}