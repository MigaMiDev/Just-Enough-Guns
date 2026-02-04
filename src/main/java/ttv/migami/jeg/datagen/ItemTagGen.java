package ttv.migami.jeg.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.init.ModTags;
import ttv.migami.jeg.init.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ItemTagGen extends ItemTagsProvider
{
    public static Map<ResourceLocation, TagKey<Block>> blockTagCache = new HashMap<>();
    public static Map<ResourceLocation, TagKey<Item>> itemTagCache = new HashMap<>();

    public ItemTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, blockTagProvider, Reference.MOD_ID, existingFileHelper);
    }

    public static TagKey<Block> getBlockTag(ResourceLocation resourceLocation) {
        if (!blockTagCache.containsKey(resourceLocation)) {
            blockTagCache.put(resourceLocation, BlockTags.create(resourceLocation));
        }
        return blockTagCache.get(resourceLocation);
    }

    public static TagKey<Item> getItemTag(ResourceLocation resourceLocation) {
        if (!itemTagCache.containsKey(resourceLocation)) {
            itemTagCache.put(resourceLocation, ItemTags.create(resourceLocation));
        }
        return itemTagCache.get(resourceLocation);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        this.tag(ModTags.Items.AMMO)
                .add(ModItems.HANDMADE_SHELL.get())
                .add(ModItems.PISTOL_AMMO.get())
                .add(ModItems.RIFLE_AMMO.get())
                .add(ModItems.SHOTGUN_SHELL.get())
                .add(ModItems.BLAZE_ROUND.get())
                .add(ModItems.SPECTRE_ROUND.get())
                .add(ModItems.FLARE.get())
                .add(ModItems.GRENADE.get())
                .add(ModItems.ROCKET_LAUNCHER.get())
                .add(ModItems.EXPLOSIVE_CHARGE.get())
                .add(ModItems.POCKET_BUBBLE.get())
                .add(ModItems.STUN_GRENADE.get())
                .add(ModItems.WATER_BOMB.get())
                .add(Items.ARROW)
                .add(Items.TIPPED_ARROW)
                .add(Items.SPECTRAL_ARROW)
                .add(Items.ECHO_SHARD)
                .add(Items.SCULK_CATALYST)
                .add(Items.WATER_BUCKET)
                .add(Items.LAVA_BUCKET)
                .add(Items.BUCKET);
    }
}
