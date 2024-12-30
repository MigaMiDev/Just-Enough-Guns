package ttv.migami.jeg.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.blockentity.ScrapWorkbenchBlockEntity;
import ttv.migami.jeg.init.ModContainers;

/**
 * Author: MrCrayfish
 */
public class ScrapWorkbenchContainer extends AbstractWorkbenchContainer {
    public ScrapWorkbenchContainer(int windowId, Container playerInventory, ScrapWorkbenchBlockEntity workbench) {
        super(windowId, playerInventory, workbench, ModContainers.SCRAP_WORKBENCH.get());
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
