package ttv.migami.jeg.faction.jeg;

import java.util.Arrays;

public class FactionDataManager {
    private static final FactionData TERROR_ARMADA_WAVE_1 = new FactionData(
            "terror_armada_wave_1",
            6,
            Arrays.asList("minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk"),
            Arrays.asList("jeg:custom_smg", "jeg:waterpipe_shotgun"),
            Arrays.asList("jeg:semi_auto_rifle"),
            Arrays.asList("jeg:double_barrel_shotgun")
    );

    private static final FactionData TERROR_ARMADA_WAVE_2 = new FactionData(
            "terror_armada_wave_2",
            8,
            Arrays.asList("minecraft:zombie", "minecraft:skeleton", "minecraft:stray"),
            Arrays.asList("jeg:combat_rifle", "jeg:repeating_shotgun"),
            Arrays.asList("jeg:assault_rifle", "jeg:infantry_rifle"),
            Arrays.asList("jeg:service_rifle")
    );

    private static final FactionData TERROR_ARMADA_WAVE_3 = new FactionData(
            "terror_armada_wave_3",
            8,
            Arrays.asList("minecraft:zombie", "minecraft:zombie_villager", "minecraft:stray", "minecraft:skeleton"),
            Arrays.asList("jeg:assault_rifle", "jeg:repeating_shotgun"),
            Arrays.asList("jeg:combat_rifle"),
            Arrays.asList("jeg:infantry_rifle", "jeg:service_rifle", "jeg:bolt_action_rifle", "jeg:grenade_launcher")
    );

    public static FactionData getTerrorArmadaWave1() {
        return TERROR_ARMADA_WAVE_1;
    }

    public static FactionData getTerrorArmadaWave2() {
        return TERROR_ARMADA_WAVE_2;
    }

    public static FactionData getTerrorArmadaWave3() {
        return TERROR_ARMADA_WAVE_3;
    }
}