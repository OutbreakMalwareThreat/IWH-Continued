package com.omt;

import com.omt.gui.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncreasedWorldHeightClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("WorldHeight/Client");
	private static KeyBinding openConfigKey;
	
	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing World Height client");
		
		// Register keybinding for opening config screen
		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.increased-world-height.open_config",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_H,
			"category.increased-world-height"
		));
		
		// Register tick event to check for key press
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new ConfigScreen(null));
				}
			}
		});
		
		LOGGER.info("World Height client initialized - Press 'H' to open config");
	}
}