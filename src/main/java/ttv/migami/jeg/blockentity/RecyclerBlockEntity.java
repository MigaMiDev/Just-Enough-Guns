package ttv.migami.jeg.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import ttv.migami.jeg.common.container.recycler.RecyclerMenu;
import ttv.migami.jeg.init.ModRecipeTypes;
import ttv.migami.jeg.init.ModTileEntities;

public class RecyclerBlockEntity extends AbstractRecyclerBlockEntity {
    public RecyclerBlockEntity(BlockPos pPos, BlockState blockState) {
        super(ModTileEntities.RECYCLER.get(), pPos, blockState, ModRecipeTypes.RECYCLING.get());
    }

    protected Component getDefaultName() {
        return Component.translatable("container.jeg.recycler");
    }

    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new RecyclerMenu(pContainerId, pInventory, this, this.dataAccess);
    }
}
