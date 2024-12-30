package ttv.migami.jeg.common.container;

/*public class BasicTurretContainer extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos pos;

    public BasicTurretContainer(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BasicTurretContainer(int id, Inventory playerInventory, BlockEntity entity) {
        super(ModContainers.BASIC_TURRET_CONTAINER.get(), id);

        if (!(entity instanceof BasicTurretBlockEntity turretBlockEntity)) {
            throw new IllegalStateException("Unexpected BlockEntity type: " + entity);
        }

        this.level = playerInventory.player.level();
        this.pos = entity.getBlockPos();
        ItemStackHandler handler = turretBlockEntity.getItemStackHandler();

        // Add slots for the turret's internal inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotItemHandler(handler, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        // Add player inventory slots
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, pos), player, ModBlocks.BASIC_TURRET.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 10) {
                if (!this.moveItemStackTo(itemstack1, 10, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 10, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public BasicTurretBlockEntity getBlockEntity() {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        return blockEntity instanceof BasicTurretBlockEntity turretBlockEntity ? turretBlockEntity : null;
    }
}*/