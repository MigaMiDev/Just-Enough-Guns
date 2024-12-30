package ttv.migami.jeg.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Author: MrCrayfish
 */
public class AtlanticShooterEnchantment extends GunEnchantment
{
    public AtlanticShooterEnchantment()
    {
        super(Rarity.VERY_RARE, EnchantmentTypes.GUN, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, Type.PROJECTILE);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isCurse() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel()
    {
        return 1;
    }

    @Override
    public int getMinCost(int level)
    {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level)
    {
        return this.getMinCost(level) + 10;
    }
}
