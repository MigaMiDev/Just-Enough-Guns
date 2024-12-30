package ttv.migami.jeg.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModEffects;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModParticleTypes;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.attachment.IAttachment;

import static ttv.migami.jeg.common.network.ServerPlayHandler.sendParticlesToAll;


@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KillEffectHandler {
    @SubscribeEvent
    public static void postKill(KillEffectEvent.Post event)
    {
        Player player = event.getEntity();
        Vec3 pos = event.getPos();
        Level level = event.getEntity().level();
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getTag();
        LivingEntity entity = event.getTarget();

        ItemStack killEffectItem = Gun.getAttachment(IAttachment.Type.KILL_EFECT, heldItem);
        if (level instanceof ServerLevel serverLevel && !killEffectItem.isEmpty()) {
            if (killEffectItem.is(ModItems.CREEPER_BIRTHDAY_PARTY_BADGE.get())) {
                sendParticlesToAll(
                        serverLevel,
                        ModParticleTypes.CONFETTI.get(),
                        true,
                        entity.getX(),
                        entity.getY() + entity.getEyeHeight() / 1.5,
                        entity.getZ(),
                        64,
                        entity.getBbWidth() / 1.5, entity.getBbHeight() - entity.getEyeHeight(), entity.getBbWidth() / 1.5,
                        0
                );
                sendParticlesToAll(
                        serverLevel,
                        ParticleTypes.EXPLOSION,
                        true,
                        entity.getX(),
                        entity.getY() + entity.getEyeHeight(),
                        entity.getZ(),
                        1,
                        entity.getBbWidth() / 1.5, entity.getBbHeight() - entity.getEyeHeight(), entity.getBbWidth() / 1.5,
                        0
                );
                serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        ModSounds.BIRTHDAY_PARTY.get(), SoundSource.PLAYERS, 5.0F, 1.0F);
            }
            if (killEffectItem.is(ModItems.HEADPOPPER_BADGE.get())) {
                entity.addEffect(new MobEffectInstance(ModEffects.POPPED.get(), 60, 0, false, false));
            }
            if (killEffectItem.is(ModItems.TRICKSHOT_BADGE.get())) {
                entity.addEffect(new MobEffectInstance(ModEffects.TRICKSHOTTED.get(), 60, 0, false, false));
            }
        }
    }
}
