package com.omt.mixin;

import com.omt.config.ModConfig;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to apply sea level changes to chunk generation
 */
@Mixin(ChunkGeneratorSettings.class)
public class ChunkGeneratorMixin {
    
    @Inject(method = "seaLevel", at = @At("HEAD"), cancellable = true)
    private void modifySeaLevel(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(ModConfig.getInstance().getSeaLevel());
    }
}