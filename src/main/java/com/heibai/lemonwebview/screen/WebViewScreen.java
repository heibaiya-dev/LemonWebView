package com.heibai.lemonwebview.screen;

import com.heibai.lemonwebview.LemonBrowser;
import com.heibai.lemonwebview.LemonWebView;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WebViewScreen extends Screen {
    private static final int MARGIN = 10;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 60;
    
    private LemonBrowser browser;
    private String url;
    private int browserWidth;
    private int browserHeight;
    
    public WebViewScreen(Component title, String url) {
        super(title);
        this.url = url;
    }
    
    @Override
    protected void init() {
        super.init();
        
        if (browser == null) {
            boolean transparent = true;
            browser = LemonWebView.createBrowser(url, transparent);
            resizeBrowser();
        }
        
        int buttonY = height - MARGIN - BUTTON_HEIGHT;
        
        addRenderableWidget(Button.builder(Component.literal("后退"), btn -> {
            if (browser != null) browser.goBack();
        }).bounds(MARGIN, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(Component.literal("前进"), btn -> {
            if (browser != null) browser.goForward();
        }).bounds(MARGIN + BUTTON_WIDTH + 5, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(Component.literal("刷新"), btn -> {
            if (browser != null) browser.reload();
        }).bounds(MARGIN + (BUTTON_WIDTH + 5) * 2, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        
        addRenderableWidget(Button.builder(Component.literal("关闭"), btn -> {
            onClose();
        }).bounds(width - MARGIN - BUTTON_WIDTH, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }
    
    private void resizeBrowser() {
        browserWidth = (int) ((width - MARGIN * 2) * minecraft.getWindow().getGuiScale());
        browserHeight = (int) ((height - MARGIN * 2 - BUTTON_HEIGHT - 5) * minecraft.getWindow().getGuiScale());
        
        if (browserWidth > 100 && browserHeight > 100) {
            browser.resize(browserWidth, browserHeight);
        }
    }
    
    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        resizeBrowser();
    }
    
    @Override
    public void onClose() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        super.onClose();
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        if (browser != null) {
            browser.captureFrame();
            
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderTexture(0, browser.getRenderer().getTextureID());
            
            int renderX = MARGIN;
            int renderY = MARGIN;
            int renderWidth = width - MARGIN * 2;
            int renderHeight = height - MARGIN * 2 - BUTTON_HEIGHT - 5;
            
            guiGraphics.blit(renderX, renderY, 0, 0, renderWidth, renderHeight, renderWidth, renderHeight);
            
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.enableDepthTest();
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        if (browser != null) {
            guiGraphics.drawCenteredString(font, browser.getURL(), width / 2, MARGIN + 2, 0xFFFFFF);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (browser != null && isInBrowserArea(mouseX, mouseY)) {
            int scaledMouseX = (int) ((mouseX - MARGIN) * minecraft.getWindow().getGuiScale());
            int scaledMouseY = (int) ((mouseY - MARGIN) * minecraft.getWindow().getGuiScale());
            browser.sendMousePress(scaledMouseX, scaledMouseY, button);
            browser.setFocus(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (browser != null && isInBrowserArea(mouseX, mouseY)) {
            int scaledMouseX = (int) ((mouseX - MARGIN) * minecraft.getWindow().getGuiScale());
            int scaledMouseY = (int) ((mouseY - MARGIN) * minecraft.getWindow().getGuiScale());
            browser.sendMouseRelease(scaledMouseX, scaledMouseY, button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (browser != null && isInBrowserArea(mouseX, mouseY)) {
            int scaledMouseX = (int) ((mouseX - MARGIN) * minecraft.getWindow().getGuiScale());
            int scaledMouseY = (int) ((mouseY - MARGIN) * minecraft.getWindow().getGuiScale());
            browser.sendMouseMove(scaledMouseX, scaledMouseY);
        }
        super.mouseMoved(mouseX, mouseY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (browser != null && isInBrowserArea(mouseX, mouseY)) {
            int scaledMouseX = (int) ((mouseX - MARGIN) * minecraft.getWindow().getGuiScale());
            int scaledMouseY = (int) ((mouseY - MARGIN) * minecraft.getWindow().getGuiScale());
            browser.sendMouseWheel(scaledMouseX, scaledMouseY, scrollY, 0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (browser != null && browser.isFocused()) {
            browser.sendKeyPress(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (browser != null && browser.isFocused()) {
            browser.sendKeyRelease(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (browser != null && browser.isFocused() && codePoint != (char) 0) {
            browser.sendKeyTyped(codePoint, modifiers);
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }
    
    private boolean isInBrowserArea(double mouseX, double mouseY) {
        int renderX = MARGIN;
        int renderY = MARGIN;
        int renderWidth = width - MARGIN * 2;
        int renderHeight = height - MARGIN * 2 - BUTTON_HEIGHT - 5;
        
        return mouseX >= renderX && mouseX <= renderX + renderWidth 
            && mouseY >= renderY && mouseY <= renderY + renderHeight;
    }
}