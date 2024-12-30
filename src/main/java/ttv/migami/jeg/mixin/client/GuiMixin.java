package ttv.migami.jeg.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.item.TelescopicScopeItem;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    protected int screenWidth;

    @Shadow
    protected int screenHeight;

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void customRenderSpyglassOverlay(GuiGraphics pGuiGraphics, float pScopeScale, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.isUsingItem() && minecraft.player.getUseItem().getItem() instanceof TelescopicScopeItem) {
            float f = (float) Math.min(this.screenWidth, this.screenHeight);
            float f1 = Math.min((float) this.screenWidth / f, (float) this.screenHeight / f) * pScopeScale;
            int i = Mth.floor(f * f1);
            int j = Mth.floor(f * f1);
            int k = (this.screenWidth - i) / 2;
            int l = (this.screenHeight - j) / 2;
            int i1 = k + i;
            int j1 = l + j;

            ResourceLocation LONG_SCOPE_OVERLAY = new ResourceLocation(Reference.MOD_ID, "textures/scope_long_overlay.png");
            pGuiGraphics.blit(LONG_SCOPE_OVERLAY, k, l, -90, 0.0F, 0.0F, i, j, i, j);
            pGuiGraphics.fill(RenderType.guiOverlay(), 0, j1, this.screenWidth, this.screenHeight, -90, -16777216);
            pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, l, -90, -16777216);
            pGuiGraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
            pGuiGraphics.fill(RenderType.guiOverlay(), i1, l, this.screenWidth, j1, -90, -16777216);

            ci.cancel();
        }
    }
}