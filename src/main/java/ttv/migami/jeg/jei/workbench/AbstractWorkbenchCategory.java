package ttv.migami.jeg.jei.workbench;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.util.RenderUtil;
import ttv.migami.jeg.crafting.workbench.AbstractWorkbenchRecipe;

import java.awt.*;

public abstract class AbstractWorkbenchCategory<T extends AbstractWorkbenchRecipe<?>> implements IRecipeCategory<T> {
    protected static final ResourceLocation BACKGROUND = new ResourceLocation(Reference.MOD_ID, "textures/gui/workbench.png");
    protected static final String TITLE_KEY = Reference.MOD_ID + ".category.workbench.title";
    protected static final String MATERIALS_KEY = Reference.MOD_ID + ".category.workbench.materials";

    protected final IDrawableStatic background;
    protected final IDrawableStatic window;
    protected final IDrawableStatic inventory;
    protected final IDrawable icon;
    protected final Component title;

    public AbstractWorkbenchCategory(IGuiHelper helper, ItemStack iconItem) {
        this.background = helper.createBlankDrawable(162, 124);
        this.window = helper.createDrawable(BACKGROUND, 7, 15, 162, 72);
        this.inventory = helper.createDrawable(BACKGROUND, 7, 101, 162, 36);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconItem);
        this.title = Component.translatable(TITLE_KEY);
    }

    @Override
    public abstract RecipeType<T> getRecipeType();

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        this.window.draw(graphics, 0, 0);
        this.inventory.draw(graphics, 0, this.window.getHeight() + 2 + 11 + 2);

        graphics.drawString(Minecraft.getInstance().font, I18n.get(MATERIALS_KEY), 0, 78, Color.DARK_GRAY.getRGB(), false);

        ItemStack output = recipe.getItem();
        MutableComponent displayName = output.getHoverName().copy();
        if (output.getCount() > 1) {
            displayName.append(Component.literal(" x " + output.getCount()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
        int titleX = this.window.getWidth() / 2;
        graphics.drawCenteredString(Minecraft.getInstance().font, displayName, titleX, 5, Color.WHITE.getRGB());

        PoseStack stack = RenderSystem.getModelViewStack();
        stack.pushPose();
        {
            stack.mulPoseMatrix(graphics.pose().last().pose());
            stack.translate(81, 40, 32);
            stack.scale(40F, 40F, 40F);
            stack.mulPose(Axis.XP.rotationDegrees(-5F));
            float partialTicks = Minecraft.getInstance().getFrameTime();
            stack.mulPose(Axis.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + partialTicks));
            stack.scale(-1, -1, -1);
            RenderSystem.applyModelViewMatrix();

            BakedModel model = RenderUtil.getModel(output);
            Lighting.setupFor3DItems();

            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().render(output, ItemDisplayContext.FIXED, false, new PoseStack(), buffer, 15728880, OverlayTexture.NO_OVERLAY, model);
            buffer.endBatch();
        }
        stack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);
}