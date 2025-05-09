package ttv.migami.jeg.common.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import ttv.migami.jeg.blockentity.inventory.IStorageBlock;

public abstract class AbstractWorkbenchContainer extends AbstractContainerMenu {
    private final BlockEntity workbench;
    private final BlockPos pos;
    private final RecipeType<?> recipeType;

    public AbstractWorkbenchContainer(int windowId, Container playerInventory, BlockEntity workbench, MenuType<?> containerType, RecipeType<?> recipeType) {
        super(containerType, windowId);
        this.workbench = workbench;
        this.pos = workbench.getBlockPos();
        this.recipeType = recipeType;

        setupContainerSlots(playerInventory);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 102 + y * 18));
            }
        }

        for (int x = 0; x < 9; x++) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 160));
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return ((IStorageBlock) workbench).stillValid(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (isSpecificItem(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.moveItemStackTo(slotStack, 28, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index <= 36 && !this.moveItemStackTo(slotStack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }

        return stack;
    }

    public BlockEntity getWorkbench() {
        return workbench;
    }

    public BlockPos getPos() {
        return pos;
    }

    public RecipeType<?> getRecipeType() {
        return recipeType;
    }

    protected abstract void setupContainerSlots(Container playerInventory);

    protected abstract boolean isSpecificItem(ItemStack stack);
}