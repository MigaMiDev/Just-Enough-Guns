package ttv.migami.jeg.client.util;

import net.minecraft.client.Minecraft;

public class FPSUtil {
    public static final Minecraft client = Minecraft.getInstance();

    public static double calc() {
        int currentFPS = client.getFps();
        final int BASE_FPS = 60;
        final int MAX_FPS = 240;

        if (currentFPS < BASE_FPS) {
            return 1;
        }

        if (currentFPS > MAX_FPS) {
            return 4;
        }

        return (double) currentFPS / BASE_FPS;
    }
}
