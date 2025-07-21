package ttv.migami.jeg.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DyeUtils {

    public static boolean hasDye(ItemStack parent) {
        CompoundTag root = parent.getTag();
        if (root == null) return false;

        CompoundTag cosmetics = root.getCompound("Cosmetics");
        if (!cosmetics.contains("Dye", CompoundTag.TAG_COMPOUND)) return false;

        CompoundTag dyeTag = cosmetics.getCompound("Dye");

        ItemStack dyeStack = ItemStack.of(dyeTag);
        return dyeStack.getItem() instanceof DyeItem;
    }

    /**
     * Reads the dye stored under Cosmetics.Dye and returns its colour
     * as an opaque 0xRRGGBB integer.  
     * If nothing valid is found, returns white (0xFFFFFF).
     */
    public static int getStoredDyeRGB(ItemStack parent) {
        CompoundTag root = parent.getTag();
        if (root == null) return 0xFFFFFF;

        CompoundTag cosmetics = root.getCompound("Cosmetics");
        if (!cosmetics.contains("Dye", CompoundTag.TAG_COMPOUND)) return 0xFFFFFF;

        CompoundTag dyeTag = cosmetics.getCompound("Dye");

        ItemStack dyeStack = ItemStack.of(dyeTag);
        if (dyeStack.getItem() instanceof DyeItem dyeItem) {
            DyeColor dyeColor = dyeItem.getDyeColor();
            return dyeColor.getFireworkColor();
        }

        String id = dyeTag.getString("id");
        if (!id.isEmpty()) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
            if (item instanceof DyeItem dyeItem2) {
                return dyeItem2.getDyeColor().getFireworkColor();
            }
        }

        return 0xFFFFFF;
    }

    public static int red(int rgb)   { return (rgb >> 16) & 0xFF; }
    public static int green(int rgb) { return (rgb >>  8) & 0xFF; }
    public static int blue(int rgb)  { return  rgb        & 0xFF; }

    public static float[] asUnitFloats(int rgb) {
        return new float[] { red(rgb)/255f, green(rgb)/255f, blue(rgb)/255f };
    }
}