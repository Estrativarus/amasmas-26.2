package com.estrativarus.amasmas.client;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.client.renderer.FinalizerSkeletonRenderer;
import com.estrativarus.amasmas.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.estrativarus.amasmas.client.renderer.IntelligentGiantRenderer;

@EventBusSubscriber(
        modid = Amasmas.MOD_ID,
        value = Dist.CLIENT
)
public final class ModEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {

        event.registerEntityRenderer(
                ModEntities.ESQUELETO_FINALIZADOR.get(),
                FinalizerSkeletonRenderer::new
        );
        event.registerEntityRenderer(ModEntities.GIGANTE.get(), IntelligentGiantRenderer::new);
    }

    private ModEntityRenderers() {
    }
}