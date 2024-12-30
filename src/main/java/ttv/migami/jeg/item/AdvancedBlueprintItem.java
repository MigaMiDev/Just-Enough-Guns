package ttv.migami.jeg.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class AdvancedBlueprintItem extends BlueprintItem {
    public AdvancedBlueprintItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flag) {

        if (stack.getTag() != null) {
            if (!stack.getTag().getString("Namespace").isEmpty() && !stack.getTag().getString("Path").isEmpty()) {
                tooltip.add(Component.translatable("info.jeg.blueprinted_gun").withStyle(ChatFormatting.GRAY)
                        .append(Component.translatable("item." + stack.getTag().getString("Namespace") + "." + stack.getTag().getString("Path")).withStyle(ChatFormatting.GOLD)));            }
        }

        tooltip.add(Component.translatable("info.jeg.tooltip_item" + "." + this.asItem()).withStyle(ChatFormatting.GRAY));
    }
}
