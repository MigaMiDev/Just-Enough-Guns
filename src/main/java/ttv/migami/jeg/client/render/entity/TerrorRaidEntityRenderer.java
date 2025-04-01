package ttv.migami.jeg.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.jeg.faction.raid.TerrorRaidEntity;

public class TerrorRaidEntityRenderer extends EntityRenderer<TerrorRaidEntity> {

    public TerrorRaidEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TerrorRaidEntity entity) {
        return null;
    }

    @Override
    public void render(TerrorRaidEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }
}