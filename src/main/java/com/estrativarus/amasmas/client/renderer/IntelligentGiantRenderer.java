package com.estrativarus.amasmas.client.renderer;

import com.estrativarus.amasmas.entity.monster.IntelligentGiant;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.GiantMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public final class IntelligentGiantRenderer
        extends GiantMobRenderer {

    private static final Identifier TEXTURA =
            Identifier.fromNamespaceAndPath(
                    "minecraft",
                    "textures/entity/zombie/zombie.png"
            );

    public IntelligentGiantRenderer(
            EntityRendererProvider.Context context
    ) {

        super(context, 6.0F);
    }

    @Override
    public Identifier getTextureLocation(
            ZombieRenderState state
    ) {

        return TEXTURA;
    }
}