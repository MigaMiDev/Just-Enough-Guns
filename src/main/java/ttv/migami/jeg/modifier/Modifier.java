package ttv.migami.jeg.modifier;

import net.minecraft.world.item.Rarity;
import ttv.migami.jeg.modifier.type.IModifierEffect;

import java.util.Arrays;
import java.util.List;

public class Modifier {
    private final String name;
    private final List<IModifierEffect> effects;
    private final Rarity rarity;
    private final float chance;
    private final int color;

    public Modifier(String name, Rarity rarity, float chance, int color, IModifierEffect... effects) {
        this.name = name;
        this.rarity = rarity;
        this.chance = chance;
        this.color = color;
        this.effects = Arrays.asList(effects);
    }

    public List<IModifierEffect> getModifiers() {
        return effects;
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public float getChance() {
        return chance;
    }

    public int getColor() {
        return color;
    }
}