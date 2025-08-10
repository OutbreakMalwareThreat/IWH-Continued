package com.omt.gui;

import com.omt.config.ModConfig;
import com.omt.gui.widget.ProfessionalSlider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // Sliders
    private ProfessionalSlider maxHeightSlider;
    private ProfessionalSlider seaLevelSlider;
    
    // Subtle animations - no flashing
    private float fadeInProgress = 0.0f;
    private float[] cardHovers = new float[4];
    private float sidebarAnimation = 0.0f;
    
    // Temporary values
    private int tempMaxHeight;
    private int tempSeaLevel;
    
    // Colors - Ultra dark theme (cleaner palette)
    private static final int BG_PRIMARY = 0xFF0A0A0B;
    private static final int BG_SECONDARY = 0xFF0F0F10;
    private static final int BG_CARD = 0xFF141416;
    private static final int BORDER_COLOR = 0xFF1F1F22;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFF9CA3AF;
    private static final int TEXT_MUTED = 0xFF6B7280;
    private static final int ACCENT_PRIMARY = 0xFF7C3AED;
    private static final int ACCENT_SUCCESS = 0xFF10B981;
    private static final int ACCENT_WARNING = 0xFFF59E0B;
    private static final int ACCENT_DANGER = 0xFFEF4444;
    
    // Sidebar items
    private List<MenuItem> menuItems = new ArrayList<>();
    private int selectedMenuItem = 0;
    private float[] menuItemAnimations;
    
    // Smooth scrolling values
    private float scrollY = 0;
    private float targetScrollY = 0;
    
    // Mouse tracking for smooth interactions
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    
    // Subtle particle effects
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    
    public ConfigScreen(Screen parent) {
        super(Text.literal("World Height Configuration"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
        
        this.tempMaxHeight = config.getMaxWorldHeight();
        this.tempSeaLevel = config.getSeaLevel();
        
        // Initialize menu items with reasonable emojis
        menuItems.add(new MenuItem("⚙", "General", "Configure basic settings"));
        menuItems.add(new MenuItem("📊", "Analytics", "View world statistics"));
        menuItems.add(new MenuItem("🌍", "World", "World generation"));
        menuItems.add(new MenuItem("💾", "Save", "Save configuration"));
        
        menuItemAnimations = new float[menuItems.size()];
        
        // Initialize subtle particles (fewer for cleaner look)
        for (int i = 0; i < 8; i++) {
            particles.add(new Particle());
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Smooth sound
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.UI_BUTTON_CLICK.value(), 0.7f));
        }
        
        // Content area - position sliders much lower
        int contentX = 240;
        int sliderStartY = this.height - 160;  // Much closer to bottom
        int sliderWidth = Math.min(350, this.width - contentX - 100);
        int sliderHeight = 25;  // Smaller height
        int sliderSpacing = 60;  // Space between sliders
        
        // Maximum Height Slider
        this.maxHeightSlider = new ProfessionalSlider(
            contentX, sliderStartY, sliderWidth, sliderHeight,
            Text.literal(""),
            Text.literal(""),
            ModConfig.SLIDER_MAX_HEIGHT_MIN,
            ModConfig.SLIDER_MAX_HEIGHT_MAX,
            config.getMaxWorldHeight(),
            value -> {
                config.setMaxWorldHeight(value);
                validateAndUpdateDisplay();
                playTickSound();
            }
        );
        maxHeightSlider.setColors(ACCENT_PRIMARY, 0xFF7C3AED, BG_CARD, TEXT_PRIMARY);
        this.addDrawableChild(maxHeightSlider);
        
        // Sea Level Slider - properly spaced below
        this.seaLevelSlider = new ProfessionalSlider(
            contentX, sliderStartY + sliderSpacing, sliderWidth, sliderHeight,
            Text.literal(""),
            Text.literal(""),
            ModConfig.SLIDER_SEA_LEVEL_MIN,
            ModConfig.SLIDER_SEA_LEVEL_MAX,
            config.getSeaLevel(),
            value -> {
                config.setSeaLevel(value);
                validateAndUpdateDisplay();
                playTickSound();
            }
        );
        seaLevelSlider.setColors(0xFF06B6D4, 0xFF0891B2, BG_CARD, TEXT_PRIMARY);
        this.addDrawableChild(seaLevelSlider);
        
        // Don't add buttons here - we'll render them custom
    }
    
    private void validateAndUpdateDisplay() {
        int maxHeight = config.getMaxWorldHeight();
        int seaLevel = config.getSeaLevel();
        
        if (seaLevel > maxHeight - 10) {
            config.setSeaLevel(maxHeight - 10);
            seaLevelSlider.setValue(config.getSeaLevel());
        }
    }
    
    private void saveConfiguration() {
        ModConfig.saveAsync();
    }
    
    private void playTickSound() {
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.UI_BUTTON_CLICK.value(), 1.5f));
        }
    }
    
    private void playSuccessSound() {
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0f));
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Track mouse for smooth interactions
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        
        // Update smooth animations at 60fps
        updateAnimations(delta);
        
        // Render background
        renderBackground(context);
        
        // Render sidebar
        renderSidebar(context, mouseX, mouseY);
        
        // Render main content
        renderMainContent(context, mouseX, mouseY);
        
        // Render cards
        renderCards(context, mouseX, mouseY);
        
        // Render custom buttons
        renderCustomButtons(context, mouseX, mouseY);
        
        // Render widgets (sliders)
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void updateAnimations(float delta) {
        // Very subtle animations
        
        // Slow fade in
        fadeInProgress = Math.min(1.0f, fadeInProgress + delta * 1.5f);
        
        // Sidebar slide (very gentle)
        sidebarAnimation = Math.min(1.0f, sidebarAnimation + delta * 2.0f);
        
        // Update card hovers (minimal)
        updateCardHovers(delta);
        
        // Update menu item animations (very subtle)
        for (int i = 0; i < menuItems.size(); i++) {
            float target = (i == selectedMenuItem) ? 1.0f : 0.0f;
            menuItemAnimations[i] = MathHelper.lerp(delta * 3.0f, menuItemAnimations[i], target);
        }
        
        // Smooth scroll
        scrollY = MathHelper.lerp(delta * 6.0f, scrollY, targetScrollY);
        
        // Update particles
        for (Particle particle : particles) {
            particle.update(delta);
        }
    }
    
    private void updateCardHovers(float delta) {
        // Subtle hover effects only
        if (selectedMenuItem == 1) { // Analytics tab
            // No hover effects for bar graphs
            return;
        }
        
        int contentX = 240;
        int cardY = 120;
        int cardWidth = (this.width - contentX - 60) / 2 - 10;
        int cardHeight = 140;
        
        for (int i = 0; i < 4; i++) {
            int row = i / 2;
            int col = i % 2;
            int x = contentX + col * (cardWidth + 20);
            int y = cardY + row * (cardHeight + 20);
            
            boolean hovered = lastMouseX >= x && lastMouseX <= x + cardWidth &&
                            lastMouseY >= y && lastMouseY <= y + cardHeight;
            
            float target = hovered ? 1.0f : 0.0f;
            cardHovers[i] = MathHelper.lerp(delta * 4.0f, cardHovers[i], target);
        }
    }
    
    private void renderBackground(DrawContext context) {
        // Ultra dark background
        context.fill(0, 0, this.width, this.height, BG_PRIMARY);
        
        // Very subtle gradient overlay (cleaner)
        context.fillGradient(0, 0, this.width, this.height / 3,
            0x00000000, 0x05000000);
        
        // Render subtle particles (with lower opacity)
        for (Particle particle : particles) {
            particle.render(context, fadeInProgress * 0.5f);
        }
    }
    
    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        int sidebarWidth = 200;
        float slideX = (1.0f - sidebarAnimation) * -100; // Reduced slide distance
        
        context.getMatrices().push();
        context.getMatrices().translate(slideX, 0, 0);
        
        // Sidebar background
        context.fill(0, 0, sidebarWidth, this.height, BG_SECONDARY);
        
        // Cleaner border (thinner)
        context.fill(sidebarWidth, 0, sidebarWidth + 1, this.height, 0xFF1A1A1C);
        
        // Logo area
        if (this.textRenderer != null) {
            context.drawText(this.textRenderer,
                Text.literal("WORLD HEIGHT"),
                25, 35,
                TEXT_PRIMARY, false);
            
            context.drawText(this.textRenderer,
                Text.literal("Configuration"),
                25, 48,
                TEXT_MUTED, false);
        }
        
        // Menu items
        int itemY = 100;
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = menuItems.get(i);
            float anim = menuItemAnimations[i];
            
            // Hover detection
            boolean hovered = mouseX < sidebarWidth && mouseY >= itemY && mouseY < itemY + 45;
            
            // Selection indicator
            if (anim > 0.01f) {
                int indicatorAlpha = (int)(anim * 255);
                context.fill(0, itemY, 3, itemY + 45,
                    ColorHelper.Argb.getArgb(indicatorAlpha, 139, 92, 246));
                
                // Background highlight
                int bgAlpha = (int)(anim * 20);
                context.fill(0, itemY, sidebarWidth - 1, itemY + 45,
                    ColorHelper.Argb.getArgb(bgAlpha, 139, 92, 246));
            }
            
            if (this.textRenderer != null) {
                // Icon with emoji
                context.drawText(this.textRenderer,
                    Text.literal(item.icon),
                    25, itemY + 14,
                    i == selectedMenuItem ? ACCENT_PRIMARY : (hovered ? TEXT_PRIMARY : TEXT_SECONDARY),
                    false);
                
                // Label
                context.drawText(this.textRenderer,
                    Text.literal(item.label),
                    50, itemY + 14,
                    i == selectedMenuItem ? TEXT_PRIMARY : (hovered ? TEXT_SECONDARY : TEXT_MUTED),
                    false);
            }
            
            itemY += 50;
        }
        
        context.getMatrices().pop();
    }
    
    private void renderMainContent(DrawContext context, int mouseX, int mouseY) {
        int contentX = 240;
        
        if (this.textRenderer != null) {
            // Simple title - no scaling animation
            String title = selectedMenuItem == 1 ? "Analytics Dashboard" : "World Height Configuration";
            String subtitle = selectedMenuItem == 1 ?
                "View detailed statistics and performance metrics" :
                "Configure world generation parameters for enhanced gameplay";
            
            context.drawTextWithShadow(this.textRenderer,
                Text.literal(title),
                contentX, 40,
                TEXT_PRIMARY);
            
            context.drawTextWithShadow(this.textRenderer,
                Text.literal(subtitle),
                contentX, 60,
                TEXT_SECONDARY);
        }
    }
    
    private void renderCards(DrawContext context, int mouseX, int mouseY) {
        int contentX = 240;
        
        if (selectedMenuItem == 1) {
            // Analytics tab - render bar graphs
            renderAnalyticsGraphs(context, contentX);
        } else {
            // General tab - render info cards
            renderInfoCards(context, contentX);
        }
        
        // Slider labels and controls (only on General tab)
        if (selectedMenuItem == 0) {
            // Make sliders visible
            if (maxHeightSlider != null) maxHeightSlider.visible = true;
            if (seaLevelSlider != null) seaLevelSlider.visible = true;
            
            if (this.textRenderer != null) {
                int sliderStartY = this.height - 160;  // Match the slider position
                int sliderSpacing = 60;
                
                // Maximum Height label above its slider
                context.drawTextWithShadow(this.textRenderer,
                    Text.literal("Maximum Height"),
                    contentX, sliderStartY - 20,
                    TEXT_PRIMARY);
                
                context.drawTextWithShadow(this.textRenderer,
                    Text.literal(String.valueOf(config.getMaxWorldHeight()) + " blocks"),
                    contentX + 280, sliderStartY - 20,
                    ACCENT_PRIMARY);
                
                // Sea Level label above its slider
                context.drawTextWithShadow(this.textRenderer,
                    Text.literal("Sea Level"),
                    contentX, sliderStartY + sliderSpacing - 20,
                    TEXT_PRIMARY);
                
                context.drawTextWithShadow(this.textRenderer,
                    Text.literal("Y=" + config.getSeaLevel()),
                    contentX + 280, sliderStartY + sliderSpacing - 20,
                    0xFF06B6D4);
            }
        } else {
            // Hide sliders on other tabs
            if (maxHeightSlider != null) maxHeightSlider.visible = false;
            if (seaLevelSlider != null) seaLevelSlider.visible = false;
        }
    }
    
    private void renderInfoCards(DrawContext context, int contentX) {
        int cardY = 100;
        int cardWidth = Math.min(250, (this.width - contentX - 60) / 2 - 10);
        int cardHeight = 100;  // Smaller cards to leave room for sliders
        
        String[] titles = {"Overview", "Statistics", "Configuration", "About"};
        String[] descriptions = {
            "Current world height: " + config.getMaxWorldHeight() + " blocks",
            "Total height range: " + config.getTotalWorldHeight() + " blocks",
            "Sea level: Y=" + config.getSeaLevel(),
            "Minimum Y: -64 (Locked - a lot of bugs)"
        };
        int[] colors = {ACCENT_PRIMARY, ACCENT_SUCCESS, ACCENT_WARNING, TEXT_MUTED};
        
        for (int i = 0; i < 4; i++) {
            int row = i / 2;
            int col = i % 2;
            int x = contentX + col * (cardWidth + 20);
            int y = cardY + row * (cardHeight + 20);
            
            renderCard(context, x, y, cardWidth, cardHeight,
                      titles[i], descriptions[i], colors[i], i);
        }
    }
    
    private void renderAnalyticsGraphs(DrawContext context, int contentX) {
        int graphY = 100;
        int graphWidth = (this.width - contentX - 80) / 3 - 20;
        int graphHeight = 250;
        
        // Three single-bar graphs
        String[] graphTitles = {"Build Height Limit", "World Bottom", "Ocean Surface"};
        int[] values = {
            config.getMaxWorldHeight(),
            -64,  // Min Y is always -64
            config.getSeaLevel()
        };
        int[] maxValues = {
            2048,  // Maximum possible world height
            -64,  // Min Y doesn't change
            config.getMaxWorldHeight()  // Sea level max is current max height
        };
        int[] colors = {ACCENT_PRIMARY, TEXT_MUTED, 0xFF06B6D4};
        
        for (int i = 0; i < 3; i++) {
            int x = contentX + i * (graphWidth + 30);
            renderSingleBarGraph(context, x, graphY, graphWidth, graphHeight,
                               graphTitles[i], values[i], maxValues[i], colors[i], i);
        }
    }
    
    private void renderSingleBarGraph(DrawContext context, int x, int y, int width, int height,
                                     String title, int value, int maxValue, int color, int index) {
        // Cleaner card background (no border for graphs)
        context.fill(x, y, x + width, y + height, BG_CARD);
        
        // Title
        if (this.textRenderer != null) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(title),
                x + width/2, y + 15,
                TEXT_PRIMARY);
        }
        
        // Calculate bar height
        float ratio;
        if (index == 1) {
            // Min Y is always full (it's locked)
            ratio = 1.0f;
        } else if (index == 2) {
            // Sea level relative to max height
            ratio = (float)(value + 64) / (maxValue + 64);
        } else {
            // Max height relative to maximum possible
            ratio = (float)value / maxValue;
        }
        
        // Draw single centered bar (thinner for cleaner look)
        int barWidth = 40;
        int maxBarHeight = height - 100;
        int barHeight = (int)(ratio * maxBarHeight);
        int barX = x + (width - barWidth) / 2;
        int barY = y + height - 50 - barHeight;
        
        // Cleaner bar (solid color, no gradient)
        context.fill(barX, barY, barX + barWidth, y + height - 50, color);
        
        // Value display
        if (this.textRenderer != null) {
            // Large value text
            String valueText;
            if (index == 0) {
                valueText = "Y: " + value;  // Build Height Limit
            } else if (index == 1) {
                valueText = "Y: -64";  // World Bottom (locked)
            } else {
                valueText = "Y: " + value;  // Ocean Surface
            }
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(valueText),
                x + width/2, barY - 25,
                color);
            
            // Percentage or description
            String percentText;
            if (index == 0) {
                percentText = String.format("%.0f%% of 2048 max", ratio * 100);  // Build height percentage
            } else if (index == 1) {
                percentText = "Locked - a lot of bugs";  // World bottom locked reason
            } else {
                percentText = String.format("Water level at %.0f%%", ratio * 100);  // Ocean surface percentage
            }
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(percentText),
                x + width/2, barY - 10,
                TEXT_SECONDARY);
            
            // Unit label
            String unitText;
            if (index == 0) {
                unitText = "blocks high";  // Build Height Limit
            } else if (index == 1) {
                unitText = "bedrock floor";  // World Bottom
            } else {
                unitText = "sea level";  // Ocean Surface
            }
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(unitText),
                x + width/2, y + height - 35,
                TEXT_MUTED);
        }
        
        // Cleaner baseline (thinner line)
        context.fill(x + 30, y + height - 50, x + width - 30, y + height - 49, 0xFF2A2A2D);
    }
    
    private void renderCard(DrawContext context, int x, int y, int width, int height,
                           String title, String description, int accentColor, int index) {
        float hover = cardHovers[index];
        
        // Extremely subtle hover effect
        float scale = 1.0f + hover * 0.005f;
        int scaledX = (int)(x - (width * (scale - 1.0f) / 2));
        int scaledY = (int)(y - (height * (scale - 1.0f) / 2));
        int scaledW = (int)(width * scale);
        int scaledH = (int)(height * scale);
        
        // Cleaner card background
        context.fill(scaledX, scaledY, scaledX + scaledW, scaledY + scaledH, BG_CARD);
        
        // Subtle border (only on hover)
        if (hover > 0.1f) {
            context.drawBorder(scaledX, scaledY, scaledW, scaledH,
                ColorHelper.Argb.getArgb((int)(hover * 40), 255, 255, 255));
        }
        
        // Cleaner accent dot instead of line
        context.fill(scaledX + 20, scaledY + 20, scaledX + 24, scaledY + 24, accentColor);
        
        // Content (cleaner typography)
        if (this.textRenderer != null) {
            context.drawText(this.textRenderer,
                Text.literal(title),
                scaledX + 35, scaledY + 18,
                TEXT_PRIMARY, false);
            
            context.drawText(this.textRenderer,
                Text.literal(description),
                scaledX + 20, scaledY + 45,
                TEXT_SECONDARY, false);
        }
    }
    
    private void renderCustomButtons(DrawContext context, int mouseX, int mouseY) {
        int buttonY = this.height - 60;
        int applyX = this.width - 260;
        int cancelX = this.width - 130;
        int buttonWidth = 120;
        int buttonHeight = 36;
        
        // Apply button
        boolean applyHovered = mouseX >= applyX && mouseX <= applyX + buttonWidth &&
                              mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
        
        // Cleaner apply button (no border)
        int applyColor = applyHovered ? 0xFF10C876 : ACCENT_SUCCESS;
        context.fill(applyX, buttonY, applyX + buttonWidth, buttonY + buttonHeight, applyColor);
        
        // Cancel button
        boolean cancelHovered = mouseX >= cancelX && mouseX <= cancelX + 100 &&
                               mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
        
        // Cleaner cancel button (no border)
        int cancelColor = cancelHovered ? 0xFF404045 : 0xFF2A2A2F;
        context.fill(cancelX, buttonY, cancelX + 100, buttonY + buttonHeight, cancelColor);
        
        // Button text
        if (this.textRenderer != null) {
            // Apply text
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Apply Changes"),
                applyX + buttonWidth / 2, buttonY + 12,
                0xFFFFFFFF);
            
            // Cancel text
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Cancel"),
                cancelX + 50, buttonY + 12,
                0xFFFFFFFF);
        }
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Override to prevent default background
    }
    
    @Override
    public void renderInGameBackground(DrawContext context) {
        // Override to prevent blur
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle custom button clicks
        int buttonY = this.height - 60;
        int applyX = this.width - 260;
        int cancelX = this.width - 130;
        
        // Apply button click
        if (mouseX >= applyX && mouseX <= applyX + 120 &&
            mouseY >= buttonY && mouseY <= buttonY + 36) {
            saveConfiguration();
            playSuccessSound();
            return true;
        }
        
        // Cancel button click
        if (mouseX >= cancelX && mouseX <= cancelX + 100 &&
            mouseY >= buttonY && mouseY <= buttonY + 36) {
            config.setMaxWorldHeight(tempMaxHeight);
            config.setSeaLevel(tempSeaLevel);
            this.client.setScreen(parent);
            return true;
        }
        
        // Handle sidebar clicks
        if (mouseX < 200) {
            int itemY = 100;
            for (int i = 0; i < menuItems.size(); i++) {
                if (mouseY >= itemY && mouseY < itemY + 45) {
                    selectedMenuItem = i;
                    playTickSound();
                    
                    if (i == 3) { // Save (index changed since we removed Gameplay)
                        saveConfiguration();
                        this.client.setScreen(parent);
                    }
                    return true;
                }
                itemY += 50;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollY -= (float)verticalAmount * 20;
        targetScrollY = MathHelper.clamp(targetScrollY, 0, 100);
        return true;
    }
    
    private float easeOutCubic(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }
    
    private float easeInOutCubic(float t) {
        return t < 0.5 ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
    }
    
    @Override
    public void close() {
        config.setMaxWorldHeight(tempMaxHeight);
        config.setSeaLevel(tempSeaLevel);
        this.client.setScreen(parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
    
    // Helper class for menu items
    private static class MenuItem {
        String icon;
        String label;
        String description;
        
        MenuItem(String icon, String label, String description) {
            this.icon = icon;
            this.label = label;
            this.description = description;
        }
    }
    
    // Subtle particle class
    private class Particle {
        float x, y;
        float vx, vy;
        float alpha;
        float size;
        
        Particle() {
            reset();
        }
        
        void reset() {
            x = random.nextFloat() * ConfigScreen.this.width;
            y = random.nextFloat() * ConfigScreen.this.height;
            vx = (random.nextFloat() - 0.5f) * 0.15f;
            vy = -random.nextFloat() * 0.2f - 0.05f;
            alpha = random.nextFloat() * 0.2f + 0.05f;  // Lower opacity
            size = 1;  // Uniform small size for cleaner look
        }
        
        void update(float delta) {
            x += vx * delta * 30;
            y += vy * delta * 30;
            alpha -= delta * 0.05f;
            
            if (alpha <= 0 || y < 0) {
                reset();
                y = ConfigScreen.this.height;
            }
        }
        
        void render(DrawContext context, float globalAlpha) {
            int particleAlpha = (int)(alpha * globalAlpha * 255);
            if (particleAlpha > 0) {
                int color = ColorHelper.Argb.getArgb(particleAlpha, 100, 100, 110);
                context.fill((int)x, (int)y, (int)(x + size), (int)(y + size), color);
            }
        }
    }
}