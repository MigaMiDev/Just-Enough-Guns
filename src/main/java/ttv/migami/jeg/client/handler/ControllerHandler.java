package ttv.migami.jeg.client.handler;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ttv.migami.jeg.Config;
import ttv.migami.jeg.client.GunButtonBindings;
import ttv.migami.jeg.client.screen.GunmetalWorkbenchScreen;
import ttv.migami.jeg.client.screen.GunniteWorkbenchScreen;
import ttv.migami.jeg.client.screen.ScrapWorkbenchScreen;
import ttv.migami.jeg.common.FireMode;
import ttv.migami.jeg.common.Gun;
import ttv.migami.jeg.init.ModSyncedDataKeys;
import ttv.migami.jeg.item.GunItem;
import ttv.migami.jeg.item.attachment.impl.Scope;
import ttv.migami.jeg.network.PacketHandler;
import ttv.migami.jeg.network.message.C2SMessageAttachments;
import ttv.migami.jeg.network.message.C2SMessageMelee;
import ttv.migami.jeg.network.message.C2SMessageUnload;
import ttv.migami.jeg.util.GunModifierHelper;

/**
 * Author: MrCrayfish
 */
public class ControllerHandler
{
    private static int reloadCounter = -1;

    // Crash fix proposed by Mikhail Tapio!
    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ControllerHandler());
        ControllerEvents.INPUT.register((controller, newButton, originalButton, state) -> {
            Player player = Minecraft.getInstance().player;
            Level world = Minecraft.getInstance().level;
            boolean shouldCancel = false;
            if(player != null && world != null && Minecraft.getInstance().screen == null)
            {
                ItemStack heldItem = player.getMainHandItem();
                if(originalButton == GunButtonBindings.SHOOT.getButton())
                {
                    if(heldItem.getItem() instanceof GunItem)
                    {
                        shouldCancel = true;
                        if(state)
                        {
                            ShootingHandler.get().fire(player, heldItem);
                        }
                    }
                }
                else if(originalButton == GunButtonBindings.AIM.getButton())
                {
                    if(heldItem.getItem() instanceof GunItem)
                    {
                        shouldCancel = true;
                    }
                }
                else if(originalButton == GunButtonBindings.STEADY_AIM.getButton())
                {
                    if(heldItem.getItem() instanceof GunItem)
                    {
                        shouldCancel = true;
                        if(state && !ControllerHandler.isAiming())
                        //if(state)
                        {
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMelee());
                        }
                    }
                }
                else if(originalButton == GunButtonBindings.RELOAD.getButton())
                {
                    if(heldItem.getItem() instanceof GunItem)
                    {
                        shouldCancel = true;
                        if(state)
                        {
                            ControllerHandler.reloadCounter = 0;
                        }
                    }
                }
                else if(originalButton == GunButtonBindings.OPEN_ATTACHMENTS.getButton())
                {
                    if(heldItem.getItem() instanceof GunItem && Minecraft.getInstance().screen == null)
                    {
                        shouldCancel = true;
                        if(state)
                        {
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
                        }
                    }
                }
            }
            return shouldCancel;
        });
        ControllerEvents.UPDATE_CAMERA.register((yawSpeed, pitchSpeed) -> {
            Player player = Minecraft.getInstance().player;
            if(player != null)
            {
                ItemStack heldItem = player.getMainHandItem();
                if(heldItem.getItem() instanceof GunItem && AimingHandler.get().isAiming())
                {
                    double adsSensitivity = Config.CLIENT.controls.aimDownSightSensitivity.get();
                    yawSpeed.set(10.0F * (float) adsSensitivity);
                    pitchSpeed.set(7.5F * (float) adsSensitivity);

                    Scope scope = Gun.getScope(heldItem);
                    Controller controller = Controllable.getController();
                    if(scope != null && controller != null && controller.isButtonPressed(GunButtonBindings.STEADY_AIM.getButton()))
                    {
                        yawSpeed.set(yawSpeed.get() / 2.0F);
                        pitchSpeed.set(pitchSpeed.get() / 2.0F);
                    }
                }
            }
            return false;
        });
        ControllerEvents.GATHER_ACTIONS.register((actions, visibility) -> {
            Minecraft mc = Minecraft.getInstance();
            if(mc.screen != null) return;

            Player player = Minecraft.getInstance().player;
            if(player != null)
            {
                ItemStack heldItem = player.getMainHandItem();
                if(heldItem.getItem() instanceof GunItem)
                {
                    actions.put(GunButtonBindings.AIM, new Action(Component.translatable("jeg.action.aim"), Action.Side.RIGHT));
                    actions.put(GunButtonBindings.SHOOT, new Action(Component.translatable("jeg.action.shoot"), Action.Side.RIGHT));

                    GunItem gunItem = (GunItem) heldItem.getItem();
                    Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                    CompoundTag tag = heldItem.getTag();
                    if(tag != null && tag.getInt("AmmoCount") < GunModifierHelper.getModifiedAmmoCapacity(heldItem, modifiedGun))
                    {
                        actions.put(GunButtonBindings.RELOAD, new Action(Component.translatable("jeg.action.reload"), Action.Side.LEFT));
                    }

                    Scope scope = Gun.getScope(heldItem);
                    if(scope != null && AimingHandler.get().isAiming())
                    {
                        actions.put(GunButtonBindings.STEADY_AIM, new Action(Component.translatable("jeg.action.steady_aim"), Action.Side.RIGHT));
                    }
                }
            }
        });
        ControllerEvents.GATHER_NAVIGATION_POINTS.register(points -> {
            Minecraft mc = Minecraft.getInstance();
            // TODO: Replace with an optimal fix!
            // (Probably will have to make an Abstract Workbench Screen!
            if(mc.screen instanceof ScrapWorkbenchScreen)
            {
                ScrapWorkbenchScreen workbench = (ScrapWorkbenchScreen) mc.screen;
                int startX = workbench.getGuiLeft();
                int startY = workbench.getGuiTop();

                for(int i = 0; i < workbench.getTabs().size(); i++)
                {
                    int tabX = startX + 28 * i + (28 / 2);
                    int tabY = startY - (28 / 2);
                    points.add(new BasicNavigationPoint(tabX, tabY));
                }

                for(int i = 0; i < 6; i++)
                {
                    int itemX = startX + 172 + (80 / 2);
                    int itemY = startY + i * 19 + 63 + (19 / 2);
                    points.add(new BasicNavigationPoint(itemX, itemY));
                }
            }
            if(mc.screen instanceof GunmetalWorkbenchScreen)
            {
                GunmetalWorkbenchScreen workbench = (GunmetalWorkbenchScreen) mc.screen;
                int startX = workbench.getGuiLeft();
                int startY = workbench.getGuiTop();

                for(int i = 0; i < workbench.getTabs().size(); i++)
                {
                    int tabX = startX + 28 * i + (28 / 2);
                    int tabY = startY - (28 / 2);
                    points.add(new BasicNavigationPoint(tabX, tabY));
                }

                for(int i = 0; i < 6; i++)
                {
                    int itemX = startX + 172 + (80 / 2);
                    int itemY = startY + i * 19 + 63 + (19 / 2);
                    points.add(new BasicNavigationPoint(itemX, itemY));
                }
            }
            if(mc.screen instanceof GunniteWorkbenchScreen)
            {
                GunniteWorkbenchScreen workbench = (GunniteWorkbenchScreen) mc.screen;
                int startX = workbench.getGuiLeft();
                int startY = workbench.getGuiTop();

                for(int i = 0; i < workbench.getTabs().size(); i++)
                {
                    int tabX = startX + 28 * i + (28 / 2);
                    int tabY = startY - (28 / 2);
                    points.add(new BasicNavigationPoint(tabX, tabY));
                }

                for(int i = 0; i < 6; i++)
                {
                    int itemX = startX + 172 + (80 / 2);
                    int itemY = startY + i * 19 + 63 + (19 / 2);
                    points.add(new BasicNavigationPoint(itemX, itemY));
                }
            }
        });
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event)
    {
        Controller controller = Controllable.getController();
        if(controller == null)
            return;

        if(event.phase == TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;

        if(controller.isButtonPressed(GunButtonBindings.SHOOT.getButton()) && Minecraft.getInstance().screen == null)
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem)
            {
                Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                if(gun.getGeneral().getFireMode() == FireMode.AUTOMATIC)
                {
                    ShootingHandler.get().fire(player, heldItem);
                }
            }
        }

        if(mc.screen == null && reloadCounter != -1)
        {
            if(controller.isButtonPressed(GunButtonBindings.RELOAD.getButton()))
            {
                reloadCounter++;
            }
        }

        if(reloadCounter > 40)
        {
            ReloadHandler.get().setReloading(false);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
            reloadCounter = -1;
        }
        else if(reloadCounter > 0 && !controller.isButtonPressed(GunButtonBindings.RELOAD.getButton()))
        {
            ReloadHandler.get().setReloading(!ModSyncedDataKeys.RELOADING.getValue(player));
            reloadCounter = -1;
        }
    }

    public static boolean isAiming()
    {
        Controller controller = Controllable.getController();
        return controller != null && controller.isButtonPressed(GunButtonBindings.AIM.getButton());
    }

    public static boolean isShooting()
    {
        Controller controller = Controllable.getController();
        return controller != null && controller.isButtonPressed(GunButtonBindings.SHOOT.getButton());
    }
}