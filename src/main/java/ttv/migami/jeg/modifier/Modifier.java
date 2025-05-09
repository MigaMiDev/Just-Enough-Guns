package ttv.migami.jeg.modifier;

import net.minecraft.world.item.Rarity;

import java.util.Arrays;
import java.util.List;

public class Modifier {
    private final String name;
    private final List<StatModifier> modifiers;
    private final Rarity rarity;
    private final float chance;
    private final int color;

    public Modifier(String name, Rarity rarity, float chance, int color, StatModifier... modifiers) {
        this.name = name;
        this.rarity = rarity;
        this.chance = chance;
        this.color = color;
        this.modifiers = Arrays.asList(modifiers);
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public List<StatModifier> getModifiers() {
        return modifiers;
    }

    public float getChance() {
        return chance;
    }

    public int getColor() {
        return color;
    }
}