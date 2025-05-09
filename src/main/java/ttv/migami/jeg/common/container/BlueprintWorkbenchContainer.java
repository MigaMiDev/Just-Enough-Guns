package ttv.migami.jeg.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.blockentity.BlueprintWorkbenchBlockEntity;
import ttv.migami.jeg.blockentity.inventory.IStorageBlock;
import ttv.migami.jeg.crafting.handler.IModularWorkbenchContainer;
import ttv.migami.jeg.init.ModContainers;
import ttv.migami.jeg.init.ModRecipeTypes;
import ttv.migami.jeg.item.BlueprintItem;

/**
 * Author: MrCrayfish
 */
public class BlueprintWorkbenchContainer extends AbstractWorkbenchContainer implements IModularWorkbenchContainer {
    public BlueprintWorkbenchContainer(int windowId, Container playerInventory, BlueprintWorkbenchBlockEntity workbench) {
        super(windowId, playerInventory, workbench, ModContainers.BLUEPRINT_WORKBENCH.get(), ModRecipeTypes.BLUEPRINT_WORKBENCH.get());
    }

    @Override
    protected void setupContainerSlots(Container playerInventory) {
        this.addSlot(new Slot((IStorageBlock) getWorkbench(), 0, 174, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof BlueprintItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    @Override
    protected boolean isSpecificItem(ItemStack stack) {
        return stack.getItem() instanceof BlueprintItem;
    }
}
