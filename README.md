# Increased World Height Mod

A Minecraft Fabric mod that allows you to customize world height limits with professional-looking sliders!

## Features

✨ **Three Professional Sliders:**
- **Maximum World Height** - Control the maximum build height (384 to 2048 blocks)
- **Minimum Y Limit** - Set the lowest Y coordinate (-2048 to -64 blocks)  
- **Sea Level** - Determine where oceans generate (0 to 256 blocks)

## Installation

1. Install Minecraft 1.21.1 with Fabric Loader
2. Install Fabric API
3. Install ModMenu (for easy config access)
4. Place the `increased-world-height-1.0.0.jar` file in your mods folder
5. Launch Minecraft!

## How to Use

### Method 1: ModMenu
1. Click on the "Mods" button in the main menu
2. Find "Increased World Height" in the list
3. Click the config button (⚙️ icon)
4. Adjust the sliders to your preference
5. Click "Save & Apply"

### Method 2: Keybinding
- Press `H` key while in-game to open the configuration screen

## Configuration

The mod saves your settings to `config/increased-world-height.json`

### Slider Ranges:
- **Maximum World Height:** 384 - 2048 blocks
- **Minimum Y Limit:** -2048 to -64 blocks
- **Sea Level:** 0 - 256 blocks

## GUI Features

- 🎨 **Professional rounded sliders** with smooth gradients
- 📝 **Helpful descriptions** for each setting
- 🔄 **Reset to Defaults** button to restore vanilla values
- 💾 **Save & Apply** button to save your changes
- ❌ **Cancel** button to discard changes
- 📊 **Live value display** showing current world height range

## Technical Details

- Built for Minecraft 1.21.1
- Requires Fabric API
- Compatible with ModMenu
- Uses Mixins to modify world generation
- Saves configuration in JSON format

## Default Values

- Maximum World Height: 384 blocks
- Minimum Y Limit: -64 blocks
- Sea Level: 63 blocks

## Notes

⚠️ **Important:** Changes to world height settings will only apply to newly generated chunks. Existing worlds may experience issues if you change these values after world generation.

## Building from Source

```bash
git clone [repository-url]
cd increased-world-height-template-1.21.1
./gradlew build
```

The built JAR will be in `build/libs/`

## License

### MIT License

## Credits
Made By Sonic.OMT Aka Gisus Cryst
Created with ❤️ for the Minecraft modding community!
