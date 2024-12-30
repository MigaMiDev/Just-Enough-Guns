package ttv.migami.jeg.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.blockentity.GunniteWorkbenchBlockEntity;
import ttv.migami.jeg.init.ModContainers;

/**
 * Author: MrCrayfish
 */
public class GunniteWorkbenchContainer extends AbstractWorkbenchContainer {
    public GunniteWorkbenchContainer(int windowId, Container playerInventory, GunniteWorkbenchBlockEntity workbench) {
        super(windowId, playerInventory, workbench, ModContainers.GUNNITE_WORKBENCH.get());
    }

    @Override
    protected void setupContainerSlots(Container playerInventory) {
        /*super(playerInventory);
        this.addSlot(new Slot((IStorageBlock) getWorkbench(), 0, 174, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DyeItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });*/
    }

    @Override
    protected boolean isSpecificItem(ItemStack stack) {
        return stack.getItem() instanceof DyeItem;
    }
}
