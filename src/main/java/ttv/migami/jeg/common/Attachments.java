package ttv.migami.jeg.common;

import ttv.migami.jeg.item.attachment.impl.Scope;

/**
 * Author: MrCrayfish
 */
public class Attachments
{
    public static final Scope REFLEX_SIGHT = Scope.builder().aimFovModifier(0.7F).modifiers(GunModifiers.SLOW_ADS).build();
    public static final Scope MONOCLE_SIGHT = Scope.builder().aimFovModifier(0.7F).modifiers(GunModifiers.SLOW_ADS).build();
    public static final Scope HOLOGRAPHIC_SIGHT = Scope.builder().aimFovModifier(0.5F).modifiers(GunModifiers.SLOW_ADS).build();
    public static final Scope COMBAT_SCOPE = Scope.builder().aimFovModifier(0.3F).modifiers(GunModifiers.SLOW_ADS).build();
    public static final Scope TELESCOPIC_SIGHT = Scope.builder().aimFovModifier(0.2F).modifiers(GunModifiers.SLOWEST_ADS).build();
}
