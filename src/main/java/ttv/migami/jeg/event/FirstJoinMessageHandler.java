package ttv.migami.jeg.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.Reference;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FirstJoinMessageHandler {

    private static final String FIRST_JOIN_TAG = "HasReceivedJEGsFirstJoinMessage";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Config.COMMON.network.firstJoinMessages.get()) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MinecraftServer server = serverPlayer.server;
        ServerLevel world = serverPlayer.serverLevel();
        CompoundTag persistentData = serverPlayer.getPersistentData();

        if (persistentData.getBoolean(FIRST_JOIN_TAG)) {
            return;
        }

        boolean isHost = server.isSingleplayer();
        boolean isAdmin = server.getPlayerList().isOp(serverPlayer.getGameProfile());
        boolean isCreative = serverPlayer.getAbilities().instabuild;

        if (isHost || isAdmin || isCreative) {
            if (Config.COMMON.gunnerMobs.gunnerMobSpawning.get() || Config.COMMON.gunnerMobs.gunnerMobRaids.get() ||
                    Config.COMMON.gunnerMobs.explosiveMobs.get() || Config.COMMON.gunnerMobs.gunnerMobPatrols.get()) {
                serverPlayer.sendSystemMessage(Component.translatable("broadcast.jeg.info.warning"));
                serverPlayer.sendSystemMessage(Component.literal(""));
                serverPlayer.sendSystemMessage(Component.translatable("broadcast.jeg.info.disable_messages"));
                serverPlayer.sendSystemMessage(Component.literal(""));
                serverPlayer.sendSystemMessage(Component.literal("Thank you for using Just Enough Guns!"));
                serverPlayer.sendSystemMessage(Component.literal("- MigaMi ♡"));
            }
            JustEnoughGuns.LOGGER.atInfo().log("Player " + serverPlayer.getGameProfile().getName() + " has received the first join message.");
            persistentData.putBoolean(FIRST_JOIN_TAG, true);
        }
    }
}