package ttv.migami.jeg.client.screen.recycler;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.client.util.RenderUtil;
import ttv.migami.jeg.common.container.recycler.AbstractRecyclerMenu;
import ttv.migami.jeg.init.ModItems;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractRecyclerScreen<T extends AbstractRecyclerMenu> extends AbstractContainerScreen<T> {
    private final ResourceLocation texture;

    public AbstractRecyclerScreen(T t, Inventory inventory, Component component, ResourceLocation resourceLocation) {
        super(t, inventory, component);
        this.texture = resourceLocation;
    }

    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public void containerTick() {
        super.containerTick();
    }

    /*protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        pGuiGraphics.drawString(this.font, "Granite", 13, 20, 4210752, false);
    }*/

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);

        int startX = this.leftPos;
        int startY = this.topPos;

        if (Config.CLIENT.display.recyclerNote.get()) {
            int graniteX = startX + 22;
            int graniteY = startY + 16;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, graniteX, graniteY, 16, 16))
            {
                pGuiGraphics.renderTooltip(this.font, Items.GRANITE.getDefaultInstance(), mouseX, mouseY);
            }

            int ingotX = startX + 20;
            int ingotY = startY + 39;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, ingotX, ingotY, 16, 16))
            {
                pGuiGraphics.renderTooltip(this.font, ModItems.GUNMETAL_INGOT.get().getDefaultInstance(), mouseX, mouseY);
            }

            int coalX = startX + 56;
            int coalY = startY + 53;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, coalX, coalY, 16, 16) && this.menu.getSlot(1).getItem().isEmpty())
            {
                pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.recycler.fuel").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
            }
        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float p_97854_, int p_97855_, int p_97856_) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(this.texture, i, j, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            int k = this.menu.getLitProgress();
            pGuiGraphics.blit(this.texture, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        int l = this.menu.getBurnProgress();
        pGuiGraphics.blit(this.texture, i + 79, j + 34, 176, 14, l + 1, 16);

        if (Config.CLIENT.display.recyclerNote.get()) {
            pGuiGraphics.blit(this.texture, i + 5, j + 8, 176, 31, 46, 51);

            pGuiGraphics.renderItem(Items.GRANITE.getDefaultInstance(), i + 22, j + 16);
            pGuiGraphics.renderItemDecorations(this.font, Items.GRANITE.getDefaultInstance(), i + 22, j + 16);

            pGuiGraphics.renderItem(ModItems.GUNMETAL_INGOT.get().getDefaultInstance(), i + 20, j + 39);
            pGuiGraphics.renderItemDecorations(this.font, ModItems.GUNMETAL_INGOT.get().getDefaultInstance(), i + 20, j + 39);
        }
    }
}