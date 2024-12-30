package ttv.migami.jeg.client.screen;

/*@OnlyIn(Dist.CLIENT)
public class BasicTurretScreen extends AbstractContainerScreen<BasicTurretContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/basic_turret.png");
    public BasicTurretScreen(BasicTurretContainer menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 114 + 3 * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        BasicTurretBlockEntity turret = menu.getBlockEntity();
        String ownerName = turret.getOwnerName();

        guiGraphics.drawString(this.font, "Owner: " + ownerName, this.leftPos + 80, this.topPos + 6, 0x404040, false);
    }

}*/