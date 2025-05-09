package ttv.migami.jeg.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.modifier.Modifier;
import ttv.migami.jeg.modifier.StatModifier;

import java.util.concurrent.CompletableFuture;

public class ModifierGen extends ModifierProvider {
    public ModifierGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void registerGroups() {
        addGroup(new ResourceLocation(Reference.MOD_ID, "awful"),
            new Modifier("awful", Rarity.COMMON, 0.5f, 0xCCCCCC,
                new StatModifier("damage", 0.7)
            )
        );

        addGroup(new ResourceLocation(Reference.MOD_ID, "unreal"),
            new Modifier("unreal", Rarity.RARE, 0.2f, 0x00AAFF,
                new StatModifier("damage", 1.5)
            )
        );
    }
}