package ttv.migami.jeg.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import ttv.migami.jeg.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ModTags
{
    public static Map<ResourceLocation, TagKey<Block>> blockTagCache = new HashMap<>();

    public static class Blocks
    {
        public static final TagKey<Block> FRAGILE = tag("fragile");
        public static final TagKey<Block> BEEHIVES = tag("beehives");
        public static final TagKey<Block> BOO_NESTS = getBlockTag("nests/beehives");

        // Use these for particles only
        public static final TagKey<Block> METAL = tag("metal");
        public static final TagKey<Block> STONE = tag("stone");
        public static final TagKey<Block> WOOD = tag("wood");
        public static final TagKey<Block> SQUISHY = tag("squishy");

        private static TagKey<Block> tag(String name)
        {
            return BlockTags.create(new ResourceLocation(Reference.MOD_ID, name));
        }
    }

    public static class Entities
    {
        public static final TagKey<EntityType<?>> NONE = tag("none");
        public static final TagKey<EntityType<?>> GUNNER = tag("gunner");
        public static final TagKey<EntityType<?>> HEAVY = tag("heavy");
        public static final TagKey<EntityType<?>> VERY_HEAVY = tag("very_heavy");
        public static final TagKey<EntityType<?>> UNDEAD = tag("undead");
        public static final TagKey<EntityType<?>> GHOST = tag("ghost");
        public static final TagKey<EntityType<?>> FIRE = tag("fire");

        public static TagKey<EntityType<?>> tag(String name)
        {
            return TagKey.create(Registries.ENTITY_TYPE,new ResourceLocation(Reference.MOD_ID, name));
        }
    }

    public static class Items
    {
        public static final TagKey<Item> AMMO = tag("ammo");

        public static TagKey<Item> tag(String name)
        {
            return TagKey.create(Registries.ITEM,new ResourceLocation(Reference.MOD_ID, name));
        }
    }

    public static TagKey<Block> getBlockTag(String name) {
        return getBlockTag(new ResourceLocation(Reference.MOD_ID, name));
    }

    public static TagKey<Block> getBlockTag(ResourceLocation resourceLocation) {
        if (!blockTagCache.containsKey(resourceLocation)) {
            blockTagCache.put(resourceLocation, BlockTags.create(resourceLocation));
        }
        return blockTagCache.get(resourceLocation);
    }
}
