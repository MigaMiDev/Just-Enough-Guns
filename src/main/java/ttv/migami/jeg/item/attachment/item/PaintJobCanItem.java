package ttv.migami.jeg.item.attachment.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.item.ToolTipItem;

import javax.annotation.Nullable;
import java.util.List;

public class PaintJobCanItem extends ToolTipItem {
    private final String paintJob;

    public PaintJobCanItem(Properties pProperties, String paintJob) {
        super(pProperties);
        this.paintJob = paintJob;
    }

    public String getPaintJob() {
        return this.paintJob;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        if (stack.is(ModItems.TOY_SPRAY_CAN.get())) {
            tooltip.add(Component.translatable("info.jeg.tooltip_item.toy_paint_job_can").withStyle(ChatFormatting.GOLD));
        } else if (stack.is(ModItems.CLASSIC_SPRAY_CAN.get())) {
            tooltip.add(Component.translatable("info.jeg.tooltip_item.classic_paint_job_can").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.translatable("info.jeg.tooltip_item.paint_job_can").withStyle(ChatFormatting.GOLD));
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("info.jeg.tooltip_item.paint_job_can.disclaimer").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.jeg.tooltip_item.paint_job_can.disclaimer_2").withStyle(ChatFormatting.GRAY));

    }

    public boolean isFoil(ItemStack stack) {
        return this.getRarity(stack).equals(Rarity.EPIC);
    }
}
