package ttv.migami.jeg.modifier;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Rarity;
import ttv.migami.jeg.modifier.type.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ModifierDeserializer implements JsonDeserializer<Modifier> {
    @Override
    public Modifier deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String name = json.get("name").getAsString();
        Rarity rarity = Rarity.valueOf(json.get("rarity").getAsString().toUpperCase());
        float chance = json.get("chance").getAsFloat();
        int color = json.get("color").getAsInt();

        List<IModifierEffect> effects = new ArrayList<>();
        for (JsonElement e : json.getAsJsonArray("modifiers")) {
            JsonObject obj = e.getAsJsonObject();
            String typeKey = obj.get("type").getAsString().toLowerCase();

            switch (typeKey) {
                case "stat" -> {
                    StatType stat = StatType.valueOf(obj.get("attribute").getAsString().toUpperCase());
                    double value = obj.get("amount").getAsDouble();
                    effects.add(new StatModifier(stat, value));
                }
                case "potion_effect" -> {
                    String effectName = obj.get("effect").getAsString();
                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(effectName));
                    if (effect == null) throw new JsonParseException("Unknown MobEffect: " + effectName);
                    int duration = obj.get("duration").getAsInt();
                    int amplifier = obj.get("amplifier").getAsInt();
                    effects.add(new PotionEffectModifier(effect, duration, amplifier));
                }
                case "explosive_ammo" -> {
                    effects.add(new ExplosiveAmmoModifier());
                }
                default -> throw new JsonParseException("Unknown modifier type: " + typeKey);
            }
        }

        return new Modifier(name, rarity, chance, color, effects.toArray(new IModifierEffect[0]));
    }
}