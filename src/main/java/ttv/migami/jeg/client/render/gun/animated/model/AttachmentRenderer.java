package ttv.migami.jeg.client.render.gun.animated.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.DynamicTextureLocator;
import ttv.migami.jeg.client.render.gun.animated.AnimatedGunModel;
import ttv.migami.jeg.item.AnimatedGunItem;

public class AttachmentRenderer extends GeoRenderLayer<AnimatedGunItem> {
    protected ItemStack currentItemStack;

    private static ResourceLocation modelResource = null;
    private static ResourceLocation textureResource = null;
    private static ResourceLocation customModelResource = null;
    private static ResourceLocation customTextureResource = null;
    private static ItemStack itemStack = null;

    public AttachmentRenderer(GeoRenderer entityRendererIn) {
        super(entityRendererIn);
    }

    public void updateTexture(ResourceLocation resourceLocation) {
        customTextureResource = resourceLocation;
    }

    public void updateModel(ResourceLocation resourceLocation) {
        customModelResource = resourceLocation;
    }

    public void updateAttachment(ItemStack attachmentStack) {

        itemStack = attachmentStack;

        if (!(attachmentStack.getItem() instanceof SwordItem))
        {
            modelResource = new ResourceLocation(Reference.MOD_ID, "geo/item/attachment/" + attachmentStack.getItem() + ".geo.json");
            textureResource = new ResourceLocation(Reference.MOD_ID, "textures/animated/attachment/" + attachmentStack.getItem() + ".png");
        }
        else
        {
            if (attachmentStack.getItem() != Items.WOODEN_SWORD &&
                    attachmentStack.getItem() != Items.STONE_SWORD &&
                    attachmentStack.getItem() != Items.IRON_SWORD &&
                    attachmentStack.getItem() != Items.GOLDEN_SWORD &&
                    attachmentStack.getItem() != Items.DIAMOND_SWORD &&
                    attachmentStack.getItem() != Items.NETHERITE_SWORD)
            {
                modelResource = new ResourceLocation(Reference.MOD_ID, "geo/item/attachment/modded_sword.geo.json");
                textureResource = DynamicTextureLocator.getItemTexture(attachmentStack.getItem());
            }
            else
            {
                modelResource = new ResourceLocation(Reference.MOD_ID, "geo/item/attachment/" + attachmentStack.getItem() + ".geo.json");
                textureResource = new ResourceLocation(Reference.MOD_ID, "textures/animated/attachment/" + attachmentStack.getItem() + ".png");
            }
        }
    }

    @Override
    public void renderForBone(PoseStack poseStack, AnimatedGunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ResourceLocation focusModelResource = customModelResource != null ? customModelResource : modelResource;
        ResourceLocation focusTextureResource = customTextureResource != null ? customTextureResource : textureResource;

        if (focusModelResource != null && focusTextureResource != null && itemStack != null) {
            AnimatedGunModel focusModel = new AnimatedGunModel(focusModelResource);
            RenderType focusRenderLayer = RenderType.entityTranslucent(focusTextureResource);

            if (bone.getName().matches("attachment_bone") && !bone.getName().matches(itemStack.getItem().toString())) {
                getRenderer().reRender(
                        focusModel.getBakedModel(focusModelResource),
                        poseStack,
                        bufferSource,
                        animatable,
                        focusRenderLayer,
                        bufferSource.getBuffer(focusRenderLayer),
                        partialTick,
                        packedLight,
                        packedOverlay,
                        1, 1, 1, 1
                );
            }
        }
    }
}