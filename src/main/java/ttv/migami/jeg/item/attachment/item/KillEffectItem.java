package ttv.migami.jeg.item.attachment.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import ttv.migami.jeg.item.ToolTipItem;

import javax.annotation.Nullable;
import java.util.List;

public class KillEffectItem extends ToolTipItem {

    public KillEffectItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(Component.translatable("info.jeg.tooltip_item.kill_effect").withStyle(ChatFormatting.GOLD));

    }

    public boolean isFoil(ItemStack stack) {
        return this.getRarity(stack).equals(Rarity.EPIC);
    }
}
