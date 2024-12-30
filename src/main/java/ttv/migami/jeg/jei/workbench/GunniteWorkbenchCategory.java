package ttv.migami.jeg.jei.workbench;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.crafting.workbench.GunniteWorkbenchRecipe;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.jei.JEGPlugin;

public class GunniteWorkbenchCategory extends AbstractWorkbenchCategory<GunniteWorkbenchRecipe> {
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "gunnite_workbench");

    public GunniteWorkbenchCategory(IGuiHelper helper) {
        super(helper, new ItemStack(ModBlocks.GUNNITE_WORKBENCH.get()));
    }

    @Override
    public RecipeType<GunniteWorkbenchRecipe> getRecipeType() {
        return JEGPlugin.GUNNITE_WORKBENCH;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GunniteWorkbenchRecipe recipe, IFocusGroup focuses) {
        ItemStack output = recipe.getItem();
        for (int i = 0; i < recipe.getMaterials().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, (i % 8) * 18 + 1, 88 + (i / 8) * 18).addIngredients(recipe.getMaterials().get(i));
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(output);
    }
}