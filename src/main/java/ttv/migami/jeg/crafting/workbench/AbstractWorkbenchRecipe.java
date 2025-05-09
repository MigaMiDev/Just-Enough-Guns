package ttv.migami.jeg.crafting.workbench;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import ttv.migami.jeg.util.InventoryUtil;

public abstract class AbstractWorkbenchRecipe<T extends BlockEntity & Container> implements Recipe<T> {
    private final ResourceLocation id;
    private final ItemStack item;
    private final ImmutableList<WorkbenchIngredient> materials;

    public AbstractWorkbenchRecipe(ResourceLocation id, ItemStack item, ImmutableList<WorkbenchIngredient> materials) {
        this.id = id;
        this.item = item;
        this.materials = materials;
    }

    public ItemStack getItem() {
        return this.item.copy();
    }

    public ImmutableList<WorkbenchIngredient> getMaterials() {
        return this.materials;
    }

    @Override
    public boolean matches(T inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(T entity, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return this.item.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public abstract RecipeSerializer<?> getSerializer();

    @Override
    public abstract RecipeType<?> getType();

    public boolean hasMaterials(Player player) {
        for (WorkbenchIngredient ingredient : this.getMaterials()) {
            if (!InventoryUtil.hasWorkstationIngredient(player, ingredient)) {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials(Player player) {
        for (WorkbenchIngredient ingredient : this.getMaterials()) {
            InventoryUtil.removeWorkstationIngredient(player, ingredient);
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}