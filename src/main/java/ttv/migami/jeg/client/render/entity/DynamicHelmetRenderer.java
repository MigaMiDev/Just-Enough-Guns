package ttv.migami.jeg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import ttv.migami.jeg.entity.DynamicHelmet;

public class DynamicHelmetRenderer extends EntityRenderer<DynamicHelmet> {

    private final HumanoidModel<?> defaultModel;

    public DynamicHelmetRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.defaultModel = new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
    }

    @Override
    public void render(DynamicHelmet entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        ItemStack stack = entity.getStoredStackData();
        Item item = stack.getItem();

        poseStack.pushPose();
        poseStack.translate(0, 0, 0);
        poseStack.popPose();

        poseStack.pushPose();
        float rotation = (entity.tickCount + partialTick) * 20f;
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (45 + rotation * 0.5)));
        poseStack.mulPose(Axis.XP.rotationDegrees((float) (rotation * 1.5)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));

        if (!(item instanceof ArmorItem)) {
            Minecraft mc = Minecraft.getInstance();
            ItemRenderer itemRenderer = mc.getItemRenderer();

            poseStack.pushPose();

             poseStack.scale(0.65F, 0.65F, 0.65F);
             itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.HEAD,
                    light,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId()
            );

            poseStack.popPose();
        }
        else if (item instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == EquipmentSlot.HEAD) {
            poseStack.scale(1.5F, 1.5F, 1.5F);
            ModelPart model = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR);
            defaultModel.getHead().setPos(0, -10F, 0);

            defaultModel.head.visible = true;
            defaultModel.hat.visible = true;

            defaultModel.body.visible = false;
            defaultModel.leftArm.visible = false;
            defaultModel.rightArm.visible = false;
            defaultModel.leftLeg.visible = false;
            defaultModel.rightLeg.visible = false;

            ResourceLocation texture = getArmorTexture(armorItem);

            if (armorItem.getMaterial() == ArmorMaterials.LEATHER) {
                int color = 0xA06540;
                CompoundTag display = stack.getTagElement("display");
                if (display != null && display.contains("color", 99)) {
                    color = display.getInt("color");
                }

                float r = (color >> 16 & 255) / 255.0F;
                float g = (color >> 8 & 255) / 255.0F;
                float b = (color & 255) / 255.0F;

                defaultModel.renderToBuffer(
                        poseStack,
                        buffer.getBuffer(RenderType.armorCutoutNoCull(texture)),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        r, g, b, 1.0F
                );

                // Optionally render overlay (gray stitching)
                ResourceLocation overlay = new ResourceLocation("minecraft", "textures/models/armor/leather_layer_1_overlay.png");
                defaultModel.renderToBuffer(
                        poseStack,
                        buffer.getBuffer(RenderType.armorCutoutNoCull(overlay)),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F
                );
            } else {
                defaultModel.renderToBuffer(
                        poseStack,
                        buffer.getBuffer(RenderType.armorCutoutNoCull(texture)),
                        light,
                        OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F
                );
            }
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DynamicHelmet entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private ResourceLocation getArmorTexture(ArmorItem armorItem) {
        String materialName = armorItem.getMaterial().getName();
        String layer = "1";
        return new ResourceLocation("minecraft", "textures/models/armor/" + materialName + "_layer_" + layer + ".png");
    }
}

