package ttv.migami.jeg.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.init.ModTags;
import ttv.migami.jeg.item.AmmoItem;

public class AmmoBoxSlot extends Slot {
    public AmmoBoxSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    public boolean mayPlace(ItemStack pStack) {
        return pStack.getItem() instanceof AmmoItem || pStack.is(ModTags.Items.AMMO);
    }
}