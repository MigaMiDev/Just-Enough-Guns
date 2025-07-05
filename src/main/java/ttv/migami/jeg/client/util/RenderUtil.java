package ttv.migami.jeg.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.MatrixUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.apache.logging.log4j.LogManager;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import ttv.migami.jeg.client.handler.AimingHandler;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.item.TelescopicScopeItem;
import ttv.migami.jeg.item.attachment.IAttachment;

import javax.annotation.Nullable;
import java.util.List;

public class RenderUtil
{
    private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
    private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");

    public static void scissor(int x, int y, int width, int height)
    {
        Minecraft mc = Minecraft.getInstance();
        int scale = (int) mc.getWindow().getGuiScale();
        GL11.glScissor(x * scale, mc.getWindow().getScreenHeight() - y * scale - height * scale, Math.max(0, width * scale), Math.max(0, height * scale));
    }

    public static BakedModel getModel(Item item)
    {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(new ItemStack(item));
    }

    public static BakedModel getModel(ItemStack item)
    {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(item);
    }

    public static void rotateZ(PoseStack poseStack, float xOffset, float yOffset, float rotation)
    {
        poseStack.translate(xOffset, yOffset, 0);
        poseStack.mulPose(Axis.ZN.rotationDegrees(rotation));
        poseStack.translate(-xOffset, -yOffset, 0);
    }

    public static void renderGun(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, @Nullable LivingEntity entity)
    {
        renderModel(stack, ItemDisplayContext.NONE, poseStack, buffer, light, overlay, entity);
    }

    public static void renderModel(ItemStack child, ItemStack parent, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(child);
        renderModel(model, ItemDisplayContext.NONE, null, child, parent, poseStack, buffer, light, overlay);
    }

    public static void renderModel(ItemStack stack, ItemDisplayContext display, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, @Nullable LivingEntity entity)
    {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
        if(entity != null)
        {
            model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity.level(), entity, 0);
        }
        renderModel(model, display, stack, poseStack, buffer, light, overlay);
    }

    public static void renderModel(ItemStack stack, ItemDisplayContext display, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, @Nullable Level world, @Nullable LivingEntity entity)
    {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, world, entity, 0);
        renderModel(model, display, stack, poseStack, buffer, light, overlay);
    }

    public static void renderModel(BakedModel model, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        renderModel(model, ItemDisplayContext.NONE, stack, poseStack, buffer, light, overlay);
    }

    public static void renderModel(BakedModel model, ItemDisplayContext display, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        renderModel(model, display, null, stack, ItemStack.EMPTY, poseStack, buffer, light, overlay);
    }

    public static void renderModel(BakedModel model, ItemDisplayContext display, @Nullable Runnable transform, ItemStack stack, ItemStack parent, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        if(!stack.isEmpty())
        {
            poseStack.pushPose();
            boolean flag = display == ItemDisplayContext.GUI || display == ItemDisplayContext.GROUND || display == ItemDisplayContext.FIXED;
            if(flag)
            {
                if(stack.is(Items.TRIDENT))
                {
                    model = Minecraft.getInstance().getModelManager().getModel(TRIDENT_MODEL);
                }
                else if(stack.is(Items.SPYGLASS))
                {
                    model = Minecraft.getInstance().getModelManager().getModel(SPYGLASS_MODEL);
                }
            }

            model = model.applyTransform(display, poseStack, false);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            if(!model.isCustomRenderer() && (stack.getItem() != Items.TRIDENT || flag))
            {
                boolean entity = true;
                if(display != ItemDisplayContext.GUI && !display.firstPerson() && stack.getItem() instanceof BlockItem)
                {
                    Block block = ((BlockItem) stack.getItem()).getBlock();
                    entity = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                }

                RenderType renderType = getRenderType(stack, entity);
                VertexConsumer builder;
                if(stack.getItem() == Items.COMPASS && stack.hasFoil())
                {
                    poseStack.pushPose();
                    PoseStack.Pose entry = poseStack.last();
                    if(display == ItemDisplayContext.GUI)
                    {
                        MatrixUtil.mulComponentWise(entry.pose(), 0.5F);
                    }
                    else if(display.firstPerson())
                    {
                        MatrixUtil.mulComponentWise(entry.pose(), 0.75F);
                    }

                    if(entity)
                    {
                        builder = ItemRenderer.getCompassFoilBufferDirect(buffer, renderType, entry);
                    }
                    else
                    {
                        builder = ItemRenderer.getCompassFoilBuffer(buffer, renderType, entry);
                    }

                    poseStack.popPose();
                }
                else if(entity)
                {
                    builder = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil() || parent.hasFoil());
                }
                else
                {
                    builder = ItemRenderer.getFoilBuffer(buffer, renderType, true, stack.hasFoil() || parent.hasFoil());
                }

                renderModel(model, stack, parent, transform, poseStack, builder, light, overlay);
            }
            else
            {
                IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, display, poseStack, buffer, light, overlay);
            }

            poseStack.popPose();
        }
    }

    public static void renderModelWithTransforms(ItemStack child, ItemStack parent, ItemDisplayContext display, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        poseStack.pushPose();
        BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(child);
        model = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(poseStack, model, display, false);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        renderItemWithoutTransforms(model, child, parent, poseStack, buffer, light, overlay);
        poseStack.popPose();
    }

    public static void renderItemWithoutTransforms(BakedModel model, ItemStack stack, ItemStack parent, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        RenderType renderType = getRenderType(stack, false);
        VertexConsumer builder = ItemRenderer.getFoilBuffer(buffer, renderType, true, stack.hasFoil() || parent.hasFoil());
        renderModel(model, stack, parent, null, poseStack, builder, light, overlay);
    }

    public static void renderItemWithoutTransforms(BakedModel model, ItemStack stack, ItemStack parent, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, @Nullable Runnable transform)
    {
        RenderType renderType = getRenderType(stack, false);
        VertexConsumer builder = ItemRenderer.getFoilBuffer(buffer, renderType, true, stack.hasFoil() || parent.hasFoil());
        renderModel(model, stack, parent, transform, poseStack, builder, light, overlay);
    }

    public static void renderModel(BakedModel model, ItemStack stack, ItemStack parent, @Nullable Runnable transform, PoseStack poseStack, VertexConsumer buffer, int light, int overlay)
    {
        if(transform != null)
        {
            transform.run();
        }
        RandomSource random = RandomSource.create();
        for(Direction direction : Direction.values())
        {
            random.setSeed(42L);
            renderQuads(poseStack, buffer, model.getQuads(null, direction, random), stack, parent, light, overlay);
        }
        random.setSeed(42L);
        renderQuads(poseStack, buffer, model.getQuads(null, null, random), stack, parent, light, overlay);
    }

    public static void renderMacerateWheel(BakedModel model, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (model == null) {
            LogManager.getLogger().warn("Tried to render a null model!");
            return;
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());
        Minecraft.getInstance().getItemRenderer().renderModelLists(model, ItemStack.EMPTY, light, overlay, poseStack, vertexConsumer);
    }

    private static void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack stack, ItemStack parent, int light, int overlay)
    {
        PoseStack.Pose entry = poseStack.last();
        for(BakedQuad quad : quads)
        {
            int color = -1;
            if(quad.isTinted())
            {
                color = getItemStackColor(stack, parent, quad.getTintIndex());
            }
            float red = (float) (color >> 16 & 255) / 255.0F;
            float green = (float) (color >> 8 & 255) / 255.0F;
            float blue = (float) (color & 255) / 255.0F;
            buffer.putBulkData(entry, quad, red, green, blue, light, overlay); //TODO check if right
        }
    }

    public static int getItemStackColor(ItemStack stack, ItemStack parent, int tintIndex)
    {
        int color = Minecraft.getInstance().getItemColors().getColor(stack, tintIndex);
        if(color == -1)
        {
            if(!parent.isEmpty())
            {
                return getItemStackColor(parent, ItemStack.EMPTY, tintIndex);
            }
        }
        return color;
    }

    public static void applyTransformType(ItemStack stack, PoseStack poseStack, ItemDisplayContext display, @Nullable LivingEntity entity)
    {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, entity != null ? entity.level() : null, entity, 0);
        boolean leftHanded = display == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || display == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        //TODO test
        model = model.applyTransform(display, poseStack, leftHanded);

        /* Flips the model and normals if left handed. */
        if(leftHanded)
        {
            //TODO test
            Matrix4f scale = new Matrix4f().scale(-1, 1, 1);
            Matrix3f normal = new Matrix3f(scale);
            poseStack.last().pose().mul(scale);
            poseStack.last().normal().mul(normal);
        }
    }

    public static boolean isMouseWithin(int mouseX, int mouseY, int x, int y, int width, int height)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack poseStack, MultiBufferSource buffer, int combinedLight)
    {
        ItemStack stack = player.getMainHandItem();
        if (AimingHandler.get().isAiming() &&
                (Gun.getAttachment(IAttachment.Type.SCOPE, stack).getItem() instanceof TelescopicScopeItem ||
                        Gun.getAttachment(IAttachment.Type.SCOPE, stack).is(Items.SPYGLASS)))
        {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        PlayerRenderer renderer = (PlayerRenderer) renderManager.getRenderer(player);
        RenderSystem.setShaderTexture(0, player.getSkinTextureLocation());
        if(hand == HumanoidArm.RIGHT)
        {
            renderer.renderRightHand(poseStack, buffer, combinedLight, player);
        }
        else
        {
            renderer.renderLeftHand(poseStack, buffer, combinedLight, player);
        }
    }

    public static RenderType getRenderType(ItemStack stack, boolean entity)
    {
        Item item = stack.getItem();
        if(item instanceof BlockItem)
        {
            Block block = ((BlockItem) item).getBlock();
            return ItemBlockRenderTypes.getRenderType(block.defaultBlockState(), !entity);
        }
        return RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
    }

    public static final float BEAM_ALPHA = 0.7F;

    public static void renderBeam(PoseStack poseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation,
                                  float pPartialTick, float pTextureScale, long gameTime, float pYOffset, float pHeight,
                                  int rgb, float alpha, float pBeamRadius, float pGlowRadius) {
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        var maxY = pYOffset + pHeight;
        poseStack.pushPose();

        float f = (float) Math.floorMod(gameTime, 40) + pPartialTick;
        float f1 = pHeight < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        poseStack.pushPose();

        float minX = -pGlowRadius;
        float maxX = -pGlowRadius;
        float minZ = -pGlowRadius;
        float maxZ = -pBeamRadius;
        float f12 = -pBeamRadius;
        float v = -1.0F + f2;
        float u = pHeight * pTextureScale * (BEAM_ALPHA / pBeamRadius) + v;

        var vertexConsumer = pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, false));

        renderPart(poseStack, vertexConsumer, r, g, b, 1.0F,
                pYOffset, maxY,
                0.0F, pBeamRadius,
                pBeamRadius, 0.0F,
                maxZ, 0.0F,
                0.0F, f12,
                u, v);

        poseStack.popPose();

        maxZ = -pGlowRadius;
        v = -1.0F + f2;
        u = pHeight * pTextureScale + v;

        renderPart(poseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)),
                r, g, b, BEAM_ALPHA,
                pYOffset, maxY, minX, maxX, pGlowRadius, minZ, maxZ,
                pGlowRadius, pGlowRadius, pGlowRadius, u, v);

        poseStack.popPose();
    }

    public static void renderPart(PoseStack poseStack, VertexConsumer pConsumer,
                                  float red, float green, float blue, float alpha,
                                  float pMinY, float pMaxY,
                                  float minX, float maxX,
                                  float minZ, float maxZ,
                                  float pX2, float pZ2,
                                  float pX3, float pZ3,
                                  float u, float v) {
        var pose = poseStack.last();
        var matrix4f = pose.pose();
        var matrix3f = pose.normal();

        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minX, maxX, minZ, maxZ, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minZ, maxZ, pX3, pZ3, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX2, pZ2, minX, maxX, u, v);
    }

    public static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                  float pRed, float pGreen, float pBlue, float pAlpha,
                                  float pMinY, float pMaxY,
                                  float pMinX, float pMinZ,
                                  float pMaxX, float pMaxZ,

                                  float pMinV, float pMaxV) {
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, 1, pMinV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, 1, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, 0, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, 0, pMinV);
    }

    public static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                 float pRed, float pGreen, float pBlue, float pAlpha, float pY,
                                 float pX, float pZ, float pU, float pV) {
        pConsumer.vertex(pPose, pX, pY, pZ)
                .color(pRed, pGreen, pBlue, pAlpha)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(pNormal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}