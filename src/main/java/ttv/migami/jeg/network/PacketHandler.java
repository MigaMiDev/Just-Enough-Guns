package ttv.migami.jeg.network;

import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import net.minecraft.resources.ResourceLocation;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.network.message.*;

public class PacketHandler
{
    private static FrameworkNetwork playChannel;

    public static void init()
    {
        playChannel = FrameworkAPI.createNetworkBuilder(new ResourceLocation(Reference.MOD_ID, "play"), 1)
                .registerPlayMessage(C2SMessageAim.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageReload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageShoot.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessagePreFireSound.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageBurst.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageUnload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageStunGrenade.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageSmokeGrenade.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageCraft.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageBulletTrail.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageAttachments.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageUpdateGuns.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageBlood.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageExplosiveAmmo.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageShooting.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageFirstPersonReload.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageGunSound.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitBlock.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageLaser.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageProjectileHitEntity.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageRemoveProjectile.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageLeftOverAmmo.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageMelee.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageInspectGun.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageCasing.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageGunUnjammed.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageFlashlight.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(S2CMessageSendMedal.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageSendKillMedal.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageSendHeadshot.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(C2SMessageToggleMedals.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageChargeSync.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageOverheat.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SMessageBurnPlayer.class, MessageDirection.PLAY_SERVER_BOUND)
                .build();
    }

    public static FrameworkNetwork getPlayChannel()
    {
        return playChannel;
    }
}
