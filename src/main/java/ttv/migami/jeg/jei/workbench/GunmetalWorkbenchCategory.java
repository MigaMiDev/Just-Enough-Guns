package ttv.migami.jeg.jei.workbench;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.crafting.workbench.GunmetalWorkbenchRecipe;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.jei.JEGPlugin;

public class GunmetalWorkbenchCategory extends AbstractWorkbenchCategory<GunmetalWorkbenchRecipe> {
    public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "gunmetal_workbench");

    public GunmetalWorkbenchCategory(IGuiHelper helper) {
        super(helper, new ItemStack(ModBlocks.GUNMETAL_WORKBENCH.get()));
    }

    @Override
    public RecipeType<GunmetalWorkbenchRecipe> getRecipeType() {
        return JEGPlugin.GUNMETAL_WORKBENCH;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GunmetalWorkbenchRecipe recipe, IFocusGroup focuses) {
        ItemStack output = recipe.getItem();
        for (int i = 0; i < recipe.getMaterials().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, (i % 8) * 18 + 1, 88 + (i / 8) * 18).addIngredients(recipe.getMaterials().get(i));
        }
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(output);
    }
}