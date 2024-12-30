package ttv.migami.jeg.jei.recycling;

import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.jeg.Reference;

public abstract class RecyclerVariantCategory<T> implements IRecipeCategory<T> {
    public static final ResourceLocation RECIPE_GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/jei_gui.png");

    protected final IDrawableStatic staticFlame;
    protected final IDrawableAnimated animatedFlame;

    public RecyclerVariantCategory(IGuiHelper guiHelper) {
        this.staticFlame = guiHelper.createDrawable(RECIPE_GUI, 82, 0, 14, 14);
        this.animatedFlame = guiHelper.createAnimatedDrawable(this.staticFlame, 300, IDrawableAnimated.StartDirection.TOP, true);
    }
}