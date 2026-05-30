package com.heibai.lemonwebview.mixins;

import com.heibai.lemonwebview.LemonWebView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(Minecraft.class)
public abstract class WebViewInitMixin {
    @Shadow
    public abstract void setScreen(@Nullable Screen guiScreen);

    @Unique
    private static AtomicBoolean recursionDetector = new AtomicBoolean(false);

    @Inject(at = @At("HEAD"), method = "setScreen", cancellable = true)
    public void onSetScreen(Screen guiScreen, CallbackInfo ci) {
        if (!LemonWebView.isInitialized()) {
            boolean recursionValue = recursionDetector.get();
            recursionDetector.set(true);

            if (!recursionValue) {
                LemonWebView.getLogger().debug("LemonWebView is attempting to initialize.");
                Minecraft.getInstance().execute(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        LemonWebView.getLogger().error("Interrupted during initialization", e);
                    }
                    LemonWebView.initialize();
                });
            }

            recursionDetector.set(recursionValue);
        }
    }
}