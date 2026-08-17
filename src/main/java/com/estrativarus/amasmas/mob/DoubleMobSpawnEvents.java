package com.estrativarus.amasmas.mob;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = Amasmas.MOD_ID)
public final class DoubleMobSpawnEvents {

    private static final int DIA_INICIO =
            7;

    @SubscribeEvent
    public static void onFinalizeSpawn(
            FinalizeSpawnEvent event
    ) {

        /*
         * Solamente duplicamos apariciones naturales.
         *
         * Así evitamos duplicar:
         *
         * comandos;
         * huevos de aparición;
         * spawners;
         * conversiones;
         * entidades creadas por el propio mod.
         */
        if (event.getSpawnType()
                != EntitySpawnReason.NATURAL) {

            return;
        }

        Mob mobOriginal =
                event.getEntity();

        /*
         * Solo mobs hostiles.
         *
         * Los animales, aldeanos y criaturas pasivas
         * no pertenecen a la categoría MONSTER.
         */
        if (mobOriginal
                .getType()
                .getCategory()
                != MobCategory.MONSTER) {

            return;
        }

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        /*
         * Días 1 a 6:
         *
         * cantidad de mobs vanilla.
         */
        if (diaActual < DIA_INICIO) {
            return;
        }

        /*
         * Esperamos al siguiente ciclo del servidor.
         *
         * En ese momento el mob original ya habrá terminado
         * su proceso de aparición.
         */
        level.getServer().execute(() ->
                crearCopia(
                        level,
                        mobOriginal
                )
        );
    }

    private static void crearCopia(
            ServerLevel level,
            Mob mobOriginal
    ) {

        if (!mobOriginal.isAlive()
                || mobOriginal.isRemoved()) {

            return;
        }

        /*
         * Creamos otro mob exactamente del mismo tipo.
         *
         * Usamos EVENT como motivo para que la copia no
         * sea tratada como otra aparición natural.
         */
        Mob copia =
                (Mob) mobOriginal
                        .getType()
                        .create(
                                level,
                                EntitySpawnReason.EVENT
                        );

        if (copia == null) {
            return;
        }

        /*
         * Colocamos la copia cerca del original.
         *
         * La distancia evita que ambas entidades ocupen
         * exactamente el mismo punto.
         */
        double desplazamientoX =
                mobOriginal
                        .getRandom()
                        .nextBoolean()
                        ? 1.0D
                        : -1.0D;

        double desplazamientoZ =
                mobOriginal
                        .getRandom()
                        .nextBoolean()
                        ? 1.0D
                        : -1.0D;

        copia.setPos(
                mobOriginal.getX()
                        + desplazamientoX,
                mobOriginal.getY(),
                mobOriginal.getZ()
                        + desplazamientoZ
        );

        copia.setYRot(
                mobOriginal.getYRot()
        );

        copia.setXRot(
                mobOriginal.getXRot()
        );

        /*
         * Ejecutamos la inicialización normal de la copia.
         *
         * Así obtiene:
         *
         * dificultad local;
         * equipamiento;
         * variantes;
         * atributos de aparición;
         * las demás modificaciones del mod.
         */
        copia.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(
                        copia.blockPosition()
                ),
                EntitySpawnReason.EVENT,
                null
        );

        level.addFreshEntity(
                copia
        );
    }

    private DoubleMobSpawnEvents() {
    }
}

