package ttv.migami.jeg.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.blockentity.GunmetalWorkbenchBlockEntity;
import ttv.migami.jeg.crafting.handler.IModularWorkbenchContainer;
import ttv.migami.jeg.init.ModContainers;
import ttv.migami.jeg.init.ModRecipeTypes;

/**
 * Author: MrCrayfish
 */
public class GunmetalWorkbenchContainer extends AbstractWorkbenchContainer implements IModularWorkbenchContainer {
    public GunmetalWorkbenchContainer(int windowId, Container playerInventory, GunmetalWorkbenchBlockEntity workbench) {
        super(windowId, playerInventory, workbench, ModContainers.GUNMETAL_WORKBENCH.get(), ModRecipeTypes.GUNMETAL_WORKBENCH.get());
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
