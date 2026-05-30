package com.heibai.lemonwebview;

import com.heibai.lemonwebview.example.LemonExampleScreen;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod(LemonWebViewMod.MODID)
public class LemonWebViewMod {
    public static final String MODID = "lemonwebview";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicBoolean exampleEnabled = new AtomicBoolean(false);

    public LemonWebViewMod(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyMappings);
        
        NeoForge.EVENT_BUS.addListener(this::clientTick);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("LemonWebView mod initialized");
    }

    private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
    }

    private void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (GLFW.glfwGetKey(
                    net.minecraft.client.Minecraft.getInstance().getWindow().getWindow(),
                    GLFW.GLFW_KEY_F10) == GLFW.GLFW_PRESS) {
                if (!exampleEnabled.get()) {
                    exampleEnabled.set(true);
                    if (LemonWebView.isInitialized()) {
                        net.minecraft.client.Minecraft.getInstance().setScreen(
                                new LemonExampleScreen(net.minecraft.network.chat.Component.literal("LemonWebView Example")));
                    }
                }
            } else {
                exampleEnabled.set(false);
            }
        }
    }
}