package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import com.estrativarus.amasmas.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class FinalizerSpawnerEvents {

    private static final int DIA_INICIO =
            21;

    private static final int INTERVALO_COMPROBACION =
            100;

    private static final int RADIO_HORIZONTAL =
            16;

    private static final int RADIO_VERTICAL =
            8;

    private static final ResourceKey<Structure>
            STRONGHOLD_END =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            "repurposed_structures",
                            "stronghold_end"
                    )
            );

    @SubscribeEvent
    public static void onLevelTick(
            LevelTickEvent.Post event
    ) {

        if (!ModList.get().isLoaded(
                "repurposed_structures"
        )) {

            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        if (level.getGameTime()
                % INTERVALO_COMPROBACION != 0) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        if (diaActual < DIA_INICIO) {
            return;
        }

        for (ServerPlayer player :
                level.players()) {

            revisarAlrededorDelJugador(
                    level,
                    player.blockPosition()
            );
        }
    }

    private static void revisarAlrededorDelJugador(
            ServerLevel level,
            BlockPos centro
    ) {

        for (int x = -RADIO_HORIZONTAL;
             x <= RADIO_HORIZONTAL;
             x++) {

            for (int y = -RADIO_VERTICAL;
                 y <= RADIO_VERTICAL;
                 y++) {

                for (int z = -RADIO_HORIZONTAL;
                     z <= RADIO_HORIZONTAL;
                     z++) {

                    BlockPos posicion =
                            centro.offset(
                                    x,
                                    y,
                                    z
                            );

                    BlockEntity blockEntity =
                            level.getBlockEntity(
                                    posicion
                            );

                    if (!(blockEntity
                            instanceof SpawnerBlockEntity spawner)) {

                        continue;
                    }

                    if (!estaDentroDelStrongholdEnd(
                            level,
                            posicion
                    )) {

                        continue;
                    }

                    spawner.setEntityId(
                            ModEntities
                                    .ESQUELETO_FINALIZADOR
                                    .get(),
                            level.getRandom()
                    );

                    spawner.setChanged();

                    level.sendBlockUpdated(
                            posicion,
                            spawner.getBlockState(),
                            spawner.getBlockState(),
                            3
                    );
                }
            }
        }
    }

    private static boolean estaDentroDelStrongholdEnd(
            ServerLevel level,
            BlockPos posicion
    ) {

        return level
                .structureManager()
                .getStructureWithPieceAt(
                        posicion,
                        holder ->
                                holder.is(
                                        STRONGHOLD_END
                                )
                )
                .isValid();
    }

    private FinalizerSpawnerEvents() {
    }
}