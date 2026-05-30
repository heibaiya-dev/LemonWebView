package com.heibai.lemonwebview;

import com.heibai.lemonwebview.listeners.LemonInitListener;
import javafx.application.Platform;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LemonWebView {
    public static final Logger LOGGER = LoggerFactory.getLogger("LemonWebView");
    private static final ArrayList<LemonInitListener> awaitingInit = new ArrayList<>();
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static LemonWebViewClient client;

    public static void scheduleForInit(LemonInitListener task) {
        awaitingInit.add(task);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static boolean initialize() {
        LemonWebView.getLogger().info("Initializing JavaFX WebView...");
        
        if (initialized.get()) {
            return true;
        }

        try {
            CompletableFuture<Boolean> initFuture = new CompletableFuture<>();
            
            Thread javafxThread = new Thread(() -> {
                try {
                    Platform.startup(() -> {
                        LemonWebView.getLogger().info("JavaFX Platform started successfully");
                        initFuture.complete(true);
                    });
                } catch (Exception e) {
                    LemonWebView.getLogger().error("Failed to start JavaFX Platform", e);
                    initFuture.complete(false);
                }
            }, "LemonWebView-JavaFX-Init");
            javafxThread.setDaemon(true);
            javafxThread.start();

            Boolean success = initFuture.get();
            
            if (success) {
                client = new LemonWebViewClient();
                initialized.set(true);
                
                awaitingInit.forEach(t -> t.onInit(true));
                awaitingInit.clear();
                LemonWebView.getLogger().info("JavaFX WebView initialized successfully");
                
                Runtime.getRuntime().addShutdownHook(new Thread(LemonWebView::shutdown, "LemonWebView-Shutdown"));
                
                return true;
            }
        } catch (Exception e) {
            LemonWebView.getLogger().error("Could not initialize JavaFX WebView", e);
        }

        awaitingInit.forEach(t -> t.onInit(false));
        awaitingInit.clear();
        LemonWebView.getLogger().error("Could not initialize JavaFX WebView");
        shutdown();
        return false;
    }

    public static LemonWebViewClient getClient() {
        assertInitialized();
        return client;
    }

    public static LemonBrowser createBrowser(String url, boolean transparent) {
        assertInitialized();
        LemonBrowser browser = new LemonBrowser(url, transparent);
        return browser;
    }

    public static LemonBrowser createBrowser(String url, boolean transparent, int width, int height) {
        assertInitialized();
        LemonBrowser browser = new LemonBrowser(url, transparent);
        browser.resize(width, height);
        return browser;
    }

    public static boolean isInitialized() {
        return initialized.get();
    }

    public static void shutdown() {
        if (isInitialized()) {
            LemonWebView.getLogger().info("Shutting down JavaFX WebView...");
            Platform.exit();
            client = null;
            initialized.set(false);
            LemonWebView.getLogger().info("JavaFX WebView shutdown complete");
        }
    }

    private static void assertInitialized() {
        if (!isInitialized()) {
            throw new RuntimeException("JavaFX WebView was never initialized.");
        }
    }
}