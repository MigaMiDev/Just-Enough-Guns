package ttv.migami.jeg.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Author: MrCrayfish
 */
public class OverCapacityEnchantment extends GunEnchantment
{
    public OverCapacityEnchantment()
    {
        super(Rarity.RARE, EnchantmentTypes.GUN, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, Type.WEAPON);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public int getMaxLevel()
    {
        return 2;
    }

    @Override
    public int getMinCost(int level)
    {
        return 5 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level)
    {
        return super.getMinCost(level) + 50;
    }
}
