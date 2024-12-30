package ttv.migami.jeg.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.client.SpecialModels;
import ttv.migami.jeg.client.render.gun.IOverrideModel;
import ttv.migami.jeg.client.util.RenderUtil;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.item.attachment.IAttachment;

/**
 * Since we want to have an animation for the charging handle, we will be overriding the standard model rendering.
 * This also allows us to replace the model for the different stocks.
 */
public class ServiceRifleModel implements IOverrideModel {

    @SuppressWarnings("resource")
    @Override
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, LivingEntity entity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {

        //Renders the static parts of the model.
        RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_MAIN.getModel(), stack, matrixStack, buffer, light, overlay);

        //Renders the iron sights if no scope is attached.
        if ((Gun.getScope(stack) == null))
            RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_IRON_SIGHT.getModel(), stack, matrixStack, buffer, light, overlay);

        if (Gun.hasAttachmentEquipped(stack, IAttachment.Type.STOCK)) {

            if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.TACTICAL_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_STOCK_TACTICAL.getModel(), stack, matrixStack, buffer, light, overlay);
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_HANDGUARD_TACTICAL.getModel(), stack, matrixStack, buffer, light, overlay);
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_RAILING.getModel(), stack, matrixStack, buffer, light, overlay);
                if ((Gun.getScope(stack) == null))
                    RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_IRON_SIGHTS_MODIFIED.getModel(), stack, matrixStack, buffer, light, overlay);
            }
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.LIGHT_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_STOCK_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_HANDGUARD_LIGHT.getModel(), stack, matrixStack, buffer, light, overlay);
                if ((Gun.getScope(stack) == null))
                    RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_IRON_SIGHTS_STOCK.getModel(), stack, matrixStack, buffer, light, overlay);
            }
            else if (Gun.getAttachment(IAttachment.Type.STOCK, stack).getItem() == ModItems.WEIGHTED_STOCK.get()) {
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_STOCK_WEIGHTED.getModel(), stack, matrixStack, buffer, light, overlay);
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_HANDGUARD_WEIGHTED.getModel(), stack, matrixStack, buffer, light, overlay);
                if ((Gun.getScope(stack) == null))
                    RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_IRON_SIGHTS_STOCK.getModel(), stack, matrixStack, buffer, light, overlay);
            }
        } else {
            RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_HANDGUARD_WEIGHTED.getModel(), stack, matrixStack, buffer, light, overlay);
            if ((Gun.getScope(stack) == null))
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_IRON_SIGHTS_STOCK.getModel(), stack, matrixStack, buffer, light, overlay);
        }

        if ((Gun.hasAttachmentEquipped(stack, IAttachment.Type.MAGAZINE)))
        {
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.EXTENDED_MAG.get())
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_MAGAZINE_EXTENDED.getModel(), stack, matrixStack, buffer, light, overlay);
            if (Gun.getAttachment(IAttachment.Type.MAGAZINE, stack).getItem() == ModItems.DRUM_MAG.get())
                RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_MAGAZINE_DRUM.getModel(), stack, matrixStack, buffer, light, overlay);
        }
        else
            RenderUtil.renderModel(SpecialModels.SERVICE_RIFLE_MAGAZINE_DEFAULT.getModel(), stack, matrixStack, buffer, light, overlay);

    }

}