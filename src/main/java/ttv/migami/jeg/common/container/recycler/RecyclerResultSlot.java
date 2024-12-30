package ttv.migami.jeg.common.container.recycler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.blockentity.AbstractRecyclerBlockEntity;

public class RecyclerResultSlot extends Slot {
    private final Player player;
    private int removeCount;

    public RecyclerResultSlot(Player player, Container container, int i, int i1, int i2) {
        super(container, i, i1, i2);
        this.player = player;
    }

    public boolean mayPlace(ItemStack pStack) {
        return false;
    }

    public ItemStack remove(int pAmount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(pAmount, this.getItem().getCount());
        }

        return super.remove(pAmount);
    }

    public void onTake(Player pPlayer, ItemStack pStack) {
        this.checkTakeAchievements(pStack);
        super.onTake(pPlayer, pStack);
    }

    protected void onQuickCraft(ItemStack pStack, int pAmount) {
        this.removeCount += pAmount;
        this.checkTakeAchievements(pStack);
    }

    protected void checkTakeAchievements(ItemStack pStack) {
        pStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
        if (this.player instanceof ServerPlayer && this.container instanceof AbstractRecyclerBlockEntity) {
            ((AbstractRecyclerBlockEntity) this.container).awardUsedRecipesAndPopExperience((ServerPlayer) this.player);
        }

        this.removeCount = 0;
        net.minecraftforge.event.ForgeEventFactory.firePlayerSmeltedEvent(this.player, pStack);
    }
}
