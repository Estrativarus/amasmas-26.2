package com.estrativarus.amasmas.deathtrain;

import com.estrativarus.amasmas.Amasmas;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class DeathTrainSavedData extends SavedData {

    public static final long MILISEGUNDOS_POR_HORA =
            60L * 60L * 1000L;

    public static final SavedDataType<DeathTrainSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Amasmas.MOD_ID,
                            "death_train"
                    ),

                    DeathTrainSavedData::new,

                    RecordCodecBuilder.create(instance ->
                            instance.group(
                                    Codec.LONG
                                            .optionalFieldOf(
                                                    "fin_tormenta",
                                                    0L
                                            )
                                            .forGetter(
                                                    data ->
                                                            data.finTormenta
                                            )
                            ).apply(
                                    instance,
                                    DeathTrainSavedData::new
                            )
                    )
            );

    /*
     * Fecha y hora reales en las que debe terminar la tormenta.
     *
     * Un valor 0 significa que el Death Train está inactivo.
     */
    private long finTormenta;

    public DeathTrainSavedData() {
        this(0L);
    }

    public DeathTrainSavedData(long finTormenta) {
        this.finTormenta = finTormenta;
    }

    public static DeathTrainSavedData get(MinecraftServer server) {
        return server
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public boolean estaActivo() {
        return finTormenta > System.currentTimeMillis();
    }

    public long getMilisegundosRestantes() {
        return Math.max(
                0L,
                finTormenta - System.currentTimeMillis()
        );
    }

    public long getHorasRestantesRedondeadas() {

        long tiempo = getMilisegundosRestantes();

        if (tiempo == 0L) {
            return 0L;
        }

        return (tiempo + MILISEGUNDOS_POR_HORA - 1L)
                / MILISEGUNDOS_POR_HORA;
    }

    /*
     * Suma horas al tiempo que ya queda.
     *
     * Si no hay tormenta activa, comienza desde ahora.
     */
    public void anadirHoras(int horas) {

        if (horas <= 0) {
            return;
        }

        long ahora = System.currentTimeMillis();

        long puntoInicial =
                estaActivo() ? finTormenta : ahora;

        this.finTormenta =
                puntoInicial
                        + horas * MILISEGUNDOS_POR_HORA;

        this.setDirty();
    }

    public void quitarHoras(int horas) {

        if (horas <= 0 || !estaActivo()) {
            return;
        }

        long nuevoFinal =
                finTormenta
                        - horas * MILISEGUNDOS_POR_HORA;

        /*
         * Si retiramos más tiempo del que queda,
         * terminamos inmediatamente el Death Train.
         */
        this.finTormenta = Math.max(
                System.currentTimeMillis(),
                nuevoFinal
        );

        this.setDirty();
    }

    /*
     * Devuelve true una sola vez cuando acaba la tormenta.
     */
    public boolean comprobarFinalizacion() {

        if (finTormenta == 0L) {
            return false;
        }

        if (finTormenta > System.currentTimeMillis()) {
            return false;
        }

        finTormenta = 0L;
        this.setDirty();

        return true;
    }

    public void detener() {
        this.finTormenta = 0L;
        this.setDirty();
    }
}