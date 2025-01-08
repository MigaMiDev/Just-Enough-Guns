package ttv.migami.jeg.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import ttv.migami.jeg.blockentity.AmmoBoxBlockEntity;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.init.ModTileEntities;
import ttv.migami.jeg.util.VoxelShapeHelper;

import javax.annotation.Nullable;
import java.util.*;

public class AmmoBoxBlock extends BaseEntityBlock {
    private final Map<BlockState, VoxelShape> SHAPES = new HashMap<>();
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");

    public AmmoBoxBlock(Properties pProperties) {
        super(pProperties);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    private VoxelShape getShape(BlockState state)
    {
        if(SHAPES.containsKey(state))
        {
            return SHAPES.get(state);
        }
        Direction direction = state.getValue(FACING);
        List<VoxelShape> shapes = new ArrayList<>();
        shapes.add(VoxelShapeHelper.getRotatedShapes(VoxelShapeHelper.rotate(Block.box(2, 0, 5, 14, 7, 11), Direction.SOUTH))[direction.get2DDataValue()]);
        shapes.add(VoxelShapeHelper.getRotatedShapes(VoxelShapeHelper.rotate(Block.box(1, 7, 5, 15, 9, 11), Direction.SOUTH))[direction.get2DDataValue()]);
        VoxelShape shape = VoxelShapeHelper.combineAll(shapes);
        SHAPES.put(state, shape);
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return this.getShape(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return this.getShape(state);
    }

    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AmmoBoxBlockEntity(pPos, pState);
    }

    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (pPlayer.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            BlockEntity $$6 = pLevel.getBlockEntity(pPos);
            if ($$6 instanceof AmmoBoxBlockEntity) {
                AmmoBoxBlockEntity $$7 = (AmmoBoxBlockEntity)$$6;
                pPlayer.openMenu($$7);
                pPlayer.awardStat(Stats.OPEN_CHEST);
                PiglinAi.angerNearbyPiglins(pPlayer, true);

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        BlockEntity $$4 = pLevel.getBlockEntity(pPos);
        if ($$4 instanceof AmmoBoxBlockEntity $$5) {
            if (!pLevel.isClientSide && pPlayer.isCreative() && !$$5.isEmpty()) {
                ItemStack $$6 = getItemStack();
                $$4.saveToItem($$6);
                if ($$5.hasCustomName()) {
                    $$6.setHoverName($$5.getCustomName());
                }

                ItemEntity $$7 = new ItemEntity(pLevel, (double)pPos.getX() + 0.5, (double)pPos.getY() + 0.5, (double)pPos.getZ() + 0.5, $$6);
                $$7.setDefaultPickUpDelay();
                pLevel.addFreshEntity($$7);
            } else {
                $$5.unpackLootTable(pPlayer);
            }
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    public static Block getBlock() {
        return ModBlocks.AMMO_BOX.get();
    }

    public static ItemStack getItemStack() {
        return new ItemStack(getBlock());
    }

    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        BlockEntity $$2 = (BlockEntity)pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if ($$2 instanceof AmmoBoxBlockEntity $$3) {
            pParams = pParams.withDynamicDrop(CONTENTS, (p_56219_) -> {
                for(int j = 0; j < $$3.getContainerSize(); ++j) {
                    p_56219_.accept($$3.getItem(j));
                }

            });
        }

        return super.getDrops(pState, pParams);
    }

    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.hasCustomHoverName()) {
            BlockEntity $$5 = pLevel.getBlockEntity(pPos);
            if ($$5 instanceof AmmoBoxBlockEntity) {
                ((AmmoBoxBlockEntity)$$5).setCustomName(pStack.getHoverName());
            }
        }

    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity $$5 = pLevel.getBlockEntity(pPos);
            if ($$5 instanceof AmmoBoxBlockEntity) {
                pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        CompoundTag $$4 = BlockItem.getBlockEntityData(pStack);
        if ($$4 != null) {
            if ($$4.contains("LootTable", 8)) {
                pTooltip.add(Component.literal("???????"));
            }

            if ($$4.contains("Items", 9)) {
                NonNullList<ItemStack> $$5 = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems($$4, $$5);
                int $$6 = 0;
                int $$7 = 0;
                Iterator var9 = $$5.iterator();

                while(var9.hasNext()) {
                    ItemStack $$8 = (ItemStack)var9.next();
                    if (!$$8.isEmpty()) {
                        ++$$7;
                        if ($$6 <= 4) {
                            ++$$6;
                            MutableComponent $$9 = $$8.getHoverName().copy();
                            $$9.append(" x").append(String.valueOf($$8.getCount()));
                            pTooltip.add($$9);
                        }
                    }
                }

                if ($$7 - $$6 > 0) {
                    pTooltip.add(Component.translatable("container.jeg.ammo_box.more", new Object[]{$$7 - $$6}).withStyle(ChatFormatting.ITALIC));
                }
            }
        }

    }

    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)pLevel.getBlockEntity(pPos));
    }

    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        ItemStack $$3 = super.getCloneItemStack(pLevel, pPos, pState);
        pLevel.getBlockEntity(pPos, ModTileEntities.AMMO_BOX.get()).ifPresent((p_187446_) -> {
            p_187446_.saveToItem($$3);
        });
        return $$3;
    }

    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }
}