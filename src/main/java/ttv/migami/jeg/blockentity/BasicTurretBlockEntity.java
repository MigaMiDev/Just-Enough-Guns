package ttv.migami.jeg.blockentity;

/*public class BasicTurretBlockEntity extends BlockEntity implements MenuProvider {
    private static double TARGETING_RADIUS = 24.0f;
    private int cooldown = 25;
    public final ItemStackHandler itemHandler = new ItemStackHandler(10) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    LivingEntity target;
    private UUID ownerUUID;
    private String ownerName;
    private LivingEntity owner;
    private float yaw;
    private double smoothedTargetX;
    private double smoothedTargetZ;
    private float pitch;
    private static final float MAX_PITCH = 60.0F;
    private static final float MIN_PITCH = -25.0F;
    private double smoothedTargetY;
    private static final float POSITION_SMOOTHING_FACTOR = 0.2F;
    private static final float ROTATION_SPEED = 0.5F;
    public static final float RECOIL_MAX = 4.0F;
    private static final float RECOIL_SPEED = 0.3F;
    public float recoilPitchOffset = 0.0F;
    private static final double MINIMUM_FIRING_DISTANCE = 1.3;
    private static final int DAMAGE_INCREASE = 2;
    private static final double RANGE_INCREASE = 8.0;

    private float previousYaw;
    private float previousPitch;
    public boolean disabled = false;
    public int disableCooldown = 0;
    public static final int MAX_DISABLE_TIME = 200;

    public BasicTurretBlockEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.BASIC_TURRET.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.basic_turret");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new BasicTurretContainer(id, playerInventory, this);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof BasicTurretBlockEntity turret) {
            if (turret.getLevel() instanceof ServerLevel serverLevel) {
                Player player = serverLevel.getPlayerByUUID(turret.getOwnerUUID());
                if (player != null) {
                    turret.setActualOwner(player);
                }
            }
            if (turret.cooldown > 0) {
                turret.cooldown -= 1;
            }
            turret.tickRecoil();

            if (turret.disabled) {
                turret.disableCooldown--;
                if (turret.disableCooldown <= 0) {
                    turret.disabled = false;
                    turret.disableCooldown = 0;
                }
                turret.resetToRestPosition();
            } else {
                turret.updateTargetRange(8);
                if (!turret.isTargetValid()) {
                    turret.target = null;
                }
                turret.findTarget(level, pos);
                turret.updateYaw();
                turret.updatePitch();
                turret.setChanged();

                if (turret.target != null && turret.cooldown <= 0 && turret.isReadyToFire()) {
                    turret.fire();
                    turret.cooldown = 3;
                }
            }
        }
    }


    private void updateTargetRange(double rangeModifier) {
        TARGETING_RADIUS = 24.0f + (float)rangeModifier;
    }

    public void onHitByLightningProjectile() {
        this.disabled = true;
        this.disableCooldown = MAX_DISABLE_TIME;
        this.resetToRestPosition();
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            spawnDisableParticles();
        }
    }

    private void spawnDisableParticles() {
        if (this.level instanceof ServerLevel serverLevel) {
            double x = this.worldPosition.getX() + 0.5;
            double y = this.worldPosition.getY() + 1.0;
            double z = this.worldPosition.getZ() + 0.5;

            int particleCount = 20;
            double spread = 0.5;

            for (int i = 0; i < particleCount; i++) {
                double offsetX = this.level.random.nextDouble() * spread - spread / 2;
                double offsetY = this.level.random.nextDouble() * spread;
                double offsetZ = this.level.random.nextDouble() * spread - spread / 2;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0, 0, 0.05);
            }
            serverLevel.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public boolean isReadyToFire() {
        if (this.target == null) return false;
        double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
        double dy = smoothedTargetY - (this.worldPosition.getY() + 1.0);
        double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI)) + 180;
        targetYaw = (targetYaw + 360) % 360;
        float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
        targetPitch = Mth.clamp(targetPitch, MIN_PITCH, MAX_PITCH);
        float yawDifference = Math.abs(targetYaw - this.yaw);
        if (yawDifference > 180) yawDifference = 360 - yawDifference;

        float pitchDifference = Math.abs(targetPitch - this.pitch);
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared < MINIMUM_FIRING_DISTANCE * MINIMUM_FIRING_DISTANCE) {
            return false;
        }

        return yawDifference < 2.0F && pitchDifference < 2.0F;
    }

    public void tickRecoil() {
        if (this.recoilPitchOffset > 0) {
            this.recoilPitchOffset -= RECOIL_SPEED;
            if (this.recoilPitchOffset < 0) {
                this.recoilPitchOffset = 0;
            }
        }
    }

    public float getRecoilPitchOffset() {
        return recoilPitchOffset;
    }

    private void resetToRestPosition() {
        this.target = null;
        float restingYaw = 0.0F;
        float restingPitch = -30.0F;
        this.previousYaw = this.yaw;
        this.previousPitch = this.pitch;
        float yawDifference = restingYaw - this.yaw;
        if (yawDifference > 180) yawDifference -= 360;
        else if (yawDifference < -180) yawDifference += 360;
        this.yaw += yawDifference * ROTATION_SPEED;
        this.yaw = this.yaw % 360.0F;
        if (this.yaw < 0) this.yaw += 360.0F;

        float pitchDifference = restingPitch - this.pitch;
        this.pitch += pitchDifference * ROTATION_SPEED;
        this.smoothedTargetX = 0;
        this.smoothedTargetY = 0;
        this.smoothedTargetZ = 0;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void fire() {
        if (this.level == null || this.target == null) {
            return;
        }

        this.level.playSound(null, this.worldPosition, ModSounds.COMBAT_RIFLE_SILENCED_FIRE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        ItemStack assaultRifle = new ItemStack(ModItems.COMBAT_RIFLE.get());
        if (assaultRifle.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(assaultRifle);

            AIGunEvent.performTurretAttack(this, assaultRifle, gun);
        }
        ejectPosedCasing(this.getLevel(), this.getBlockPos().getCenter().add(0, 1, 0));

        //this.recoilPitchOffset = RECOIL_MAX;
    }

    private boolean hasLineOfSight(Level level, Vec3 turretPos, LivingEntity target) {
        Vec3 targetPos = target.getEyePosition();
        Vec3 toTarget = targetPos.subtract(turretPos);
        double distance = toTarget.length();
        Vec3 rayVector = toTarget.normalize().scale(distance);

        // Adjust the start position to be slightly above the turret base
        Vec3 adjustedTurretPos = turretPos.add(0, 0.5, 0);

        ClipContext clipContext = new ClipContext(adjustedTurretPos, adjustedTurretPos.add(rayVector), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        BlockHitResult hitResult = level.clip(clipContext);

        return hitResult.getType() == HitResult.Type.MISS;
    }

    private boolean isTargetValid() {
        if (this.target == null || !this.target.isAlive() || this.target.isRemoved() || this.target.isDeadOrDying()) {
            return false;
        }

        ChunkPos targetChunkPos = new ChunkPos(this.target.blockPosition());
        if (!this.level.hasChunk(targetChunkPos.x, targetChunkPos.z)) {
            return false;
        }
        double distanceSquared = this.target.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        );
        return distanceSquared <= (TARGETING_RADIUS * TARGETING_RADIUS);
    }

    private void findTarget(Level level, BlockPos pos) {
        this.target = null;

        Vec3 turretPos = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5);

        double verticalSearchRange = TARGETING_RADIUS;
        AABB searchBox = new AABB(pos).inflate(TARGETING_RADIUS, verticalSearchRange, TARGETING_RADIUS);

        List<LivingEntity> potentialTargets = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity instanceof Enemy
        );

        if (!potentialTargets.isEmpty()) {
            this.target = potentialTargets.stream()
                    .filter(entity -> hasLineOfSight(level, turretPos, entity))
                    .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(turretPos)))
                    .orElse(null);

            if (this.target != null) {
                double predictedX = this.target.getX() + this.target.getDeltaMovement().x * 7;
                double predictedY = this.target.getY() + (this.target.getBbHeight() / 2); // Target center of entity
                double predictedZ = this.target.getZ() + this.target.getDeltaMovement().z * 7;

                smoothedTargetX = lerp(smoothedTargetX, predictedX, POSITION_SMOOTHING_FACTOR);
                smoothedTargetY = lerp(smoothedTargetY, predictedY, POSITION_SMOOTHING_FACTOR);
                smoothedTargetZ = lerp(smoothedTargetZ, predictedZ, POSITION_SMOOTHING_FACTOR);
            }
        }
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private void updateYaw() {
        this.previousYaw = this.yaw;

        if (smoothedTargetX != 0 || smoothedTargetZ != 0) {
            double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
            double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
            float targetYaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI)) + 180;
            targetYaw = (targetYaw + 360) % 360;
            this.yaw = (this.yaw + 360) % 360;

            float yawDifference = targetYaw - this.yaw;
            if (yawDifference > 180) {
                yawDifference -= 360;
            } else if (yawDifference < -180) {
                yawDifference += 360;
            }

            this.yaw += yawDifference * ROTATION_SPEED;
            this.yaw = this.yaw % 360.0F;
            if (this.yaw < 0) this.yaw += 360.0F;
        }
    }

    private void updatePitch() {
        this.previousPitch = this.pitch;

        if (smoothedTargetY != 0) {
            double dx = smoothedTargetX - (this.worldPosition.getX() + 0.5);
            double dy = smoothedTargetY - (this.worldPosition.getY() + 1.0);
            double dz = smoothedTargetZ - (this.worldPosition.getZ() + 0.5);
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

            float targetPitch = (float) (Math.atan2(dy, horizontalDistance) * (180 / Math.PI));
            targetPitch = Mth.clamp(targetPitch, MIN_PITCH, MAX_PITCH);

            float pitchDifference = targetPitch - this.pitch;

            this.pitch += pitchDifference * ROTATION_SPEED;
        }
    }

    public float getPreviousYaw() {
        return this.previousYaw;
    }

    public float getPreviousPitch() {
        return this.previousPitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putFloat("Yaw", this.yaw);
        tag.putFloat("Pitch", this.pitch);
        tag.putBoolean("Disabled", this.disabled);
        tag.putInt("DisableCooldown", this.disableCooldown);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
            tag.putString("OwnerName", ownerName);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.yaw = tag.getFloat("Yaw");
        this.previousYaw = this.yaw;
        this.pitch = tag.getFloat("Pitch");
        this.previousPitch = this.pitch;
        this.disabled = tag.getBoolean("Disabled");
        this.disableCooldown = tag.getInt("DisableCooldown");
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
            this.ownerName = tag.getString("OwnerName");
        }
    }

    private boolean isOwner(LivingEntity entity) {
        return entity.getUUID().equals(this.ownerUUID);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    public SimpleContainer getContainer() {
        SimpleContainer container = new SimpleContainer(10);
        for (int i = 0; i < 10; i++) {
            container.setItem(i, itemHandler.getStackInSlot(i));
        }
        return container;
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != Direction.UP) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public ItemStackHandler getItemStackHandler() {
        return this.itemHandler;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void setOwner(ServerPlayer player) {
        this.ownerUUID = player.getUUID();
        this.ownerName = player.getName().getString();
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public UUID getOwnerUUID() { return this.ownerUUID; }

    public LivingEntity getOwner() { return this.owner; }

    public void setActualOwner(Player player) {
        this.owner = player;
    }
}*/