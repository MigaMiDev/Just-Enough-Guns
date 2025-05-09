package ttv.migami.jeg.modifier;

import com.google.gson.*;
import net.minecraft.world.item.Rarity;

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

        List<StatModifier> modifiers = new ArrayList<>();
        for (JsonElement e : json.getAsJsonArray("modifiers")) {
            JsonObject obj = e.getAsJsonObject();
            String stat = obj.get("attribute").getAsString();
            float value = obj.get("amount").getAsFloat();
            modifiers.add(new StatModifier(stat, value));
        }

        return new Modifier(name, rarity, chance, color, modifiers.toArray(new StatModifier[0]));
    }
}