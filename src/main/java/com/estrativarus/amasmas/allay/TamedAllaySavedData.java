package com.estrativarus.amasmas.allay;

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

public class TamedAllaySavedData extends SavedData {

    /*
     * Relaciona:
     *
     * UUID del Allay -> UUID del propietario.
     */
    private final Map<String, String> propietarios;

    public static final SavedDataType<TamedAllaySavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Amasmas.MOD_ID,
                            "allays_domesticados"
                    ),

                    TamedAllaySavedData::new,

                    RecordCodecBuilder.create(instance ->
                            instance.group(
                                    Codec.unboundedMap(
                                                    Codec.STRING,
                                                    Codec.STRING
                                            )
                                            .optionalFieldOf(
                                                    "propietarios",
                                                    Map.of()
                                            )
                                            .forGetter(
                                                    data ->
                                                            data.propietarios
                                            )
                            ).apply(
                                    instance,
                                    TamedAllaySavedData::new
                            )
                    )
            );

    public TamedAllaySavedData() {
        this(new HashMap<>());
    }

    public TamedAllaySavedData(
            Map<String, String> propietarios
    ) {
        this.propietarios =
                new HashMap<>(propietarios);
    }

    public static TamedAllaySavedData get(
            MinecraftServer server
    ) {
        return server
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public boolean estaDomesticado(UUID allayUuid) {
        return propietarios.containsKey(
                allayUuid.toString()
        );
    }

    public UUID getPropietario(UUID allayUuid) {

        String propietario =
                propietarios.get(
                        allayUuid.toString()
                );

        if (propietario == null) {
            return null;
        }

        try {
            return UUID.fromString(propietario);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public boolean esPropietario(
            UUID allayUuid,
            UUID jugadorUuid
    ) {

        UUID propietario =
                getPropietario(allayUuid);

        return propietario != null
                && propietario.equals(jugadorUuid);
    }

    public void domesticar(
            UUID allayUuid,
            UUID propietarioUuid
    ) {

        propietarios.put(
                allayUuid.toString(),
                propietarioUuid.toString()
        );

        /*
         * Hace que Minecraft guarde el cambio.
         */
        this.setDirty();
    }

    public void eliminarAllay(UUID allayUuid) {

        if (propietarios.remove(
                allayUuid.toString()
        ) != null) {

            this.setDirty();
        }
    }
}