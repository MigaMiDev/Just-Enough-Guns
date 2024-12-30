package ttv.migami.jeg.client.render.gun.animated;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.util.RenderUtils;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.GunRenderType;
import ttv.migami.jeg.client.handler.AimingHandler;
import ttv.migami.jeg.client.handler.GunRecoilHandler;
import ttv.migami.jeg.client.handler.GunRenderingHandler;
import ttv.migami.jeg.client.handler.ShootingHandler;
import ttv.migami.jeg.client.render.gun.animated.model.AttachmentRenderer;
import ttv.migami.jeg.client.util.PropertyHelper;
import ttv.migami.jeg.common.GripType;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.event.GunFireEvent;
import ttv.migami.jeg.init.ModEnchantments;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.AnimatedBowItem;
import ttv.migami.jeg.item.AnimatedGunItem;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.TelescopicScopeItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.item.attachment.IBarrel;
import ttv.migami.jeg.item.attachment.impl.Scope;
import ttv.migami.jeg.item.attachment.item.PaintJobCanItem;
import ttv.migami.jeg.util.GunModifierHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

// Huge credit goes to ElidhanMC from Simple Animated Guns! Massive thanks!
public class AnimatedGunRenderer extends GeoItemRenderer<AnimatedGunItem> implements GeoRenderer<AnimatedGunItem> {

	private static ResourceLocation customPath = null;
	private ResourceLocation oldTextureResource = null;
	private ResourceLocation oldModelResource = null;
    private static ResourceLocation oldGunTexture = null;
	private static ResourceLocation oldGunModel = null;

	public AnimatedGunRenderer(ResourceLocation path) {
		super(new AnimatedGunModel(path));

		//addRenderLayer(new AttachmentRenderer(this));
	}

	private static AnimatedGunRenderer instance;

	public static AnimatedGunRenderer get()
	{
		if(instance == null)
		{
			instance = new AnimatedGunRenderer(customPath);
		}
		return instance;
	}

	private final AttachmentRenderer attachmentRenderer = new AttachmentRenderer(this);

	private MultiBufferSource bufferSource;
	private ItemDisplayContext renderType;

	private int sprintTransition;
	private int prevSprintTransition;
	private int sprintCooldown;
	private float sprintIntensity;

	private float immersiveRoll;
	private float fallSway;
	private float prevFallSway;

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase != TickEvent.Phase.END)
			return;

		this.updateSprinting();
		this.updateImmersiveCamera();
	}

	private void updateSprinting()
	{
		this.prevSprintTransition = this.sprintTransition;
		Minecraft mc = Minecraft.getInstance();
		if(mc.player != null && mc.player.isSprinting() && !ModSyncedDataKeys.SHOOTING.getValue(mc.player) && !ModSyncedDataKeys.RELOADING.getValue(mc.player) && !AimingHandler.get().isAiming() && this.sprintCooldown == 0)
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

	private void updateImmersiveCamera()
	{
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
	public void onGunFire(GunFireEvent.Post event)
	{
		if(!event.isClient())
			return;

		this.sprintTransition = 0;
		this.sprintCooldown = 20;
	}

	private ResourceLocation getValidTexture(ResourceLocation texture, ItemStack stack) {
		Minecraft client = Minecraft.getInstance();
		Optional<Resource> resource = client.getResourceManager().getResource(texture);

		if (resource.isPresent()) {
			return texture;
		} else {
			if (stack.getItem() instanceof GunItem) {
				return new ResourceLocation(Reference.MOD_ID, "textures/animated/gun/" + stack.getItem() + ".png");
			} else {
				return new ResourceLocation(Reference.MOD_ID, "textures/animated/attachment/" + stack.getItem() + ".png");
			}
		}
	}

	private ResourceLocation getValidModel(ResourceLocation model, ItemStack stack) {
		Minecraft client = Minecraft.getInstance();
		Optional<Resource> resource = client.getResourceManager().getResource(model);

		if (resource.isPresent()) {
			return model;
		} else {
			if (stack.getItem() instanceof GunItem) {
				return new ResourceLocation(Reference.MOD_ID, "geo/item/gun/" + stack.getItem() + ".geo.json");
			} else {
				return new ResourceLocation(Reference.MOD_ID, "geo/item/attachment/" + stack.getItem() + ".geo.json");
			}
		}
	}

	private void updateGunSkin(ItemStack stack) {
		// Gun Skin testing! "Paint Jobs"
		ResourceLocation newGunTexture;
		if (stack.hasTag() && (Gun.getAttachment(IAttachment.Type.PAINT_JOB, stack).getItem() instanceof PaintJobCanItem paintJobCanItem)) {
			newGunTexture = new ResourceLocation(Reference.MOD_ID, "textures/animated/gun/paintjob/" + paintJobCanItem.getPaintJob() + "/" + stack.getItem() + ".png");
		} else {
			newGunTexture = new ResourceLocation(Reference.MOD_ID, "textures/animated/gun/" + stack.getItem() + ".png");
		}
		newGunTexture = getValidTexture(newGunTexture, stack);

		if (!newGunTexture.equals(oldGunTexture)) {
			oldGunTexture = newGunTexture;
			AnimatedGunModel animaModel = (AnimatedGunModel) this.getGeoModel();
			animaModel.setCurrentTexture(oldGunTexture);
		}

		ResourceLocation newGunModel;
		if (stack.hasTag() && (Gun.getAttachment(IAttachment.Type.PAINT_JOB, stack).getItem() instanceof PaintJobCanItem paintJobCanItem)) {
			newGunModel = new ResourceLocation(Reference.MOD_ID, "geo/item/gun/paintjob/" + paintJobCanItem.getPaintJob() + "/" + stack.getItem() + ".geo.json");
		} else {
			newGunModel = new ResourceLocation(Reference.MOD_ID, "geo/item/gun/" + stack.getItem() + ".geo.json");
		}
		newGunModel = getValidModel(newGunModel, stack);

		if (!newGunModel.equals(oldGunModel)) {
			oldGunModel = newGunModel;
			AnimatedGunModel animaModel = (AnimatedGunModel) this.getGeoModel();
			animaModel.setCurrentModel(oldGunModel);
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
	{
		this.bufferSource = bufferSource;
		this.renderType = transformType;

		if ((this.renderType != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && !transformType.equals(ItemDisplayContext.FIXED) && !transformType.equals(ItemDisplayContext.GROUND)
			&& !stack.is(ModItems.FINGER_GUN.get()))) {
			// Hack to remove transforms created by ItemRenderer#render
			poseStack.popPose();

			poseStack.pushPose();
			{
				Minecraft mc = Minecraft.getInstance();
				GunRenderingHandler.get().renderWeapon(mc.player, stack, transformType, poseStack, bufferSource, packedLight, Minecraft.getInstance().getDeltaFrameTime());
			}
			poseStack.popPose();

			// Push the stack again since we popped the pose prior
			poseStack.pushPose();
			return;
		}

		if (AimingHandler.get().isAiming() &&  this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND &&
			(Gun.getAttachment(IAttachment.Type.SCOPE, getCurrentItemStack()).getItem() instanceof TelescopicScopeItem ||
					Gun.getAttachment(IAttachment.Type.SCOPE, getCurrentItemStack()).is(Items.SPYGLASS))) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		Player player = client.player;

		boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? player.getUsedItemHand() == InteractionHand.MAIN_HAND : player.getUsedItemHand() == InteractionHand.OFF_HAND;
		HumanoidArm hand = right ? HumanoidArm.RIGHT : HumanoidArm.LEFT;

		ItemStack overrideModel = ItemStack.EMPTY;
		if(stack.getTag() != null)
		{
			if(stack.getTag().contains("Model", Tag.TAG_COMPOUND))
			{
				overrideModel = ItemStack.of(stack.getTag().getCompound("Model"));
			}
		}

		LocalPlayer localPlayer = Objects.requireNonNull(Minecraft.getInstance().player);
		BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(overrideModel.isEmpty() ? stack : overrideModel, player.level(), player, 0);
		float scaleX = model.getTransforms().firstPersonRightHand.scale.x();
		float scaleY = model.getTransforms().firstPersonRightHand.scale.y();
		float scaleZ = model.getTransforms().firstPersonRightHand.scale.z();
		float translateX = model.getTransforms().firstPersonRightHand.translation.x();
		float translateY = model.getTransforms().firstPersonRightHand.translation.y();
		float translateZ = model.getTransforms().firstPersonRightHand.translation.z();

		// Gun Skin testing! "Paint Jobs"
		this.updateGunSkin(stack);

		/* Applies some of the original transforms */
		if (stack.getItem() instanceof AnimatedGunItem && this.renderType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
			Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);

			this.applyBobbingTransforms(poseStack, Minecraft.getInstance().getPartialTick());

			/*this.sprintTransition = GunRenderingHandler.get().sprintTransition;
			this.prevSprintTransition = GunRenderingHandler.get().prevSprintTransition;
			this.sprintCooldown = GunRenderingHandler.get().sprintCooldown;
			this.sprintIntensity = GunRenderingHandler.get().sprintIntensity;*/

			this.immersiveRoll = GunRenderingHandler.get().immersiveRoll;
			this.fallSway = GunRenderingHandler.get().fallSway / 2;
			this.prevFallSway = GunRenderingHandler.get().prevFallSway / 2;

			int offset = right ? 1 : -1;
			//poseStack.translate(0.56 * offset, -0.52, -0.72);
			this.applyRecoilTransforms(poseStack, stack, modifiedGun);
			this.applyAimingTransforms(poseStack, stack, modifiedGun, translateX, translateY, translateZ, offset);
			this.applySwayTransforms(poseStack, modifiedGun, localPlayer, translateX, translateY, translateZ, Minecraft.getInstance().getPartialTick());
			//this.applySprintingTransforms(modifiedGun, hand, poseStack, Minecraft.getInstance().getPartialTick());

			if (stack.getItem() instanceof AnimatedBowItem) {
				poseStack.translate(-0.5, 0.5, -0.1);
				poseStack.mulPose(Axis.ZP.rotationDegrees(-45));
			}

			if(AimingHandler.get().getNormalisedAdsProgress() > 0 && modifiedGun.canAimDownSight())
			{
				//if(player.getUsedItemHand() == InteractionHand.MAIN_HAND)
				{
					double xOffset = translateX;
					double yOffset = translateY;
					double zOffset = translateZ;

					/* Offset since rendering translates to the center of the model */
					xOffset -= 0.5 * scaleX;
					yOffset -= 0.5 * scaleY;
					zOffset -= 0.5 * scaleZ;

					/* Translate to the origin of the weapon */
					Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
					xOffset += gunOrigin.x * 0.0625 * scaleX;
					yOffset += gunOrigin.y * 0.0625 * scaleY;
					zOffset += gunOrigin.z * 0.0625 * scaleZ;

					/* Creates the required offsets to position the scope into the middle of the screen. */
					Scope scope = Gun.getScope(stack);
					if(modifiedGun.canAttachType(IAttachment.Type.SCOPE) && scope != null)
					{
						/* Translate to the mounting position of scopes */
						Vec3 scopePosition = PropertyHelper.getAttachmentPosition(stack, modifiedGun, IAttachment.Type.SCOPE).subtract(gunOrigin);
						xOffset += scopePosition.x * 0.0625 * scaleX;
						yOffset += scopePosition.y * 0.0625 * scaleY;
						zOffset += scopePosition.z * 0.0625 * scaleZ;

						/* Translate to the reticle of the scope */
						ItemStack scopeStack = Gun.getScopeStack(stack);
						Vec3 scopeOrigin = PropertyHelper.getModelOrigin(scopeStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
						Vec3 scopeCamera = PropertyHelper.getScopeCamera(scopeStack).subtract(scopeOrigin);
						Vec3 scopeScale = PropertyHelper.getAttachmentScale(stack, modifiedGun, IAttachment.Type.SCOPE);
						xOffset += scopeCamera.x * 0.0625 * scaleX * scopeScale.x;
						//yOffset += ((scopeCamera.y * 0.0625 * scaleY) + 0.5957) * scopeScale.y;
						yOffset += ((scopeCamera.y * 0.0625 * scaleY) + 0.54) * scopeScale.y;
						zOffset += ((scopeCamera.z * 0.0625 * scaleZ) - 0.16) * scopeScale.z;
						if (scopeStack.is(Items.SPYGLASS)) {
							xOffset += scopeCamera.x * 0.0625 * scaleX * scopeScale.x;
							yOffset += ((scopeCamera.y * 0.0625 * scaleY) + 0.075) * scopeScale.y;
							zOffset += ((scopeCamera.z * 0.0625 * scaleZ) + 0.6) * scopeScale.z;
						}
					}
					else
					{
						/* Translate to iron sight */
						Vec3 ironSightCamera = PropertyHelper.getIronSightCamera(stack, modifiedGun, gunOrigin).subtract(gunOrigin);
						xOffset += ironSightCamera.x * 0.0625 * scaleX;
						yOffset += (ironSightCamera.y * 0.0625 * scaleY) + 0.6059;
						zOffset += (ironSightCamera.z * 0.0625 * scaleZ) - 0.16;

						/* Need to add this to ensure old method still works */
						if(PropertyHelper.isLegacyIronSight(stack))
						{
							zOffset += 0.72;
						}
					}

					/* Controls the direction of the following translations, changes depending on the main hand. */
					float side = right ? 1.0F : -1.0F;
					if(Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT && player.getOffhandItem().getItem() instanceof ShieldItem) {
						side = 1.0F;
					}
					if(Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT && player.getOffhandItem().getItem() instanceof ShieldItem) {
						side = -1.0F;
					}
					side = 1.0F;

					double time = AimingHandler.get().getNormalisedAdsProgress();
					double transition = PropertyHelper.getSightAnimations(stack, modifiedGun).getSightCurve().apply(time);

					if (stack.getItem() instanceof AnimatedBowItem) {
						poseStack.mulPose(Axis.ZP.rotationDegrees((float) (45 * transition)));
						poseStack.translate(0.508 * transition, -0.6 * transition, 0);
					}

					/* Reverses the original first person translations */
					poseStack.translate(-0.56 * side * transition, 0.52 * transition, 0.72 * transition);

					/* Reverses the first person translations of the item in order to position it in the center of the screen */
					poseStack.translate(-xOffset * side * transition, -yOffset * transition, -zOffset * transition);
				}
			}
		}
		if (transformType.firstPerson()) {
			if (ShootingHandler.get().isShooting() && !GunModifierHelper.isSilencedFire(stack)) {
				this.renderMuzzleFlash(Minecraft.getInstance().player, poseStack, bufferSource, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, Minecraft.getInstance().getPartialTick());
			}
		}

		/* Determines the lighting for the weapon. Weapon will appear bright from muzzle flash or light sources */
		int blockLight = player.isOnFire() ? 15 : player.level().getBrightness(LightLayer.BLOCK, BlockPos.containing(player.getEyePosition(Minecraft.getInstance().getPartialTick())));
		if (ShootingHandler.get().isShooting() && !GunModifierHelper.isSilencedFire(stack) && transformType.firstPerson()) {
			blockLight += (GunRenderingHandler.entityIdForMuzzleFlash.contains(player.getId()) ? 3 : 0);
		}
		blockLight = Math.min(blockLight, 15);
		if (this.renderType == ItemDisplayContext.GUI) {
			packedLight = 15728880;
		}
		else {
			packedLight = LightTexture.pack(blockLight, player.level().getBrightness(LightLayer.SKY, BlockPos.containing(player.getEyePosition(Minecraft.getInstance().getPartialTick()))));
		}

		super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
	}

	@Override
	public void renderRecursively(PoseStack poseStack, AnimatedGunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		if (this.renderPerspective != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && this.renderPerspective != ItemDisplayContext.FIXED && this.renderPerspective != ItemDisplayContext.GROUND
				&& !currentItemStack.is(ModItems.FINGER_GUN.get())) {
			return;
		}

		if (AimingHandler.get().isAiming() && this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND &&
				(Gun.getAttachment(IAttachment.Type.SCOPE, getCurrentItemStack()).getItem() instanceof TelescopicScopeItem ||
						Gun.getAttachment(IAttachment.Type.SCOPE, getCurrentItemStack()).is(Items.SPYGLASS))) {
			return;
		}

		Minecraft client = Minecraft.getInstance();

		boolean renderArms = false;
		VertexConsumer buffer1 = this.bufferSource.getBuffer(renderType);

		poseStack.pushPose();

		{
			if (currentItemStack.getItem() == ModItems.SERVICE_RIFLE.get()) {
				switch(bone.getName())
				{
					case "railing" -> bone.setHidden(Gun.getScope(currentItemStack) == null);
					//case "railing" -> bone.setHidden(!Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).isEmpty());
					case "stock_iron_sight" -> bone.setHidden(Gun.getScope(currentItemStack) != null ||
					//case "stock_iron_sight" -> bone.setHidden(!Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).isEmpty() ||
							Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() == ModItems.TACTICAL_STOCK.get());
					case "modified_iron_sight" -> bone.setHidden(Gun.getScope(currentItemStack) != null ||
					//case "modified_iron_sight" -> bone.setHidden(!Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).isEmpty() ||
							Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.TACTICAL_STOCK.get());

					case "weighted_handguard" -> bone.setHidden(!Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).isEmpty() &&
							Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.WEIGHTED_STOCK.get());
                    case "tactical_handguard" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.TACTICAL_STOCK.get());
					case "light_handguard" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.LIGHT_STOCK.get());
				}
			}

			if (animatable instanceof AnimatedBowItem && currentItemStack.getTag() != null) {
				if (bone.getName().matches("arrow")) {
					if (this.renderPerspective.equals(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)) {
						bone.setHidden(currentItemStack.getTag().getInt("AmmoCount") <= 0);
					} else {
						bone.setHidden(true);
					}
				}
			}

			// Makes the gun glow if needed
			if (bone.getName().matches("glow") || bone.getName().matches("glow_2") ||
					bone.getName().matches("glow_3")) {
				packedLight = 15728880;
			}

			if (currentItemStack.getTag() != null) {
				int ammoCount = currentItemStack.getTag().getInt("AmmoCount");

				Map<String, Integer> boneAmmoThresholds = Map.of(
						"bullet_1", 1,
						"bullet_2", 2,
						"bullet_3", 3,
						"bullet_4", 4,
						"bullet_5", 5,
						"bullet_6", 6,
						"bullet_7", 7
				);

				boneAmmoThresholds.forEach((boneName, threshold) -> {
					if (bone.getName().matches(boneName)) {
						bone.setHidden(ammoCount < threshold);
					}
				});
			}

			if (bone.getName().matches("flashlight_glow")) {
				if (Gun.hasAttachmentEquipped(currentItemStack, IAttachment.Type.SPECIAL)) {
					if (Gun.getAttachment(IAttachment.Type.SPECIAL, currentItemStack).getItem() == ModItems.FLASHLIGHT.get()) {
						ItemStack flashlight = Gun.getAttachment(IAttachment.Type.SPECIAL, currentItemStack);
						if (flashlight.getTag() != null && flashlight.getTag().getBoolean("Powered")) {
							bone.setHidden(false);
						}
						else {
							bone.setHidden(true);
						}
					}
				}
			}

			// Makes it so the bone always faces the camera! Pretty cool!
			/*Vec3 cameraPos = client.gameRenderer.getMainCamera().getPosition();
			Vec3 bonePos = new Vec3(bone.getPivotX(), bone.getPivotY(), bone.ge());
			Vec3 directionToCamera = cameraPos.subtract(bonePos).normalize();

			float yaw = (float) Math.atan2(directionToCamera.z, directionToCamera.x);
			float pitch = (float) Math.asin(directionToCamera.y);

			yaw = (float) Math.toDegrees(yaw);
			pitch = (float) Math.toDegrees(pitch);

			if (bone.getName().matches("face_camera")) {
				bone.setRotX(-pitch);
				bone.setRotY(yaw + 90.0F);
			}*/

			// It failed.

			switch(bone.getName())
			{
				case "left_arm", "right_arm", "fake_left_arm", "fake_right_arm" ->
				{
					bone.setHidden(true);
					bone.setChildrenHidden(false);
					renderArms = true;
				}
				// TODO: Make the attachment system not so trash.
				/* Custom Attachment Models */
				// Scope
				case "iron_sight" -> bone.setHidden(Gun.getScope(currentItemStack) != null);
				case "hidden_iron_sight" -> bone.setHidden(Gun.getScope(currentItemStack) == null);
				//case "iron_sight" -> bone.setHidden(!Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).isEmpty());
				//case "hidden_iron_sight" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).isEmpty());
				// Stock
				case "makeshift_stock" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.MAKESHIFT_STOCK.get());
				case "light_stock" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.LIGHT_STOCK.get());
				case "tactical_stock" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.TACTICAL_STOCK.get());
				case "weighted_stock" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.STOCK, currentItemStack).getItem() != ModItems.WEIGHTED_STOCK.get());
				// Barrel
				case "silencer" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.BARREL, currentItemStack).getItem() != ModItems.SILENCER.get());
				// Under Barrel
				case "light_grip" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.UNDER_BARREL, currentItemStack).getItem() != ModItems.LIGHT_GRIP.get());
				case "vertical_grip" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.UNDER_BARREL, currentItemStack).getItem() != ModItems.VERTICAL_GRIP.get());
				case "angled_grip" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.UNDER_BARREL, currentItemStack).getItem() != ModItems.ANGLED_GRIP.get());
				// Magazine
				case "default_mag", "default_mag_2" -> bone.setHidden(!Gun.getAttachment(IAttachment.Type.MAGAZINE, currentItemStack).isEmpty());
				case "extended_mag", "extended_mag_2" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.MAGAZINE, currentItemStack).getItem() != ModItems.EXTENDED_MAG.get());
				case "drum_mag", "drum_mag_2" -> bone.setHidden(Gun.getAttachment(IAttachment.Type.MAGAZINE, currentItemStack).getItem() != ModItems.DRUM_MAG.get());

            }

			if (this.renderType != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
				if (animatable.getDefaultInstance().getItem() == ModItems.FINGER_GUN.get()) {
					if (this.renderType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
						renderArms = false;
					}
					else {
						renderArms = true;
					}
				}
				else {
					renderArms = false;
				}
			}

			if (renderArms)
			{
				PlayerRenderer playerEntityRenderer = (PlayerRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
				PlayerModel<AbstractClientPlayer> playerEntityModel = playerEntityRenderer.getModel();

				RenderUtils.translateMatrixToBone(poseStack, bone);
				RenderUtils.translateToPivotPoint(poseStack, bone);
				RenderUtils.rotateMatrixAroundBone(poseStack, bone);
				RenderUtils.scaleMatrixForBone(poseStack, bone);
				RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

				ResourceLocation playerSkin = client.player.getSkinTextureLocation();
				VertexConsumer arm = this.bufferSource.getBuffer(RenderType.entitySolid(playerSkin));
				VertexConsumer sleeve = this.bufferSource.getBuffer(RenderType.entityTranslucent(playerSkin));

				Player player = client.player;
				Gun modifiedGun = ((GunItem) currentItemStack.getItem()).getModifiedGun(currentItemStack);
				final long id = GeoItem.getId(player.getMainHandItem());
				AnimationController<GeoAnimatable> animationController = animatable.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("controller");

				if (bone.getName().equals("left_arm") || bone.getName().equals("fake_left_arm")) {
					boolean isOneHanded = modifiedGun.getGeneral().getGripType().equals(GripType.ONE_HANDED);
					boolean isHoldingShield = player.getOffhandItem().getItem() instanceof ShieldItem;
					boolean isPlayingCriticalAnimation = animationController != null && animationController.getCurrentAnimation() != null
							&& (animationController.getCurrentAnimation().animation().name().matches("draw")
							|| animationController.getCurrentAnimation().animation().name().matches("reload")
							|| animationController.getCurrentAnimation().animation().name().matches("reload_start")
							|| animationController.getCurrentAnimation().animation().name().matches("reload_loop")
							|| animationController.getCurrentAnimation().animation().name().matches("reload_end"));

					//if (!isOneHanded || !isHoldingShield || isPlayingCriticalAnimation) {
						poseStack.scale(0.67f, 0.8f, 0.67f);
						poseStack.translate(-0.25, -0.1, 0.1625);
						playerEntityModel.leftArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
						playerEntityModel.leftArm.setRotation(0, 0, 0);
						playerEntityModel.leftArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

						playerEntityModel.leftSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
						playerEntityModel.leftSleeve.setRotation(0, 0, 0);
						playerEntityModel.leftSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
					//}
				}
				else if (bone.getName().equals("right_arm") || bone.getName().equals("fake_right_arm"))
				{
					poseStack.scale(0.67f, 0.8f, 0.67f);
					poseStack.translate(0.25, -0.1, 0.1625);
					playerEntityModel.rightArm.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
					playerEntityModel.rightArm.setRotation(0, 0, 0);
					playerEntityModel.rightArm.render(poseStack, arm, packedLight, packedOverlay, 1, 1, 1, 1);

					playerEntityModel.rightSleeve.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
					playerEntityModel.rightSleeve.setRotation(0, 0, 0);
					playerEntityModel.rightSleeve.render(poseStack, sleeve, packedLight, packedOverlay, 1, 1, 1, 1);
				}
			}
		}
		if (bone.getName().matches("attachment_bone"))
			this.renderAttachments(bone, currentItemStack, poseStack, renderType, buffer, packedLight, Minecraft.getInstance().getPartialTick(), packedOverlay);

		super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer1, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
		poseStack.popPose();
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
		poseStack.mulPose(Axis.YP.rotationDegrees((recoilSway * recoilReduction) / 5));
		poseStack.mulPose(Axis.ZP.rotationDegrees((recoilSway * recoilReduction) / 5));
		poseStack.mulPose(Axis.XP.rotationDegrees((recoilLift * recoilReduction) / 5));
		poseStack.translate(0, 0, -0.15);
	}

	private void applyBobbingTransforms(PoseStack poseStack, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.options.bobView().get() && mc.getCameraEntity() instanceof Player player)
		{
			float deltaDistanceWalked = player.walkDist - player.walkDistO;
			float distanceWalked = -(player.walkDist + deltaDistanceWalked * partialTicks);
			float bobbing = Mth.lerp(partialTicks, player.oBob, player.bob);

			/* Reverses the original bobbing rotations and translations, so it can be controlled */
			poseStack.mulPose(Axis.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F)));
			poseStack.mulPose(Axis.ZP.rotationDegrees(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F)));
			poseStack.translate(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 0.5F), -(-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * bobbing)), 0.0D);

			/* Slows down the bob by half */
			bobbing *= player.isSprinting() ? 8.0 : 4.0;
			bobbing *= Config.CLIENT.display.bobbingIntensity.get();

			/* The new controlled bobbing */
			double invertZoomProgress = 1.0 - AimingHandler.get().getNormalisedAdsProgress() * this.sprintIntensity;
			//poseStack.translate((double) (Mth.sin(distanceWalked * (float) Math.PI) * cameraYaw * 0.5F) * invertZoomProgress, (double) (-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * cameraYaw)) * invertZoomProgress, 0.0D);
			if (!AimingHandler.get().isAiming()) {
				poseStack.mulPose(Axis.XP.rotationDegrees((Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * -2.0F) * (float) invertZoomProgress));
				poseStack.mulPose(Axis.ZP.rotationDegrees((Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F) * (float) invertZoomProgress));
			}
		}
	}

	private void applyAimingTransforms(PoseStack poseStack, ItemStack stack, Gun modifiedGun, float x, float y, float z, int offset)
	{
		//if(!Config.CLIENT.display.oldAnimations.get())
		{
			poseStack.translate(x * offset, y, z);
			poseStack.translate(0, -0.25, 0.25);
			float aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180F));
			aiming = PropertyHelper.getSightAnimations(stack, modifiedGun).getAimTransformCurve().apply(aiming / 2);
			poseStack.mulPose(Axis.ZP.rotationDegrees(aiming * 10F * offset));
			poseStack.mulPose(Axis.XP.rotationDegrees(aiming * 8F));
			poseStack.mulPose(Axis.YP.rotationDegrees(aiming * 8F * offset));
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
			poseStack.mulPose(Config.CLIENT.display.swayType.get().getPitchRotation().rotationDegrees(swayPitch * Config.CLIENT.display.swaySensitivity.get().floatValue() / 2));

			float bobYaw = Mth.rotLerp(partialTicks, player.yBobO, player.yBob);
			float headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
			float swayYaw = headYaw - bobYaw;
			swayYaw *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
			poseStack.mulPose(Config.CLIENT.display.swayType.get().getYawRotation().rotationDegrees(swayYaw * Config.CLIENT.display.swaySensitivity.get().floatValue() / 2));

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

			poseStack.translate(-0.1 * transition, -0.3 * transition, 0.9 * transition);

			if ((Gun.getAttachment(IAttachment.Type.BARREL, mc.player.getMainHandItem()).getItem() instanceof SwordItem)) {
				//poseStack.translate(-0.25 * leftHanded * transition, -0.1 * transition, 0);
				poseStack.mulPose(Axis.XP.rotationDegrees(15F * transition));
			}
			else if (modifiedGun.getGeneral().getGripType().equals(GripType.TWO_HANDED)){
				poseStack.translate(-0.25 * leftHanded * transition, -0.1 * transition, 0);
				poseStack.mulPose(Axis.YP.rotationDegrees(45F * leftHanded * transition));
				poseStack.mulPose(Axis.XP.rotationDegrees(-25F * transition));
			}

		}
	}

	private void renderAttachments(GeoBone bone, ItemStack stack, PoseStack poseStack, RenderType renderType, VertexConsumer renderTypeBuffer, int light, float partialTicks, int packedOverlay)
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

					if (!attachmentStack.isEmpty())
					{
						poseStack.pushPose();

						/* Translates the attachment to a standard position by removing the origin */
						Vec3 origin = PropertyHelper.getModelOrigin(attachmentStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
						poseStack.translate(-origin.x * 0.0625, -origin.y * 0.0625, -origin.z * 0.0625);

						/* Translation to the origin on the weapon */
						Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
						//poseStack.translate(gunOrigin.x * 0.0625, gunOrigin.y * 0.0625 + 0.05633, gunOrigin.z * 0.0625 - 0.0223);
						poseStack.translate(gunOrigin.x * 0.0625, gunOrigin.y * 0.0625, gunOrigin.z * 0.0625);

						/* Translate to the position this attachment mounts on the weapon */
						Vec3 translation = PropertyHelper.getAttachmentPosition(stack, modifiedGun, type).subtract(gunOrigin);
						poseStack.translate(translation.x * 0.0625, translation.y * 0.0625, translation.z * 0.0625);

						/* Scales the attachment. Also translates the delta of the attachment origin to (8, 8, 8) since this is the centered origin for scaling */
						Vec3 scale = PropertyHelper.getAttachmentScale(stack, modifiedGun, type);
						Vec3 center = origin.subtract(8, 8, 8).scale(0.0625);
						poseStack.translate(center.x, center.y, center.z);
						poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
						poseStack.translate(-center.x, -center.y, -center.z);

						{
							// Gun Skin testing! "Paint Jobs"
                            ResourceLocation newTextureResource;
                            if (stack.hasTag() && (Gun.getAttachment(IAttachment.Type.PAINT_JOB, stack).getItem() instanceof PaintJobCanItem paintJobCanItem)) {
								newTextureResource = new ResourceLocation(Reference.MOD_ID, "textures/animated/attachment/paintjob/" + paintJobCanItem.getPaintJob() + "/" + attachmentStack.getItem() + ".png");
							} else {
								newTextureResource = new ResourceLocation(Reference.MOD_ID, "textures/animated/attachment/" + attachmentStack.getItem() + ".png");
							}
							newTextureResource = getValidTexture(newTextureResource, attachmentStack);

							// Update texture only if it has changed
							if (!newTextureResource.equals(oldTextureResource)) {

							}
							attachmentRenderer.updateTexture(newTextureResource);
							oldTextureResource = newTextureResource;

                            /*ResourceLocation newModelResource;
                            if (stack.hasTag() && (Gun.getAttachment(IAttachment.Type.PAINT_JOB, stack).getItem() instanceof PaintJobCanItem paintJobCanItem)) {
								newModelResource = new ResourceLocation(Reference.MOD_ID, "geo/item/attachment/paintjob/" + paintJobCanItem.getPaintJob() + "/" + attachmentStack.getItem() + ".geo.json");
							} else {
								newModelResource = new ResourceLocation(Reference.MOD_ID, "geo/item/attachment/" + attachmentStack.getItem() + ".geo.json");
							}
							newModelResource = getValidModel(newModelResource, attachmentStack);

							// Update model only if it has changed
							if (!newModelResource.equals(oldModelResource)) {
								attachmentRenderer.updateModel(newModelResource);
								oldModelResource = newModelResource;
							}*/

							attachmentRenderer.updateAttachment(attachmentStack);
							attachmentRenderer.renderForBone(poseStack, animatable, bone, renderType, bufferSource, renderTypeBuffer, partialTicks, light, packedOverlay);
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

		if(entity == null || !GunRenderingHandler.entityIdForMuzzleFlash.contains(entity.getId()))
			return;

		float randomValue = GunRenderingHandler.entityIdToRandomValue.get(entity.getId());
		this.drawMuzzleFlash(weapon, modifiedGun, randomValue, randomValue >= 0.5F, poseStack, buffer, partialTicks, display);
	}

	private void drawMuzzleFlash(ItemStack weapon, Gun modifiedGun, float random, boolean flip, PoseStack poseStack, MultiBufferSource buffer, float partialTicks, ItemDisplayContext displayContext)
	{
		if(!PropertyHelper.hasMuzzleFlash(weapon, modifiedGun))
			return;

		poseStack.pushPose();

		// Translate to the position where the muzzle flash should spawn
		Vec3 weaponOrigin = PropertyHelper.getModelOrigin(weapon, PropertyHelper.GUN_DEFAULT_ORIGIN);
		Vec3 flashPosition = PropertyHelper.getMuzzleFlashPosition(weapon, modifiedGun).subtract(weaponOrigin);
		poseStack.translate(weaponOrigin.x * 0.0625, weaponOrigin.y * 0.0625, weaponOrigin.z * 0.0625);
		poseStack.translate(flashPosition.x * 0.0625 + 0.5, flashPosition.y * 0.0625 + 1.025, flashPosition.z * 0.0625 + 0.525);
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

		float scale = 1.5F;
		poseStack.scale(scaleX * scale, scaleY * scale, 1.0F);

		float scaleModifier = (float) GunModifierHelper.getMuzzleFlashScale(weapon, 1.0);
		poseStack.scale(scaleModifier, scaleModifier, 1.0F);

		// Center the texture
		poseStack.translate(-0.5, -0.5, 0);

		float minU = weapon.isEnchanted() ? 0.5F : 0.0F;
		float maxU = weapon.isEnchanted() ? 1.0F : 0.5F;

		if (weapon.getItem() == ModItems.SUBSONIC_RIFLE.get() ||
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
}