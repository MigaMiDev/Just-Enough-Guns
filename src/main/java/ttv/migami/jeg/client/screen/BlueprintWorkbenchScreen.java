package ttv.migami.jeg.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.opengl.GL11;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.blockentity.BlueprintWorkbenchBlockEntity;
import ttv.migami.jeg.client.util.RecipeHelper;
import ttv.migami.jeg.client.util.RenderUtil;
import ttv.migami.jeg.common.container.BlueprintWorkbenchContainer;
import ttv.migami.jeg.crafting.workbench.BlueprintWorkbenchRecipe;
import ttv.migami.jeg.crafting.workbench.WorkbenchIngredient;
import ttv.migami.jeg.crafting.workbench.WorkbenchRecipes;
import ttv.migami.jeg.init.ModRecipeTypes;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.C2SMessageCraft;
import ttv.migami.jeg.util.InventoryUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class BlueprintWorkbenchScreen extends AbstractContainerScreen<BlueprintWorkbenchContainer> {
    private static final ResourceLocation GUI_BASE = new ResourceLocation(Reference.MOD_ID, "textures/gui/workbench.png");
    private static boolean showRemaining = false;

    private final List<MaterialItem> materials;
    private List<MaterialItem> filteredMaterials;
    private final Inventory playerInventory;
    private final BlueprintWorkbenchBlockEntity workbench;
    private Button btnCraft;
    private CheckBox checkBoxMaterials;
    private ItemStack displayStack;
    private ItemStack oldStack;
    private ItemStack newStack;
    private boolean changed;

    public BlueprintWorkbenchScreen(BlueprintWorkbenchContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.playerInventory = playerInventory;
        this.workbench = (BlueprintWorkbenchBlockEntity) container.getWorkbench();
        this.imageWidth = 275;
        this.imageHeight = 184;
        this.materials = new ArrayList<>();
        this.createRecipeFromBlueprint();
    }

    private void createRecipeFromBlueprint() {
        ItemStack blueprintStack = this.workbench.getItem(0);
        if (!blueprintStack.isEmpty() && blueprintStack.hasTag() && blueprintStack.getTag().contains("Namespace") && blueprintStack.getTag().contains("Path")) {
            String gunRecipeId = blueprintStack.getTag().getString("Gun");
            NonNullList<BlueprintWorkbenchRecipe> recipes = WorkbenchRecipes.getAll(playerInventory.player.level(), ModRecipeTypes.BLUEPRINT_WORKBENCH.get());

            ResourceLocation itemLocation = new ResourceLocation(blueprintStack.getTag().getString("Namespace"), blueprintStack.getTag().getString("Path"));
            Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

            for (BlueprintWorkbenchRecipe recipe : recipes) {
                if (recipe.getId().toString().equals(gunRecipeId)) {
                    this.displayStack = item.getDefaultInstance();
                    this.loadRecipe(recipe);
                    return;
                }
            }
        }
        this.displayStack = ItemStack.EMPTY;
    }

    private void loadRecipe(BlueprintWorkbenchRecipe recipe) {
        this.materials.clear();
        List<WorkbenchIngredient> ingredients = recipe.getMaterials();
        if (ingredients != null) {
            for (WorkbenchIngredient ingredient : ingredients) {
                MaterialItem item = new MaterialItem(ingredient);
                item.updateEnabledState();
                this.materials.add(item);
            }
        }
    }

    @Override
    public void init() {
        super.init();
        this.btnCraft = this.addRenderableWidget(Button.builder(Component.translatable("gui.jeg.workbench.assemble"), button -> {
            BlueprintWorkbenchRecipe recipe = this.getCurrentRecipe();
            if (recipe != null) {
                ResourceLocation registryName = recipe.getId();
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageCraft(registryName, this.workbench.getBlockPos()));
            }
        }).pos(this.leftPos + 195, this.topPos + 16).size(74, 20).build());
        this.btnCraft.active = false;
        this.checkBoxMaterials = this.addRenderableWidget(new CheckBox(this.leftPos + 172, this.topPos + 51, Component.translatable("gui.jeg.workbench.show_remaining")));
        this.checkBoxMaterials.setToggled(BlueprintWorkbenchScreen.showRemaining);
    }

    private BlueprintWorkbenchRecipe getCurrentRecipe() {
        ItemStack blueprintStack = this.workbench.getItem(0);
        if (!blueprintStack.isEmpty() && blueprintStack.hasTag()) {
            String namespace = blueprintStack.getTag().getString("Namespace");
            String path = blueprintStack.getTag().getString("Path");
            
            return RecipeHelper.getRecipeFromItem(namespace, path, WorkbenchRecipes.getAll(playerInventory.player.level(), ModRecipeTypes.BLUEPRINT_WORKBENCH.get()));
        }
        return null;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        for (MaterialItem material : this.materials) {
            material.tick();
        }
        boolean canCraft = this.materials.stream().allMatch(MaterialItem::isEnabled);
        this.btnCraft.active = canCraft;
        this.updateItem();
    }

    private void updateItem() {
        if (!this.workbench.getItem(0).isEmpty() && this.workbench.getItem(0).hasTag()) {
            ItemStack blueprintStack = this.workbench.getItem(0);

            String namespace = blueprintStack.getTag().getString("Namespace");
            String path = blueprintStack.getTag().getString("Path");

            ResourceLocation itemLocation = new ResourceLocation(namespace, path);
            Item item = ForgeRegistries.ITEMS.getValue(itemLocation);

            if (item != Items.AIR) {
                this.displayStack = item.getDefaultInstance();
            }

            this.oldStack = this.workbench.getItem(0);
            if (this.newStack != this.oldStack) {
                this.loadItem(namespace, path);
            }
        } else {
            this.displayStack = null;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        BlueprintWorkbenchScreen.showRemaining = this.checkBoxMaterials.isToggled();
        return result;
    }

    private void loadItem(String namespace, String path)
    {
        BlueprintWorkbenchRecipe recipe = RecipeHelper.getRecipeFromItem(namespace, path, WorkbenchRecipes.getAll(playerInventory.player.level(), ModRecipeTypes.BLUEPRINT_WORKBENCH.get()));

        this.materials.clear();

        //if (recipe != null && recipe.getItem().getItem() instanceof GunItem) {
        if (recipe != null) {
            List<WorkbenchIngredient> ingredients = recipe.getMaterials();
            if(ingredients != null)
            {
                for(WorkbenchIngredient ingredient : ingredients)
                {
                    MaterialItem item = new MaterialItem(ingredient);
                    item.updateEnabledState();
                    this.materials.add(item);
                }
            }

            this.newStack = this.oldStack;
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY);
        int startX = this.leftPos;
        int startY = this.topPos;

        if (this.displayStack != null) {
            if (this.filteredMaterials != null && !this.filteredMaterials.isEmpty()) {
                for (int i = 0; i < this.filteredMaterials.size(); i++) {
                    int itemX = startX + 172;
                    int itemY = startY + i * 19 + 63;
                    if (RenderUtil.isMouseWithin(mouseX, mouseY, itemX, itemY, 80, 19)) {
                        MaterialItem materialItem = this.filteredMaterials.get(i);
                        if (materialItem != MaterialItem.EMPTY) {
                            pGuiGraphics.renderTooltip(this.font, materialItem.getDisplayStack(), mouseX, mouseY);
                            return;
                        }
                    }
                }
            }
            if (RenderUtil.isMouseWithin(mouseX, mouseY, startX + 8, startY + 38, 160, 48)) {
                pGuiGraphics.renderTooltip(this.font, this.displayStack, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY + 19, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        partialTicks = Minecraft.getInstance().getFrameTime();

        int startX = this.leftPos;
        int startY = this.topPos;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_BASE);

        pGuiGraphics.blit(GUI_BASE, startX, startY, 0, 0, 173, 184);
        pGuiGraphics.blit(GUI_BASE, startX + 173, startY, 78, 184, 173, 0, 1, 184, 256, 256);
        pGuiGraphics.blit(GUI_BASE, startX + 251, startY, 174, 0, 24, 184);
        pGuiGraphics.blit(GUI_BASE, startX + 171, startY + 15, 218, 0, 22, 22);

        if (this.workbench.getItem(0).isEmpty()) {
            pGuiGraphics.blit(GUI_BASE, startX + 174, startY + 18, 180, 199, 16, 16);
        }

        if (this.displayStack != null) {
            ItemStack currentItem = this.displayStack;
            StringBuilder builder = new StringBuilder(currentItem.getHoverName().getString());
            if (currentItem.getCount() > 1) {
                builder.append(ChatFormatting.GOLD);
                builder.append(ChatFormatting.BOLD);
                builder.append(" x ");
                builder.append(currentItem.getCount());
            }
            pGuiGraphics.drawCenteredString(this.font, builder.toString(), startX + 88, startY + 22, Color.WHITE.getRGB());

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(startX + 8, startY + 17, 160, 70);

            PoseStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushPose();
            {
                modelViewStack.translate(startX + 88, startY + 60, 100);
                modelViewStack.scale(50F, -50F, 50F);
                modelViewStack.mulPose(Axis.XP.rotationDegrees(5F));
                modelViewStack.mulPose(Axis.YP.rotationDegrees(Minecraft.getInstance().player.tickCount + partialTicks));
                RenderSystem.applyModelViewMatrix();
                MultiBufferSource.BufferSource buffer = this.minecraft.renderBuffers().bufferSource();
                Minecraft.getInstance().getItemRenderer().render(currentItem, ItemDisplayContext.FIXED, false, pGuiGraphics.pose(), buffer, 15728880, OverlayTexture.NO_OVERLAY, RenderUtil.getModel(currentItem));
                buffer.endBatch();
            }
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            this.filteredMaterials = this.getMaterials();
            for (int i = 0; i < this.filteredMaterials.size(); i++) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, GUI_BASE);

                MaterialItem materialItem = this.filteredMaterials.get(i);
                ItemStack stack = materialItem.getDisplayStack();
                if (!stack.isEmpty()) {
                    Lighting.setupForFlatItems();
                    if (materialItem.isEnabled()) {
                        pGuiGraphics.blit(GUI_BASE,startX + 172, startY + i * 19 + 63, 0, 184, 80, 19);
                    } else {
                        pGuiGraphics.blit(GUI_BASE,startX + 172, startY + i * 19 + 63, 0, 222, 80, 19);
                    }

                    String name = stack.getHoverName().getString();
                    if (this.font.width(name) > 55) {
                        name = this.font.plainSubstrByWidth(name, 50).trim() + "...";
                    }
                    pGuiGraphics.drawString(this.font, name, startX + 172 + 22, startY + i * 19 + 6 + 63, Color.WHITE.getRGB());

                    pGuiGraphics.renderItem(stack, startX + 172 + 2, startY + i * 19 + 1 + 63);

                    if (this.checkBoxMaterials.isToggled()) {
                        int count = InventoryUtil.getItemStackAmount(Minecraft.getInstance().player, stack);
                        stack = stack.copy();
                        stack.setCount(stack.getCount() - count);
                    }

                    pGuiGraphics.renderItemDecorations(this.font, stack, startX + 172 + 2, startY + i * 19 + 1 + 63);
                }
            }
        }
    }

    private List<MaterialItem> getMaterials() {
        List<MaterialItem> materials = NonNullList.withSize(6, MaterialItem.EMPTY);
        List<MaterialItem> filteredMaterials = this.materials.stream().filter(materialItem -> this.checkBoxMaterials.isToggled() ? !materialItem.isEnabled() : materialItem != MaterialItem.EMPTY).collect(Collectors.toList());
        for(int i = 0; i < filteredMaterials.size() && i < materials.size(); i++)
        {
            materials.set(i, filteredMaterials.get(i));
        }
        return materials;
    }

    public static class MaterialItem
    {
        public static final MaterialItem EMPTY = new MaterialItem();

        private long lastTime = System.currentTimeMillis();
        private int displayIndex;
        private boolean enabled = false;
        private WorkbenchIngredient ingredient;
        private final List<ItemStack> displayStacks = new ArrayList<>();

        private MaterialItem() {}

        private MaterialItem(WorkbenchIngredient ingredient)
        {
            this.ingredient = ingredient;
            Stream.of(ingredient.getItems()).forEach(stack -> {
                ItemStack displayStack = stack.copy();
                displayStack.setCount(ingredient.getCount());
                this.displayStacks.add(displayStack);
            });
        }

        public WorkbenchIngredient getIngredient()
        {
            return this.ingredient;
        }

        public void tick()
        {
            if(this.ingredient == null)
                return;

            this.updateEnabledState();
            long currentTime = System.currentTimeMillis();
            if(currentTime - this.lastTime >= 1000)
            {
                this.displayIndex = (this.displayIndex + 1) % this.displayStacks.size();
                this.lastTime = currentTime;
            }
        }

        public ItemStack getDisplayStack()
        {
            return this.ingredient != null ? this.displayStacks.get(this.displayIndex) : ItemStack.EMPTY;
        }

        public void updateEnabledState()
        {
            this.enabled = InventoryUtil.hasWorkstationIngredient(Minecraft.getInstance().player, this.ingredient);
        }

        public boolean isEnabled()
        {
            return this.ingredient == null || this.enabled;
        }
    }
}
