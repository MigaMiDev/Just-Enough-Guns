package ttv.migami.jeg.util;

import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.item.GunItem;

/**
 * Author: NineZero
 */
public class GunCompositeStatHelper
{
	// This helper delivers composite stats derived from GunModifierHelper and GunEnchantmentHelper.
	
	public static int getCompositeRate(ItemStack weapon, Gun modifiedGun)
    {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }
	public static int getCompositeRate(ItemStack weapon) {
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
	}
	
	public static int getCompositeBaseRate(ItemStack weapon, Gun modifiedGun)
    {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }
	public static int getCompositeBaseRate(ItemStack weapon) {
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
		return GunModifierHelper.getModifiedRate(weapon, a);
	}
	
	public static float getCompositeSpread(ItemStack weapon, Gun modifiedGun)
    {
        //float a = GunEnchantmentHelper.getSpread(weapon, modifiedGun);
		//return GunModifierHelper.getModifiedSpread(weapon, a);
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());
    }
	
	public static float getCompositeMinSpread(ItemStack weapon, Gun modifiedGun)
    {
        //float a = GunEnchantmentHelper.getMinSpread(weapon, modifiedGun);
		//return GunModifierHelper.getModifiedSpread(weapon, a);
        return GunModifierHelper.getModifiedSpread(weapon, 0);
    }
}