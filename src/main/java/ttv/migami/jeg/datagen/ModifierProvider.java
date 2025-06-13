package ttv.migami.jeg.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.modifier.Modifier;
import ttv.migami.jeg.modifier.type.ExplosiveAmmoModifier;
import ttv.migami.jeg.modifier.type.IModifierEffect;
import ttv.migami.jeg.modifier.type.PotionEffectModifier;
import ttv.migami.jeg.modifier.type.StatModifier;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class ModifierProvider implements DataProvider {
    protected final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final Map<ResourceLocation, Modifier> groupMap = new HashMap<>();

    protected ModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "modifiers");
        this.registries = registries;
    }

    protected abstract void registerGroups();

    protected final void addGroup(ResourceLocation id, Modifier group) {
        this.groupMap.put(id, group);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.registries.thenCompose(provider -> {
            this.groupMap.clear();
            this.registerGroups();
            return CompletableFuture.allOf(this.groupMap.entrySet().stream().map(entry -> {
                ResourceLocation key = entry.getKey();
                Modifier group = entry.getValue();
                Path path = this.pathProvider.json(key);
                JsonObject object = toJson(group);
                return DataProvider.saveStable(cache, object, path);
            }).toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Modifier Groups: " + Reference.MOD_ID;
    }

    private JsonObject toJson(Modifier group) {
        JsonObject json = new JsonObject();
        json.addProperty("name", group.getName());
        json.addProperty("rarity", group.getRarity().name().toLowerCase());
        json.addProperty("chance", group.getChance());
        json.addProperty("color", group.getColor());

        JsonArray mods = new JsonArray();
        for (IModifierEffect effect : group.getModifiers()) {
            JsonObject modJson = new JsonObject();

            if (effect instanceof StatModifier statMod) {
                modJson.addProperty("type", "stat");
                modJson.addProperty("attribute", statMod.getStatType().name().toLowerCase());
                modJson.addProperty("amount", statMod.getValue());
            }

            else if (effect instanceof PotionEffectModifier potionMod) {
                modJson.addProperty("type", "potion");
                modJson.addProperty("effect", BuiltInRegistries.MOB_EFFECT.getKey(potionMod.getEffect()).toString());
                modJson.addProperty("duration", potionMod.getDuration());
                modJson.addProperty("amplifier", potionMod.getAmplifier());
            }

            else if (effect instanceof ExplosiveAmmoModifier) {
                modJson.addProperty("type", "explosive");
                modJson.addProperty("value", true);
            }

            mods.add(modJson);
        }
        json.add("modifiers", mods);

        return json;
    }
}