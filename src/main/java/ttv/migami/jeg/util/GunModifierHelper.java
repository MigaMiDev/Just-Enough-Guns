package ttv.migami.jeg.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.interfaces.IGunModifier;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.IAttachment;

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper
{
    private static final IGunModifier[] EMPTY = {};

    private static IGunModifier[] getModifiers(ItemStack weapon, IAttachment.Type type)
    {
        ItemStack stack = Gun.getAttachment(type, weapon);
        if(!stack.isEmpty() && stack.getItem() instanceof IAttachment<?> attachment)
        {
            return attachment.getProperties().getModifiers();
        }
        return EMPTY;
    }

    public static int getModifiedProjectileLife(ItemStack weapon, int life)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                life = modifier.modifyProjectileLife(life);
            }
        }
        return life;
    }

    public static double getModifiedProjectileGravity(ItemStack weapon, double gravity)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                gravity = modifier.modifyProjectileGravity(gravity);
            }
        }
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                gravity += modifier.additionalProjectileGravity();
            }
        }
        return gravity;
    }

    public static float getModifiedSpread(ItemStack weapon, float spread)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                spread = modifier.modifyProjectileSpread(spread);
            }
        }
        return spread;
    }

    public static double getModifiedProjectileSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyProjectileSpeed(speed);
            }
        }
        return speed;
    }

    public static float getFireSoundVolume(ItemStack weapon)
    {
        float volume = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                volume = modifier.modifyFireSoundVolume(volume);
            }
        }
        // Used to be 16.0F...
        // Update: Now I see why it was 16, my bad hehe
        return Mth.clamp(volume, 0.0F, Config.COMMON.world.playerGunfireVolume.get() - 0.5F);
    }

    public static double getMuzzleFlashScale(ItemStack weapon, double scale)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                scale = modifier.modifyMuzzleFlashScale(scale);
            }
        }
        return scale;
    }

    public static float getKickReduction(ItemStack weapon)
    {
        float kickReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                kickReduction *= Mth.clamp(modifier.kickModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - kickReduction;
    }

    public static float getRecoilModifier(ItemStack weapon)
    {
        float recoilReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                recoilReduction *= Mth.clamp(modifier.recoilModifier(), 0.0F, 1.0F);
            }
        }
        return 1.0F - recoilReduction;
    }

    public static boolean isSilencedFire(ItemStack weapon)
    {
        if (weapon.getItem() instanceof GunItem gunItem) {
            Gun gun = gunItem.getModifiedGun(weapon);
            if (gun.getGeneral().isSilenced()) {
                return true;
            }
        }
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                if(modifier.silencedFire())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getModifiedFireSoundRadius(ItemStack weapon, double radius)
    {
        double minRadius = radius;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                double newRadius = modifier.modifyFireSoundRadius(radius);
                if(newRadius < minRadius)
                {
                    minRadius = newRadius;
                }
            }
        }
        return Mth.clamp(minRadius, 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(ItemStack weapon)
    {
        float additionalDamage = 0.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                additionalDamage += modifier.additionalDamage();
            }
        }
        return additionalDamage;
    }

    public static float getModifiedProjectileDamage(ItemStack weapon, float damage)
    {
        float finalDamage = damage;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                finalDamage = modifier.modifyProjectileDamage(finalDamage);
            }
        }
        return finalDamage;
    }

    public static float getChargeDamage(ItemStack weapon, float damage, float chargeProgress) {
        if (!(weapon.getItem() instanceof GunItem gunItem)) {
            return damage;
        }

        Gun modifiedGun = gunItem.getModifiedGun(weapon);
        if (modifiedGun.getGeneral().getMaxHoldFire() <= 0) {
            return damage;
        }

        return damage * chargeProgress;
    }

    public static float getChargeSpeed(ItemStack weapon, float speed, float chargeProgress) {
        if (!(weapon.getItem() instanceof GunItem gunItem)) {
            return speed;
        }

        Gun modifiedGun = gunItem.getModifiedGun(weapon);
        if (modifiedGun.getGeneral().getMaxHoldFire() <= 0) {
            return speed;
        }

        return speed * chargeProgress;
    }

    public static float getModifiedDamage(ItemStack weapon, Gun modifiedGun, float damage)
    {
        float finalDamage = damage;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                finalDamage = modifier.modifyProjectileDamage(finalDamage);
            }
        }
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                finalDamage += modifier.additionalDamage();
            }
        }
        return finalDamage;
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyAimDownSightSpeed(speed);
            }
        }
        return Mth.clamp(speed, 0.01, Double.MAX_VALUE);
    }

    public static int getModifiedRate(ItemStack weapon, int rate)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                rate = modifier.modifyFireRate(rate);
            }
        }
        return Mth.clamp(rate, 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(ItemStack weapon)
    {
        float chance = 0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                chance += modifier.criticalChance();
            }
        }
        chance += GunEnchantmentHelper.getPuncturingChance(weapon);
        return Mth.clamp(chance, 0F, 1F);
    }

    public static int getModifiedAmmoCapacity(ItemStack weapon, Gun modifiedGun)
    {
        double percentage;
        int capacity = modifiedGun.getReloads().getMaxAmmo();
        if (Gun.getAttachment(IAttachment.Type.MAGAZINE, weapon).getItem() == ModItems.EXTENDED_MAG.get())
        {
            percentage = 0.5;
            int ammoIncrease = (int) (percentage * capacity);
            capacity += ammoIncrease;

            if (weapon.getItem() == ModItems.INFANTRY_RIFLE.get()) {
                capacity = 20;
            }
        }
        if (Gun.getAttachment(IAttachment.Type.MAGAZINE, weapon).getItem() == ModItems.DRUM_MAG.get())
        {
            percentage = 1.0;
            int ammoIncrease = (int) (percentage * capacity);
            capacity += ammoIncrease;

            if (weapon.getItem() == ModItems.INFANTRY_RIFLE.get()) {
                capacity = 40;
            }
        }
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.OVER_CAPACITY.get(), weapon);
        if(level > 0)
        {
            capacity += Math.max(level, (capacity / 4) * level);
        }

        return capacity;
    }

    public static float getSwordDamage(Player player) {
        float damage = 1;

        if (player.getMainHandItem().getItem() instanceof GunItem && Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem swordItem) {
            damage = swordItem.getDamage();

            ItemStack bayonet = Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem());
            damage = damage + bayonet.getEnchantmentLevel(Enchantments.SHARPNESS);
        }
        if (player.getMainHandItem().getItem() == ModItems.ATLANTEAN_SPEAR.get()) {
            damage = 8;
        }

        return damage;
    }

    public static int getSwordKnockBack(Player player) {
        int level = 0;

        if (player.getMainHandItem().getItem() instanceof GunItem && Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem) {
            ItemStack bayonet = Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem());
            level = level + bayonet.getEnchantmentLevel(Enchantments.KNOCKBACK);
        }

        return level;
    }

    public static int getSwordFireAspect(Player player) {
        int level = 0;

        if (player.getMainHandItem().getItem() instanceof GunItem && Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem) {
            ItemStack bayonet = Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem());
            level = level + bayonet.getEnchantmentLevel(Enchantments.FIRE_ASPECT);
        }

        return level;
    }

    public static int getSwordSweepingEdge(Player player) {
        int level = 0;

        if (player.getMainHandItem().getItem() instanceof GunItem && Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem()).getItem() instanceof SwordItem) {
            ItemStack bayonet = Gun.getAttachment(IAttachment.Type.BARREL, player.getMainHandItem());
            level = level + bayonet.getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
        }

        return level;
    }
}
