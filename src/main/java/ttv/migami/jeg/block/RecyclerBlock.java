package ttv.migami.jeg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import ttv.migami.jeg.blockentity.AbstractRecyclerBlockEntity;
import ttv.migami.jeg.blockentity.RecyclerBlockEntity;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.init.ModTileEntities;

import javax.annotation.Nullable;

public class RecyclerBlock extends AbstractRecyclerBlock {
    //private static final VoxelShape INSIDE = box(2.0D, 14.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    //protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE), BooleanOp.ONLY_FIRST);
    private static final VoxelShape SHAPE = makeShape();

    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0.5625, 0.125, 0.875, 0.875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.875, 0.25, 0.1875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.75, 0.125, 0.1875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0.875, 0.875, 0.1875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.75, 1, 0.1875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0, 1, 0.1875, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.125, 1, 0.1875, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0, 0.25, 0.1875, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 0.125, 0.1875, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.1875, 0, 1, 1, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0.875, 0.875, 1, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0, 0.125, 1, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.1875, 0.125, 1, 1, 1), BooleanOp.OR);
        return shape;
    }

    private static final VoxelShape COVER = makeCover();

    public static VoxelShape makeCover() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 1.1875, -0.1875, 1, 1.4375, -0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1, -0.0625, 1, 1.25, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.1875, 1.0625, 1, 1.4375, 1.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1, 0.9375, 1, 1.25, 1.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 1, 0, 1.0625, 1.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.0625, 1, 0, 0.0625, 1.25, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.1875, 1.1875, 0, -0.0625, 1.4375, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.0625, 1.1875, 0, 1.1875, 1.4375, 1), BooleanOp.OR);
        return shape;
    }

    private static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE, COVER);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_COLLISION;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos pPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }


    public RecyclerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RecyclerBlockEntity(pPos, pState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> pBlockEntityType) {
        return createRecyclerTicker(level, pBlockEntityType, ModTileEntities.RECYCLER.get());
    }

    protected void openContainer(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockentity = level.getBlockEntity(blockPos);
        if (blockentity instanceof RecyclerBlockEntity) {
            player.openMenu((MenuProvider) blockentity);
        }
    }

    public void animateTick(BlockState blockState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        AbstractRecyclerBlockEntity p_155017_ = (AbstractRecyclerBlockEntity) pLevel.getBlockEntity(pPos);
        //ItemStack itemStack = p_155017_.items.get(0);
        //if (blockState.getValue(LIT) && itemStack.isEmpty())
        if (blockState.getValue(LIT))
        {
            double d0 = (double) pPos.getX() + 0.5D;
            double d1 = pPos.getY();
            double d2 = (double) pPos.getZ() + 0.5D;
            if (pRandom.nextDouble() < 0.1D) {
                pLevel.playLocalSound(d0, d1, d2, ModSounds.RECYCLER_SHREDDING.get(), SoundSource.BLOCKS, 0.2F, 1.0F, false);
                pLevel.playLocalSound(d0, d1, d2, ModSounds.RECYCLER_LOOP.get(), SoundSource.BLOCKS, 0.15F, 1.0F, false);
            }
            double x = pPos.getX() + 0.5;
            double y = pPos.getY() + 0.9;
            double z = pPos.getZ() + 0.5;
            pLevel.addParticle(ModParticleTypes.SCRAP.get(), true, x, y, z, 1, 0, 0);
        }
    }
}