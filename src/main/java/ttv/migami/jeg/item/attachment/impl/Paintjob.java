package ttv.migami.jeg.item.attachment.impl;

import ttv.migami.jeg.interfaces.IGunModifier;

/**
 * An attachment class related to magazines. Use {@link #create(IGunModifier...)} to create an
 * get.
 * <p>
 * Author: MrCrayfish
 */
public class Paintjob extends Attachment
{
    private Paintjob(IGunModifier... modifier)
    {
        super(modifier);
    }

    /**
     * Creates a side get
     *
     * @param modifier an array of gun modifiers
     * @return a side get
     */
    public static Paintjob create(IGunModifier... modifier)
    {
        return new Paintjob(modifier);
    }
}
