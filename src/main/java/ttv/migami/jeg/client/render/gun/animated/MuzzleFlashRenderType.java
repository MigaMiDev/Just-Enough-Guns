package ttv.migami.jeg.client.render.gun.animated;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.jeg.Reference;

public class MuzzleFlashRenderType extends RenderType {

    public MuzzleFlashRenderType(String name, VertexFormat vertexFormat, VertexFormat.Mode mode, int bufferSize, boolean hasCrumbling, boolean translucent, Runnable setupState, Runnable clearState) {
        super(name, vertexFormat, mode, bufferSize, hasCrumbling, translucent, setupState, clearState);
    }

    private static final RenderType MUZZLE_FLASH = create(Reference.MOD_ID + ":muzzle_flash",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            true,
            false,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash_1.png"), false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    public static RenderType getMuzzleFlash() {
        return MUZZLE_FLASH;
    }
}