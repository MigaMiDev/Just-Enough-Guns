package ttv.migami.jeg.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.init.ModContainers;
import ttv.migami.jeg.world.inventory.AmmoBoxSlot;

public class AmmoBoxMenu extends AbstractContainerMenu {
    private static final int CONTAINER_SIZE = 15;
    private final Container container;

    public AmmoBoxMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, new SimpleContainer(15));
    }

    public AmmoBoxMenu(int pContainerId, Inventory pPlayerInventory, Container pContainer) {
        super(ModContainers.AMMO_BOX.get(), pContainerId);
        checkContainerSize(pContainer, 15);
        this.container = pContainer;
        pContainer.startOpen(pPlayerInventory.player);

        int rows;
        int columns;
        for(rows = 0; rows < 3; ++rows) {
            for(columns = 0; columns < 5; ++columns) {
                this.addSlot(new AmmoBoxSlot(pContainer, columns + rows * 5, 43 + columns * 18, 18 + rows * 18));
            }
        }

        for(rows = 0; rows < 3; ++rows) {
            for(columns = 0; columns < 9; ++columns) {
                this.addSlot(new Slot(pPlayerInventory, columns + rows * 9 + 9, 8 + columns * 18, 91 + rows * 18));
            }
        }

        for(rows = 0; rows < 9; ++rows) {
            this.addSlot(new Slot(pPlayerInventory, rows, 8 + rows * 18, 149));
        }

    }

    public boolean stillValid(Player pPlayer) {
        return this.container.stillValid(pPlayer);
    }

    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack $$2 = ItemStack.EMPTY;
        Slot $$3 = (Slot)this.slots.get(pIndex);
        if ($$3 != null && $$3.hasItem()) {
            ItemStack $$4 = $$3.getItem();
            $$2 = $$4.copy();
            if (pIndex < this.container.getContainerSize()) {
                if (!this.moveItemStackTo($$4, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo($$4, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if ($$4.isEmpty()) {
                $$3.setByPlayer(ItemStack.EMPTY);
            } else {
                $$3.setChanged();
            }
        }

        return $$2;
    }

    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.container.stopOpen(pPlayer);
    }
}