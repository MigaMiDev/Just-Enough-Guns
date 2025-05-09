package ttv.migami.jeg.modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ModifierHelper {
    private static final Random RANDOM = new Random();

    public static Modifier getRandomGroup() {
        List<Modifier> groups = new ArrayList<>(ModifierRegistry.getAll());

        Collections.shuffle(groups, RANDOM);

        float totalWeight = 0F;
        for (Modifier group : groups) {
            totalWeight += group.getChance();
        }

        float randomValue = RANDOM.nextFloat() * totalWeight;
        float cumulativeWeight = 0F;

        for (Modifier group : groups) {
            cumulativeWeight += group.getChance();
            if (randomValue <= cumulativeWeight) {
                return group;
            }
        }

        return groups.stream().findFirst().orElse(null);
    }

    public static Modifier getGroupByName(String name) {
        for (Modifier group : ModifierRegistry.getAll()) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }
}