package com.estrativarus.amasmas.client.renderer;

import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.resources.Identifier;

public final class FinalizerSkeletonRenderer
        extends BoggedRenderer {

    private static final Identifier TEXTURA =
            Identifier.fromNamespaceAndPath(
                    "amasmas",
                    "textures/entity/skeleton/esqueleto_finalizador.png"
            );

    public FinalizerSkeletonRenderer(
            EntityRendererProvider.Context context
    ) {

        super(
                context
        );
    }

    @Override
    public Identifier getTextureLocation(
            BoggedRenderState state
    ) {

        return TEXTURA;
    }
}