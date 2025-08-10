package com.omt.mixin.client;

import com.omt.config.ModConfig;
import com.omt.gui.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a simple World Height Configuration button to the Create World screen
 */
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends net.minecraft.client.gui.screen.Screen {
    
    protected CreateWorldScreenMixin(Text title) {
        super(title);
    }
    
    @Inject(method = "init", at = @At("TAIL"))
    private void addWorldHeightConfigButton(CallbackInfo ci) {
        // Add a simple button in the top-center area
        int buttonWidth = 150;
        int buttonHeight = 20;
        
        // Position it at the top center
        int buttonX = (this.width - buttonWidth) / 2;
        int buttonY = 60;
        
        // Create a simple button with a reasonable emoji
        ButtonWidget configButton = ButtonWidget.builder(
            Text.literal("⚙ World Height"),
            button -> {
                // Open the ConfigScreen when clicked
                MinecraftClient.getInstance().setScreen(new ConfigScreen(this));
            })
            .dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
            .build();
        
        this.addDrawableChild(configButton);
    }
}
