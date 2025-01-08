package ttv.migami.jeg.init;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.blockentity.BlueprintWorkbenchBlockEntity;
import ttv.migami.jeg.blockentity.GunmetalWorkbenchBlockEntity;
import ttv.migami.jeg.blockentity.GunniteWorkbenchBlockEntity;
import ttv.migami.jeg.blockentity.ScrapWorkbenchBlockEntity;
import ttv.migami.jeg.common.container.*;
import ttv.migami.jeg.common.container.recycler.RecyclerMenu;

/**
 * Author: MrCrayfish
 */
public class ModContainers {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Reference.MOD_ID);

    public static final RegistryObject<MenuType<ScrapWorkbenchContainer>> SCRAP_WORKBENCH = register("scrap_workbench", (IContainerFactory<ScrapWorkbenchContainer>) (windowId, playerInventory, data) -> {
        ScrapWorkbenchBlockEntity scrap_workbench = (ScrapWorkbenchBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new ScrapWorkbenchContainer(windowId, playerInventory, scrap_workbench);
    });
    public static final RegistryObject<MenuType<GunmetalWorkbenchContainer>> GUNMETAL_WORKBENCH = register("gunmetal_workbench", (IContainerFactory<GunmetalWorkbenchContainer>) (windowId, playerInventory, data) -> {
        GunmetalWorkbenchBlockEntity gunmetal_workbench = (GunmetalWorkbenchBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new GunmetalWorkbenchContainer(windowId, playerInventory, gunmetal_workbench);
    });
    public static final RegistryObject<MenuType<GunniteWorkbenchContainer>> GUNNITE_WORKBENCH = register("gunnite_workbench", (IContainerFactory<GunniteWorkbenchContainer>) (windowId, playerInventory, data) -> {
        GunniteWorkbenchBlockEntity gunnite_workbench = (GunniteWorkbenchBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new GunniteWorkbenchContainer(windowId, playerInventory, gunnite_workbench);
    });
    public static final RegistryObject<MenuType<BlueprintWorkbenchContainer>> BLUEPRINT_WORKBENCH = register("blueprint_workbench", (IContainerFactory<BlueprintWorkbenchContainer>) (windowId, playerInventory, data) -> {
        BlueprintWorkbenchBlockEntity blueprint_workbench = (BlueprintWorkbenchBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos());
        return new BlueprintWorkbenchContainer(windowId, playerInventory, blueprint_workbench);
    });

    public static final RegistryObject<MenuType<SchematicStationMenu>> SCHEMATIC_STATION = register("schematic_station", SchematicStationMenu::new);

    public static final RegistryObject<MenuType<AttachmentContainer>> ATTACHMENTS = register("attachments", AttachmentContainer::new);

    public static final RegistryObject<MenuType<RecyclerMenu>> RECYCLER = register("recycler", RecyclerMenu::new);

    public static final RegistryObject<MenuType<AmmoBoxMenu>> AMMO_BOX = register("ammo_box", AmmoBoxMenu::new);

    /*public static final RegistryObject<MenuType<BasicTurretContainer>> BASIC_TURRET_CONTAINER =
            REGISTER.register("basic_turret_container", () -> IForgeMenuType.create(BasicTurretContainer::new));*/

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, MenuType.MenuSupplier<T> factory) {
        return REGISTER.register(id, () -> new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS));
    }
}
