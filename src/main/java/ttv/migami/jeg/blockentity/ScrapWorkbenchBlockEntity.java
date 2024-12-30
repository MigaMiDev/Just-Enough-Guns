package ttv.migami.jeg.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import ttv.migami.jeg.common.container.ScrapWorkbenchContainer;
import ttv.migami.jeg.init.ModTileEntities;

/**
 * Author: MrCrayfish
 */
public class ScrapWorkbenchBlockEntity extends AbstractWorkbenchBlockEntity {
    public ScrapWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.SCRAP_WORKBENCH.get(), pos, state);
    }

    @Override
    protected String getRegistryName() {
        return "jeg.scrap_workbench";
    }

    @Override
    protected AbstractContainerMenu createSpecificMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new ScrapWorkbenchContainer(windowId, playerInventory, this);
    }
}
