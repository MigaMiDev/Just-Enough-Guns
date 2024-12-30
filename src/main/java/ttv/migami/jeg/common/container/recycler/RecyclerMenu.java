package ttv.migami.jeg.common.container.recycler;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import ttv.migami.jeg.init.ModContainers;
import ttv.migami.jeg.init.ModRecipeTypes;

public class RecyclerMenu extends AbstractRecyclerMenu {
    public RecyclerMenu(int i, Inventory inventory) {
        super(ModContainers.RECYCLER.get(), ModRecipeTypes.RECYCLING.get(), i, inventory);
    }

    public RecyclerMenu(int i, Inventory inventory, Container container, ContainerData containerData) {
        super(ModContainers.RECYCLER.get(), ModRecipeTypes.RECYCLING.get(), i, inventory, container, containerData);
    }
}