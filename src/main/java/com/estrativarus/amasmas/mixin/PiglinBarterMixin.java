package com.estrativarus.amasmas.mixin;

import com.estrativarus.amasmas.day.SistemaDiasSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinAi.class)
public abstract class PiglinBarterMixin {

    /*
     * Interceptamos el momento en el que Minecraft calcula
     * la recompensa que debe entregar el Piglin.
     *
     * El Piglin ya ha recogido y consumido el oro antes
     * de llegar a este punto.
     */
    @Inject(
            method = "getBarterResponseItems",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void amasmas$impedirTruequeDesdeDiaSiete(
            Piglin piglin,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {

        /*
         * Esta lógica solo debe ejecutarse en el servidor.
         */
        if (!(piglin.level() instanceof ServerLevel level)) {
            return;
        }

        int diaActual =
                SistemaDiasSavedData
                        .get(level.getServer())
                        .getDiaActual();

        /*
         * Durante los días 1 a 6 se mantiene el
         * comportamiento vanilla completo.
         */
        if (diaActual < 7) {
            return;
        }

        /*
         * Desde el día 7 devolvemos una lista vacía.
         *
         * El Piglin se queda con el lingote de oro,
         * pero no entrega ningún objeto.
         */
        cir.setReturnValue(List.of());
    }
}