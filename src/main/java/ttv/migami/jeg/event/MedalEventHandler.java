package ttv.migami.jeg.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.medal.MedalType;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.S2CMessageSendKillMedal;
import ttv.migami.jeg.network.message.S2CMessageSendMedal;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MedalEventHandler {

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if(Config.COMMON.gameplay.overrideHideMedals.get())
            return;

        if (event.getEntity().level().isClientSide) {
            return;
        }

        LivingEntity entity = event.getEntity();
        LivingEntity killer = entity.getKillCredit();

        if (killer == null) {
            return;
        }

        if (killer.getMainHandItem().getTag() != null && !killer.getMainHandItem().getTag().getBoolean("MedalsEnabled")) {
            return;
        }

        //if (!(entity instanceof NeutralMob) && !(entity instanceof Enemy) && !(entity instanceof Player)) {
        if (!(entity instanceof Enemy) && !(entity instanceof Player)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (event.getSource().is(DamageTypes.EXPLOSION) || event.getSource().is(DamageTypes.PLAYER_EXPLOSION))
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendMedal(MedalType.GEAR_BOOM.ordinal()));

        if (!(killer.getMainHandItem().getItem() instanceof GunItem)) {
            return;
        }

        player.level().getServer().execute(() -> PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendKillMedal()));

        if (player.getMainHandItem().getTag() == null) {
            return;
        }

        if (entity instanceof Creeper creeper && creeper.swell != 0) {
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendMedal(MedalType.COMBAT_HUSH.ordinal()));
        }

        if (entity.isOnFire())
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendMedal(MedalType.GEAR_BBQ.ordinal()));


        if (entity.getTags().contains("EliteGunner"))
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendMedal(MedalType.COMBAT_KINGSLAYER.ordinal()));

        if (player.getMainHandItem().getTag().getInt("AmmoCount") <= 1 && !player.isCreative())
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) player, new S2CMessageSendMedal(MedalType.COMBAT_JUST_ENOUGH_AMMO.ordinal()));
    }
}