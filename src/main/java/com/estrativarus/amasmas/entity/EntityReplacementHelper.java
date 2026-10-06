package com.estrativarus.amasmas.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class EntityReplacementHelper {

    public static final String TAG_REEMPLAZO_EN_CURSO =
            "amasmas_reemplazo_en_curso";

    public static final String TAG_ENTIDAD_REEMPLAZADA =
            "amasmas_entidad_reemplazada";

    public static boolean puedeSerReemplazada(
            Entity original
    ) {

        if (original == null) {
            return false;
        }

        if (original.isRemoved()) {
            return false;
        }

        if (!original.isAlive()) {
            return false;
        }

        return !original
                .getPersistentData()
                .contains(
                        TAG_REEMPLAZO_EN_CURSO
                );
    }

    public static boolean iniciarReemplazo(
            Entity original
    ) {

        if (!puedeSerReemplazada(
                original
        )) {

            return false;
        }

        original
                .getPersistentData()
                .putBoolean(
                        TAG_REEMPLAZO_EN_CURSO,
                        true
                );

        return true;
    }

    public static void marcarReemplazoEnCurso(
            Entity original
    ) {

        if (original == null
                || original.isRemoved()) {

            return;
        }

        original
                .getPersistentData()
                .putBoolean(
                        TAG_REEMPLAZO_EN_CURSO,
                        true
                );
    }

    public static void cancelarReemplazo(
            Entity original
    ) {

        if (original == null
                || original.isRemoved()) {

            return;
        }

        original
                .getPersistentData()
                .remove(
                        TAG_REEMPLAZO_EN_CURSO
                );
    }

    public static boolean reemplazar(
            ServerLevel level,
            Entity original,
            Entity reemplazo
    ) {

        if (!sonEntidadesValidas(
                level,
                original,
                reemplazo
        )) {

            descartarEntidadNueva(
                    reemplazo
            );

            cancelarReemplazo(
                    original
            );

            return false;
        }

        copiarTransformacion(
                original,
                reemplazo
        );

        copiarNombre(
                original,
                reemplazo
        );

        reemplazo
                .getPersistentData()
                .putBoolean(
                        TAG_ENTIDAD_REEMPLAZADA,
                        true
                );

        boolean reemplazoAnadido =
                level.addFreshEntity(
                        reemplazo
                );

        if (!reemplazoAnadido) {

            descartarEntidadNueva(
                    reemplazo
            );

            cancelarReemplazo(
                    original
            );

            return false;
        }

        prepararEliminacionOriginal(
                original
        );

        eliminarDefinitivamente(
                original
        );

        return true;
    }

    public static boolean reemplazarConJinete(
            ServerLevel level,
            Entity original,
            Mob montura,
            Mob jinete
    ) {

        if (level == null
                || original == null
                || montura == null
                || jinete == null) {

            descartarEntidadNueva(
                    montura
            );

            descartarEntidadNueva(
                    jinete
            );

            cancelarReemplazo(
                    original
            );

            return false;
        }

        if (original.isRemoved()
                || !original.isAlive()) {

            descartarEntidadNueva(
                    montura
            );

            descartarEntidadNueva(
                    jinete
            );

            return false;
        }

        copiarTransformacion(
                original,
                montura
        );

        copiarNombre(
                original,
                montura
        );

        jinete.setPos(
                original.getX(),
                original.getY() + 1.0D,
                original.getZ()
        );

        jinete.setYRot(
                original.getYRot()
        );

        jinete.setXRot(
                0.0F
        );

        montura.setPersistenceRequired();
        jinete.setPersistenceRequired();

        montura
                .getPersistentData()
                .putBoolean(
                        TAG_ENTIDAD_REEMPLAZADA,
                        true
                );

        jinete
                .getPersistentData()
                .putBoolean(
                        TAG_ENTIDAD_REEMPLAZADA,
                        true
                );

        boolean monturaAnadida =
                level.addFreshEntity(
                        montura
                );

        if (!monturaAnadida) {

            descartarEntidadNueva(
                    montura
            );

            descartarEntidadNueva(
                    jinete
            );

            cancelarReemplazo(
                    original
            );

            return false;
        }

        boolean jineteAnadido =
                level.addFreshEntity(
                        jinete
                );

        if (!jineteAnadido) {

            eliminarDefinitivamente(
                    montura
            );

            descartarEntidadNueva(
                    jinete
            );

            cancelarReemplazo(
                    original
            );

            return false;
        }

        boolean montado =
                jinete.startRiding(
                        montura
                );

        if (!montado) {

            eliminarDefinitivamente(
                    jinete
            );

            eliminarDefinitivamente(
                    montura
            );

            cancelarReemplazo(
                    original
            );

            return false;
        }

        prepararEliminacionOriginal(
                original
        );

        eliminarDefinitivamente(
                original
        );

        return true;
    }

    private static boolean sonEntidadesValidas(
            ServerLevel level,
            Entity original,
            Entity reemplazo
    ) {

        if (level == null
                || original == null
                || reemplazo == null) {

            return false;
        }

        if (original.isRemoved()
                || !original.isAlive()) {

            return false;
        }

        return reemplazo.level()
                == level;
    }

    private static void copiarTransformacion(
            Entity original,
            Entity reemplazo
    ) {

        reemplazo.setPos(
                original.getX(),
                original.getY(),
                original.getZ()
        );

        reemplazo.setYRot(
                original.getYRot()
        );

        reemplazo.setXRot(
                original.getXRot()
        );

        reemplazo.setYHeadRot(
                original.getYHeadRot()
        );

        reemplazo.setDeltaMovement(
                original.getDeltaMovement()
        );

        reemplazo.setOnGround(
                original.onGround()
        );
    }

    private static void copiarNombre(
            Entity original,
            Entity reemplazo
    ) {

        if (!original.hasCustomName()
                || original.getCustomName() == null) {

            return;
        }

        reemplazo.setCustomName(
                original
                        .getCustomName()
                        .copy()
        );

        reemplazo.setCustomNameVisible(
                original.isCustomNameVisible()
        );
    }

    private static void prepararEliminacionOriginal(
            Entity original
    ) {

        original.stopRiding();
        original.ejectPassengers();
    }

    public static void eliminarDefinitivamente(
            Entity entity
    ) {

        if (entity == null
                || entity.isRemoved()) {

            return;
        }

        entity.stopRiding();
        entity.ejectPassengers();

        entity.remove(
                Entity.RemovalReason.DISCARDED
        );
    }

    private static void descartarEntidadNueva(
            Entity entity
    ) {

        if (entity == null
                || entity.isRemoved()) {

            return;
        }

        entity.remove(
                Entity.RemovalReason.DISCARDED
        );
    }

    private EntityReplacementHelper() {
    }
}