package com.omt.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class AppleStyleSlider extends SliderWidget {
    private final int minValue;
    private final int maxValue;
    private final ValueChangeListener listener;
    private float animatedValue = 0;
    private long lastUpdateTime = 0;
    private boolean wasHovered = false;
    private float hoverAnimation = 0;
    
    public interface ValueChangeListener {
        void onValueChange(int value);
    }
    
    public AppleStyleSlider(int x, int y, int width, int height, 
                            int minValue, int maxValue, int currentValue,
                            ValueChangeListener listener) {
        super(x, y, width, height, Text.empty(), 
              (double)(currentValue - minValue) / (maxValue - minValue));
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.listener = listener;
        this.animatedValue = (float)this.value;
        updateMessage();
    }
    
    @Override
    protected void updateMessage() {
        // Message is handled in custom render
    }
    
    @Override
    protected void applyValue() {
        int intValue = (int)Math.round(this.value * (maxValue - minValue) + minValue);
        if (listener != null) {
            listener.onValueChange(intValue);
        }
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update animations
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000f, 0.1f);
        lastUpdateTime = currentTime;
        
        // Smooth value animation
        animatedValue = MathHelper.lerp(deltaTime * 10, animatedValue, (float)this.value);
        
        // Hover animation
        boolean isHovered = this.isHovered();
        if (isHovered && !wasHovered) {
            MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 2.0F, 0.4F)
            );
        }
        wasHovered = isHovered;
        
        float targetHover = isHovered ? 1.0f : 0.0f;
        hoverAnimation = MathHelper.lerp(deltaTime * 8, hoverAnimation, targetHover);
        
        // Draw track background - subtle gradient
        int trackY = this.getY() + this.height / 2 - 2;
        int trackHeight = 4;
        
        // Track shadow
        context.fill(this.getX(), trackY + 1, this.getX() + this.width, trackY + trackHeight + 1, 0x20000000);
        
        // Track background with subtle gradient
        context.fillGradient(this.getX(), trackY, this.getX() + this.width, trackY + trackHeight,
                            0xFF1C1C1E, 0xFF2C2C2E);
        
        // Track inner highlight
        context.fill(this.getX(), trackY, this.getX() + this.width, trackY + 1, 0x20FFFFFF);
        
        // Calculate handle position
        int handleX = this.getX() + (int)(animatedValue * (this.width - 20));
        
        // Draw filled track with gradient
        if (animatedValue > 0) {
            // Gradient fill with Apple-style blue
            int fillEnd = handleX + 10;
            context.fillGradient(this.getX(), trackY, fillEnd, trackY + trackHeight,
                                0xFF007AFF, 0xFF0051D5);
            
            // Glow effect when active
            if (hoverAnimation > 0) {
                int glowAlpha = (int)(20 * hoverAnimation);
                context.fillGradient(this.getX(), trackY - 2, fillEnd, trackY + trackHeight + 2,
                                    (glowAlpha << 24) | 0x007AFF, 0x00007AFF);
            }
        }
        
        // Draw handle with Apple-style design
        drawAppleHandle(context, handleX, this.getY() + this.height / 2);
        
        // Draw value text with SF Pro style
        int currentValue = (int)Math.round(animatedValue * (maxValue - minValue) + minValue);
        String valueText = String.valueOf(currentValue);
        
        // Value background pill
        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(valueText);
        int pillX = handleX + 10 - textWidth / 2;
        int pillY = this.getY() - 25;
        int pillPadding = 8;
        
        // Only show when hovering or dragging
        if (hoverAnimation > 0 || this.isFocused()) {
            float pillAlpha = hoverAnimation;
            
            // Pill shadow
            context.fill(pillX - pillPadding, pillY + 1, 
                        pillX + textWidth + pillPadding, pillY + 16,
                        (int)(0x40 * pillAlpha) << 24);
            
            // Pill background
            context.fill(pillX - pillPadding, pillY, 
                        pillX + textWidth + pillPadding, pillY + 15,
                        ((int)(0xE0 * pillAlpha) << 24) | 0x2C2C2E);
            
            // Value text
            context.drawTextWithShadow(client.textRenderer, valueText,
                                      pillX, pillY + 3,
                                      0xFFFFFF | ((int)(0xFF * pillAlpha) << 24));
        }
    }
    
    private void drawAppleHandle(DrawContext context, int x, int y) {
        int size = 20;
        int halfSize = size / 2;
        
        // Scale based on hover/drag
        float scale = 1.0f + hoverAnimation * 0.1f;
        int scaledSize = (int)(size * scale);
        int scaledHalf = scaledSize / 2;
        
        // Outer shadow
        for (int i = 3; i > 0; i--) {
            int shadowSize = scaledSize + i * 2;
            int shadowAlpha = 0x10 / i;
            drawCircle(context, x + halfSize, y, shadowSize / 2, shadowAlpha << 24);
        }
        
        // Main handle - white with subtle gradient
        drawCircle(context, x + halfSize, y, scaledHalf, 0xFFFFFFFF);
        
        // Inner shadow for depth
        drawCircle(context, x + halfSize, y, scaledHalf - 1, 0x10000000);
        
        // Highlight
        drawCircle(context, x + halfSize - 2, y - 2, scaledHalf / 3, 0x40FFFFFF);
    }
    
    private void drawCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
        // Simple circle approximation using squares
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (x * x + y * y <= radius * radius) {
                    context.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                }
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F)
            );
            return true;
        }
        return false;
    }
    
    public void setValue(int value) {
        this.value = (double)(value - minValue) / (maxValue - minValue);
        this.animatedValue = (float)this.value;
        updateMessage();
    }
    
    public int getValue() {
        return (int)Math.round(this.value * (maxValue - minValue) + minValue);
    }
}