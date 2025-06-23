package ttv.migami.jeg.common.container.slot;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.container.AttachmentContainer;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.AnimatedMakeshiftGunItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.MakeshiftGunItem;
import ttv.migami.jeg.item.attachment.item.PaintJobCanItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.item.attachment.item.*;

/**
 * Author: MrCrayfish
 */
public class AttachmentSlot extends Slot
{
    private final AttachmentContainer container;
    private final ItemStack weapon;
    private final IAttachment.Type type;
    private final Player player;

    public AttachmentSlot(AttachmentContainer container, Container weaponInventory, ItemStack weapon, IAttachment.Type type, Player player, int index, int x, int y)
    {
        super(weaponInventory, index, x, y);
        this.container = container;
        this.weapon = weapon;
        this.type = type;
        this.player = player;
    }

    public IAttachment.Type getType()
    {
        return this.type;
    }

    @Override
    public boolean isActive()
    {
        if(!(this.weapon.getItem() instanceof GunItem))
        {
            return false;
        }
        GunItem item = (GunItem) this.weapon.getItem();
        Gun modifiedGun = item.getModifiedGun(this.weapon);

        if (this.type.equals(IAttachment.Type.PAINT_JOB) || this.type.equals(IAttachment.Type.DYE) ||this.type.equals(IAttachment.Type.KILL_EFECT)) {
            return true;
        }
        return modifiedGun.canAttachType(this.type);
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        if (!(this.weapon.getItem() instanceof GunItem))
        {
            return false;
        }
        GunItem item = (GunItem) this.weapon.getItem();
        Gun modifiedGun = item.getModifiedGun(this.weapon);

        if (!(stack.getItem() instanceof IAttachment ||
                stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof SpyglassItem ||
                stack.getItem() instanceof PaintJobCanItem ||
                stack.getItem() instanceof DyeItem ||
                stack.getItem() instanceof KillEffectItem))
        {
            return false;
        }

        IAttachment attachment;

        if (stack.getItem() instanceof SwordItem)
        {
            attachment = new PseudoBarrel(stack);
        }
        else if (stack.getItem() instanceof SpyglassItem)
        {
            attachment = new PseudoScope(stack);
        }
        else if (stack.getItem() instanceof PaintJobCanItem)
        {
            return this.type == IAttachment.Type.PAINT_JOB;
        }
        else if (stack.getItem() instanceof DyeItem)
        {
            return this.type == IAttachment.Type.DYE;
        }
        else if (stack.getItem() instanceof KillEffectItem)
        {
            return this.type == IAttachment.Type.KILL_EFECT;
        }
        else
        {
            attachment = (IAttachment) stack.getItem();
        }

        if (item instanceof MakeshiftGunItem || item instanceof AnimatedMakeshiftGunItem)
        {
            if (attachment instanceof MakeshiftStockItem ||
                    attachment instanceof BarrelItem ||
                    attachment instanceof ScopeItem ||
                    attachment instanceof UnderBarrelItem ||
                    attachment instanceof MagazineItem ||
                    attachment instanceof SpecialItem ||
                    attachment instanceof PseudoBarrel ||
                    attachment instanceof PseudoScope ||
                    attachment instanceof PaintJobCanItem ||
                    attachment instanceof DyeItem ||
                    attachment instanceof KillEffectItem)
            {
                return attachment.getType() == this.type && modifiedGun.canAttachType(this.type) && attachment.canAttachTo(this.weapon);
            }
            else return false;
        }
        else if (stack.getItem() instanceof MakeshiftStockItem)
        {
            return false;
        }
        return attachment.getType() == this.type && modifiedGun.canAttachType(this.type) && attachment.canAttachTo(this.weapon);
    }

    @Override
    public void setChanged()
    {
        if(this.container.isLoaded())
        {
            this.player.level().playSound(null, this.player.getX(), this.player.getY() + 1.0, this.player.getZ(), ModSounds.UI_WEAPON_ATTACH.get(), SoundSource.PLAYERS, 0.5F, this.hasItem() ? 1.0F : 0.75F);
        }
    }

    @Override
    public int getMaxStackSize()
    {
        return 1;
    }

    @Override
    public boolean mayPickup(Player player)
    {
        ItemStack itemstack = this.getItem();
        return (itemstack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(player);
    }
}
