package ttv.migami.jeg.crafting.workbench;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import ttv.migami.jeg.blockentity.ScrapWorkbenchBlockEntity;
import ttv.migami.jeg.init.ModRecipeSerializers;
import ttv.migami.jeg.init.ModRecipeTypes;

public class ScrapWorkbenchRecipe extends AbstractWorkbenchRecipe<ScrapWorkbenchBlockEntity> {
    public ScrapWorkbenchRecipe(ResourceLocation id, ItemStack item, ImmutableList<WorkbenchIngredient> materials) {
        super(id, item, materials);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SCRAP_WORKBENCH.get();
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return ModRecipeTypes.SCRAP_WORKBENCH.get();
    }
}