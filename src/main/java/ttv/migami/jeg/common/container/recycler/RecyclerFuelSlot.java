package ttv.migami.jeg.common.container.recycler;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RecyclerFuelSlot extends Slot {
    private final AbstractRecyclerMenu menu;

    public RecyclerFuelSlot(AbstractRecyclerMenu abstractRecyclerMenu, Container container, int i, int i1, int i2) {
        super(container, i, i1, i2);
        this.menu = abstractRecyclerMenu;
    }

    public boolean mayPlace(ItemStack fuel) {
        return this.menu.isFuel(fuel) || isBucket(fuel);
    }

    public int getMaxStackSize(ItemStack pStack) {
        return isBucket(pStack) ? 1 : super.getMaxStackSize(pStack);
    }

    public static boolean isBucket(ItemStack pStack) {
        return pStack.is(Items.BUCKET);
    }
}