package ttv.migami.jeg.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.entity.throwable.ThrowableGrenadeEntity;
import ttv.migami.jeg.faction.Faction;
import ttv.migami.jeg.faction.GunnerManager;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.ScoreStreakItem;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityKillEventHandler {

    private static final Random RANDOM = new Random();
    private static final double GRENADE_SPAWN_CHANCE = 0.05; // 5% chance
    private static final double ECHO_SHARD_SPAWN_CHANCE = 0.3; // 30% chance

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        LivingEntity entity = event.getEntity();
        LivingEntity killer = entity.getKillCredit();

        if (killer == null) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) event.getEntity().level();

        // Score Streak
        if (event.getSource().getEntity() instanceof Player player && (player.getMainHandItem().getItem() instanceof GunItem || event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION))) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof ScoreStreakItem scorestreakItem) {
                    int multiplier = 5 * (entity.getMainHandItem().getEnchantmentLevel(ModEnchantments.RECLAIMED.get()) + 1);
                    scorestreakItem.setPoints(stack, (int) (scorestreakItem.getPoints(stack) + (event.getEntity().getMaxHealth() * multiplier)));
                }
            }
        }

        // Drop Raid Flares
        if (entity.getTags().contains("MobGunner")) {
            if (entity.level().random.nextFloat() < 0.025F) {
                GunnerManager manager = new GunnerManager(GunnerManager.getConfigFactions());
                Faction faction = manager.getFactionForMob(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));
                if (faction != null) {
                    Item flare = ModItems.FLARE.get();
                    ItemStack flareStack = new ItemStack(flare, 1);

                    flareStack.getOrCreateTag().putBoolean("HasRaid", true);
                    flareStack.getOrCreateTag().putString("Raid", faction.getName());

                    ItemEntity flareItemStack = new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), flareStack);
                    serverLevel.addFreshEntity(flareItemStack);
                }
            }
        }

        // Drop Ammo from Enemies
        if (entity.getMainHandItem().getItem() instanceof GunItem gunItem && Config.COMMON.gunnerMobs.dropAmmo.get() &&
                entity.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            ItemStack entityStack = entity.getMainHandItem();
            Gun gun = gunItem.getModifiedGun(entityStack);

            ResourceLocation ammoItem = new ResourceLocation(gun.getProjectile().getItem().toString());
            Item item = ForgeRegistries.ITEMS.getValue(ammoItem);

            if (item != null) {
                int ammoCount = entity.getRandom().nextInt((gun.getReloads().getMaxAmmo() / 2) + 1);

                ItemStack echoShardStack = new ItemStack(item, ammoCount);
                ItemEntity echoShardEntity = new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), echoShardStack);
                serverLevel.addFreshEntity(echoShardEntity);
            }
        }

        // Drop Echo Shards
        if (entity.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) &&
                RANDOM.nextDouble() < (ECHO_SHARD_SPAWN_CHANCE * entity.getMainHandItem().getEnchantmentLevel(ModEnchantments.RECLAIMED.get()) + 1) && Config.COMMON.world.entitiesDropEchoShards.get()) {
            BlockPos pos = entity.blockPosition();

            if (killer.getMainHandItem().getItem() instanceof GunItem gunItem) {
                ItemStack stack = killer.getMainHandItem();
                Gun gun = gunItem.getModifiedGun(stack);

                ResourceLocation echoShard = new ResourceLocation(Items.ECHO_SHARD.toString());
                ResourceLocation sculkCatalyst = new ResourceLocation(Items.SCULK_CATALYST.toString());
                ResourceLocation reloadItem = new ResourceLocation(gun.getProjectile().getItem().toString());
                if (echoShard.equals(reloadItem) || sculkCatalyst.equals(reloadItem)) {
                    int shardCount = RANDOM.nextDouble() < 0.3 ? 2 : 1;

                    ItemStack echoShardStack = new ItemStack(Items.ECHO_SHARD, shardCount);
                    ItemEntity echoShardEntity = new ItemEntity(serverLevel, pos.getX(), pos.getY(), pos.getZ(), echoShardStack);
                    serverLevel.addFreshEntity(echoShardEntity);

                    killer.level().playLocalSound(killer.getX(), killer.getY(), killer.getZ(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 1.0F, 1.0F, false);

                    serverLevel.playSound(entity, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 10F, 1F);
                }
            }
        }

        // Creepers drop Grenades
        if ((event.getEntity() instanceof Creeper)) {
            if (RANDOM.nextDouble() < GRENADE_SPAWN_CHANCE && Config.COMMON.world.creepersDropLiveGrenades.get()) {
                LivingEntity creeper = event.getEntity();
                BlockPos pos = creeper.blockPosition();
                ThrowableGrenadeEntity grenade = new ThrowableGrenadeEntity(creeper.level(), creeper, 60);
                if (killer instanceof Player) {
                    killer.level().playLocalSound(killer.getX(), killer.getY(), killer.getZ(), ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
                }
                serverLevel.playSound(creeper, creeper.blockPosition(), ModSounds.ITEM_GRENADE_PIN.get(), SoundSource.HOSTILE, 10F, 1F);
                grenade.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
                serverLevel.addFreshEntity(grenade);
            }
        }

        // Get the Finger Gun
        if (event.getEntity() instanceof EnderDragon dragon) {
            LivingEntity livingEntity = dragon.getKillCredit();

            if (livingEntity instanceof Player player) {
                if (player.getMainHandItem().getItem() == Items.AIR) {
                    ItemStack itemStack = new ItemStack(ModItems.FINGER_GUN.get());
                    if (!player.getInventory().add(itemStack)) {
                        player.drop(itemStack, false);
                    }
                }
            }
        }

        // Get Infinity
        if (Config.COMMON.world.bossEnchants.get()) {
            if (event.getEntity() instanceof EnderDragon dragon) {
                BlockPos dragonPos = dragon.blockPosition();

                boolean enchantedAtLeastOneGun = false;

                for (Player player : serverLevel.players()) {
                    double distance = player.blockPosition().distSqr(dragonPos);
                    if (distance <= 200 * 200) {

                        ItemStack mainHandItem = player.getMainHandItem();
                        if (mainHandItem.getItem() instanceof GunItem gunItem) {
                            if (!mainHandItem.is(ModItems.FINGER_GUN.get())) {
                                Gun modifiedGun = gunItem.getModifiedGun(mainHandItem);

                                if (!modifiedGun.getGeneral().isInfinityDisabled()) {
                                    if (mainHandItem.getEnchantmentLevel(ModEnchantments.INFINITY.get()) == 0) {
                                        mainHandItem.enchant(ModEnchantments.INFINITY.get(), 1);
                                        enchantedAtLeastOneGun = true;
                                    }
                                } else {
                                    Component message = Component.translatable("chat.jeg.infinity_disabled")
                                            .withStyle(ChatFormatting.GRAY);
                                    player.displayClientMessage(message, true);
                                }
                            }
                        }
                    }
                }

                if (enchantedAtLeastOneGun) {
                    Component message = Component.translatable("broadcast.jeg.infinity")
                            .withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD);
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
                }
            }

            // Get Withered
            if (event.getEntity() instanceof WitherBoss witherBoss) {
                // Exit if not in The Nether and bossRequirements is set to true
                if (serverLevel.dimension() != Level.NETHER && Config.COMMON.world.bossRequirements.get()) {
                    return;
                }

                BlockPos witherBossPos = witherBoss.blockPosition();

                StructureManager structureManager = serverLevel.structureManager();
                boolean isNearNetherFortress = false;

                if (Config.COMMON.world.bossRequirements.get()) {
                    int radius = 64;
                    for (int x = -radius; x <= radius; x += 16) {
                        for (int z = -radius; z <= radius; z += 16) {
                            BlockPos checkPos = witherBossPos.offset(x, 0, z);
                            boolean isVanillaFortress = structureManager.getStructureAt(
                                    checkPos,
                                    Objects.requireNonNull(serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE)
                                            .get(BuiltinStructures.FORTRESS))
                            ).isValid();

                            boolean isModdedFortress = false;
                            if(JustEnoughGuns.yungsNetherFortLoaded) {
                                isModdedFortress = structureManager.getStructureAt(
                                        checkPos,
                                        Objects.requireNonNull(serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE)
                                                .get(new ResourceLocation("betterfortresses", "fortress")))
                                ).isValid();
                            }

                            if (isVanillaFortress || isModdedFortress) {
                                isNearNetherFortress = true;
                                break;
                            }
                        }
                        if (isNearNetherFortress) {
                            break;
                        }
                    }
                }

                boolean isInSoulSandValley = serverLevel.getBiome(witherBossPos).is(Biomes.SOUL_SAND_VALLEY);

                if (!Config.COMMON.world.bossRequirements.get()) {
                    isNearNetherFortress = true;
                    isInSoulSandValley = true;
                }

                if (!isNearNetherFortress &&  !isInSoulSandValley) {
                    return;
                }

                boolean enchantedAtLeastOneGun = false;

                for (Player player : serverLevel.players()) {
                    double distance = player.blockPosition().distSqr(witherBossPos);
                    if (distance <= 200 * 200) {

                        ItemStack mainHandItem = player.getMainHandItem();
                        if (mainHandItem.getItem() instanceof GunItem gunItem) {
                            if (!mainHandItem.is(ModItems.FINGER_GUN.get())) {
                                Gun modifiedGun = gunItem.getModifiedGun(mainHandItem);

                                if (!modifiedGun.getGeneral().isWitheredDisabled()) {
                                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(mainHandItem);

                                    int currentWitheredLevel = enchantments.getOrDefault(ModEnchantments.WITHERED.get(), 0);

                                    if (currentWitheredLevel < Config.COMMON.world.maxWitheredLevel.get()) {
                                        enchantments.put(ModEnchantments.WITHERED.get(), currentWitheredLevel + 1);

                                        EnchantmentHelper.setEnchantments(enchantments, mainHandItem);
                                        enchantedAtLeastOneGun = true;
                                    }
                                }
                            }
                        }
                    }
                }

                if (enchantedAtLeastOneGun) {
                    Component message = Component.translatable("broadcast.jeg.withered")
                            .withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.BOLD);
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(message, false);
                }
            }

            /*if (event.getEntity() instanceof Warden wardenBoss) {
                ServerLevel serverLevel = (ServerLevel) event.getEntity().level();
                BlockPos wardenBossPos = wardenBoss.blockPosition();

                if (killer instanceof Player) {
                    for (Player player : serverLevel.players()) {
                        double distance = player.blockPosition().distSqr(wardenBossPos);
                        if (distance <= 200 * 200) {

                            ItemStack itemStack = new ItemStack(ModItems.WARDEN_TREASURE_BAG.get());
                            if (!player.getInventory().add(itemStack)) {
                                player.drop(itemStack, false);
                            }
                        }
                    }
                }
            }*/
        }
    }
}