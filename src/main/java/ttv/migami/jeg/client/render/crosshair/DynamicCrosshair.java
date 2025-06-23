package ttv.migami.jeg.client.render.crosshair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.DotRenderMode;
import ttv.migami.jeg.client.handler.AimingHandler;
import ttv.migami.jeg.client.handler.GunRenderingHandler;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.SpreadTracker;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.util.GunCompositeStatHelper;

/**
 * Author: NineZero
 */
public class DynamicCrosshair extends Crosshair
{
    private static final ResourceLocation DYNAMIC_CROSSHAIR_H = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/dynamic_horizontal.png");
    private static final ResourceLocation DYNAMIC_CROSSHAIR_V = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/dynamic_vertical.png");
    private static final ResourceLocation DOT_CROSSHAIR = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/dot.png");

    private float scale;
    private float prevScale;
    private float fireBloom;
    private float prevFireBloom;

    public DynamicCrosshair()
    {
        super(new ResourceLocation(Reference.MOD_ID, "dynamic"));
    }

    @Override
    public void tick()
    {
        this.prevScale = this.scale;
        this.scale *= 0.5F;
        this.prevFireBloom = this.fireBloom;
        if (this.fireBloom > 0)
        {
        	float i = (float) Config.COMMON.projectileSpread.spreadThreshold.get()/50;
        	this.fireBloom -= Math.min(3F/(Math.max(i,1)), this.fireBloom);
        }
    }

    @Override
    public void onGunFired()
    {
        this.prevScale = 0.0F;
        this.scale = 0.6F;
        this.fireBloom = 3.0F;
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks)
    {
        float alpha = 1.0F;// - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size1 = 7F;
        float size2 = 1F;
        float spread = 0F;
    	float scaleMultiplier = 2.0F;
    	boolean renderDot = false;
        if (mc.player != null)
        {
        	ItemStack heldItem = mc.player.getMainHandItem();
            if((heldItem.getItem() instanceof GunItem gunItem))
            {
            	GunItem gun = (GunItem) heldItem.getItem();
            	Gun modifiedGun = gun.getModifiedGun(heldItem);
            	float aiming = (float) AimingHandler.get().getNormalisedAdsProgress();
                float sprintTransition = (float) GunRenderingHandler.get().getSprintTransition(Minecraft.getInstance().getFrameTime());
                float spreadCount = (SpreadTracker.get(mc.player).getNextSpread(gun,aiming));
            	float spreadModifier = ((spreadCount+(1F/Math.max(Config.COMMON.projectileSpread.maxCount.get(),1F)))*Math.min(Mth.lerp(partialTicks, this.prevFireBloom, this.fireBloom),1F));
            	spreadModifier = (float) (Mth.lerp(sprintTransition*0.5, spreadModifier, 1.0F));
            	float baseSpread = GunCompositeStatHelper.getCompositeSpread(heldItem, modifiedGun);
                GunCompositeStatHelper.getCompositeMinSpread(heldItem, modifiedGun);
                float minSpread = modifiedGun.getGeneral().isAlwaysSpread() ? baseSpread : 0;
            	float aimingSpreadMultiplier = (float) (Mth.lerp(aiming, 1.0F, 1.0F - 0.5F));
                spread = Mth.clamp(Mth.lerp(spreadModifier,minSpread,baseSpread)*(aimingSpreadMultiplier),0F,32F);
            	
            	DotRenderMode dotRenderMode = Config.CLIENT.display.dynamicCrosshairDotMode.get();
            	renderDot = (dotRenderMode == DotRenderMode.ALWAYS)
            	|| (dotRenderMode == DotRenderMode.AT_MIN_SPREAD && (SpreadTracker.get(mc.player).getNextSpread(gun,aiming)*spreadModifier <= 0 && spread<=Config.CLIENT.display.dynamicCrosshairDotThreshold.get()))
    			|| (dotRenderMode == DotRenderMode.THRESHOLD && spread <= Config.CLIENT.display.dynamicCrosshairDotThreshold.get())
    			&& (!Config.CLIENT.display.onlyRenderDotWhileAiming.get() || aiming > 0.9F);
            }
        }
        
        float baseScale = 1F + (Mth.lerp(partialTicks, this.prevScale, this.scale)*scaleMultiplier);
        float scale = (float) (baseScale + (spread*(2F*Config.CLIENT.display.dynamicCrosshairSpreadMultiplier.get())));
        float scaleSize = (scale/6F)+1.15F;
        float crosshairBaseTightness = (float) (0.8-(Config.CLIENT.display.dynamicCrosshairBaseSpread.get()/2));
        float finalSpreadTranslate = (float) ( (Mth.lerp(0.95,scaleSize-1,Math.log(scaleSize)))*(2.8F) );
        //float rawSpreadTranslation = (scaleSize-1)*(3.0F);
        //float finalSpreadTranslate = (float) (Mth.lerp(0.0,rawSpreadTranslation,Math.log(rawSpreadTranslation+1)-1));
        
        double windowCenteredX = Math.round((windowWidth) / 2F)-0.5;
        double windowCenteredY = Math.round((windowHeight) / 2F)-0.5;

        boolean blend = true;
        RenderSystem.enableBlend();
        if (blend)
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // Left
        stack.pushPose();
        {
        	Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_H);

            stack.translate(windowCenteredX, windowCenteredY, 0);
            stack.scale(scaleSize, 1, 1);
            stack.translate((-size1 / 2F) - (finalSpreadTranslate) + crosshairBaseTightness -0.0F, -size2 / 2F, 0);

            float sizeX = size1;
            float sizeY = size2;
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 1F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(1, 1F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(1, 0F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        // Right
        stack.pushPose();
        {
        	Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_H);

            stack.translate(windowCenteredX, windowCenteredY, 0);
            stack.scale(scaleSize, 1, 1);
            stack.translate((-size1 / 2F) + (finalSpreadTranslate) - crosshairBaseTightness, -size2 / 2F, 0);

            float sizeX = size1;
            float sizeY = size2;
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 9F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(1, 9F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(1, 8F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 8F/9F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        // Top
        stack.pushPose();
        {
        	Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_V);

            stack.translate(windowCenteredX, windowCenteredY, 0);
            stack.scale(1, scaleSize, 1);
            stack.translate(-size2 / 2F, (-size1 / 2F) - (finalSpreadTranslate) + crosshairBaseTightness, 0);

            float sizeX = size2;
            float sizeY = size1;
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0F/9F, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(1F/9F, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(1F/9F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0F/9F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        // Bottom
        stack.pushPose();
        {
        	Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR_V);

            stack.translate(windowCenteredX, windowCenteredY, 0);
            stack.scale(1, scaleSize, 1);
            stack.translate(-size2 / 2F -0.0F, (-size1 / 2F) + (finalSpreadTranslate) - crosshairBaseTightness, 0);

            float sizeX = size2;
            float sizeY = size1;
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(8F/9F, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(9F/9F, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(9F/9F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(8F/9F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        // Center Dot (will be configurable)
        if (renderDot)
        {
        stack.pushPose();
        {
        	int dotSize = 9;
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DOT_CROSSHAIR);
            Matrix4f matrix = stack.last().pose();
            stack.translate(windowCenteredX, windowCenteredY, 0);
            stack.translate(-dotSize / 2F, -dotSize / 2F, 0);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, dotSize, 0).uv(0, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, dotSize, dotSize, 0).uv(1, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, dotSize, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();
        }

        if (blend)
        RenderSystem.defaultBlendFunc();

        /*
        // Left
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size1;
            float sizeY = size2;

            stack.translate(Math.round((windowWidth-1) / 2F), Math.round((windowHeight-1) / 2F), 0);
            stack.translate(-sizeX + 1F, -sizeY / 2F, 0);
            stack.translate(-scale*2, 0, 0);

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 2F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(10F/16F, 2F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(10F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        // Right
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size1;
            float sizeY = size2;

            stack.translate(Math.round((windowWidth) / 2F), Math.round((windowHeight-1) / 2F), 0);
            stack.translate(-1F, -sizeY / 2F, 0);
            stack.translate(scale*2, 0, 0);

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(0, 5F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(10F/16F, 5F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(10F/16F, 3F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 3F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        // Top
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size2;
            float sizeY = size1;

            stack.translate(Math.round(windowWidth / 2F), Math.round((windowHeight-1) / 2F), 0);
            stack.translate(-sizeX, -sizeY + 1F, 0);
            stack.translate(0, -scale*2, 0);

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(13F/16F, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(11F/16F, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(13F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(11F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        // Bottom
        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DYNAMIC_CROSSHAIR);
            float sizeX = size2;
            float sizeY = size1;

            stack.translate(Math.round(windowWidth / 2F), Math.round((windowHeight-1) / 2F), 0);
            stack.translate(-sizeX, -1F, 0);
            stack.translate(0, scale*2, 0);

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, sizeY, 0).uv(14F/16F, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).uv(1, 10F/16F).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(14F/16F, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();
        */
    }
}