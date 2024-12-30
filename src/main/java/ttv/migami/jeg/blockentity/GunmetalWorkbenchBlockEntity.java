package ttv.migami.jeg.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import ttv.migami.jeg.common.container.GunmetalWorkbenchContainer;
import ttv.migami.jeg.init.ModTileEntities;

/**
 * Author: MrCrayfish
 */
public class GunmetalWorkbenchBlockEntity extends AbstractWorkbenchBlockEntity {
    public GunmetalWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.GUNMETAL_WORKBENCH.get(), pos, state);
    }

    @Override
    protected String getRegistryName() {
        return "jeg.gunmetal_workbench";
    }

    @Override
    protected AbstractContainerMenu createSpecificMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new GunmetalWorkbenchContainer(windowId, playerInventory, this);
    }
}
