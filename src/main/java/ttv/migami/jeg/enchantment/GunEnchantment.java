package ttv.migami.jeg.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Author: MrCrayfish
 */
public abstract class GunEnchantment extends Enchantment
{
    private final Type type;

    protected GunEnchantment(Rarity rarityIn, EnchantmentCategory typeIn, EquipmentSlot[] slots, Type type)
    {
        super(rarityIn, typeIn, slots);
        this.type = type;
    }

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        if (enchantment instanceof GunEnchantment) {
            GunEnchantment gunEnchantment = (GunEnchantment) enchantment;

            if (this.type == Type.BOSS && gunEnchantment.type == Type.BOSS) {
                return true;
            }

            return gunEnchantment.type != this.type;
        }

        return super.checkCompatibility(enchantment);
    }

    public enum Type
    {
        WEAPON, AMMO, PROJECTILE, BOSS
    }
}
