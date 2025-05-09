package ttv.migami.jeg.modifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModifierRegistry {
    private static final Map<ResourceLocation, Modifier> GROUPS = new HashMap<>();

    public static void setGroups(Map<ResourceLocation, Modifier> groups) {
        GROUPS.clear();
        GROUPS.putAll(groups);
    }

    @Nullable
    public static Modifier get(ResourceLocation id) {
        return GROUPS.get(id);
    }

    public static Collection<Modifier> getAll() {
        return GROUPS.values();
    }

    @Nullable
    public static Modifier getRandom(RandomSource random) {
        float totalWeight = 0F;
        for (Modifier group : GROUPS.values()) {
            totalWeight += group.getChance();
        }

        if (totalWeight <= 0F) return null;

        float roll = random.nextFloat() * totalWeight;
        float cumulative = 0F;

        for (Modifier group : GROUPS.values()) {
            cumulative += group.getChance();
            if (roll <= cumulative) {
                return group;
            }
        }

        return null;
    }
}