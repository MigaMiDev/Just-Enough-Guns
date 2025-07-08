package ttv.migami.jeg.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.client.screen.widget.MiniButton;
import ttv.migami.jeg.client.util.RenderUtil;
import ttv.migami.jeg.common.container.AttachmentContainer;
import ttv.migami.jeg.common.container.slot.AttachmentSlot;
import ttv.migami.jeg.init.ModItems;
import ttv.migami.jeg.init.ModSounds;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.IAttachment;
import ttv.migami.jeg.item.attachment.item.KillEffectItem;
import ttv.migami.jeg.item.attachment.item.PaintJobCanItem;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.C2SMessageToggleMedals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class AttachmentScreen extends AbstractContainerScreen<AttachmentContainer>
{
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(Reference.MOD_ID, "textures/gui/attachments.png");
    private static final Component CONFIG_TOOLTIP = Component.translatable("jeg.button.config.tooltip");

    private final Inventory playerInventory;
    private final Container weaponInventory;

    public AttachmentScreen(AttachmentContainer screenContainer, Inventory playerInventory, Component titleIn)
    {
        super(screenContainer, playerInventory, titleIn);
        this.playerInventory = playerInventory;
        this.weaponInventory = screenContainer.getWeaponInventory();
        this.imageHeight = 184;

        //this.updateStats();
    }

    @Override
    protected void init()
    {
        super.init();

        List<MiniButton> buttons = this.gatherButtons();
        for(int i = 0; i < buttons.size(); i++)
        {
            MiniButton button = buttons.get(i);
            switch(Config.CLIENT.buttonAlignment.get())
            {
                case LEFT -> {
                    int titleWidth = this.minecraft.font.width(this.title);
                    button.setX(this.leftPos + titleWidth + 8 + 3 + i * 13);
                }
                case RIGHT -> {
                    button.setX(this.leftPos + this.imageWidth - 7 - 10 - (buttons.size() - 1 - i) * 13);
                }
            }
            button.setY(this.topPos + 90);
            this.addRenderableWidget(button);
        }
    }

    private List<MiniButton> gatherButtons()
    {
        List<MiniButton> buttons = new ArrayList<>();
        if(!Config.CLIENT.hideConfigButton.get())
        {
            MiniButton configButton = new MiniButton(0, 0, 192, 0, GUI_TEXTURES, onPress -> this.openConfigScreen());
            configButton.setTooltip(Tooltip.create(CONFIG_TOOLTIP));
            buttons.add(configButton);
        }
        return buttons;
    }

    @Override
    public void containerTick()
    {
        super.containerTick();
        if(this.minecraft != null && this.minecraft.player != null)
        {
            if(!(this.minecraft.player.getMainHandItem().getItem() instanceof GunItem))
            {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);

        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if(RenderUtil.isMouseWithin(mouseX, mouseY, startX - 67, startY + 100, 22, 22)) {
            pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("cutesy.jeg.thanks"), Component.literal("- MigaMi ♡").withStyle(ChatFormatting.BLUE)), mouseX, mouseY);
        }

        if(RenderUtil.isMouseWithin(mouseX, mouseY, startX - 31, startY + 148, 22, 22)) {
            if (Config.COMMON.gameplay.overrideHideMedals.get()) {
                pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.toggle_medals.disabled").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
            } else {
                pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.toggle_medals").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
            }
        }

        for(int i = 0; i < 6; i++)
        {
            int x = -64;
            int y = (8 + (i % 9) * 18) - 18;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, startX + x, startY + y, 18, 18))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                if(this.menu.getSlot(i).isActive() && this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty())
                {
                    if (RenderUtil.isMouseWithin(mouseX, mouseY, startX + x, startY + 26, 18, 18)) {
                        pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.attachment." + type.getTranslationKey()), Component.translatable("slot.jeg.attachment.swords").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
                    } else {
                        pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.attachment." + type.getTranslationKey())), mouseX, mouseY);
                    }
                }
                else if(!this.menu.getSlot(i).isActive())
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.attachment." + type.getTranslationKey()), Component.translatable("slot.jeg.attachment.not_applicable")), mouseX, mouseY);
                }
                else if(this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty() && !this.isCompatible(this.menu.getCarried(), slot))
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.attachment.incompatible").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
                }
            }
        }

        // Cosmetic slots
        for(int i = 6; i < IAttachment.Type.values().length; i++)
        {
            int x = -64;
            int y = (8 + 26 + (i % 9) * 18) - 18;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, startX + x, startY + y, 18, 18))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                if(this.menu.getSlot(i).isActive() && this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty())
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.attachment." + type.getTranslationKey())), mouseX, mouseY);
                }
                else if(!this.menu.getSlot(i).isActive())
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.attachment." + type.getTranslationKey()), Component.translatable("slot.jeg.attachment.not_applicable")), mouseX, mouseY);
                }
                else if(this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty() && !this.isCompatible(this.menu.getCarried(), slot))
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.jeg.attachment.incompatible").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack mainHandItem = minecraft.player.getMainHandItem();

        //pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        if (mainHandItem.getItem() instanceof GunItem) {
            String itemName = mainHandItem.getHoverName().getString();
            pGuiGraphics.drawCenteredString(this.font, itemName, this.imageWidth / 2, -42, mainHandItem.getRarity().color.getColor());

            String modId = ForgeRegistries.ITEMS.getKey(mainHandItem.getItem()).getNamespace();
            String modName = ModList.get().getModContainerById(modId)
                    .map(container -> container.getModInfo().getDisplayName())
                    .orElse("JEG: Add-on");

            if (mainHandItem.is(ModItems.ABSTRACT_GUN.get())) {
                modName = "JEG: Gun-Packs!";
            }

            pGuiGraphics.drawCenteredString(this.font, modName, this.imageWidth / 2, -30, 6843377);
        }
        pGuiGraphics.drawString(this.font, this.playerInventory.getDisplayName(), this.inventoryLabelX, this.inventoryLabelY + 19, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);
        pGuiGraphics.blit(GUI_TEXTURES, left - 71, top - 18, 203, 0, 32, 202);
        if (this.minecraft.player.getMainHandItem().getTag() != null && this.minecraft.player.getMainHandItem().getTag().getBoolean("MedalsEnabled")) {
            pGuiGraphics.blit(GUI_TEXTURES, left - 31, top + 148, 176, 161, 22, 22);
        } else {
            pGuiGraphics.blit(GUI_TEXTURES, left - 31, top + 148, 176, 183, 22, 22);
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.scissor(left - 40, top - 100, this.width, 85 + 100);

        //pGuiGraphics.enableScissor(left - 40, top - 100, this.width, 85 + 100);

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        {
            modelViewStack.translate(left + 88, top + 24, 500);
            modelViewStack.scale(75F, -75F, 75F);
            modelViewStack.mulPose(Axis.XP.rotationDegrees(5F));
            modelViewStack.mulPose(Axis.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + Minecraft.getInstance().getFrameTime()));
            RenderSystem.applyModelViewMatrix();
            MultiBufferSource.BufferSource buffer = this.minecraft.renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().render(this.minecraft.player.getMainHandItem(), ItemDisplayContext.FIXED, false, pGuiGraphics.pose(), buffer, 15728880, OverlayTexture.NO_OVERLAY, RenderUtil.getModel(this.minecraft.player.getMainHandItem()));
            buffer.endBatch();
        }
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();

        //pGuiGraphics.disableScissor();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        /* Draws the icons for each attachment slot. If not applicable
         * for the weapon, it will draw a cross instead. */
        for (int i = 0; i < 6; i++) {
            int x = -64;
            int y = (8 + (i % 9) * 18) - 18;
            if (!this.canPlaceAttachmentInSlot(this.menu.getCarried(), this.menu.getSlot(i))) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 176, 0, 16, 16);
            } else if (this.weaponInventory.getItem(i).isEmpty()) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 176, 16 + (i % 9) * 16, 16, 16);
            }
        }

        // Cosmetic slots
        for (int i = 6; i < IAttachment.Type.values().length; i++) {
            int x = -64;
            int y = (8 + 26 + (i % 9) * 18) - 18;
            if (!this.canPlaceAttachmentInSlot(this.menu.getCarried(), this.menu.getSlot(i))) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 176, 0, 16, 16);
            } else if (this.weaponInventory.getItem(i).isEmpty()) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 176, 16 + (i % 9) * 16, 16, 16);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2;
        int startY = (this.height - this.imageHeight) / 2;

        if (!Config.COMMON.gameplay.overrideHideMedals.get()) {
            if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX - 31, startY + 148, 22, 22))
            {
                if((button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
                {
                    this.toggleMedals();
                    Minecraft.getInstance().player.playSound(
                            ModSounds.MEDAL_GENERIC.get(),
                            1.0F,
                            1.0F
                    );
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleMedals() {
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageToggleMedals());
    }

    private boolean canPlaceAttachmentInSlot(ItemStack stack, Slot slot)
    {
        if(!slot.isActive())
            return false;

        if(!slot.equals(this.getSlotUnderMouse()))
            return true;

        if(!slot.getItem().isEmpty())
            return true;

        if(!(slot instanceof AttachmentSlot s))
            return true;

        // Not compatible check
        if (stack.getItem() instanceof SwordItem &&
                (stack.is(Items.WOODEN_SWORD) ||
                        stack.is(Items.STONE_SWORD) ||
                        stack.is(Items.IRON_SWORD) ||
                        stack.is(Items.GOLDEN_SWORD) ||
                        stack.is(Items.DIAMOND_SWORD) ||
                        stack.is(Items.NETHERITE_SWORD)))
            return true;

        if (stack.getItem() instanceof SpyglassItem &&
                (stack.is(Items.SPYGLASS)))
            return true;

        if(!(stack.getItem() instanceof IAttachment<?> a))
            return true;

        if(!s.getType().equals(a.getType()))
            return true;

        return s.mayPlace(stack);
    }

    private boolean isCompatible(ItemStack stack, AttachmentSlot slot)
    {
        if(stack.isEmpty())
            return true;

        // Not compatible check
        if (stack.getItem() instanceof SwordItem)
            return true;

        if (stack.getItem() instanceof SpyglassItem)
            return true;

        if (stack.getItem() instanceof PaintJobCanItem)
            return true;

        if (stack.getItem() instanceof DyeItem)
            return true;

        if (stack.getItem() instanceof KillEffectItem)
            return true;

        if(!(stack.getItem() instanceof IAttachment<?> attachment))
            return false;

        if(!attachment.getType().equals(slot.getType()))
            return true;

        if(!attachment.canAttachTo(stack))
            return false;

        return slot.mayPlace(stack);
    }

    private void openConfigScreen()
    {
        ModList.get().getModContainerById(Reference.MOD_ID).ifPresent(container ->
        {
            Screen screen = container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class).map(function -> function.screenFunction().apply(this.minecraft, null)).orElse(null);
            if(screen != null)
            {
                this.minecraft.setScreen(screen);
            }
            else if(this.minecraft != null && this.minecraft.player != null)
            {
                MutableComponent modName = Component.literal("Configured");
                modName.setStyle(modName.getStyle()
                        .withColor(ChatFormatting.YELLOW)
                        .withUnderlined(true)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("jeg.chat.open_curseforge_page")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured")));
                Component message = Component.translatable("jeg.chat.install_configured", modName);
                this.minecraft.player.displayClientMessage(message, false);
            }
        });
    }
}
