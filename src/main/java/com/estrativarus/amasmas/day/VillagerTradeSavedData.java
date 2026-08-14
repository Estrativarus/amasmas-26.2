package com.estrativarus.amasmas.day;

import com.estrativarus.amasmas.Amasmas;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillagerTradeSavedData extends SavedData {

    /*
     * La clave contiene:
     *
     * UUID del aldeano + índice de la oferta.
     *
     * Ejemplo:
     * "uuid-del-aldeano:0" -> 12 usos
     */
    private final Map<String, Integer> usosGuardados;

    public static final SavedDataType<VillagerTradeSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Amasmas.MOD_ID,
                            "usos_intercambios_aldeanos"
                    ),

                    VillagerTradeSavedData::new,

                    RecordCodecBuilder.create(instance ->
                            instance.group(
                                    Codec.unboundedMap(
                                                    Codec.STRING,
                                                    Codec.INT
                                            )
                                            .optionalFieldOf(
                                                    "usos_guardados",
                                                    Map.of()
                                            )
                                            .forGetter(
                                                    data ->
                                                            data.usosGuardados
                                            )
                            ).apply(
                                    instance,
                                    VillagerTradeSavedData::new
                            )
                    )
            );

    public VillagerTradeSavedData() {
        this(new HashMap<>());
    }

    public VillagerTradeSavedData(
            Map<String, Integer> usosGuardados
    ) {
        this.usosGuardados =
                new HashMap<>(usosGuardados);
    }

    public static VillagerTradeSavedData get(
            MinecraftServer server
    ) {
        return server
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    private static String crearClave(
            UUID aldeanoUuid,
            int indiceOferta
    ) {
        return aldeanoUuid
                + ":"
                + indiceOferta;
    }

    /*
     * Obtiene el mayor número de usos que hemos registrado.
     *
     * Si todavía no existe información sobre la oferta,
     * utilizamos su número de usos actual.
     */
    public int getUsosGuardados(
            UUID aldeanoUuid,
            int indiceOferta,
            int usosActuales
    ) {
        return usosGuardados.getOrDefault(
                crearClave(
                        aldeanoUuid,
                        indiceOferta
                ),
                usosActuales
        );
    }

    /*
     * Guarda los usos solamente si el nuevo valor es mayor.
     *
     * De esta forma una reposición vanilla no puede reducir
     * accidentalmente nuestro registro.
     */
    public void guardarMayorNumeroDeUsos(
            UUID aldeanoUuid,
            int indiceOferta,
            int usosActuales
    ) {

        String clave =
                crearClave(
                        aldeanoUuid,
                        indiceOferta
                );

        int usosAnteriores =
                usosGuardados.getOrDefault(
                        clave,
                        -1
                );

        if (usosActuales > usosAnteriores) {

            usosGuardados.put(
                    clave,
                    usosActuales
            );

            this.setDirty();
        }
    }

    /*
     * Elimina los datos de un aldeano.
     */
    public void eliminarAldeano(
            UUID aldeanoUuid
    ) {

        String comienzoClave =
                aldeanoUuid + ":";

        boolean seEliminoAlgo =
                usosGuardados
                        .keySet()
                        .removeIf(
                                clave ->
                                        clave.startsWith(
                                                comienzoClave
                                        )
                        );

        if (seEliminoAlgo) {
            this.setDirty();
        }
    }

    /*
     * Durante la etapa vanilla no necesitamos conservar
     * restricciones antiguas.
     */
    public void limpiarTodo() {

        if (!usosGuardados.isEmpty()) {

            usosGuardados.clear();
            this.setDirty();
        }
    }
}