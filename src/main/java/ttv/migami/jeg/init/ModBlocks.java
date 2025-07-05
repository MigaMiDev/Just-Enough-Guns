package ttv.migami.jeg.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.block.*;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Author: MrCrayfish
 */
public class ModBlocks {

    private static final ToIntFunction<BlockState> light_Level_3 = BlockState -> 3;
    private static final ToIntFunction<BlockState> light_Level_7 = BlockState -> 7;

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

    public static final RegistryObject<Block> AMMO_BOX = register("ammo_box",
            () -> new AmmoBoxBlock(BlockBehaviour.Properties.copy(Blocks.CHEST)
                    .strength(0.5F)));

    /*public static final RegistryObject<Block> BASIC_TURRET = register("basic_turret",
            () -> new BasicTurretBlock(BlockBehaviour.Properties.copy(Blocks.EMERALD_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));*/

    public static final RegistryObject<Block> SCRAP_WORKBENCH = register("scrap_workbench",
            () -> new ScrapWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .strength(1.5F)));
    public static final RegistryObject<Block> GUNMETAL_WORKBENCH = register("gunmetal_workbench",
            () -> new GunmetalWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion()
                    .strength(3.0F)));
    public static final RegistryObject<Block> GUNNITE_WORKBENCH = register("gunnite_workbench",
            () -> new GunniteWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .strength(5.0F)));
    public static final RegistryObject<Block> RECYCLER = register("recycler",
            () -> new RecyclerBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final RegistryObject<Block> SCRAP_BIN = register("scrap_bin",
            () -> new ScrapBinBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)));
    public static final RegistryObject<Block> SCHEMATIC_STATION = register("schematic_station",
            () -> new SchematicStationBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));
    public static final RegistryObject<Block> BLUEPRINT_WORKBENCH = register("blueprint_workbench",
            () -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));

    public static final RegistryObject<Block> SCRAP_ORE = register("scrap_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final RegistryObject<Block> DEEPSLATE_SCRAP_ORE = register("deepslate_scrap_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(4.5F)));
    public static final RegistryObject<Block> SCRAP_BLOCK = register("scrap_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BARS)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F)));
    public static final RegistryObject<Block> GUNMETAL_BLOCK = register("gunmetal_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(4.0F)));
    public static final RegistryObject<Block> GUNNITE_BLOCK = register("gunnite_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)));
    public static final RegistryObject<Block> BRIMSTONE_ORE = register("brimstone_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DRIPSTONE_BLOCK)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F)
                    .lightLevel(light_Level_3)));
    public static final RegistryObject<Block> BLACKSTONE_BRIMSTONE_ORE = register("blackstone_brimstone_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLACKSTONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F)
                    .lightLevel(light_Level_3)));
    public static final RegistryObject<Block> BASALT_BRIMSTONE_ORE = register("basalt_brimstone_ore",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BASALT)
                    .requiresCorrectToolForDrops()
                    .strength(1.5F)
                    .lightLevel(light_Level_3)));

    public static final RegistryObject<Block> BOO_NEST = register("boo_nest",
            () -> new BooNest(Block.Properties.copy(Blocks.BEE_NEST)
                    .lightLevel(light_Level_7)));
    public static final RegistryObject<Block> BOOHIVE = register("boohive",
            () -> new BooNest(Block.Properties.copy(Blocks.BEEHIVE)
                    .lightLevel(light_Level_7)));

    public static final RegistryObject<Block> DYNAMIC_LIGHT = register("dynamic_light",
            DynamicLightBlock::new);
    public static final RegistryObject<Block> BRIGHT_DYNAMIC_LIGHT = register("bright_dynamic_light",
            BrightDynamicLightBlock::new);

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier) {
        return register(id, blockSupplier, block1 -> new BlockItem(block1, new Item.Properties()));
    }

    private static <T extends Block> RegistryObject<T> register(String id, Supplier<T> blockSupplier, @Nullable Function<T, BlockItem> supplier) {
        RegistryObject<T> registryObject = REGISTER.register(id, blockSupplier);
        if (supplier != null) {
            ModItems.REGISTER.register(id, () -> supplier.apply(registryObject.get()));
        }
        return registryObject;
    }
}
