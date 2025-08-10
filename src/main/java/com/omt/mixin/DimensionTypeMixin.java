package com.omt.mixin;

import com.omt.config.ModConfig;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify dimension height properties
 */
@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin {
    
    /**
     * Overwrite the height method to return our custom height
     * @author IncreasedWorldHeight
     * @reason Custom world height from configuration
     */
    @Overwrite
    public int height() {
        ModConfig config = ModConfig.getInstance();
        return config.getMaxWorldHeight() - config.getMinYLimit();
    }
    
    /**
     * Overwrite the minY method to return our custom minimum Y
     * @author IncreasedWorldHeight
     * @reason Custom minimum Y from configuration
     */
    @Overwrite
    public int minY() {
        return ModConfig.getInstance().getMinYLimit();
    }
    
    /**
     * Inject into logicalHeight to modify it
     */
    @Inject(method = "logicalHeight", at = @At("HEAD"), cancellable = true)
    private void modifyLogicalHeight(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(ModConfig.getInstance().getMaxWorldHeight());
    }
}