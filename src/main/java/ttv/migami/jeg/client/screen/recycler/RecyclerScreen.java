package ttv.migami.jeg.client.screen.recycler;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ttv.migami.jeg.Reference;
import ttv.migami.jeg.common.container.recycler.RecyclerMenu;

@OnlyIn(Dist.CLIENT)
public class RecyclerScreen extends AbstractRecyclerScreen<RecyclerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/recycler.png");

    public RecyclerScreen(RecyclerMenu recyclerMenu, Inventory inventory, Component component) {
        super(recyclerMenu, inventory, component, TEXTURE);
    }
}