package com.omt.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class MinimalSlider extends SliderWidget {
    private final int minValue;
    private final int maxValue;
    private final ValueChangeListener listener;
    private float animatedValue = 0;
    private float hoverAnimation = 0;
    private boolean wasHovered = false;
    
    public interface ValueChangeListener {
        void onValueChange(int value);
    }
    
    public MinimalSlider(int x, int y, int width, int height, 
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
        // Message handled in custom render
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
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Smooth animations
        animatedValue = MathHelper.lerp(0.15f, animatedValue, (float)this.value);
        
        boolean isHovered = this.isHovered();
        if (isHovered && !wasHovered) {
            client.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 2.0F, 0.3F)
            );
        }
        wasHovered = isHovered;
        
        float targetHover = isHovered ? 1.0f : 0.0f;
        hoverAnimation = MathHelper.lerp(0.12f, hoverAnimation, targetHover);
        
        // Minimal track - just a thin line
        int trackY = this.getY() + this.height / 2;
        
        // Track background - very thin
        context.fill(this.getX(), trackY, this.getX() + this.width, trackY + 1, 0x30FFFFFF);
        
        // Filled portion - slightly thicker when hovered
        int fillEnd = this.getX() + (int)(animatedValue * this.width);
        int thickness = isHovered ? 2 : 1;
        context.fill(this.getX(), trackY, fillEnd, trackY + thickness, 0xFFFFFFFF);
        
        // Minimal handle - just a small circle
        int handleX = this.getX() + (int)(animatedValue * this.width);
        int handleSize = (int)(4 + hoverAnimation * 2);
        
        // Draw handle
        for (int dx = -handleSize; dx <= handleSize; dx++) {
            for (int dy = -handleSize; dy <= handleSize; dy++) {
                if (dx * dx + dy * dy <= handleSize * handleSize) {
                    context.fill(handleX + dx, trackY + dy, handleX + dx + 1, trackY + dy + 1, 0xFFFFFFFF);
                }
            }
        }
        
        // Value display - only on hover
        if (hoverAnimation > 0.1f) {
            int currentValue = (int)Math.round(animatedValue * (maxValue - minValue) + minValue);
            String valueText = String.valueOf(currentValue);
            
            int alpha = (int)(255 * hoverAnimation);
            context.drawTextWithShadow(client.textRenderer, valueText,
                                      handleX - client.textRenderer.getWidth(valueText) / 2,
                                      this.getY() - 15,
                                      0xFFFFFF | (alpha << 24));
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.6F)
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