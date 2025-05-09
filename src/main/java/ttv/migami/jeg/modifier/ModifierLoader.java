package ttv.migami.jeg.modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class ModifierLoader extends SimplePreparableReloadListener<Map<ResourceLocation, Modifier>>  {
    private static final String DATA_FOLDER = "modifiers"; // path in: data/yourmodid/modifiers/*.json

    private final Gson gson;

    public ModifierLoader() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Modifier.class, new ModifierDeserializer())
            .create();
    }

    @Override
    protected Map<ResourceLocation, Modifier> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Modifier> map = new HashMap<>();

        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("modifiers", (fileName) -> fileName.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation id = entry.getKey();
            try (Reader reader = new InputStreamReader(entry.getValue().open())) {
                Modifier group = gson.fromJson(reader, Modifier.class);
                map.put(id, group);
            } catch (Exception e) {
                System.err.println("Failed to load modifier group " + id + ": " + e.getMessage());
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, Modifier> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        ModifierRegistry.setGroups(map);
        System.out.println("Loaded " + map.size() + " modifier groups");
    }
}