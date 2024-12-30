package ttv.migami.jeg.item.attachment.item;

import net.minecraft.world.item.ItemStack;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.item.attachment.IScope;
import ttv.migami.jeg.item.attachment.impl.Scope;

public class PseudoScope implements IScope
{
    private final ItemStack spyglass;

    public PseudoScope(ItemStack spyglass)
    {
        this.spyglass = spyglass;
    }

    @Override
    public IAttachment.Type getType()
    {
        return Type.SCOPE;
    }

    @Override
    public Scope getProperties() {
        return null;
    }

    @Override
    public boolean canAttachTo(ItemStack weapon)
    {
        return true;
    }
}
