package ttv.migami.jeg.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import ttv.migami.jeg.init.ModEnchantments;

public class AnimatedBowItem extends AnimatedGunItem implements GeoAnimatable, GeoItem {

    public AnimatedBowItem(Properties properties, String path) {
        super(properties, path);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.FLAMING_ARROWS) {
            return true;
        }
        if (enchantment == ModEnchantments.OVER_CAPACITY.get() ||
                enchantment == ModEnchantments.TRIGGER_FINGER.get() ||
                enchantment == ModEnchantments.FIRE_STARTER.get()) {
            return false;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }
}
