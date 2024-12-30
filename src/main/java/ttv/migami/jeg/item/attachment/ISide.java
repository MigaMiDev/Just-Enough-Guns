package ttv.migami.jeg.item.attachment;

import ttv.migami.jeg.item.attachment.impl.Side;
import ttv.migami.jeg.item.attachment.item.MagazineItem;

/**
 * An interface to turn an any item into a under barrel attachment. This is useful if your item
 * extends a custom item class otherwise {@link MagazineItem} can be
 * used instead of this interface.
 * <p>
 * Author: MrCrayfish
 */
public interface ISide extends IAttachment<Side>
{
    /**
     * @return The type of this attachment
     */
    @Override
    default Type getType()
    {
        return Type.SPECIAL;
    }
}
