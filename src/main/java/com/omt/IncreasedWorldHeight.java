package com.omt;

import com.omt.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncreasedWorldHeight implements ModInitializer {
	public static final String MOD_ID = "increased-world-height";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Increased World Height mod v1.0.0");
		
		// Load configuration
		try {
			ModConfig.load();
			LOGGER.info("Configuration loaded successfully:");
			LOGGER.info("  - Max Height: {} blocks", ModConfig.getInstance().getMaxWorldHeight());
			LOGGER.info("  - Min Y: {} blocks", ModConfig.getInstance().getMinYLimit());
			LOGGER.info("  - Sea Level: Y={}", ModConfig.getInstance().getSeaLevel());
		} catch (Exception e) {
			LOGGER.error("Failed to load configuration, using defaults", e);
		}
		
		// Register server lifecycle events
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			LOGGER.info("World Height mod active on server");
		});
		
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			// Save configuration on server stop
			ModConfig.save();
			LOGGER.info("Configuration saved on server stop");
		});
		
		LOGGER.info("Increased World Height mod initialized successfully!");
	}
}