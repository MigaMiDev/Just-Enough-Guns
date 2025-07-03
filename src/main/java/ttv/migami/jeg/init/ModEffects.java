package ttv.migami.jeg.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.effect.IncurableEffect;
import ttv.migami.jeg.effect.KillEffectEffect;
import ttv.migami.jeg.effect.SmokedEffect;

/**
 * Author: MrCrayfish
 */
public class ModEffects
{
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Reference.MOD_ID);

    public static final RegistryObject<IncurableEffect> BLINDED = REGISTER.register("blinded", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<IncurableEffect> DEAFENED = REGISTER.register("deafened", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<SmokedEffect> SMOKED = REGISTER.register("smoked", () -> new SmokedEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<IncurableEffect> BULLET_PROTECTION = REGISTER.register("bullet_protection", () -> new IncurableEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<IncurableEffect> PLAYER_BULLET_PROTECTION = REGISTER.register("player_bullet_protection", () -> new IncurableEffect(MobEffectCategory.BENEFICIAL, 0));

    /* Kill Effects */
    public static final RegistryObject<KillEffectEffect> POPPED = REGISTER.register("popped", () -> new KillEffectEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<KillEffectEffect> TRICKSHOTTED = REGISTER.register("trickshotted", () -> new KillEffectEffect(MobEffectCategory.HARMFUL, 0));
}
