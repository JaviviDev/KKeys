package me.javivi.kkeys.events;

import me.javivi.kkeys.kkeys;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = kkeys.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof KeyBindsScreen) {
            kkeys.getInstance().getConfig().filterKeyBindings();
        }
    }
}