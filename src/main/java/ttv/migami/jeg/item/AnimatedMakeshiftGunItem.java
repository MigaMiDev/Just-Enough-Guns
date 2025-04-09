package ttv.migami.jeg.item;

import net.minecraft.world.item.ItemStack;

public class AnimatedMakeshiftGunItem extends AnimatedGunItem {

    public AnimatedMakeshiftGunItem(Properties properties, String path) {
        super(properties, path);
    }

    public boolean isFoil(ItemStack ignored) {
        return false;
    }

}