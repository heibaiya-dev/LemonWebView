package com.heibai.lemonwebview;

import com.heibai.lemonwebview.listeners.LemonCursorChangeListener;
import com.mojang.blaze3d.systems.RenderSystem;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.*;

public class LemonBrowser {
    private final LemonRenderer renderer;
    private final WebView webView;
    private final WebEngine webEngine;
    private LemonCursorChangeListener cursorChangeListener;
    private boolean browserControls = true;
    private int lastWidth = 0, lastHeight = 0;
    private int btnMask = 0;
    private double deviceScaleFactor = 1;
    private boolean autoDSF = true;
    private final AtomicBoolean focused = new AtomicBoolean(false);
    private volatile boolean isReady = false;

    public LemonBrowser(String url, boolean transparent) {
        this.renderer = new LemonRenderer(transparent);
        this.cursorChangeListener = (cursorID) -> setCursor(cursorID);
        
        Minecraft.getInstance().submit(renderer::initialize);
        
        Platform.runLater(() -> {
            webView = new WebView();
            webEngine = webView.getEngine();
            
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    LemonWebView.LOGGER.info("Page loaded: " + webEngine.getLocation());
                }
            });
            
            if (url != null && !url.isEmpty()) {
                webEngine.load(url);
            }
            
            isReady = true;
        });
    }

    public LemonRenderer getRenderer() {
        return renderer;
    }

    public void setAutoDSF(boolean autoDSF) {
        this.autoDSF = autoDSF;
    }

    public boolean isAutoDSF() {
        return autoDSF;
    }

    public void setDeviceScaleFactor(double deviceScaleFactor) {
        this.deviceScaleFactor = deviceScaleFactor;
    }

    public double getDeviceScaleFactor() {
        if (autoDSF) {
            long window = Minecraft.getInstance().getWindow().getWindow();

            int[] fbWidth = new int[1];
            int[] fbHeight = new int[1];
            GLFW.glfwGetFramebufferSize(window, fbWidth, fbHeight);

            int[] winWidth = new int[1];
            int[] winHeight = new int[1];
            GLFW.glfwGetWindowSize(window, winWidth, winHeight);

            return Math.max(1, Math.min(fbWidth[0] / winWidth[0], fbHeight[0] / winHeight[0]));
        } else {
            return deviceScaleFactor > 0 ? deviceScaleFactor : 1;
        }
    }

    public int scaleX(int x) {
        return (int) (x / getDeviceScaleFactor());
    }

    public int scaleY(int y) {
        return (int) (y / getDeviceScaleFactor());
    }

    public LemonCursorChangeListener getCursorChangeListener() {
        return cursorChangeListener;
    }

    public void setCursorChangeListener(LemonCursorChangeListener cursorChangeListener) {
        this.cursorChangeListener = cursorChangeListener;
    }

    public boolean usingBrowserControls() {
        return browserControls;
    }

    public LemonBrowser useBrowserControls(boolean browserControls) {
        this.browserControls = browserControls;
        return this;
    }

    public void resize(int width, int height) {
        width = scaleX(width);
        height = scaleY(height);

        if (width == lastWidth && height == lastHeight) return;
        
        lastWidth = width;
        lastHeight = height;

        Platform.runLater(() -> {
            if (webView != null) {
                webView.setPrefSize(width, height);
                webView.setMaxSize(width, height);
                webView.setMinSize(width, height);
            }
        });
    }

    public void loadURL(String url) {
        Platform.runLater(() -> {
            if (webEngine != null) {
                webEngine.load(url);
            }
        });
    }

    public String getURL() {
        if (webEngine == null) return "";
        return webEngine.getLocation();
    }

    public boolean canGoBack() {
        return webEngine != null;
    }

    public void goBack() {
        Platform.runLater(() -> {
            if (webEngine != null) {
                String history = webEngine.getHistory().getCurrentIndex() > 0 ? "back" : "";
                if (webEngine.getHistory().getCurrentIndex() > 0) {
                    webEngine.getHistory().go(-1);
                }
            }
        });
    }

    public boolean canGoForward() {
        if (webEngine == null) return false;
        return webEngine.getHistory().getCurrentIndex() < webEngine.getHistory().getEntries().size() - 1;
    }

    public void goForward() {
        Platform.runLater(() -> {
            if (webEngine != null && canGoForward()) {
                webEngine.getHistory().go(1);
            }
        });
    }

    public void reload() {
        Platform.runLater(() -> {
            if (webEngine != null) {
                webEngine.reload();
            }
        });
    }

    public void executeJavaScript(String js) {
        Platform.runLater(() -> {
            if (webEngine != null) {
                webEngine.executeScript(js);
            }
        });
    }

    public double getZoomLevel() {
        if (webView == null) return 0;
        return webView.getZoom() - 1;
    }

    public void setZoomLevel(double level) {
        Platform.runLater(() -> {
            if (webView != null) {
                webView.setZoom(1 + level);
            }
        });
    }

    public void setFocus(boolean focus) {
        focused.set(focus);
    }

    public boolean isFocused() {
        return focused.get();
    }

    public void sendKeyPress(int keyCode, long scanCode, int modifiers) {
        if (browserControls) {
            if (modifiers == GLFW_MOD_CONTROL) {
                if (keyCode == GLFW_KEY_R) {
                    reload();
                    return;
                } else if (keyCode == GLFW_KEY_EQUAL) {
                    if (getZoomLevel() < 9) setZoomLevel(getZoomLevel() + 1);
                    return;
                } else if (keyCode == GLFW_KEY_MINUS) {
                    if (getZoomLevel() > -9) setZoomLevel(getZoomLevel() - 1);
                    return;
                } else if (keyCode == GLFW_KEY_0) {
                    setZoomLevel(0);
                    return;
                }
            } else if (modifiers == GLFW_MOD_ALT) {
                if (keyCode == GLFW_KEY_LEFT && canGoBack()) {
                    goBack();
                    return;
                } else if (keyCode == GLFW_KEY_RIGHT && canGoForward()) {
                    goForward();
                    return;
                }
            }
        }
    }

    public void sendKeyRelease(int keyCode, long scanCode, int modifiers) {
    }

    public void sendKeyTyped(char c, int modifiers) {
    }

    public void sendMouseMove(int mouseX, int mouseY) {
        mouseX = scaleX(mouseX);
        mouseY = scaleY(mouseY);
    }

    public void sendMousePress(int mouseX, int mouseY, int button) {
        mouseX = scaleX(mouseX);
        mouseY = scaleY(mouseY);

        if (button == 1) button = 2;
        else if (button == 2) button = 1;

        if (button == 0) btnMask |= 1;
        else if (button == 1) btnMask |= 2;
        else if (button == 2) btnMask |= 4;
    }

    public void sendMouseRelease(int mouseX, int mouseY, int button) {
        mouseX = scaleX(mouseX);
        mouseY = scaleY(mouseY);

        if (button == 1) button = 2;
        else if (button == 2) button = 1;

        if (button == 0 && (btnMask & 1) != 0) btnMask ^= 1;
        else if (button == 1 && (btnMask & 2) != 0) btnMask ^= 2;
        else if (button == 2 && (btnMask & 4) != 0) btnMask ^= 4;
    }

    public void sendMouseWheel(int mouseX, int mouseY, double amount, int modifiers) {
        mouseX = scaleX(mouseX);
        mouseY = scaleY(mouseY);

        if (browserControls) {
            if ((modifiers & GLFW_MOD_CONTROL) != 0) {
                if (amount > 0) {
                    if (getZoomLevel() < 9) setZoomLevel(getZoomLevel() + 1);
                } else if (getZoomLevel() > -9) setZoomLevel(getZoomLevel() - 1);
                return;
            }
        }
    }

    public void setCursor(int cursorType) {
        if (cursorType == 0) {
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        } else {
            GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    public void captureFrame() {
        if (!isReady || webView == null) return;
        
        Platform.runLater(() -> {
            try {
                BufferedImage image = SwingFXUtils.fromFXImage(webView.snapshot(null, null), null);
                if (image != null && renderer.getTextureID() != 0) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    
                    int[] pixels = new int[width * height];
                    image.getRGB(0, 0, width, height, pixels, 0, width);
                    
                    ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
                    buffer.order(ByteOrder.nativeOrder());
                    
                    for (int i = 0; i < pixels.length; i++) {
                        int pixel = pixels[i];
                        buffer.put((byte)((pixel >> 16) & 0xFF));
                        buffer.put((byte)((pixel >> 8) & 0xFF));
                        buffer.put((byte)(pixel & 0xFF));
                        buffer.put((byte)((pixel >> 24) & 0xFF));
                    }
                    buffer.flip();
                    
                    Minecraft.getInstance().submit(() -> {
                        renderer.updateTexture(buffer, width, height);
                    });
                }
            } catch (Exception e) {
                LemonWebView.LOGGER.error("Failed to capture frame", e);
            }
        });
    }

    public void close() {
        renderer.cleanup();
        cursorChangeListener.onCursorChange(0);
        Platform.runLater(() -> {
            if (webView != null) {
                webView.getEngine().loadContent("");
            }
        });
    }
}