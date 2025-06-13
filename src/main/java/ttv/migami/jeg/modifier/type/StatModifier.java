package ttv.migami.jeg.modifier.type;

public class StatModifier implements IModifierEffect {
    private final StatType statType;
    private final double value;

    public StatModifier(StatType statType, double value) {
        this.statType = statType;
        this.value = value;
    }

    public StatType getStatType() {
        return statType;
    }

    public double getValue() {
        return value;
    }
}