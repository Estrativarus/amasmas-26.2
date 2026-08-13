package com.estrativarus.amasmas.day;

import com.estrativarus.amasmas.Amasmas;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class SistemaDiasSavedData extends SavedData {

    /*
     * Número de milisegundos que contiene un día real:
     *
     * 24 horas
     * 60 minutos por hora
     * 60 segundos por minuto
     * 1000 milisegundos por segundo
     */
    public static final long MILISEGUNDOS_POR_DIA =
            24L * 60L * 60L * 1000L;

    /*
     * Describe cómo se guarda y carga la información.
     */
    public static final SavedDataType<SistemaDiasSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Amasmas.MOD_ID,
                            "sistema_dias"
                    ),

                    SistemaDiasSavedData::new,

                    RecordCodecBuilder.create(instance ->
                            instance.group(
                                    Codec.LONG
                                            .fieldOf("tiempo_inicio")
                                            .forGetter(
                                                    data -> data.tiempoInicio
                                            )
                            ).apply(
                                    instance,
                                    SistemaDiasSavedData::new
                            )
                    )
            );

    /*
     * Momento en el que comenzó el día 1.
     */
    private long tiempoInicio;

    /*
     * Constructor utilizado cuando todavía no existen datos.
     *
     * El momento actual pasa a ser el comienzo del día 1.
     */
    public SistemaDiasSavedData() {
        this(System.currentTimeMillis());

        /*
         * Indicamos que los datos nuevos deben guardarse.
         */
        this.setDirty();
    }

    /*
     * Constructor utilizado por el Codec al cargar los datos del mundo.
     */
    public SistemaDiasSavedData(long tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    /*
     * Obtiene o crea los datos asociados al servidor.
     */
    public static SistemaDiasSavedData get(MinecraftServer server) {
        return server
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    /*
     * Calcula el día real actual.
     */
    public int getDiaActual() {

        long tiempoActual = System.currentTimeMillis();

        /*
         * Por seguridad impedimos valores negativos si el reloj
         * del ordenador se cambia hacia atrás.
         */
        long tiempoTranscurrido =
                Math.max(0L, tiempoActual - tiempoInicio);

        long diasCompletados =
                tiempoTranscurrido / MILISEGUNDOS_POR_DIA;

        /*
         * El primer periodo de 24 horas corresponde al día 1.
         */
        long diaActual = diasCompletados + 1L;

        /*
         * Evitamos un desbordamiento extremadamente improbable.
         */
        return (int) Math.min(diaActual, Integer.MAX_VALUE);
    }

    /*
     * Cambia el día y reinicia el progreso interno al principio
     * del día seleccionado.
     *
     * Ejemplo:
     * establecerDia(14) hace que ahora sea el comienzo del día 14.
     */
    public void establecerDia(int nuevoDia) {

        if (nuevoDia < 1) {
            nuevoDia = 1;
        }

        long diasQueDebenHaberPasado = nuevoDia - 1L;

        this.tiempoInicio =
                System.currentTimeMillis()
                        - diasQueDebenHaberPasado
                        * MILISEGUNDOS_POR_DIA;

        /*
         * Muy importante: hace que Minecraft guarde el cambio.
         */
        this.setDirty();
    }

    /*
     * Permite comprobar fácilmente si estamos en un día concreto.
     */
    public boolean esDia(int dia) {
        return getDiaActual() == dia;
    }

    /*
     * Permite comprobar si ya se ha alcanzado un día.
     *
     * Este será especialmente útil para cambios permanentes.
     */
    public boolean esDiaOPosterior(int dia) {
        return getDiaActual() >= dia;
    }

    /*
     * Devuelve el bloque actual de siete días.
     *
     * Días 1-6   → etapa 0
     * Días 7-13  → etapa 1
     * Días 14-20 → etapa 2
     * ...
     * Día 70+    → etapa 10
     */
    public int getEtapaDeSieteDias() {
        return Math.min(getDiaActual() / 7, 10);
    }
}