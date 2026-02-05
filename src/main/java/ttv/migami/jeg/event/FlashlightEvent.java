package ttv.migami.jeg.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.GripType;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.FlashlightItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.S2CMessageLaser;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlashlightEvent {

    @SubscribeEvent
    public static void laserPointer(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        boolean isAiming = ModSyncedDataKeys.AIMING.getValue(player);

        if (player.isDeadOrDying()) {
            return;
        }

        if (player.isSprinting() && !player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
            return;
        }

        if (!player.level().isClientSide && !player.isSpectator() && player.getMainHandItem().getItem() instanceof GunItem) {
            if (Gun.hasAttachmentEquipped(player.getMainHandItem(), IAttachment.Type.SPECIAL)) {
                if (Gun.getAttachment(IAttachment.Type.SPECIAL, player.getMainHandItem()).getItem() == ModItems.LASER_POINTER.get()) {
                    Vec3 userPos = player.getEyePosition();
                    Vec3 targetPos = userPos.add(player.getLookAngle().scale(100));

                    HitResult blockResult = player.level().clip(new ClipContext(userPos, targetPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                    EntityHitResult entityResult = rayTraceEntities(player, userPos, targetPos, 100);

                    Vec3 laserEndPos = null;
                    if (entityResult != null && (blockResult == null || userPos.distanceTo(entityResult.getLocation()) < userPos.distanceTo(blockResult.getLocation()))) {
                        laserEndPos = entityResult.getLocation();
                        Entity hitEntity = entityResult.getEntity();
                        if (hitEntity instanceof LivingEntity livingEntity && Config.COMMON.gameplay.glowingLaserPointers.get() && isAiming) {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5, 0, false, false, true));
                        }
                    } else if (blockResult instanceof BlockHitResult blockHitResult) {
                        laserEndPos = blockHitResult.getLocation();
                        Direction face = blockHitResult.getDirection();

                        PacketHandler.getPlayChannel().sendToTrackingChunk(() -> player.level().getChunkAt(blockHitResult.getBlockPos()), new S2CMessageLaser(blockHitResult.getLocation().x, blockHitResult.getLocation().y, blockHitResult.getLocation().z, blockHitResult.getBlockPos(), face));
                    }

                    List<Class<? extends PathfinderMob>> mobTypes = List.of(Cat.class, Ocelot.class);
                    for (Class<? extends PathfinderMob> mobType : mobTypes) {
                        List<? extends PathfinderMob> mobs = player.level().getEntitiesOfClass(mobType, new AABB(laserEndPos.subtract(5, 5, 5), laserEndPos.add(5, 5, 5)));

                        for (PathfinderMob mob : mobs) {
                            if (mob instanceof Cat cat && !cat.isInSittingPose()) {
                                if (mob.getRandom().nextFloat() < 0.02) {
                                    mob.getJumpControl().jump();
                                }
                                mob.getNavigation().moveTo(laserEndPos.x, laserEndPos.y, laserEndPos.z, 1.2);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void flashlight(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        LevelAccessor world = player.level();

        if (world.isClientSide()) {
            return;
        }

        if (player.isDeadOrDying()) {
            return;
        }

        if (player.isSprinting() && !player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
            return;
        }

        boolean attachment = false;
        if (Gun.hasAttachmentEquipped(player.getMainHandItem(), IAttachment.Type.SPECIAL)) {
            if (Gun.getAttachment(IAttachment.Type.SPECIAL, player.getMainHandItem()).getItem() == ModItems.FLASHLIGHT.get()) {
                if (!Config.COMMON.gameplay.allowFlashlights.get()) {
                    Component message = Component.translatable("chat.jeg.disabled_flashlights")
                            .withStyle(ChatFormatting.GRAY);
                    player.displayClientMessage(message, true);
                    return;
                }

                ItemStack flashlight = Gun.getAttachment(IAttachment.Type.SPECIAL, player.getMainHandItem());
                if (flashlight.getTag() != null && flashlight.getTag().getBoolean("Powered")) {
                    attachment = true;
                }
            }
            else {
                attachment = false;
            }
        } else {
            attachment = false;
        }
        if (player.getMainHandItem().isEmpty()) {
            attachment = false;
        }

        boolean item = false;
        boolean rightHand = false;
        boolean leftHand = false;
        if (player.getMainHandItem().getItem() == ModItems.FLASHLIGHT.get()) {
            if (player.getMainHandItem().getTag() != null && player.getMainHandItem().getTag().getBoolean("Powered")) {
                rightHand = true;
            }
            else {
                rightHand = false;
            }
        } else {
            rightHand = false;
        }
        if (player.getOffhandItem().getItem() == ModItems.FLASHLIGHT.get()) {
            if (player.getMainHandItem().getItem() instanceof GunItem gunItem) {
                if (!gunItem.getGun().getGeneral().getGripType().equals(GripType.ONE_HANDED) && !attachment) {
                    return;
                }
            }

            if (player.getOffhandItem().getTag() != null && player.getOffhandItem().getTag().getBoolean("Powered")) {
                leftHand = true;
            }
            else {
                leftHand = false;
            }
        } else {
            leftHand = false;
        }
        if (rightHand || leftHand) {
            item = true;
        }

        if ((attachment || item) && !player.isSpectator()) {
            double distance = 2.0;
            for (int index = 0; index < Config.COMMON.gameplay.flashlightDistance.get(); index++) {
                BlockPos targetPos = player.level().clip(new ClipContext(
                        player.getEyePosition(1.0F),
                        player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(distance)),
                        ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)).getBlockPos();

                BlockState targetState = world.getBlockState(targetPos);
                if (targetState.is(ModBlocks.DYNAMIC_LIGHT.get())) {
                    BlockEntity be = world.getBlockEntity(targetPos);
                    if (be != null) {
                        be.getPersistentData().putDouble("Delay", 2.0);
                    }
                } else if (targetState.getBlock() == Blocks.AIR || targetState.getBlock() == Blocks.CAVE_AIR) {
                    BlockState dynamicLightState = ModBlocks.DYNAMIC_LIGHT.get().defaultBlockState();
                    world.setBlock(targetPos, dynamicLightState, 3);
                } else if (targetState.getBlock() == Blocks.WATER) {
                    //BlockState dynamicLightState = ModBlocks.DYNAMIC_LIGHT.get().defaultBlockState();
                    //dynamicLightState.setValue(BlockStateProperties.WATERLOGGED, true);
                    BlockState dynamicLightState = ModBlocks.DYNAMIC_LIGHT.get()
                            .defaultBlockState()
                            .setValue(BlockStateProperties.WATERLOGGED, true);
                    world.setBlock(targetPos, dynamicLightState, 3);
                }

                distance += 1.0;
            }
        }
    }

    public static void chargeFlashlight(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof FlashlightItem)) {
            return;
        }

        CompoundTag tag = heldItem.getOrCreateTag();
        int batteryLife = tag.getInt(FlashlightItem.TAG_BATTERY_LIFE);

        batteryLife = Math.min(batteryLife + 40, FlashlightItem.MAX_BATTERY_LIFE);
        tag.putInt(FlashlightItem.TAG_BATTERY_LIFE, batteryLife);

        player.level().gameEvent(player, GameEvent.NOTE_BLOCK_PLAY, player.getPosition(1F));
        float randomPitch = 0.9F + (player.getRandom().nextFloat() * (1.75F - 1.5F));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.FLASHLIGHT_CHARGE.get(), SoundSource.PLAYERS, 1.0F, randomPitch);
    }

    private static void updateDelayAndNotify(LevelAccessor world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.getPersistentData().putDouble("Delay", 5.0);
        }
        if (world instanceof Level) {
            ((Level) world).sendBlockUpdated(pos, state, state, 3);
        }
    }

    public static double getValue(LevelAccessor world, BlockPos pos, String tag) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.getPersistentData().getDouble(tag) : -1.0;
    }

    /**
     * A custom implementation that allows you to pass a predicate to ignore certain blocks when checking for collisions.
     *
     * @author: Mr. Crayfish
     *
     * @param world     the world to perform the ray trace
     * @param context   the ray trace context
     * @param ignorePredicate the block state predicate
     * @return a result of the raytrace
     */
    private static BlockHitResult rayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate) {

        return performRayTrace(context, (rayTraceContext, blockPos) -> {
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

    /**
     * Ray trace for entities within the given range.
     */
    private static EntityHitResult rayTraceEntities(Player player, Vec3 start, Vec3 end, double range) {
        Level level = player.level();
        Vec3 direction = end.subtract(start).normalize();
        AABB box = new AABB(start, start).inflate(range);
        EntityHitResult closestEntityHitResult = null;
        double closestDistance = range;

        for (Entity entity : level.getEntities(player, box, e -> e.isPickable())) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hitPos = entityBox.clip(start, end);

            if (hitPos.isPresent()) {
                double distance = start.distanceTo(hitPos.get());

                if (distance < closestDistance) {
                    closestEntityHitResult = new EntityHitResult(entity, hitPos.get());
                    closestDistance = distance;
                }
            }
        }

        return closestEntityHitResult;
    }

    /*
     * Also by Mr. Crayfish
     */
    private static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> p_217300_2_) {
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
}