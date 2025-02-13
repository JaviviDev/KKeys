package me.javivi.kkeys.network;

import me.javivi.kkeys.Config;
import me.javivi.kkeys.kkeys;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class UpdateKeysPacket {
    private final String key;
    private final String action; // "hide", "unhide", "block", "unblock"
    
    public UpdateKeysPacket(String key, String action) {
        this.key = key;
        this.action = action;
    }
    
    public static void encode(UpdateKeysPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.key);
        buffer.writeUtf(packet.action);
    }
    
    public static UpdateKeysPacket decode(FriendlyByteBuf buffer) {
        return new UpdateKeysPacket(buffer.readUtf(), buffer.readUtf());
    }
    
    public static void handle(UpdateKeysPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Usando DistExecutor para asegurarnos que el código del cliente solo se ejecuta en el cliente
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet));
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(UpdateKeysPacket packet) {
        Config config = kkeys.getInstance().getConfig();
        Set<String> hiddenKeybinds = new HashSet<>(config.getHiddenKeybinds());
        Set<String> disabledKeys = new HashSet<>(config.getDisabledKeys());
        
        switch (packet.action) {
            case "hide" -> hiddenKeybinds.add(packet.key);
            case "unhide" -> hiddenKeybinds.remove(packet.key);
            case "block" -> disabledKeys.add(packet.key);
            case "unblock" -> disabledKeys.remove(packet.key);
        }
        
        config.setHiddenKeybinds(hiddenKeybinds);
        config.setDisabledKeys(disabledKeys);
        config.saveConfig();
        
        if (packet.action.equals("hide") || packet.action.equals("unhide")) {
            // Actualizar la pantalla de controles si está abierta
            if (Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.controls.KeyBindsScreen) {
                config.filterKeyBindings();
            }
        }
    }
}