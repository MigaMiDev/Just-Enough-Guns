package ttv.migami.jeg.client;

import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.item.GunItem;

import java.util.Collections;
import java.util.List;

public enum ClientSideCache {
    INSTANCE;

    private volatile List<ItemStack> creativeSamples = Collections.emptyList();

    public void setCreativeSamples(List<ItemStack> samples) {
        this.creativeSamples = List.copyOf(samples);
    }

    public List<ItemStack> getCreativeSamples() {
        maxAmmoSamples();
        return this.creativeSamples;
    }

    public void maxAmmoSamples() {
        for (ItemStack stack : this.creativeSamples) {
            if (!stack.isEmpty() && stack.getItem() instanceof GunItem gunItem) {
                Gun gun = gunItem.getModifiedGun(stack);
                stack.getOrCreateTag().putInt("AmmoCount", gun.getReloads().getMaxAmmo());
            }
        }
    }
}
