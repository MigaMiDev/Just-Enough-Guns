package ttv.migami.jeg.client.handler;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.JustEnoughGuns;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.GunButtonBindings;
import ttv.migami.jeg.client.GunModel;
import ttv.migami.jeg.client.GunRenderType;
import ttv.migami.jeg.client.KeyBinds;
import ttv.migami.jeg.client.render.gun.IOverrideModel;
import ttv.migami.jeg.client.render.gun.ModelOverrides;
import ttv.migami.jeg.client.util.PropertyHelper;
import ttv.migami.jeg.client.util.RenderUtil;
import ttv.migami.jeg.common.FireMode;
import ttv.migami.jeg.common.GripType;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.common.properties.SightAnimation;
import ttv.migami.jeg.event.GunFireEvent;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GrenadeItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.TelescopicScopeItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.item.attachment.IBarrel;
import ttv.migami.jeg.item.attachment.impl.Scope;
import ttv.migami.jeg.util.GunEnchantmentHelper;
import ttv.migami.jeg.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class GunRenderingHandler {
    protected static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");

    private static GunRenderingHandler instance;

    public static GunRenderingHandler get() {
        if (instance == null) {
            instance = new GunRenderingHandler();
        }
        return instance;
    }

    public static final ResourceLocation MUZZLE_FLASH_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash.png");
    public static final ResourceLocation BUBBLE_FLASH_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/effect/bubble_flash.png");

    public final Random random = new Random();
    public static final Set<Integer> entityIdForMuzzleFlash = new HashSet<>();
    public final Set<Integer> entityIdForDrawnMuzzleFlash = new HashSet<>();
    public static final Map<Integer, Float> entityIdToRandomValue = new HashMap<>();

    public int sprintTransition;
    public int prevSprintTransition;
    public int sprintCooldown;
    public float sprintIntensity;

    private int meleeTransition;
    private int prevMeleeTransition;
    private boolean isMeleeAttacking;
    private int meleeCooldown;
    private boolean meleePressed;

    private float offhandTranslate;
    private float prevOffhandTranslate;

    private Field equippedProgressMainHandField;
    private Field prevEquippedProgressMainHandField;

    public float immersiveRoll;
    private float prevImmersiveRoll;
    public float fallSway;
    public float prevFallSway;

    private boolean playingHitMarker = false;
    private int hitMarkerTime;
    private int prevHitMarkerTime;
    private int hitMarkerMaxTime = 2;
    private boolean hitMarkerCrit = false;

    private boolean controllerFlag;

    @Nullable
    private ItemStack renderingWeapon;

    private GunRenderingHandler() {
    }

    @Nullable
    public ItemStack getRenderingWeapon() {
        return this.renderingWeapon;
    }

    public float getSprintTransition(float partialTicks) {
        return (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
    }

    public float getHitMarkerProgress(float partialTicks) {
        return ((this.prevHitMarkerTime + (this.hitMarkerTime - this.prevHitMarkerTime) * partialTicks) / (float) hitMarkerMaxTime);
    }

    public boolean isRenderingHitMarker() {
        return playingHitMarker;
    }

    public boolean getHitMarkerCrit()
    {
        return hitMarkerCrit;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        this.updateHitMarker();
        this.updateSprinting();
        this.updateMelee();
        this.updateMuzzleFlash();
        this.updateOffhandTranslate();
        this.updateImmersiveCamera();
    }

    private void updateMelee() {
        this.prevMeleeTransition = this.meleeTransition;

        Minecraft mc = Minecraft.getInstance();

        if(JustEnoughGuns.controllableLoaded)
        {
            Controller controller = Controllable.getController();
            controllerFlag = controller != null && controller.isButtonPressed(GunButtonBindings.STEADY_AIM.getButton());
        }

        if (mc.player != null) {
            if((!KeyBinds.KEY_MELEE.isDown() && !controllerFlag) && this.meleeCooldown == 0) {
                this.meleePressed = false;
            }

            if((KeyBinds.KEY_MELEE.isDown() || controllerFlag) && !this.isMeleeAttacking && this.meleeCooldown == 0 && !this.meleePressed) {
                this.isMeleeAttacking = true;
                this.meleeTransition = 0;
                this.meleeCooldown = 14;
                this.meleePressed = true;
            }

            if(this.isMeleeAttacking) {
                if(this.meleeTransition < 5) {
                    this.meleeTransition++;
                } else {
                    this.isMeleeAttacking = false;
                }
            } else if(this.meleeTransition > 0) {
                this.meleeTransition--;
            }

            if(this.meleeCooldown > 0) {
                this.meleeCooldown--;
            }
        }
    }

    private void updateSprinting()
    {
        this.prevSprintTransition = this.sprintTransition;

        Minecraft mc = Minecraft.getInstance();

        if(mc.player != null && mc.player.isSprinting() && !ModSyncedDataKeys.SHOOTING.getValue(mc.player) && !ModSyncedDataKeys.RELOADING.getValue(mc.player) && !AimingHandler.get().isAiming() && this.sprintCooldown == 0 && this.meleeCooldown == 0)
        {
            if(this.sprintTransition < 5)
            {
                this.sprintTransition++;
            }
        }
        else if(this.sprintTransition > 0)
        {
            this.sprintTransition--;
        }

        if(this.sprintCooldown > 0)
        {
            this.sprintCooldown--;
        }
    }

    private void updateHitMarker()
    {
        this.prevHitMarkerTime = this.hitMarkerTime;

        if(playingHitMarker)
        {
            this.hitMarkerTime++;
            if(this.hitMarkerTime > hitMarkerMaxTime)
            {
                this.playingHitMarker=false;
                this.hitMarkerTime=0;
            }
        }
        else
        {
            this.hitMarkerTime=0;
        }
    }

    private void updateMuzzleFlash()
    {
        this.entityIdForMuzzleFlash.removeAll(this.entityIdForDrawnMuzzleFlash);
        this.entityIdToRandomValue.keySet().removeAll(this.entityIdForDrawnMuzzleFlash);
        this.entityIdForDrawnMuzzleFlash.clear();
        this.entityIdForDrawnMuzzleFlash.addAll(this.entityIdForMuzzleFlash);
    }

    private void updateOffhandTranslate()
    {
        this.prevOffhandTranslate = this.offhandTranslate;
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        boolean down = false;
        ItemStack heldItem = mc.player.getMainHandItem();
        if(heldItem.getItem() instanceof GunItem)
        {
            Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            down = !modifiedGun.getGeneral().getGripType().getHeldAnimation().canRenderOffhandItem();
        }

        float direction = down ? -0.3F : 0.3F;
        this.offhandTranslate = Mth.clamp(this.offhandTranslate + direction, 0.0F, 1.0F);
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event)
    {
        if(!event.isClient())
            return;

        this.sprintTransition = 0;
        this.sprintCooldown = 20;

        ItemStack heldItem = event.getStack();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(modifiedGun.getDisplay().getFlash() != null)
        {
            this.showMuzzleFlashForPlayer(Minecraft.getInstance().player.getId());
        }
    }

    public void showMuzzleFlashForPlayer(int entityId)
    {
        this.entityIdForMuzzleFlash.add(entityId);
        this.entityIdToRandomValue.put(entityId, this.random.nextFloat());
    }

    public void playHitMarker(boolean crit) {
        this.playingHitMarker=true;
        this.hitMarkerCrit=crit;
        this.hitMarkerTime=1;
        this.prevHitMarkerTime=0;
    }

    /**
     * Handles calculating the FOV of the first person viewport when aiming with a scope. Changing
     * the FOV allows the user to look through the model of the scope. At a high FOV, the model is
     * very hard to see through, so by lowering the FOV it makes it possible to look through it. This
     * avoids having to render the game twice, which saves a lot of performance.
     */
    @SubscribeEvent
    public void onComputeFov(ViewportEvent.ComputeFov event)
    {
        // We only want to modify the FOV of the viewport for rendering hand/items in first person
        if(event.usedConfiguredFov())
            return;

        // Test if the gun has a scope
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        ItemStack heldItem = player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(!modifiedGun.canAimDownSight())
            return;

        // Change the FOV of the first person viewport based on the scope and aim progress
        if(AimingHandler.get().getNormalisedAdsProgress() <= 0)
            return;

        // Calculate the time curve
        double time = AimingHandler.get().getNormalisedAdsProgress();
        SightAnimation sightAnimation = PropertyHelper.getSightAnimations(heldItem, modifiedGun);
        time = sightAnimation.getViewportCurve().apply(time);

        // Apply the new FOV
        double viewportFov = PropertyHelper.getViewportFov(heldItem, modifiedGun);
        double newFov = viewportFov > 0 ? viewportFov : event.getFOV(); // Backwards compatibility
        event.setFOV(Mth.lerp(time, event.getFOV(), newFov));
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderHandEvent event)
    {
        PoseStack poseStack = event.getPoseStack();

        boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? event.getHand() == InteractionHand.MAIN_HAND : event.getHand() == InteractionHand.OFF_HAND;
        HumanoidArm hand = right ? HumanoidArm.RIGHT : HumanoidArm.LEFT;

        ItemStack heldItem = event.getItemStack();
        if(event.getHand() == InteractionHand.OFF_HAND)
        {
            if(heldItem.getItem() instanceof GunItem)
            {
                event.setCanceled(true);
                return;
            }

            float offhand = 1.0F - Mth.lerp(event.getPartialTick(), this.prevOffhandTranslate, this.offhandTranslate);
            poseStack.translate(0, offhand * -0.6F, 0);

            Player player = Minecraft.getInstance().player;
            if(player != null && player.getMainHandItem().getItem() instanceof GunItem)
            {
                Gun modifiedGun = ((GunItem) player.getMainHandItem().getItem()).getModifiedGun(player.getMainHandItem());
                if(!modifiedGun.getGeneral().getGripType().getHeldAnimation().canRenderOffhandItem())
                {
                    return;
                }
            }

            /* Makes the off hand item move out of view */
            poseStack.translate(0, -1 * AimingHandler.get().getNormalisedAdsProgress(), 0);
        }

        if(!(heldItem.getItem() instanceof GunItem gunItem))
        {
            return;
        }

        Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);

        // TODO: Animation bookmark #2
        /* Cancel it because we are doing our own custom render */
        if (!(heldItem.getItem() instanceof AnimatedGunItem)) {
            event.setCanceled(true);
        }

        ItemStack overrideModel = ItemStack.EMPTY;
        if(heldItem.getTag() != null)
        {
            if(heldItem.getTag().contains("Model", Tag.TAG_COMPOUND))
            {
                overrideModel = ItemStack.of(heldItem.getTag().getCompound("Model"));
            }
        }

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(overrideModel.isEmpty() ? heldItem : overrideModel, player.level(), player, 0);
        float scaleX = model.getTransforms().firstPersonRightHand.scale.x();
        float scaleY = model.getTransforms().firstPersonRightHand.scale.y();
        float scaleZ = model.getTransforms().firstPersonRightHand.scale.z();
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        float translateY = model.getTransforms().firstPersonRightHand.translation.y();
        float translateZ = model.getTransforms().firstPersonRightHand.translation.z();

        poseStack.pushPose();

        if(AimingHandler.get().getNormalisedAdsProgress() > 0 && modifiedGun.canAimDownSight())
        {
            if(event.getHand() == InteractionHand.MAIN_HAND)
            {
                double xOffset = translateX;
                double yOffset = translateY;
                double zOffset = translateZ;

                /* Offset since rendering translates to the center of the model */
                xOffset -= 0.5 * scaleX;
                yOffset -= 0.5 * scaleY;
                zOffset -= 0.5 * scaleZ;

                /* Translate to the origin of the weapon */
                Vec3 gunOrigin = PropertyHelper.getModelOrigin(heldItem, PropertyHelper.GUN_DEFAULT_ORIGIN);
                xOffset += gunOrigin.x * 0.0625 * scaleX;
                yOffset += gunOrigin.y * 0.0625 * scaleY;
                zOffset += gunOrigin.z * 0.0625 * scaleZ;

                /* Creates the required offsets to position the scope into the middle of the screen. */
                Scope scope = Gun.getScope(heldItem);
                if(modifiedGun.canAttachType(IAttachment.Type.SCOPE) && scope != null)
                {
                    /* Translate to the mounting position of scopes */
                    Vec3 scopePosition = PropertyHelper.getAttachmentPosition(heldItem, modifiedGun, IAttachment.Type.SCOPE).subtract(gunOrigin);
                    xOffset += scopePosition.x * 0.0625 * scaleX;
                    yOffset += scopePosition.y * 0.0625 * scaleY;
                    zOffset += scopePosition.z * 0.0625 * scaleZ;

                    /* Translate to the reticle of the scope */
                    ItemStack scopeStack = Gun.getScopeStack(heldItem);
                    Vec3 scopeOrigin = PropertyHelper.getModelOrigin(scopeStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                    Vec3 scopeCamera = PropertyHelper.getScopeCamera(scopeStack).subtract(scopeOrigin);
                    Vec3 scopeScale = PropertyHelper.getAttachmentScale(heldItem, modifiedGun, IAttachment.Type.SCOPE);
                    xOffset += scopeCamera.x * 0.0625 * scaleX * scopeScale.x;
                    yOffset += scopeCamera.y * 0.0625 * scaleY * scopeScale.y;
                    zOffset += ((scopeCamera.z * 0.0625 * scaleZ) - 0.36) * scopeScale.z;
                    if (scopeStack.is(Items.SPYGLASS)) {
                        xOffset += scopeCamera.x * 0.0625 * scaleX * scopeScale.x;
                        yOffset += ((scopeCamera.y * 0.0625 * scaleY) + 0.0375) * scopeScale.y;
                        zOffset += ((scopeCamera.z * 0.0625 * scaleZ) + 0.7) * scopeScale.z;
                    }
                }
                else
                {
                    /* Translate to iron sight */
                    Vec3 ironSightCamera = PropertyHelper.getIronSightCamera(heldItem, modifiedGun, gunOrigin).subtract(gunOrigin);
                    xOffset += ironSightCamera.x * 0.0625 * scaleX;
                    yOffset += ironSightCamera.y * 0.0625 * scaleY;
                    zOffset += ironSightCamera.z * 0.0625 * scaleZ - 0.16;

                    /* Need to add this to ensure old method still works */
                    if(PropertyHelper.isLegacyIronSight(heldItem))
                    {
                        zOffset += 0.72;
                    }
                }

                /* Controls the direction of the following translations, changes depending on the main hand. */
                float side = right ? 1.0F : -1.0F;
                double time = AimingHandler.get().getNormalisedAdsProgress();
                double transition = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getSightCurve().apply(time);

                /* Reverses the original first person translations */
                poseStack.translate(-0.56 * side * transition, 0.52 * transition, 0.72 * transition);

                /* Reverses the first person translations of the item in order to position it in the center of the screen */
                poseStack.translate(-xOffset * side * transition, -yOffset * transition, -zOffset * transition);
            }
        }

        /* Applies custom bobbing animations */
        this.applyBobbingTransforms(poseStack, event.getPartialTick());

        /* Applies equip progress animation translations */
        float equipProgress = this.getEquipProgress(event.getPartialTick());
        //poseStack.translate(0, equipProgress * -0.6F, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(equipProgress * -50F));

        // TODO: Animation bookmark #4
        /* Renders the reload arm. Will only render if actually reloading. This is applied before
         * any recoil or reload rotations as the animations would be borked if applied after. */
        if (!(heldItem.getItem() instanceof AnimatedGunItem)) {
            this.renderReloadArm(poseStack, event.getMultiBufferSource(), event.getPackedLight(), modifiedGun, heldItem, hand, translateX);
        }

        // Values are based on vanilla translations for first person
        int offset = right ? 1 : -1;
        poseStack.translate(0.56 * offset, -0.52, -0.72);

        /* Applies recoil and reload rotations */
        this.applyAimingTransforms(poseStack, heldItem, modifiedGun, translateX, translateY, translateZ, offset);
        this.applySwayTransforms(poseStack, modifiedGun, player, translateX, translateY, translateZ, event.getPartialTick());
        this.applySprintingTransforms(modifiedGun, hand, poseStack, event.getPartialTick());
        this.applyRecoilTransforms(poseStack, heldItem, modifiedGun);
        this.applyReloadTransforms(poseStack, event.getPartialTick());
        this.applyShieldTransforms(poseStack, player, modifiedGun, event.getPartialTick());
        this.applyMeleeTransforms(modifiedGun, hand, poseStack, event.getPartialTick());

        /* Determines the lighting for the weapon. Weapon will appear bright from muzzle flash or light sources */
        int blockLight = player.isOnFire() ? 15 : player.level().getBrightness(LightLayer.BLOCK, BlockPos.containing(player.getEyePosition(Minecraft.getInstance().getPartialTick())));
        if (ShootingHandler.get().isShooting() && !GunModifierHelper.isSilencedFire(heldItem)) {
            blockLight += (GunRenderingHandler.entityIdForMuzzleFlash.contains(player.getId()) ? 3 : 0);
        }
        blockLight = Math.min(blockLight, 15);
        int packedLight = LightTexture.pack(blockLight, player.level().getBrightness(LightLayer.SKY, BlockPos.containing(player.getEyePosition(Minecraft.getInstance().getPartialTick()))));

        /* Renders the first persons arms from the grip type of the weapon */
        // TODO: Animation bookmark #3
        if (!(heldItem.getItem() instanceof AnimatedGunItem)) {
            poseStack.pushPose();
            modifiedGun.getGeneral().getGripType().getHeldAnimation().renderFirstPersonArms(Minecraft.getInstance().player, hand, heldItem, poseStack, event.getMultiBufferSource(), packedLight, event.getPartialTick());
            poseStack.popPose();
        }

        /* Renders the weapon */
        ItemDisplayContext display = right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        this.renderWeapon(Minecraft.getInstance().player, heldItem, display, event.getPoseStack(), event.getMultiBufferSource(), packedLight, event.getPartialTick());

        poseStack.popPose();
    }

    private void applyBobbingTransforms(PoseStack poseStack, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.options.bobView().get() && mc.getCameraEntity() instanceof Player player)
        {
            float deltaDistanceWalked = player.walkDist - player.walkDistO;
            float distanceWalked = -(player.walkDist + deltaDistanceWalked * partialTicks);
            float bobbing = Mth.lerp(partialTicks, player.oBob, player.bob);

            /* Reverses the original bobbing rotations and translations so it can be controlled */
            poseStack.mulPose(Axis.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F)));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F)));
            poseStack.translate(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 0.5F), -(-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * bobbing)), 0.0D);

            /* Slows down the bob by half */
            bobbing *= player.isSprinting() ? 8.0 : 4.0;
            bobbing *= Config.CLIENT.display.bobbingIntensity.get();

            /* The new controlled bobbing */
            double invertZoomProgress = 1.0 - AimingHandler.get().getNormalisedAdsProgress() * this.sprintIntensity;
            //poseStack.translate((double) (Mth.sin(distanceWalked * (float) Math.PI) * cameraYaw * 0.5F) * invertZoomProgress, (double) (-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * cameraYaw)) * invertZoomProgress, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees((Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F) * (float) invertZoomProgress));
            poseStack.mulPose(Axis.XP.rotationDegrees((Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F) * (float) invertZoomProgress));
        }
    }

    private void applyAimingTransforms(PoseStack poseStack, ItemStack heldItem, Gun modifiedGun, float x, float y, float z, int offset)
    {
        if(!Config.CLIENT.display.oldAnimations.get())
        {
            poseStack.translate(x * offset, y, z);
            poseStack.translate(0, -0.25, 0.25);
            float aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180F));
            aiming = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getAimTransformCurve().apply(aiming);
            poseStack.mulPose(Axis.ZP.rotationDegrees(aiming * 10F * offset));
            poseStack.mulPose(Axis.XP.rotationDegrees(aiming * 5F));
            poseStack.mulPose(Axis.YP.rotationDegrees(aiming * 5F * offset));
            poseStack.translate(0, 0.25, -0.25);
            poseStack.translate(-x * offset, -y, -z);
        }
    }

    private void applySwayTransforms(PoseStack poseStack, Gun modifiedGun, LocalPlayer player, float x, float y, float z, float partialTicks)
    {
        if(Config.CLIENT.display.weaponSway.get() && player != null)
        {
            poseStack.translate(x, y, z);

            double zOffset = modifiedGun.getGeneral().getGripType().getHeldAnimation().getFallSwayZOffset();
            poseStack.translate(0, -0.25, zOffset);
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevFallSway, this.fallSway)));
            poseStack.translate(0, 0.25, -zOffset);

            float bobPitch = Mth.rotLerp(partialTicks, player.xBobO, player.xBob);
            float headPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
            float swayPitch = headPitch - bobPitch;
            swayPitch *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getPitchRotation().rotationDegrees(swayPitch * Config.CLIENT.display.swaySensitivity.get().floatValue()));

            float bobYaw = Mth.rotLerp(partialTicks, player.yBobO, player.yBob);
            float headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
            float swayYaw = headYaw - bobYaw;
            swayYaw *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.display.swayType.get().getYawRotation().rotationDegrees(swayYaw * Config.CLIENT.display.swaySensitivity.get().floatValue()));

            poseStack.translate(-x, -y, -z);
        }
    }

    private void applySprintingTransforms(Gun modifiedGun, HumanoidArm hand, PoseStack poseStack, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();

        if(Config.CLIENT.display.sprintAnimation.get() && modifiedGun.getGeneral().getGripType().getHeldAnimation().canApplySprintingAnimation())
        {
            float leftHanded = hand == HumanoidArm.LEFT ? -1 : 1;
            float transition = (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
            transition = (float) Math.sin((transition * Math.PI) / 2);

            if ((Gun.getAttachment(IAttachment.Type.BARREL, mc.player.getMainHandItem()).getItem() instanceof SwordItem)) {
                //poseStack.translate(-0.25 * leftHanded * transition, -0.1 * transition, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(15F * transition));
            }
            else if (modifiedGun.getGeneral().getGripType().equals(GripType.TWO_HANDED)){
                poseStack.translate(-0.25 * leftHanded * transition, -0.1 * transition, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(45F * leftHanded * transition));
                poseStack.mulPose(Axis.XP.rotationDegrees(-25F * transition));
            } else {
                poseStack.translate(0 * leftHanded * transition, 0.5 * transition, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(55F * transition));
            }

        }
    }

    private void applyMeleeTransforms(Gun modifiedGun, HumanoidArm hand, PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        float leftHanded = hand == HumanoidArm.LEFT ? -1 : 1;
        float transition = (this.prevMeleeTransition + (this.meleeTransition - this.prevMeleeTransition) * partialTicks) / 5F;
        transition = (float) Math.sin((transition * Math.PI) / 2);

        if ((Gun.getAttachment(IAttachment.Type.BARREL, mc.player.getMainHandItem()).getItem() instanceof SwordItem || mc.player.getMainHandItem().getItem() == ModItems.ATLANTEAN_SPEAR.get())) {
            poseStack.translate(-1.0 * leftHanded * transition, 0.4 * transition, -0.3 * transition);
            poseStack.mulPose(Axis.YP.rotationDegrees(100F * leftHanded * transition));
            poseStack.mulPose(Axis.XP.rotationDegrees(45F * transition));
            poseStack.mulPose(Axis.ZP.rotationDegrees(45F * transition));
        }
        else {
            poseStack.translate(-0.8 * leftHanded * transition, 0.2 * transition, -0.1 * transition);
            poseStack.mulPose(Axis.YP.rotationDegrees(100F * leftHanded * transition));
            poseStack.mulPose(Axis.XP.rotationDegrees(-35F * transition));
            poseStack.mulPose(Axis.ZP.rotationDegrees(35F * transition));
        }

    }

    private void applyReloadTransforms(PoseStack poseStack, float partialTicks)
    {
        float reloadProgress = ReloadHandler.get().getReloadProgress(partialTicks);
        poseStack.translate(0, 0.35 * reloadProgress, 0);
        poseStack.translate(0, 0, -0.1 * reloadProgress);
        poseStack.mulPose(Axis.XP.rotationDegrees(45F * reloadProgress));
    }

    private void applyRecoilTransforms(PoseStack poseStack, ItemStack item, Gun gun)
    {
        double recoilNormal = GunRecoilHandler.get().getGunRecoilNormal();
        if(Gun.hasAttachmentEquipped(item, gun, IAttachment.Type.SCOPE))
        {
            recoilNormal -= recoilNormal * (0.5 * AimingHandler.get().getNormalisedAdsProgress());
        }
        float kickReduction = 1.0F - GunModifierHelper.getKickReduction(item);
        float recoilReduction = 1.0F - GunModifierHelper.getRecoilModifier(item);
        double kick = gun.getGeneral().getRecoilKick() * 0.0625 * recoilNormal * GunRecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilLift = (float) (gun.getGeneral().getRecoilAngle() * recoilNormal) * (float) GunRecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilSwayAmount = (float) (2F + 1F * (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
        float recoilSway = (float) ((GunRecoilHandler.get().getGunRecoilRandom() * recoilSwayAmount - recoilSwayAmount / 2F) * recoilNormal);
        poseStack.translate(0, 0, kick * kickReduction);
        poseStack.translate(0, 0, 0.15);
        poseStack.mulPose(Axis.YP.rotationDegrees(recoilSway * recoilReduction));
        poseStack.mulPose(Axis.ZP.rotationDegrees(recoilSway * recoilReduction));
        poseStack.mulPose(Axis.XP.rotationDegrees(recoilLift * recoilReduction));
        poseStack.translate(0, 0, -0.15);
    }

    private void applyShieldTransforms(PoseStack poseStack, LocalPlayer player, Gun modifiedGun, float partialTick)
    {
        if(player.isUsingItem() && player.getOffhandItem().getItem() instanceof ShieldItem && modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED)
        {
            double time = Mth.clamp((player.getTicksUsingItem() + partialTick), 0.0, 4.0) / 4.0;
            poseStack.translate(0, 0.35 * time, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(45F * (float) time));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase.equals(TickEvent.Phase.START))
            return;

        Minecraft mc = Minecraft.getInstance();
        if(!mc.isWindowActive())
            return;

        Player player = mc.player;
        if(player == null)
            return;

        if(Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.isEmpty())
            return;

        if(player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND && heldItem.getItem() instanceof GrenadeItem)
        {
            if(!((GrenadeItem) heldItem.getItem()).canCook())
                return;

            int duration = player.getTicksUsingItem();
            if(duration >= 10)
            {
                float cookTime = 1.0F - ((float) (duration - 10) / (float) (player.getUseItem().getUseDuration() - 10));
                if(cookTime > 0.0F)
                {
                    float scale = 3;
                    Window window = mc.getWindow();
                    int i = (int) ((window.getGuiScaledHeight() / 2 - 7 - 60) / scale);
                    int j = (int) Math.ceil((window.getGuiScaledWidth() / 2 - 8 * scale) / scale);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);

                    GuiGraphics pGuiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
                    pGuiGraphics.pose().scale(scale, scale, scale);
                    int progress = (int) Math.ceil((cookTime) * 17.0F) - 1;
                    pGuiGraphics.blit(GUI_ICONS_LOCATION, j, i, 36, 94, 16, 4, 256, 256);
                    pGuiGraphics.blit(GUI_ICONS_LOCATION, j, i, 52, 94, progress, 4, 256, 256);

                    RenderSystem.disableBlend();
                }
            }
            return;
        }

        if(heldItem.getItem() instanceof GunItem) {

            if(Config.CLIENT.display.cooldownIndicator.get())
            {
                Gun gun = ((GunItem) heldItem.getItem()).getGun();
                if(gun.getGeneral().getFireMode() != FireMode.AUTOMATIC)
                {
                    float coolDown = player.getCooldowns().getCooldownPercent(heldItem.getItem(), event.renderTickTime);
                    if(coolDown > 0.0F)
                    {
                        float scale = 3;
                        Window window = mc.getWindow();
                        int i = (int) ((window.getGuiScaledHeight() / 2 - 7 - 60) / scale);
                        int j = (int) Math.ceil((window.getGuiScaledWidth() / 2 - 8 * scale) / scale);

                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
                        RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);

                        GuiGraphics pGuiGraphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
                        pGuiGraphics.pose().scale(scale, scale, scale);
                        int progress = (int) Math.ceil((coolDown + 0.05) * 17.0F) - 1;
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, j, i, 36, 94, 16, 4, 256, 256);
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, j, i, 52, 94, progress, 4, 256, 256);

                        RenderSystem.disableBlend();
                    }
                }
            }
        }
    }

    public void applyWeaponScale(ItemStack heldItem, PoseStack stack)
    {
        if(heldItem.getTag() != null)
        {
            CompoundTag compound = heldItem.getTag();
            if(compound.contains("Scale", Tag.TAG_FLOAT))
            {
                float scale = compound.getFloat("Scale");
                stack.scale(scale, scale, scale);
            }
        }
    }

    public boolean renderWeapon(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext display, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        if(stack.getItem() instanceof GunItem)
        {
            if (AimingHandler.get().isAiming() &&
                    (Gun.getAttachment(IAttachment.Type.SCOPE, stack).getItem() instanceof TelescopicScopeItem ||
                            Gun.getAttachment(IAttachment.Type.SCOPE, stack).is(Items.SPYGLASS)))
            {
                if (display.firstPerson())
                    return false;
            }

            boolean doRender = false;
            if (stack.getItem() instanceof AnimatedGunItem) {
                if (!display.firstPerson() && !display.equals(ItemDisplayContext.FIXED)) {
                //if (!display.firstPerson()) {
                    //if (!display.firstPerson()) {
                    doRender = true;
                }

                if (stack.hasTag() && stack.getTag() != null) {
                    if (stack.getTag().contains("GunId")) {
                        doRender = false;
                    }
                }
            } else {
                doRender = true;
            }

            if (doRender)
            {
                poseStack.pushPose();

                ItemStack model = ItemStack.EMPTY;
                if(stack.getTag() != null)
                {
                    if(stack.getTag().contains("Model", Tag.TAG_COMPOUND))
                    {
                        model = ItemStack.of(stack.getTag().getCompound("Model"));
                    }
                }

                RenderUtil.applyTransformType(stack, poseStack, display, entity);

                this.renderingWeapon = stack;
                this.renderGun(entity, display, model.isEmpty() ? stack : model, poseStack, renderTypeBuffer, light, partialTicks);
                // Had to disable it for now :(
                if (!(stack.getItem() instanceof AnimatedGunItem)) {
                    this.renderAttachments(entity, display, stack, poseStack, renderTypeBuffer, light, partialTicks);
                }
                if (ShootingHandler.get().isShooting() && !GunModifierHelper.isSilencedFire(stack)) {
                    if (stack.getItem() instanceof AnimatedGunItem) {
                        if (!display.firstPerson() && !display.equals(ItemDisplayContext.FIXED)) {
                            this.renderMuzzleFlash(entity, poseStack, renderTypeBuffer, stack, display, partialTicks);
                        }
                    } else {
                        this.renderMuzzleFlash(entity, poseStack, renderTypeBuffer, stack, display, partialTicks);
                    }
                }
                this.renderingWeapon = null;

                poseStack.popPose();
                return true;
            }
        }
        return false;
    }

    // TODO: Animation bookmark #1
    private void renderGun(@Nullable LivingEntity entity, ItemDisplayContext display, ItemStack stack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        if(stack.getItem() instanceof GunItem gunItem) {
            if(ModelOverrides.hasModel(stack))
            {
                IOverrideModel model = ModelOverrides.getModel(stack);
                if(model != null)
                {
                    if (display == ItemDisplayContext.GUI) {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(15F));
                        model.render(partialTicks, display, stack, ItemStack.EMPTY, entity, poseStack, renderTypeBuffer, 15728880, OverlayTexture.NO_OVERLAY);
                    } else {
                        model.render(partialTicks, display, stack, ItemStack.EMPTY, entity, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
                    }
                }
            }
            else if (!(stack.getItem() instanceof AnimatedGunItem))
            {
                Level level = entity != null ? entity.level() : null;
                BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, entity, 0);
                Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, bakedModel);

                /*Level level = entity != null ? entity.level() : null;

                // Determine the texture based on the NBT tag
                ResourceLocation texture = new ResourceLocation("modid", "textures/item/default_gun.png");
                if (stack.hasTag() && stack.getTag().getBoolean("CustomTag")) {
                    texture = new ResourceLocation("modid", "textures/item/custom_gun.png");
                }

                // Use the texture to render the gun
                BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, entity, 0);
                VertexConsumer vertexConsumer = renderTypeBuffer.getBuffer(RenderType.entityCutout(texture));
                Minecraft.getInstance().getItemRenderer().renderModelLists(bakedModel, stack, light, OverlayTexture.NO_OVERLAY, poseStack, vertexConsumer);*/
            }
        }
    }

    private void renderAttachments(@Nullable LivingEntity entity, ItemDisplayContext display, ItemStack stack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        if(stack.getItem() instanceof GunItem gunItem)
        {
            Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            CompoundTag gunTag = stack.getOrCreateTag();
            CompoundTag attachments = gunTag.getCompound("Attachments");
            for(String tagKey : attachments.getAllKeys())
            {
                IAttachment.Type type = IAttachment.Type.byTagKey(tagKey);
                if(type != null && modifiedGun.canAttachType(type))
                {
                    ItemStack attachmentStack = Gun.getAttachment(type, stack);
                    if(!attachmentStack.isEmpty())
                    {
                        poseStack.pushPose();

                        /* Translates the attachment to a standard position by removing the origin */
                        Vec3 origin = PropertyHelper.getModelOrigin(attachmentStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                        if (stack.getItem() instanceof AnimatedGunItem) {
                            origin = origin.multiply(1, 0.8, 0.8);
                        }
                        poseStack.translate(-origin.x * 0.0625, -origin.y * 0.0625, -origin.z * 0.0625);

                        /* Translation to the origin on the weapon */
                        Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
                        if (stack.getItem() instanceof AnimatedGunItem) {
                            gunOrigin = gunOrigin.multiply(1, 0.8, 0.8);
                        }
                        poseStack.translate(gunOrigin.x * 0.0625, gunOrigin.y * 0.0625, gunOrigin.z * 0.0625);


                        /* Translate to the position this attachment mounts on the weapon */
                        Vec3 translation = PropertyHelper.getAttachmentPosition(stack, modifiedGun, type).subtract(gunOrigin);
                        if (stack.getItem() instanceof AnimatedGunItem) {
                            translation = translation.multiply(0.8, 0.8, 0.8);
                        }
                        poseStack.translate(translation.x * 0.0625, translation.y * 0.0625, translation.z * 0.0625);

                        /* Scales the attachment. Also translates the delta of the attachment origin to (8, 8, 8) since this is the centered origin for scaling */
                        Vec3 scale = PropertyHelper.getAttachmentScale(stack, modifiedGun, type);
                        Vec3 center = origin.subtract(8, 8, 8).scale(0.0625);
                        poseStack.translate(center.x, center.y, center.z);
                        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
                        poseStack.translate(-center.x, -center.y, -center.z);
                        /*if (stack.getItem() instanceof AnimatedGunItem && type != IAttachment.Type.BARREL && type != IAttachment.Type.SPECIAL) {
                            poseStack.scale(1.3F, 1.3F, 1.3F);
                        }*/

                        if (attachmentStack.getItem() instanceof SwordItem && !(attachmentStack.is(Items.WOODEN_SWORD) ||
                                attachmentStack.is(Items.STONE_SWORD) ||
                                attachmentStack.is(Items.IRON_SWORD) ||
                                attachmentStack.is(Items.GOLDEN_SWORD) ||
                                attachmentStack.is(Items.DIAMOND_SWORD) ||
                                attachmentStack.is(Items.NETHERITE_SWORD)))
                        {
                            poseStack.scale((float) 0.3, (float) 0.3, (float) 0.3);
                            poseStack.mulPose(Axis.XN.rotationDegrees(90));
                            poseStack.mulPose(Axis.YN.rotationDegrees(90));
                            poseStack.mulPose(Axis.ZN.rotationDegrees(-45));
                            poseStack.translate(-0.4F, -0.2F, 0.0F);
                        }
                        if (attachmentStack.getItem() instanceof SpyglassItem)
                        {
                            poseStack.scale((float) 0.5, (float) 0.5, (float) 0.5);
                            poseStack.mulPose(Axis.XN.rotationDegrees(90));
                            poseStack.mulPose(Axis.YN.rotationDegrees(90));
                            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                            poseStack.translate(-0.08F, -0.05F, 0.0F);
                        }

                        IOverrideModel model = ModelOverrides.getModel(attachmentStack);
                        if(model != null)
                        {
                            model.render(partialTicks, display, attachmentStack, stack, entity, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
                        }
                        else
                        {
                            Level level = entity != null ? entity.level() : null;
                            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(attachmentStack, level, entity, 0);
                            Minecraft.getInstance().getItemRenderer().render(attachmentStack, ItemDisplayContext.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, GunModel.wrap(bakedModel));
                        }

                        poseStack.popPose();
                    }
                }
            }
        }
    }

    private void renderMuzzleFlash(@Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, ItemStack weapon, ItemDisplayContext display, float partialTicks)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        if(modifiedGun.getDisplay().getFlash() == null)
            return;

        if(display != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && display != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND && display != ItemDisplayContext.FIRST_PERSON_LEFT_HAND && display != ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
            return;

        if(entity == null || !this.entityIdForMuzzleFlash.contains(entity.getId()))
            return;

        float randomValue = this.entityIdToRandomValue.get(entity.getId());
        this.drawMuzzleFlash(weapon, modifiedGun, randomValue, randomValue >= 0.5F, poseStack, buffer, partialTicks);
    }

    private void drawMuzzleFlash(ItemStack weapon, Gun modifiedGun, float random, boolean flip, PoseStack poseStack, MultiBufferSource buffer, float partialTicks)
    {
        if(!PropertyHelper.hasMuzzleFlash(weapon, modifiedGun))
            return;

        poseStack.pushPose();

        // Translate to the position where the muzzle flash should spawn
        Vec3 weaponOrigin = PropertyHelper.getModelOrigin(weapon, PropertyHelper.GUN_DEFAULT_ORIGIN);
        Vec3 flashPosition = PropertyHelper.getMuzzleFlashPosition(weapon, modifiedGun).subtract(weaponOrigin);
        poseStack.translate(weaponOrigin.x * 0.0625, weaponOrigin.y * 0.0625, weaponOrigin.z * 0.0625);
        poseStack.translate(flashPosition.x * 0.0625, flashPosition.y * 0.0625, flashPosition.z * 0.0625);
        poseStack.translate(-0.5, -0.5, -0.5);

        // Legacy method to move muzzle flash to be at the end of the barrel attachment
        ItemStack barrelStack = Gun.getAttachment(IAttachment.Type.BARREL, weapon);
        if(!barrelStack.isEmpty() && barrelStack.getItem() instanceof IBarrel barrel && !PropertyHelper.isUsingBarrelMuzzleFlash(barrelStack))
        {
            Vec3 scale = PropertyHelper.getAttachmentScale(weapon, modifiedGun, IAttachment.Type.BARREL);
            double length = barrel.getProperties().getLength();
            poseStack.translate(0, 0, -length * 0.0625 * scale.z);
        }

        poseStack.mulPose(Axis.ZP.rotationDegrees(360F * random));
        poseStack.mulPose(Axis.XP.rotationDegrees(flip ? 180F : 0F));

        Vec3 flashScale = PropertyHelper.getMuzzleFlashScale(weapon, modifiedGun);
        float scaleX = ((float) flashScale.x / 2F) - ((float) flashScale.x / 2F) * (1.0F - partialTicks);
        float scaleY = ((float) flashScale.y / 2F) - ((float) flashScale.y / 2F) * (1.0F - partialTicks);
        poseStack.scale(scaleX * 2, scaleY * 2, 1.0F);

        float scaleModifier = (float) GunModifierHelper.getMuzzleFlashScale(weapon, 1.0);
        poseStack.scale(scaleModifier, scaleModifier, 1.0F);

        // Center the texture
        poseStack.translate(-0.5, -0.5, 0);

        float minU = weapon.isEnchanted() ? 0.5F : 0.0F;
        float maxU = weapon.isEnchanted() ? 1.0F : 0.5F;

        if (weapon.getItem() == ModItems.SUBSONIC_RIFLE.get() ||
                weapon.getItem() == ModItems.FLAMETHROWER.get() ||
                weapon.getItem() == ModItems.SUPERSONIC_SHOTGUN.get() ||
                weapon.getItem() == ModItems.HYPERSONIC_CANNON.get() ||
                weapon.getItem() == ModItems.SOULHUNTER_MK2.get() ||
                weapon.getItem() == ModItems.BLOSSOM_RIFLE.get() ||
                weapon.getItem() == ModItems.HOLY_SHOTGUN.get()) {
            minU = 0.5F;
            maxU = 1.0F;
        }

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer builder = buffer.getBuffer(GunRenderType.getMuzzleFlash());

        Minecraft mc = Minecraft.getInstance();
        if (weapon.getEnchantmentLevel(ModEnchantments.ATLANTIC_SHOOTER.get()) != 0 && mc.player != null && mc.player.isUnderWater()) {
            builder = buffer.getBuffer(GunRenderType.getBubbleFlash());
            minU = 0.0F;
            maxU = 1.0F;
        }

        builder.vertex(matrix, 0, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, 1.0F).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, 1.0F).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, 0).uv2(15728880).endVertex();
        builder.vertex(matrix, 0, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, 0).uv2(15728880).endVertex();

        poseStack.popPose();
    }

    private void renderReloadArm(PoseStack poseStack, MultiBufferSource buffer, int light, Gun modifiedGun, ItemStack stack, HumanoidArm hand, float translateX)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.player.tickCount < ReloadHandler.get().getStartReloadTick() || ReloadHandler.get().getReloadTimer() != 5)
            return;

        Item item = ForgeRegistries.ITEMS.getValue(modifiedGun.getProjectile().getItem());
        if(item == null)
            return;

        if (AimingHandler.get().isAiming() &&
                (Gun.getAttachment(IAttachment.Type.SCOPE, stack).getItem() instanceof TelescopicScopeItem ||
                        Gun.getAttachment(IAttachment.Type.SCOPE, stack).is(Items.SPYGLASS)))
        {
            return;
        }

        poseStack.pushPose();

        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(translateX * side, 0, 0);

        float interval = GunEnchantmentHelper.getRealReloadSpeed(stack);
        float reload = ((mc.player.tickCount - ReloadHandler.get().getStartReloadTick() + mc.getFrameTime()) % interval) / interval;
        float percent = 1.0F - reload;
        if(percent >= 0.5F)
        {
            percent = 1.0F - percent;
        }
        percent *= 2F;
        percent = percent < 0.5 ? 2 * percent * percent : -1 + (4 - 2 * percent) * percent;

        poseStack.translate(3.5 * side * 0.0625, -0.5625, -0.5625);
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        poseStack.translate(0, -0.35 * (1.0 - percent), 0);
        poseStack.translate(side * 0.0625, 0, 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(90F));
        poseStack.mulPose(Axis.YP.rotationDegrees(35F * -side));
        poseStack.mulPose(Axis.XP.rotationDegrees(-75F * percent));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        RenderUtil.renderFirstPersonArm(mc.player, hand.getOpposite(), poseStack, buffer, light);

        if(reload < 0.5F)
        {
            poseStack.pushPose();
            poseStack.translate(-side * 5 * 0.0625, 15 * 0.0625, -1 * 0.0625);
            poseStack.mulPose(Axis.XP.rotationDegrees(180F));
            poseStack.scale(0.75F, 0.75F, 0.75F);
            ItemStack ammo = new ItemStack(item, modifiedGun.getReloads().getReloadAmount());
            BakedModel model = RenderUtil.getModel(ammo);
            boolean isModel = model.isGui3d();
            this.random.setSeed(Item.getId(item));
            int count = Math.min(modifiedGun.getReloads().getReloadAmount(), 5);
            for(int i = 0; i < count; ++i)
            {
                poseStack.pushPose();
                if(i > 0)
                {
                    if(isModel)
                    {
                        float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float z = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        poseStack.translate(x, y, z);
                    }
                    else
                    {
                        float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        float y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        poseStack.translate(x, y, 0);
                    }
                }

                RenderUtil.renderModel(ammo, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, null);
                poseStack.popPose();

                if(!isModel)
                {
                    poseStack.translate(0.0, 0.0, 0.09375F);
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /**
     * A temporary hack to get the equip progress until Forge fixes the issue.
     * @return
     */
    private float getEquipProgress(float partialTicks)
    {
        if(this.equippedProgressMainHandField == null)
        {
            this.equippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109302_");
            this.equippedProgressMainHandField.setAccessible(true);
        }
        if(this.prevEquippedProgressMainHandField == null)
        {
            this.prevEquippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109303_");
            this.prevEquippedProgressMainHandField.setAccessible(true);
        }
        ItemInHandRenderer firstPersonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        try
        {
            float equippedProgressMainHand = (float) this.equippedProgressMainHandField.get(firstPersonRenderer);
            float prevEquippedProgressMainHand = (float) this.prevEquippedProgressMainHandField.get(firstPersonRenderer);
            return 1.0F - Mth.lerp(partialTicks, prevEquippedProgressMainHand, equippedProgressMainHand);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return 0.0F;
    }

    private void updateImmersiveCamera()
    {
        this.prevImmersiveRoll = this.immersiveRoll;
        this.prevFallSway = this.fallSway;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        ItemStack heldItem = mc.player.getMainHandItem();
        float targetAngle = heldItem.getItem() instanceof GunItem || !Config.CLIENT.display.restrictCameraRollToWeapons.get() ? mc.player.input.leftImpulse: 0F;
        float speed = mc.player.input.leftImpulse != 0 ? 0.1F : 0.15F;
        this.immersiveRoll = Mth.lerp(speed, this.immersiveRoll, targetAngle);

        float deltaY = (float) Mth.clamp((mc.player.yo - mc.player.getY()), -1.0, 1.0);
        deltaY *= 1.0 - AimingHandler.get().getNormalisedAdsProgress();
        deltaY *= 1.0 - (Mth.abs(mc.player.getXRot()) / 90.0F);
        this.fallSway = Mth.approach(this.fallSway, deltaY * 60F * Config.CLIENT.display.swaySensitivity.get().floatValue(), 10.0F);

        float intensity = mc.player.isSprinting() ? 0.75F : 1.0F;
        this.sprintIntensity = Mth.approach(this.sprintIntensity, intensity, 0.1F);
    }

    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event)
    {
        if(Config.CLIENT.display.cameraRollEffect.get())
        {
            float roll = (float) Mth.lerp(event.getPartialTick(), this.prevImmersiveRoll, this.immersiveRoll);
            roll = (float) Math.sin((roll * Math.PI) / 2.0);
            roll *= Config.CLIENT.display.cameraRollAngle.get().floatValue();
            event.setRoll(-roll);
        }
    }
}
