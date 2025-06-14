package ttv.migami.jeg.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.PathPackResources;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.common.NetworkGunManager;

import java.nio.file.Path;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ConfigPackLoader {
    private static final String PACK_ID = "jeg_cfg_config_resources";
    private static final Path ROOT = NetworkGunManager.CONFIG_PACK_DIR;

    private static final Pack.ResourcesSupplier CONFIG_RESOURCES = (id) -> {
        return new PathPackResources(id, false, ROOT);
    };

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent e) {
        if (e.getPackType() != PackType.CLIENT_RESOURCES) return;

        JustEnoughGuns.LOGGER.error("Trying to register a pack with the ID {} in the Root {}", PACK_ID, ROOT);

        Pack pack = Pack.readMetaAndCreate(
                PACK_ID,
                Component.literal("Just Enough Guns – config resources"),
                /*alwaysEnabled=*/true,
                CONFIG_RESOURCES,
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                PackSource.BUILT_IN
        );

        if (pack != null) {
            e.addRepositorySource(finder -> finder.accept(pack));
            JustEnoughGuns.LOGGER.error("Registered a Resource Pack for {}", PACK_ID);
        } else JustEnoughGuns.LOGGER.error("Could not register the Resource Pack for {}", PACK_ID);
    }
}
