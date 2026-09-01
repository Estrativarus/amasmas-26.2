package com.estrativarus.amasmas.health;

import com.estrativarus.amasmas.Amasmas;
import com.estrativarus.amasmas.item.NetheriteAppleItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import com.estrativarus.amasmas.day.SistemaDiasSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHealthSavedData extends SavedData {

    /*
     * Salud base vanilla:
     *
     * 20 puntos = 10 corazones.
     */
    public static final int SALUD_BASE =
            20;

    public static final int DIA_REDUCCION_SALUD =
            21;

    public static final int REDUCCION_SALUD_DIA_21 =
            10;

    /*
     * Guardamos una bonificación total por UUID.
     *
     * Esto permitirá añadir más objetos en el futuro:
     *
     * Manzana de Netherita -> +8
     * Futuro objeto A      -> +4
     * Futuro objeto B      -> +6
     */
    private final Map<String, Integer>
            bonificacionesPorJugador;

    /*
     * También guardamos qué mejoras únicas ha recibido.
     */
    private final Map<String, Boolean>
            manzanaNetheritaConsumida;

    public static final SavedDataType<PlayerHealthSavedData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            Amasmas.MOD_ID,
                            "salud_jugadores"
                    ),

                    PlayerHealthSavedData::new,

                    RecordCodecBuilder.create(instance ->
                            instance.group(

                                    Codec.unboundedMap(
                                                    Codec.STRING,
                                                    Codec.INT
                                            )
                                            .optionalFieldOf(
                                                    "bonificaciones",
                                                    Map.of()
                                            )
                                            .forGetter(
                                                    data ->
                                                            data.bonificacionesPorJugador
                                            ),

                                    Codec.unboundedMap(
                                                    Codec.STRING,
                                                    Codec.BOOL
                                            )
                                            .optionalFieldOf(
                                                    "manzana_netherita",
                                                    Map.of()
                                            )
                                            .forGetter(
                                                    data ->
                                                            data.manzanaNetheritaConsumida
                                            )

                            ).apply(
                                    instance,
                                    PlayerHealthSavedData::new
                            )
                    )
            );

    public PlayerHealthSavedData() {
        this(
                new HashMap<>(),
                new HashMap<>()
        );
    }

    public PlayerHealthSavedData(
            Map<String, Integer> bonificaciones,
            Map<String, Boolean> manzanas
    ) {

        this.bonificacionesPorJugador =
                new HashMap<>(bonificaciones);

        this.manzanaNetheritaConsumida =
                new HashMap<>(manzanas);
    }

    public static PlayerHealthSavedData get(
            MinecraftServer server
    ) {

        return server
                .getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public int getBonificacionTotal(
            UUID uuid
    ) {

        return bonificacionesPorJugador
                .getOrDefault(
                        uuid.toString(),
                        0
                );
    }

    public boolean tieneManzanaNetherita(
            UUID uuid
    ) {

        return manzanaNetheritaConsumida
                .getOrDefault(
                        uuid.toString(),
                        false
                );
    }

    public void concederManzanaNetherita(
            UUID uuid
    ) {

        /*
         * Impedimos sumar la bonificación dos veces.
         */
        if (tieneManzanaNetherita(uuid)) {
            return;
        }

        String clave =
                uuid.toString();

        manzanaNetheritaConsumida.put(
                clave,
                true
        );

        int bonificacionActual =
                getBonificacionTotal(uuid);

        bonificacionesPorJugador.put(
                clave,
                bonificacionActual
                        + NetheriteAppleItem
                        .BONIFICACION_SALUD
        );

        this.setDirty();
    }

    /*
     * Método preparado para futuros objetos.
     */
    public void anadirBonificacion(
            UUID uuid,
            int cantidad
    ) {

        if (cantidad <= 0) {
            return;
        }

        String clave =
                uuid.toString();

        bonificacionesPorJugador.put(
                clave,
                getBonificacionTotal(uuid)
                        + cantidad
        );

        this.setDirty();
    }

    /*
     * Recalcula y aplica la salud máxima guardada.
     */
    public static void aplicarSaludGuardada(
            ServerPlayer player
    ) {

        if (!(player.level()
                instanceof ServerLevel level)) {

            return;
        }

        PlayerHealthSavedData datos =
                get(
                        level.getServer()
                );

        int diaActual =
                SistemaDiasSavedData
                        .get(
                                level.getServer()
                        )
                        .getDiaActual();

        double saludObjetivo =
                SALUD_BASE
                        + datos.getBonificacionTotal(
                        player.getUUID()
                );

        if (diaActual >= DIA_REDUCCION_SALUD) {

            saludObjetivo -=
                    REDUCCION_SALUD_DIA_21;
        }

        saludObjetivo =
                Math.max(
                        2.0D,
                        saludObjetivo
                );

        AttributeInstance atributoSalud =
                player.getAttribute(
                        Attributes.MAX_HEALTH
                );

        if (atributoSalud == null) {
            return;
        }

        if (atributoSalud.getBaseValue()
                != saludObjetivo) {

            atributoSalud.setBaseValue(
                    saludObjetivo
            );
        }

        if (player.getHealth()
                > saludObjetivo) {

            player.setHealth(
                    (float) saludObjetivo
            );
        }
    }
}