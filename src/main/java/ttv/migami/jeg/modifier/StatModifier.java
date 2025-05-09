package ttv.migami.jeg.modifier;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class StatModifier {
    private final AttributeModifierType type;
    private final double value;

    public StatModifier(String type, double value) {
        this.type = AttributeModifierType.valueOf(type.toUpperCase());
        this.value = value;
    }

    public AttributeModifierType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    private void applyModifier(ItemStack stack, StatModifier modifier) {
        UUID uuid = UUID.nameUUIDFromBytes((modifier.getType().name() + stack.hashCode()).getBytes());

        switch (modifier.getType()) {
            case DAMAGE -> {
            }
            case FIRE_RATE -> {
            }
            case KNOCKBACK -> {
            }
            case PROJECTILE_VELOCITY -> {
            }
            case CRIT_CHANCE -> {
            }
        }
    }
}