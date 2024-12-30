package ttv.migami.jeg.client.render.gun.animated;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.jeg.Reference;

public class AttachmentRenderType extends RenderType
{
    public AttachmentRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, vertexFormat, drawMode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType getAttachment(int attachmentType, int attachmentID)
    {
        String texturePath;
        String layerName;

        switch (attachmentType)
        {
            case 1 -> {
                texturePath = "textures/misc/attach_si_" + attachmentID + ".png";
                layerName = Reference.MOD_ID + ":1_" + attachmentID;
            }
            case 2 -> {
                texturePath = "textures/misc/attach_gr_" + attachmentID + ".png";
                layerName = Reference.MOD_ID + ":2_" + attachmentID;
            }
            case 3 -> {
                texturePath = "textures/misc/attach_mz_" + attachmentID + ".png";
                layerName = Reference.MOD_ID + ":3_" + attachmentID;
            }
            default -> {
                texturePath = "textures/misc/attach_si_" + attachmentID + ".png";
                return RenderType.entityTranslucent(new ResourceLocation(Reference.MOD_ID, texturePath));
            }
        }

        ResourceLocation textureLocation = new ResourceLocation(Reference.MOD_ID, texturePath);

        CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(textureLocation, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false);

        return RenderType.create(layerName, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, compositeState);
    }
}
