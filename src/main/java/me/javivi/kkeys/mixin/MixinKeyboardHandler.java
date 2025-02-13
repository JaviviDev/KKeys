package me.javivi.kkeys.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import me.javivi.kkeys.Config;
import me.javivi.kkeys.kkeys;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().getWindow()) {
            String keyName = InputConstants.getKey(key, scancode).getName();
            Config config = kkeys.getInstance().getConfig();
            if (config != null && config.isKeyDisabled(keyName)) {
                ci.cancel();
            }
        }
    }
}
