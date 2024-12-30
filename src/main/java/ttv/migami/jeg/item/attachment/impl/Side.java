package ttv.migami.jeg.item.attachment.impl;

import ttv.migami.jeg.interfaces.IGunModifier;

/**
 * An attachment class related to magazines. Use {@link #create(IGunModifier...)} to create an
 * get.
 * <p>
 * Author: MrCrayfish
 */
public class Side extends Attachment
{
    private Side(IGunModifier... modifier)
    {
        super(modifier);
    }

    /**
     * Creates a side get
     *
     * @param modifier an array of gun modifiers
     * @return a side get
     */
    public static Side create(IGunModifier... modifier)
    {
        return new Side(modifier);
    }
}
