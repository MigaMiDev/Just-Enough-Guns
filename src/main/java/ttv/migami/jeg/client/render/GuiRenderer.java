package ttv.migami.jeg.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.handler.AimingHandler;
import ttv.migami.jeg.client.handler.ShootingHandler;
import ttv.migami.jeg.client.medal.MedalManager;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.ReloadType;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.TelescopicScopeItem;
import ttv.migami.jeg.item.attachment.IAttachment;

import static ttv.migami.jeg.common.Gun.getTotalAmmoCount;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public class GuiRenderer {
    protected static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
    protected static final ResourceLocation LONG_SCOPE_OVERLAY = new ResourceLocation(Reference.MOD_ID, "textures/scope_long_overlay.png");
    protected static final ResourceLocation AMMO = new ResourceLocation(Reference.MOD_ID, "textures/gui/ammo/bullet.png");
    protected static final ResourceLocation SHELL = new ResourceLocation(Reference.MOD_ID, "textures/gui/ammo/shell.png");
    protected static final ResourceLocation OVERHEAT = new ResourceLocation(Reference.MOD_ID, "textures/gui/timer/overheat.png");
    protected static final ResourceLocation POWER = new ResourceLocation(Reference.MOD_ID, "textures/gui/timer/power.png");
    protected static final ResourceLocation HOLD = new ResourceLocation(Reference.MOD_ID, "textures/gui/timer/hold.png");

    protected static int screenWidth;
    protected static int screenHeight;
    protected static float scopeScale;

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        // Spyglass/Telescopic Scopes
        screenWidth = minecraft.getWindow().getGuiScaledWidth();
        screenHeight = minecraft.getWindow().getGuiScaledHeight();
        float f = minecraft.getDeltaFrameTime();
        scopeScale = Mth.lerp(0.5F * f, scopeScale, 1.125F);
        if (minecraft.options.getCameraType().isFirstPerson()) {
            if (AimingHandler.get().isAiming() || player.getUseItem().getItem() instanceof TelescopicScopeItem) {
                if (event.getOverlay() == VanillaGuiOverlay.SPYGLASS.type()) {
                    if (Gun.getAttachment(IAttachment.Type.SCOPE, player.getMainHandItem()).getItem() instanceof TelescopicScopeItem) {
                        renderSpyglassOverlay(event.getGuiGraphics(), scopeScale, LONG_SCOPE_OVERLAY);
                    }
                    if (Gun.getAttachment(IAttachment.Type.SCOPE, player.getMainHandItem()).is(Items.SPYGLASS)) {
                        renderSpyglassOverlay(event.getGuiGraphics(), scopeScale, SPYGLASS_SCOPE_LOCATION);
                    }
                }
            } else {
                scopeScale = 0.5F;
            }
        }

        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
            // Render Medals
            MedalManager.render(event.getGuiGraphics());

            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty() && heldItem.getItem() instanceof GunItem gunItem) {
                CompoundTag tag = heldItem.getTag();
                if (tag == null) return;
                Gun gun = gunItem.getModifiedGun(heldItem);
                GuiGraphics guiGraphics = event.getGuiGraphics();

                // Timers
                if (Config.CLIENT.display.showTimersGUI.get()) {
                    boolean renderWhileAiming;
                    if (!Config.CLIENT.display.aimingHidesTimers.get()) {
                        renderWhileAiming = true;
                    } else {
                        renderWhileAiming = !AimingHandler.get().isAiming();
                    }
                    if (AimingHandler.get().getNormalisedAdsProgress() > 0.5 && (!minecraft.options.getCameraType().isFirstPerson()))
                    {
                        renderWhileAiming = true;
                    }
                    if (renderWhileAiming) {
                        // Hold Bar
                        if (gun.getGeneral().getFireTimer() != 0) {
                            RenderSystem.enableBlend();
                            RenderSystem.defaultBlendFunc();

                            guiGraphics.blit(HOLD,
                                    minecraft.getWindow().getGuiScaledWidth() / 2 - 32,
                                    minecraft.getWindow().getGuiScaledHeight() / 2 + 38,
                                    0, 0, 64, 6, 64, 12);

                            RenderSystem.disableBlend();

                            float fireTimer = gun.getGeneral().getFireTimer();
                            float currentFireTimer = ShootingHandler.get().getFireTimer();
                            float progress = (currentFireTimer / fireTimer);
                            int width = (int) (64 * progress);

                            guiGraphics.blit(HOLD,
                                    minecraft.getWindow().getGuiScaledWidth() / 2 - 32,
                                    minecraft.getWindow().getGuiScaledHeight() / 2 + 38,
                                    0, 6, width, 6, 64, 12);
                        }

                        // Power Bar
                        if (gun.getGeneral().getMaxHoldFire() != 0) {
                            RenderSystem.enableBlend();
                            RenderSystem.defaultBlendFunc();

                            guiGraphics.blit(POWER,
                                    minecraft.getWindow().getGuiScaledWidth() / 2 - 32,
                                    minecraft.getWindow().getGuiScaledHeight() / 2 + 31,
                                    0, 0, 64, 6, 64, 12);

                            RenderSystem.disableBlend();

                            float maxHoldTimer = gun.getGeneral().getMaxHoldFire();
                            float currentHoldTimer = ShootingHandler.get().getHoldFire();
                            float progress = (currentHoldTimer / maxHoldTimer);
                            int width = (int) (64 * progress);

                            guiGraphics.blit(POWER,
                                    minecraft.getWindow().getGuiScaledWidth() / 2 - 32,
                                    minecraft.getWindow().getGuiScaledHeight() / 2 + 31,
                                    0, 6, width, 6, 64, 12);
                        }

                        // Overheat Bar
                        if (gun.getGeneral().getOverheatTimer() != 0) {
                            RenderSystem.enableBlend();
                            RenderSystem.defaultBlendFunc();

                            guiGraphics.blit(OVERHEAT,
                                    minecraft.getWindow().getGuiScaledWidth() / 2 - 32,
                                    minecraft.getWindow().getGuiScaledHeight() / 2 + 24,
                                    0, 0, 64, 6, 64, 12);

                            RenderSystem.disableBlend();

                            float maxOverheatTimer = gun.getGeneral().getOverheatTimer();
                            float currentOverheatTimer = ShootingHandler.get().getOverheatTimer();
                            float progress = (currentOverheatTimer / maxOverheatTimer);
                            int width = (int) (64 * progress);

                            guiGraphics.blit(OVERHEAT,
                                    minecraft.getWindow().getGuiScaledWidth() / 2 - 32,
                                    minecraft.getWindow().getGuiScaledHeight() / 2 + 24,
                                    0, 6, width, 6, 64, 12);
                        }
                    }
                }

                // Ammo Counter
                if(!Config.CLIENT.display.showAmmoGUI.get())
                    return;

                if (heldItem.is(ModItems.FINGER_GUN.get()))
                    return;

                if (!tag.contains("AmmoCount")) return;

                int ammoCount = tag.getInt("AmmoCount");
                ItemStack[] ammoStack;
                if (!gun.getReloads().getReloadType().equals(ReloadType.SINGLE_ITEM)) {
                    ammoStack = Gun.findAmmoStack(player, gun.getProjectile().getItem());
                } else {
                    ammoStack = Gun.findAmmoStack(player, gun.getReloads().getReloadItem());
                }
                int startX = (int) (minecraft.getWindow().getGuiScaledWidth() * 0.95 - 100) + Config.CLIENT.display.displayAmmoGUIXOffset.get();
                int startY = (int) (minecraft.getWindow().getGuiScaledHeight() - 65) + Config.CLIENT.display.displayAmmoGUIYOffset.get();

                String name = heldItem.getHoverName().getString();
                guiGraphics.drawString(Minecraft.getInstance().font, name, startX, startY - 10, heldItem.getRarity().color.getColor(), true);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(startX, startY, 0);
                guiGraphics.pose().scale(2.0f, 2.0f, 1.0f);

                String ammoText = String.format("%03d", ammoCount);

                if (ModSyncedDataKeys.RELOADING.getValue(player) && !player.isCrouching()) {
                    ammoText = "Reloading";
                }

                guiGraphics.drawString(Minecraft.getInstance().font, ammoText, 0, 0, 0xFFFFFF, true);
                guiGraphics.pose().popPose();

                boolean showReserve = true;
                if (ModSyncedDataKeys.RELOADING.getValue(player)) {
                    showReserve = player.isCrouching();
                }

                if (showReserve) {
                    String inventoryAmmo = String.format("%04d", ammoStack.length);
                    if (gun.getReloads().getReloadType().equals(ReloadType.SINGLE_ITEM)) {
                        inventoryAmmo = String.format("%04d", ammoStack.length * gun.getReloads().getMaxAmmo());
                    } else {
                        inventoryAmmo = String.format("%04d", getTotalAmmoCount(ammoStack));
                    }
                    if (player.isCreative() || heldItem.getEnchantmentLevel(ModEnchantments.INFINITY.get()) != 0 ||
                            heldItem.getTag().getBoolean("IgnoreAmmo")) {
                        inventoryAmmo = "9999";
                    }
                    guiGraphics.drawString(Minecraft.getInstance().font, inventoryAmmo, startX + 38, startY, 0x878787, true);

                }

                /*ResourceLocation ammoTexture = AMMO;
                int segmentWidth = 3;
                int segmentHeight = 9;
                int rowPadding = 0;
                int rowHeight = segmentHeight + rowPadding;
                int segmentsPerRow = 30;
                int textureRows = 4;

                //if (gunItem.getGun().getReloads().getMaxAmmo() <= 12) {
                if (GunModifierHelper.getModifiedAmmoCapacity(heldItem, gunItem.getGun()) <= 12) {
                    ammoTexture = SHELL;
                    segmentWidth = 15;
                    segmentsPerRow = 6;
                }

                int maxAmmo = segmentsPerRow * textureRows;
                if (ammoCount > maxAmmo) ammoCount = maxAmmo;

                int startX = (int) (minecraft.getWindow().getGuiScaledWidth() * 0.95 - 90) + Config.CLIENT.display.displayAmmoGUIXOffset.get();
                int startY = (int) (minecraft.getWindow().getGuiScaledHeight() - 45) + Config.CLIENT.display.displayAmmoGUIYOffset.get();

                int textWidth = Minecraft.getInstance().font.width(name);
                //guiGraphics.drawString(Minecraft.getInstance().font, name, startX + (90 - textWidth) / 2, startY - 9, 0xFFFFFF, true);
                guiGraphics.drawString(Minecraft.getInstance().font, name, startX, startY - 9, 0xFFFFFF, true);

                int fullRows = ammoCount / segmentsPerRow;
                int extraSegments = ammoCount % segmentsPerRow;

                for (int row = 0; row < fullRows; row++) {
                    int renderWidth = segmentsPerRow * segmentWidth;
                    int renderHeight = segmentHeight;
                    int textureY = row * rowHeight;

                    guiGraphics.blit(ammoTexture, startX, startY + row * rowHeight, 0, textureY, renderWidth, renderHeight, 90, 9);
                }

                if (extraSegments > 0) {
                    int renderWidth = extraSegments * segmentWidth;
                    int renderHeight = segmentHeight;
                    int textureY = fullRows * rowHeight;

                    guiGraphics.blit(ammoTexture, startX, startY + fullRows * rowHeight, 0, textureY, renderWidth, renderHeight, 90, 9);
                }*/
            }
        }
    }

    public static void renderSpyglassOverlay(GuiGraphics pGuiGraphics, float pScopeScale, ResourceLocation resourceLocation) {
        float f = (float)Math.min(screenWidth, screenHeight);
        float f1 = Math.min((float)screenWidth / f, (float)screenHeight / f) * pScopeScale;
        int i = Mth.floor(f * f1);
        int j = Mth.floor(f * f1);
        int k = (screenWidth - i) / 2;
        int l = (screenHeight - j) / 2;
        int i1 = k + i;
        int j1 = l + j;
        pGuiGraphics.blit(resourceLocation, k, l, -90, 0.0F, 0.0F, i, j, i, j);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, j1, screenWidth, screenHeight, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, screenWidth, l, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), i1, l, screenWidth, j1, -90, -16777216);
    }
}