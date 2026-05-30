package com.heibai.lemonwebview.mixins;

import com.heibai.lemonwebview.LemonWebView;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class WebViewRenderUpdateMixin {
    @Inject(at = @At("HEAD"), method = "render")
    public void preRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
    }
}