package ttv.migami.jeg.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.container.AmmoBoxMenu;

@OnlyIn(Dist.CLIENT)
public class AmmoBoxScreen extends AbstractContainerScreen<AmmoBoxMenu> {
    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/container/ammo_box.png");

    public AmmoBoxScreen(AmmoBoxMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        ++this.imageHeight;
    }

    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int $$4 = (this.width - this.imageWidth) / 2;
        int $$5 = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(CONTAINER_TEXTURE, $$4, $$5, 0, 0, this.imageWidth, this.imageHeight);
    }
}