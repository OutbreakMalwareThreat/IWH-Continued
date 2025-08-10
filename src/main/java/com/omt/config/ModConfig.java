package com.omt.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("WorldHeight/Config");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("increased-world-height.json");
    
    private static ModConfig INSTANCE;
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
    
    // Configuration fields with validation
    private int maxWorldHeight = 384;
    private int minYLimit = -64;  // FIXED at vanilla value to prevent corruption
    private int seaLevel = 63;
    
    // Validation constants
    public static final int ABSOLUTE_MIN_Y = -64;  // DO NOT CHANGE - prevents corruption
    public static final int ABSOLUTE_MAX_Y = 1024;
    public static final int DEFAULT_MAX_HEIGHT = 384;
    public static final int DEFAULT_MIN_Y = -64;
    public static final int DEFAULT_SEA_LEVEL = 63;
    
    // Slider bounds - SAFE VALUES ONLY
    public static final int SLIDER_MAX_HEIGHT_MIN = 384;  // Vanilla height
    public static final int SLIDER_MAX_HEIGHT_MAX = 2048;  // Maximum as requested
    public static final int SLIDER_MIN_Y_MIN = -64;       // LOCKED to vanilla
    public static final int SLIDER_MIN_Y_MAX = -64;       // LOCKED to vanilla
    public static final int SLIDER_SEA_LEVEL_MIN = 0;
    public static final int SLIDER_SEA_LEVEL_MAX = 256;
    
    private ModConfig() {}
    
    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            synchronized (ModConfig.class) {
                if (INSTANCE == null) {
                    load();
                }
            }
        }
        return INSTANCE;
    }
    
    public static void load() {
        LOCK.writeLock().lock();
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);
                LOGGER.info("Configuration loaded successfully");
            } else {
                INSTANCE = new ModConfig();
                LOGGER.info("Created new configuration with defaults");
            }
            
            if (INSTANCE == null) {
                INSTANCE = new ModConfig();
            }
            
            INSTANCE.validate();
            save(); // Save after validation to ensure file is valid
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration, using defaults", e);
            INSTANCE = new ModConfig();
        } finally {
            LOCK.writeLock().unlock();
        }
    }
    
    public static CompletableFuture<Void> saveAsync() {
        return CompletableFuture.runAsync(ModConfig::save);
    }
    
    public static void save() {
        LOCK.readLock().lock();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(getInstance());
            Files.writeString(CONFIG_PATH, json);
            LOGGER.debug("Configuration saved");
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration", e);
        } finally {
            LOCK.readLock().unlock();
        }
    }
    
    private void validate() {
        boolean changed = false;
        
        // FORCE min Y to vanilla value to prevent corruption
        if (minYLimit != -64) {
            LOGGER.warn("Min Y limit forced to vanilla value (-64) to prevent rendering corruption");
            minYLimit = -64;
            changed = true;
        }
        
        // Validate max world height
        int oldMaxHeight = maxWorldHeight;
        maxWorldHeight = Math.max(SLIDER_MAX_HEIGHT_MIN, Math.min(SLIDER_MAX_HEIGHT_MAX, maxWorldHeight));
        if (oldMaxHeight != maxWorldHeight) {
            LOGGER.warn("Corrected invalid max world height: {} -> {}", oldMaxHeight, maxWorldHeight);
            changed = true;
        }
        
        // Validate sea level
        int oldSeaLevel = seaLevel;
        seaLevel = Math.max(SLIDER_SEA_LEVEL_MIN, Math.min(SLIDER_SEA_LEVEL_MAX, seaLevel));
        if (oldSeaLevel != seaLevel) {
            LOGGER.warn("Corrected invalid sea level: {} -> {}", oldSeaLevel, seaLevel);
            changed = true;
        }
        
        // Ensure logical consistency
        if (seaLevel > maxWorldHeight) {
            seaLevel = Math.min(seaLevel, maxWorldHeight - 10);
            LOGGER.warn("Adjusted sea level to be below max height");
            changed = true;
        }
        
        if (changed) {
            LOGGER.info("Configuration validated and corrected");
        }
    }
    
    // Thread-safe getters
    public int getMaxWorldHeight() {
        LOCK.readLock().lock();
        try {
            return maxWorldHeight;
        } finally {
            LOCK.readLock().unlock();
        }
    }
    
    public int getMinYLimit() {
        // ALWAYS return vanilla value to prevent corruption
        return -64;
    }
    
    public int getSeaLevel() {
        LOCK.readLock().lock();
        try {
            return seaLevel;
        } finally {
            LOCK.readLock().unlock();
        }
    }
    
    // Thread-safe setters with validation
    public void setMaxWorldHeight(int value) {
        LOCK.writeLock().lock();
        try {
            int oldValue = maxWorldHeight;
            maxWorldHeight = Math.max(SLIDER_MAX_HEIGHT_MIN, Math.min(SLIDER_MAX_HEIGHT_MAX, value));
            if (oldValue != maxWorldHeight) {
                LOGGER.debug("Max world height changed: {} -> {}", oldValue, maxWorldHeight);
            }
        } finally {
            LOCK.writeLock().unlock();
        }
    }
    
    public void setMinYLimit(int value) {
        // DO NOTHING - min Y is locked to prevent corruption
        LOGGER.warn("Attempted to change min Y limit - this is disabled to prevent rendering corruption");
    }
    
    public void setSeaLevel(int value) {
        LOCK.writeLock().lock();
        try {
            int oldValue = seaLevel;
            seaLevel = Math.max(SLIDER_SEA_LEVEL_MIN, Math.min(SLIDER_SEA_LEVEL_MAX, value));
            if (oldValue != seaLevel) {
                LOGGER.debug("Sea level changed: {} -> {}", oldValue, seaLevel);
            }
        } finally {
            LOCK.writeLock().unlock();
        }
    }
    
    public void resetToDefaults() {
        LOCK.writeLock().lock();
        try {
            maxWorldHeight = DEFAULT_MAX_HEIGHT;
            minYLimit = DEFAULT_MIN_Y;
            seaLevel = DEFAULT_SEA_LEVEL;
            LOGGER.info("Configuration reset to defaults");
        } finally {
            LOCK.writeLock().unlock();
        }
    }
    
    public int getTotalWorldHeight() {
        LOCK.readLock().lock();
        try {
            return maxWorldHeight - minYLimit;
        } finally {
            LOCK.readLock().unlock();
        }
    }
}