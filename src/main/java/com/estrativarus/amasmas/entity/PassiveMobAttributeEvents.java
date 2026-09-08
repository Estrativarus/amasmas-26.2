package com.estrativarus.amasmas.entity;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.minecraft.world.entity.EntityTypes;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class PassiveMobAttributeEvents {

    private static final double DANO_ANIMALES =
            4.0D;

    @SubscribeEvent
    public static void onModifyAttributes(
            EntityAttributeModificationEvent event
    ) {

        anadirDano(
                event,
                EntityTypes.COW
        );

        anadirDano(
                event,
                EntityTypes.MOOSHROOM
        );

        anadirDano(
                event,
                EntityTypes.PIG
        );

        anadirDano(
                event,
                EntityTypes.SHEEP
        );

        anadirDano(
                event,
                EntityTypes.CHICKEN
        );

        anadirDano(
                event,
                EntityTypes.RABBIT
        );

        anadirDano(
                event,
                EntityTypes.HORSE
        );

        anadirDano(
                event,
                EntityTypes.DONKEY
        );

        anadirDano(
                event,
                EntityTypes.MULE
        );

        anadirDano(
                event,
                EntityTypes.CAMEL
        );

        anadirDano(
                event,
                EntityTypes.CAT
        );

        anadirDano(
                event,
                EntityTypes.OCELOT
        );

        anadirDano(
                event,
                EntityTypes.PARROT
        );

        anadirDano(
                event,
                EntityTypes.TURTLE
        );

        anadirDano(
                event,
                EntityTypes.ARMADILLO
        );

        anadirDano(
                event,
                EntityTypes.SNIFFER
        );
    }

    private static void anadirDano(
            EntityAttributeModificationEvent event,
            EntityType<? extends Mob> type
    ) {

        if (event.has(
                type,
                Attributes.ATTACK_DAMAGE
        )) {

            return;
        }

        event.add(
                type,
                Attributes.ATTACK_DAMAGE,
                DANO_ANIMALES
        );
    }

    private PassiveMobAttributeEvents() {
    }
}