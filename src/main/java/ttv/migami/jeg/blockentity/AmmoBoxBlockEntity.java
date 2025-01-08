package ttv.migami.jeg.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import ttv.migami.jeg.block.AmmoBoxBlock;
import ttv.migami.jeg.common.container.AmmoBoxMenu;
import ttv.migami.jeg.init.ModTileEntities;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class AmmoBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> itemStacks;
    private int openCount;

    public AmmoBoxBlockEntity(@Nullable DyeColor pColor, BlockPos pPos, BlockState pBlockState) {
        super(ModTileEntities.AMMO_BOX.get(), pPos, pBlockState);
        this.itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    public AmmoBoxBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModTileEntities.AMMO_BOX.get(), pPos, pBlockState);
        this.itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    }

    public int getContainerSize() {
        return this.itemStacks.size();
    }

    public boolean triggerEvent(int pId, int pType) {
        if (pId == 1) {
            this.openCount = pType;
            if (pType == 0) {
                doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
            }

            if (pType == 1) {
                doNeighborUpdates(this.getLevel(), this.worldPosition, this.getBlockState());
            }

            return true;
        } else {
            return super.triggerEvent(pId, pType);
        }
    }

    private static void doNeighborUpdates(Level pLevel, BlockPos pPos, BlockState pState) {
        pState.updateNeighbourShapes(pLevel, pPos, 3);
    }

    public void startOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }

            ++this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount == 1) {
                this.level.gameEvent(pPlayer, GameEvent.CONTAINER_OPEN, this.worldPosition);
                this.level.playSound((Player)null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    public void stopOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            --this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount <= 0) {
                this.level.gameEvent(pPlayer, GameEvent.CONTAINER_CLOSE, this.worldPosition);
                this.level.playSound((Player)null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            }
        }

    }

    protected Component getDefaultName() {
        return Component.translatable("container.jeg.ammo_box");
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.loadFromTag(pTag);
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        if (!this.trySaveLootTable(pTag)) {
            ContainerHelper.saveAllItems(pTag, this.itemStacks, false);
        }

    }

    public void loadFromTag(CompoundTag pTag) {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(pTag) && pTag.contains("Items", 9)) {
            ContainerHelper.loadAllItems(pTag, this.itemStacks);
        }

    }

    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    protected void setItems(NonNullList<ItemStack> pItems) {
        this.itemStacks = pItems;
    }

    public int[] getSlotsForFace(Direction pSide) {
        return SLOTS;
    }

    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        return !(Block.byItem(pItemStack.getItem()) instanceof AmmoBoxBlock) && pItemStack.getItem().canFitInsideContainerItems();
    }

    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        return true;
    }

    protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer) {
        return new AmmoBoxMenu(pId, pPlayer, this);
    }

    protected IItemHandler createUnSidedHandler() {
        return new SidedInvWrapper(this, Direction.UP);
    }
}