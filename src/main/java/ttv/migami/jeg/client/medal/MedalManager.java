package ttv.migami.jeg.client.medal;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.util.FPSUtil;
import ttv.migami.jeg.init.ModSounds;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MedalManager {

    private static final ResourceLocation HEADSHOT = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/combat_headshot.png");
    private static final ResourceLocation DOUBLE_KILL = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/multikill_kill_double.png");
    private static final ResourceLocation TRIPLE_KILL = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/multikill_kill_triple.png");
    private static final ResourceLocation QUAD_KILL = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/multikill_kill_quad.png");
    private static final ResourceLocation PENTA_KILL = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/multikill_kill_penta.png");
    private static final ResourceLocation KILLING_SPREE = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/multikill_killing_spree.png");

    private static final List<Medal> MEDALS = new ArrayList<>();
    private static final Queue<Medal> PENDING_MEDALS = new LinkedList<>();
    private static Medal currentPlayingMedal = null;
    private static long lastKillTime = 0;
    private static int multiKillCount = 0;
    private static boolean isHeadshot = false;

    private static Medal newMedal(ResourceLocation texture, MutableComponent description, SoundEvent soundEvent) {
        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return new Medal(texture, screenWidth / 2 - 32, 20, 60 * FPSUtil.calc(), description, soundEvent);
    }

    public static void addEnumMedal(int id) {
        String  medalType;
        if (getMedalByPosition(id) == null) {
            return;
        } else {
            medalType = getMedalByPosition(id).toString().toLowerCase();
        }

        ResourceLocation texture = new ResourceLocation(Reference.MOD_ID, "textures/gui/medal/" + medalType + ".png");
        MutableComponent description = Component.translatable("medal.jeg." + medalType);
        SoundEvent soundEvent = ModSounds.MEDAL_GENERIC.get();

        Medal medal = newMedal(texture, description, soundEvent);

        if (currentPlayingMedal == null) {
            currentPlayingMedal = medal;
            playMedalSound(soundEvent);
            MEDALS.add(medal);
        } else {
            PENDING_MEDALS.add(medal);
        }
    }

    public static MedalType getMedalByPosition(int position) {
        if (position < 0 || position >= MedalType.values().length) {
            return null;
        }
        return MedalType.values()[position];
    }

    public void addMedal(ResourceLocation texture, MutableComponent description, SoundEvent soundEvent) {
        Medal medal = newMedal(texture, description, soundEvent);

        if (currentPlayingMedal == null) {
            currentPlayingMedal = medal;
            playMedalSound(soundEvent);
            MEDALS.add(medal);
        } else {
            PENDING_MEDALS.add(medal);
        }
    }

    public static void addKillMedal(ResourceLocation texture, MutableComponent description) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastKillTime <= 2000) {
            multiKillCount++;
        } else {
            multiKillCount = 1;
        }

        lastKillTime = currentTime;

        if (isHeadshot) {
            ResourceLocation headshotTexture = HEADSHOT;
            MutableComponent headshotDescription = Component.translatable("medal.jeg.combat_headshot");
            SoundEvent headshotSound = ModSounds.MEDAL_HEADSHOT.get();

            Medal headshotMedal = newMedal(headshotTexture, headshotDescription, headshotSound);

            if (currentPlayingMedal == null) {
                currentPlayingMedal = headshotMedal;
                playMedalSound(headshotSound);
                MEDALS.add(headshotMedal);
            } else {
                PENDING_MEDALS.add(headshotMedal);
            }

            isHeadshot = false;
        }

        ResourceLocation medalTexture = getMedalTexture(texture);
        description = getMedalText(description);
        SoundEvent sound = ModSounds.MEDAL_GENERIC.get();

        Medal medal = newMedal(medalTexture, description, sound);
        if (currentPlayingMedal == null) {
            currentPlayingMedal = medal;
            playMedalSound(sound);
            MEDALS.add(medal);
        } else {
            PENDING_MEDALS.add(medal);
        }
    }

    public static void setHeadshot(boolean headshot) {
        isHeadshot = headshot;
    }

    private static ResourceLocation getMedalTexture(ResourceLocation texture) {
        ResourceLocation medalTexture = texture;
        if (multiKillCount == 2) {
            medalTexture = DOUBLE_KILL;
        } else if (multiKillCount == 3) {
            medalTexture = TRIPLE_KILL;
        } else if (multiKillCount == 4) {
            medalTexture = QUAD_KILL;
        } else if (multiKillCount == 5) {
            medalTexture = PENTA_KILL;
        } else if (multiKillCount > 5) {
            medalTexture = KILLING_SPREE;
        }
        return medalTexture;
    }

    private static MutableComponent getMedalText(MutableComponent description) {
        if (multiKillCount == 2) {
            description = Component.translatable("medal.jeg.multikill_double_kill");
        } else if (multiKillCount == 3) {
            description = Component.translatable("medal.jeg.multikill_triple_kill");
        } else if (multiKillCount == 4) {
            description = Component.translatable("medal.jeg.multikill_quad_kill");
        } else if (multiKillCount == 5) {
            description = Component.translatable("medal.jeg.multikill_penta_kill");
        } else if (multiKillCount > 5) {
            description = Component.translatable("medal.jeg.multikill_killing_spree");
        }
        return description;
    }

    private static void playMedalSound(SoundEvent sound) {
        if (sound != null) {
            Minecraft.getInstance().player.playSound(
                    sound,
                    1.0F,
                    1.0F
            );
        }
    }

    public static void render(GuiGraphics guiGraphics) {
        if (currentPlayingMedal != null && currentPlayingMedal.getLifetime() <= 30 * FPSUtil.calc() && !PENDING_MEDALS.isEmpty()) {
            currentPlayingMedal = PENDING_MEDALS.poll();
            playMedalSound(currentPlayingMedal.getSound());
            MEDALS.add(currentPlayingMedal);
        }

        MEDALS.removeIf(Medal::tick);

        if (MEDALS.isEmpty()) {
            currentPlayingMedal = null;
        }

        for (Medal medal : MEDALS) {
            guiGraphics.pose().pushPose();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, medal.getOpacity());

            guiGraphics.blit(
                    medal.getTexture(),
                    medal.getX(),
                    medal.getY(),
                    0, 0,
                    64, 64,
                    64, 64
            );

            String description = medal.getDescription().getString();
            int textWidth = Minecraft.getInstance().font.width(description);
            int textX = medal.getX() + (64 - textWidth) / 2;
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    description,
                    textX,
                    medal.getY() + 70,
                    0xFFFFFF
            );

            RenderSystem.disableBlend();
            guiGraphics.pose().popPose();
        }
    }
}