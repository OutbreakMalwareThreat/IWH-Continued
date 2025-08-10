package com.omt.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

public class ProfessionalSlider extends SliderWidget {
    private final int min;
    private final int max;
    private final Consumer<Integer> onValueChange;
    
    // Custom colors
    private int trackColor = 0xFF2A2A2D;
    private int fillColor = 0xFF8B5CF6;
    private int thumbColor = 0xFFFFFFFF;
    private int textColor = 0xFFFFFFFF;
    
    // Smooth animation values
    private float animatedValue = 0.0f;
    private float hoverAnimation = 0.0f;
    private float clickAnimation = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    
    // Hover state
    private boolean wasHovered = false;
    
    public ProfessionalSlider(int x, int y, int width, int height, Text prefix, Text suffix, 
                              int min, int max, int value, Consumer<Integer> onValueChange) {
        super(x, y, width, height, prefix, (double)(value - min) / (max - min));
        this.min = min;
        this.max = max;
        this.onValueChange = onValueChange;
        this.animatedValue = (float)this.value;
        updateMessage();
    }
    
    public void setColors(int fillColor, int trackColor, int thumbColor, int textColor) {
        this.fillColor = fillColor;
        this.trackColor = trackColor;
        this.thumbColor = thumbColor;
        this.textColor = textColor;
    }
    
    @Override
    protected void updateMessage() {
        int currentValue = (int)(min + value * (max - min));
        setMessage(Text.literal(String.valueOf(currentValue)));
    }
    
    @Override
    protected void applyValue() {
        int currentValue = (int)(min + value * (max - min));
        if (onValueChange != null) {
            onValueChange.accept(currentValue);
        }
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.visible) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Update animations
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000.0f, 0.1f);
        lastUpdateTime = currentTime;
        
        // Smooth value animation
        float targetValue = (float)this.value;
        float lerpSpeed = deltaTime * 15.0f; // Smooth animation speed
        animatedValue = MathHelper.lerp(Math.min(lerpSpeed, 1.0f), animatedValue, targetValue);
        
        // Hover animation
        boolean isHovered = this.isHovered();
        float hoverTarget = isHovered ? 1.0f : 0.0f;
        hoverAnimation = MathHelper.lerp(deltaTime * 10.0f, hoverAnimation, hoverTarget);
        
        // Click animation (spring back)
        if (clickAnimation > 0) {
            clickAnimation = Math.max(0, clickAnimation - deltaTime * 8.0f);
        }
        
        // Track dimensions with animation
        int trackY = this.getY() + this.height / 2 - 2;
        int trackHeight = 4;
        
        // Add subtle grow effect on hover
        int growAmount = (int)(hoverAnimation * 2);
        trackY -= growAmount / 2;
        trackHeight += growAmount;
        
        // Draw track background with rounded corners effect
        context.fill(this.getX(), trackY, this.getX() + this.width, trackY + trackHeight, trackColor);
        
        // Draw filled portion with smooth animation
        int fillWidth = (int)(this.width * animatedValue);
        if (fillWidth > 0) {
            // Add glow effect when hovering
            if (hoverAnimation > 0.01f) {
                int glowSize = (int)(hoverAnimation * 3);
                int glowAlpha = (int)(hoverAnimation * 30);
                context.fill(
                    this.getX() - glowSize, 
                    trackY - glowSize,
                    this.getX() + fillWidth + glowSize, 
                    trackY + trackHeight + glowSize,
                    (glowAlpha << 24) | (fillColor & 0x00FFFFFF)
                );
            }
            
            // Main fill
            context.fill(this.getX(), trackY, this.getX() + fillWidth, trackY + trackHeight, fillColor);
        }
        
        // Draw thumb with smooth animation
        int thumbX = this.getX() + (int)(animatedValue * (this.width - 8));
        int thumbY = this.getY() + this.height / 2 - 6;
        int thumbSize = 12 + (int)(hoverAnimation * 2) + (int)(clickAnimation * 4);
        
        // Thumb shadow
        context.fill(
            thumbX - 1, thumbY + 1,
            thumbX + thumbSize + 1, thumbY + thumbSize + 1,
            0x40000000
        );
        
        // Thumb with animation
        int thumbAlpha = 255;
        int thumbColorAnimated = ColorHelper.Argb.getArgb(
            thumbAlpha,
            ColorHelper.Argb.getRed(thumbColor) + (int)(hoverAnimation * 20),
            ColorHelper.Argb.getGreen(thumbColor) + (int)(hoverAnimation * 20),
            ColorHelper.Argb.getBlue(thumbColor) + (int)(hoverAnimation * 20)
        );
        
        context.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize, thumbColorAnimated);
        
        // Draw value text with fade animation
        if (client.textRenderer != null) {
            String valueText = String.valueOf((int)(min + animatedValue * (max - min)));
            int textWidth = client.textRenderer.getWidth(valueText);
            
            // Position text above slider when hovering
            int textX = this.getX() + this.width / 2 - textWidth / 2;
            int textY = this.getY() - (int)(10 + hoverAnimation * 5);
            
            if (hoverAnimation > 0.01f) {
                int textAlpha = (int)(hoverAnimation * 255);
                context.drawText(client.textRenderer, Text.literal(valueText),
                    textX, textY, (textAlpha << 24) | (textColor & 0x00FFFFFF), false);
            }
            
            // Always show value at the end
            context.drawText(client.textRenderer, Text.literal(valueText),
                this.getX() + this.width + 10, this.getY() + this.height / 2 - 4,
                textColor, false);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && button == 0) {
            if (this.clicked(mouseX, mouseY)) {
                clickAnimation = 1.0f; // Start click animation
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        super.onDrag(mouseX, mouseY, deltaX, deltaY);
        // Add some resistance for smoother dragging
        if (Math.abs(deltaX) > 0) {
            clickAnimation = 0.3f; // Subtle animation during drag
        }
    }
    
    public void setValue(int value) {
        this.value = (double)(value - min) / (max - min);
        updateMessage();
    }
    
    public int getValue() {
        return (int)(min + value * (max - min));
    }
}