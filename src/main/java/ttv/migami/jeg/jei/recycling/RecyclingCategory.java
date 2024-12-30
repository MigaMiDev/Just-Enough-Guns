package ttv.migami.jeg.jei.recycling;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Ingredient;
import ttv.migami.jeg.crafting.recycling.RecyclingRecipe;
import ttv.migami.jeg.init.ModBlocks;
import ttv.migami.jeg.jei.JEGPlugin;

public class RecyclingCategory extends AbstractRecyclingCategory<RecyclingRecipe> {
    private final IDrawable background;

    public RecyclingCategory(IGuiHelper guiHelper) {
        super(guiHelper, ModBlocks.RECYCLER.get(), "gui.jeg.category.recycling", 400);
        this.background = guiHelper.drawableBuilder(RecyclerVariantCategory.RECIPE_GUI, 0, 0, 82, 34).addPadding(0, 10, 0, 0).build();
    }

    public RecipeType<RecyclingRecipe> getRecipeType() {
        return JEGPlugin.RECYCLING;
    }

    public IDrawable getBackground() {
        return this.background;
    }

    public void draw(RecyclingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.animatedFlame.draw(guiGraphics, 1, 20);
        IDrawableAnimated arrow = this.getArrow(recipe);
        arrow.draw(guiGraphics, 24, 8);
        this.drawCookTime(recipe, guiGraphics, 35);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecyclingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients((Ingredient)recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 9).addItemStack(recipe.getItem());
    }
}