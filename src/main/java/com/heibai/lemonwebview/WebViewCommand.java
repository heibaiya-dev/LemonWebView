package com.heibai.lemonwebview;

import com.heibai.lemonwebview.screen.WebViewScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WebViewCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("webview")
            .then(Commands.literal("start")
                .then(Commands.argument("url", StringArgumentType.greedyString())
                    .executes(WebViewCommand::openWebView)
                )
            )
            .then(Commands.literal("stop")
                .executes(WebViewCommand::closeWebView)
            )
        );
    }
    
    private static int openWebView(CommandContext<CommandSourceStack> context) {
        String url = StringArgumentType.getString(context, "url");
        
        if (!LemonWebView.isInitialized()) {
            LemonWebView.initialize();
        }
        
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreen(new WebViewScreen(
                Component.literal("WebView - " + url), 
                url
            ));
        });
        
        context.getSource().sendSuccess(() -> Component.literal("正在打开WebView: " + url), false);
        return Command.SINGLE_SUCCESS;
    }
    
    private static int closeWebView(CommandContext<CommandSourceStack> context) {
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().screen instanceof WebViewScreen) {
                Minecraft.getInstance().setScreen(null);
            }
        });
        
        context.getSource().sendSuccess(() -> Component.literal("已关闭WebView"), false);
        return Command.SINGLE_SUCCESS;
    }
}