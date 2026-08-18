package com.estrativarus.amasmas.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndSpikeFeature.class)
public abstract class EndSpikeBedrockMixin {

    @ModifyExpressionValue(
            method = "placeSpike",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/block/Blocks;OBSIDIAN:Lnet/minecraft/world/level/block/Block;"
            )
    )
    private Block amasmas$usarBedrockEnTorres(
            Block bloqueOriginal
    ) {

        return Blocks.BEDROCK;
    }
}