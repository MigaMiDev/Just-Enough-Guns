// src/main/java/ttv/migami/jeg/client/ClientSideCache.java
package ttv.migami.jeg.client;

import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Collections;

public enum ClientSideCache {
    INSTANCE;

    private volatile List<ItemStack> creativeSamples = Collections.emptyList();

    public void setCreativeSamples(List<ItemStack> samples) {
        this.creativeSamples = List.copyOf(samples);
    }

    public List<ItemStack> getCreativeSamples() {
        return this.creativeSamples;
    }
}
