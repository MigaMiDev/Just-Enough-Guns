package ttv.migami.jeg.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import ttv.migami.jeg.init.ModTileEntities;

public class DynamicLightBlockEntity extends BlockEntity {
    private double delay = 5.0;

    public DynamicLightBlockEntity(BlockPos position, BlockState state) {
        super(ModTileEntities.DYNAMIC_LIGHT.get(), position, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("Delay")) {
            this.delay = compound.getDouble("Delay");
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putDouble("Delay", this.delay);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DynamicLightBlockEntity blockEntity) {
        if (!level.isClientSide) {
            if (blockEntity.delay > 0) {
                blockEntity.delay -= 1;
            } else {
                level.setBlock(pos, state.getFluidState().isSource() ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }
}
