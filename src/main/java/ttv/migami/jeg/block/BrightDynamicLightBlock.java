package ttv.migami.jeg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import ttv.migami.jeg.blockentity.DynamicLightBlockEntity;

public class BrightDynamicLightBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {
    public static final BooleanProperty WATERLOGGED;
    public static final IntegerProperty AGE;

    public BrightDynamicLightBlock() {
        super(Properties.of().randomTicks().liquid().mapColor(MapColor.NONE).sound(SoundType.GLASS).strength(1.0F, 10.0F).lightLevel((s) -> 12).noCollission().noOcclusion().pushReaction(PushReaction.IGNORE).isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(AGE, 0)
                .setValue(WATERLOGGED, false));
    }

    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getFluidState().isEmpty();
    }

    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, WATERLOGGED);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        level.scheduleTick(pos, this, 2);
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        this.tick(pState, pLevel, pPos, pRandom);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        vanish(state, level, pos);
        BlockState replacement = state.getFluidState().isSource() ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        level.setBlock(pos, replacement, 3);
    }

    private boolean vanish(BlockState pState, Level pLevel, BlockPos pPos) {
        int $$3 = pState.getValue(AGE);
        if ($$3 < 1) {
            pLevel.setBlock(pPos, (BlockState) pState.setValue(AGE, $$3 + 1), 2);
            pLevel.scheduleTick(pPos, this, 2);
            return false;
        } else {
            BlockState currentState = pLevel.getBlockState(pPos);
            BlockState newState = currentState.getFluidState().isSource() ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            if (currentState != newState) {
                pLevel.setBlock(pPos, newState, 3);
            }
            return true;
        }
    }


    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (!world.isClientSide()) {
            BlockPos blockPos = BlockPos.containing(x, y, z);
            double delay = getDelay(world, blockPos, "Delay");

            if (delay > 0.0) {
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity != null) {
                    blockEntity.getPersistentData().putDouble("Delay", delay - 1.0);
                }
            } else {
                BlockState currentState = world.getBlockState(blockPos);
                BlockState newState = currentState.getFluidState().isSource() ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                if (currentState != newState) {
                    world.setBlock(blockPos, newState, 3);
                }
            }
        }
    }

    public static double getDelay(LevelAccessor world, BlockPos pos, String tag) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.getPersistentData().getDouble(tag) : -1.0;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DynamicLightBlockEntity(pos, state);
    }

    static {
        AGE = BlockStateProperties.AGE_2;
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
    }
}