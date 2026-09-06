package com.estrativarus.amasmas.menu;

import com.estrativarus.amasmas.item.GiantBagItem;
import com.estrativarus.amasmas.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class GiantBagMenu
        extends AbstractContainerMenu {

    public static final int SLOTS_BOLSA =
            4;

    private static final int PRIMER_SLOT_JUGADOR =
            SLOTS_BOLSA;

    private static final int SLOTS_INVENTARIO_JUGADOR =
            36;

    private static final int TOTAL_SLOTS =
            SLOTS_BOLSA
                    + SLOTS_INVENTARIO_JUGADOR;

    private final SimpleContainer contenedorBolsa;

    private final Inventory inventarioJugador;

    private final ItemStack bolsaAbierta;

    private final int slotBolsaAbierta;

    public GiantBagMenu(
            int containerId,
            Inventory inventarioJugador
    ) {

        this(
                containerId,
                inventarioJugador,
                inventarioJugador.getSelectedSlot()
        );
    }

    public GiantBagMenu(
            int containerId,
            Inventory inventarioJugador,
            int slotBolsa
    ) {

        super(
                ModMenus.BOLSA_GIGANTE_MENU.get(),
                containerId
        );

        this.inventarioJugador =
                inventarioJugador;

        this.slotBolsaAbierta =
                slotBolsa;

        this.bolsaAbierta =
                obtenerBolsa(
                        inventarioJugador,
                        slotBolsa
                );

        this.contenedorBolsa =
                crearContenedorBolsa(
                        bolsaAbierta
                );

        agregarSlotsBolsa();

        agregarInventarioJugador(
                inventarioJugador
        );
    }

    private static ItemStack obtenerBolsa(
            Inventory inventario,
            int slotBolsa
    ) {

        if (slotBolsa < 0
                || slotBolsa
                >= inventario.getContainerSize()) {

            return ItemStack.EMPTY;
        }

        ItemStack stack =
                inventario.getItem(
                        slotBolsa
                );

        if (!GiantBagItem.esBolsaGigante(
                stack
        )) {

            return ItemStack.EMPTY;
        }

        return stack;
    }

    private static SimpleContainer crearContenedorBolsa(
            ItemStack bolsa
    ) {

        SimpleContainer contenedor =
                new SimpleContainer(
                        SLOTS_BOLSA
                ) {

                    @Override
                    public void setChanged() {

                        super.setChanged();

                        if (!bolsa.isEmpty()) {

                            guardarContenedorEnBolsa(
                                    bolsa,
                                    this
                            );
                        }
                    }
                };

        if (bolsa.isEmpty()) {
            return contenedor;
        }

        ItemContainerContents contenido =
                GiantBagItem.obtenerContenido(
                        bolsa
                );

        NonNullList<ItemStack> objetosGuardados =
                NonNullList.withSize(
                        SLOTS_BOLSA,
                        ItemStack.EMPTY
                );

        contenido.copyInto(
                objetosGuardados
        );

        for (int slot = 0;
             slot < SLOTS_BOLSA;
             slot++) {

            contenedor.setItem(
                    slot,
                    objetosGuardados
                            .get(slot)
                            .copy()
            );
        }

        return contenedor;
    }

    private static void guardarContenedorEnBolsa(
            ItemStack bolsa,
            Container contenedor
    ) {

        if (bolsa.isEmpty()) {
            return;
        }

        NonNullList<ItemStack> objetos =
                NonNullList.withSize(
                        SLOTS_BOLSA,
                        ItemStack.EMPTY
                );

        for (int slot = 0;
             slot < SLOTS_BOLSA;
             slot++) {

            objetos.set(
                    slot,
                    contenedor
                            .getItem(slot)
                            .copy()
            );
        }

        ItemContainerContents contenido =
                ItemContainerContents.fromItems(
                        objetos
                );

        GiantBagItem.guardarContenido(
                bolsa,
                contenido
        );
    }

    private void agregarSlotsBolsa() {

        int xInicial =
                53;

        int y =
                20;

        for (int slot = 0;
             slot < SLOTS_BOLSA;
             slot++) {

            int x =
                    xInicial
                            + slot
                            * 18;

            addSlot(
                    new Slot(
                            contenedorBolsa,
                            slot,
                            x,
                            y
                    ) {

                        @Override
                        public boolean mayPlace(
                                ItemStack stack
                        ) {

                            return !GiantBagItem
                                    .esBolsaGigante(
                                            stack
                                    );
                        }
                    }
            );
        }
    }

    private void agregarInventarioJugador(
            Inventory inventario
    ) {

        int yInventario =
                52;

        for (int fila = 0;
             fila < 3;
             fila++) {

            for (int columna = 0;
                 columna < 9;
                 columna++) {

                int indiceInventario =
                        columna
                                + fila
                                * 9
                                + 9;

                int x =
                        8
                                + columna
                                * 18;

                int y =
                        yInventario
                                + fila
                                * 18;

                addSlot(
                        crearSlotJugador(
                                inventario,
                                indiceInventario,
                                x,
                                y
                        )
                );
            }
        }

        int yBarraRapida =
                110;

        for (int columna = 0;
             columna < 9;
             columna++) {

            int x =
                    8
                            + columna
                            * 18;

            addSlot(
                    crearSlotJugador(
                            inventario,
                            columna,
                            x,
                            yBarraRapida
                    )
            );
        }
    }

    private Slot crearSlotJugador(
            Inventory inventario,
            int indiceInventario,
            int x,
            int y
    ) {

        return new Slot(
                inventario,
                indiceInventario,
                x,
                y
        ) {

            @Override
            public boolean mayPickup(
                    Player player
            ) {

                if (indiceInventario
                        == slotBolsaAbierta) {

                    return false;
                }

                return super.mayPickup(
                        player
                );
            }
        };
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {

        if (index < 0
                || index >= slots.size()) {

            return ItemStack.EMPTY;
        }

        Slot slot =
                slots.get(
                        index
                );

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackOriginal =
                slot.getItem();

        ItemStack copia =
                stackOriginal.copy();

        if (index < SLOTS_BOLSA) {

            if (!moveItemStackTo(
                    stackOriginal,
                    PRIMER_SLOT_JUGADOR,
                    TOTAL_SLOTS,
                    true
            )) {

                return ItemStack.EMPTY;
            }

        } else {

            if (GiantBagItem.esBolsaGigante(
                    stackOriginal
            )) {

                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(
                    stackOriginal,
                    0,
                    SLOTS_BOLSA,
                    false
            )) {

                return ItemStack.EMPTY;
            }
        }

        if (stackOriginal.isEmpty()) {

            slot.set(
                    ItemStack.EMPTY
            );

        } else {

            slot.setChanged();
        }

        if (stackOriginal.getCount()
                == copia.getCount()) {

            return ItemStack.EMPTY;
        }

        slot.onTake(
                player,
                stackOriginal
        );

        contenedorBolsa.setChanged();

        return copia;
    }

    @Override
    public boolean stillValid(
            Player player
    ) {

        if (bolsaAbierta.isEmpty()) {
            return false;
        }

        if (!bolsaAbierta.is(
                ModItems.BOLSA_GIGANTE.get()
        )) {

            return false;
        }

        if (slotBolsaAbierta < 0
                || slotBolsaAbierta
                >= inventarioJugador
                .getContainerSize()) {

            return false;
        }

        return inventarioJugador
                .getItem(
                        slotBolsaAbierta
                )
                == bolsaAbierta;
    }

    @Override
    public void removed(
            Player player
    ) {

        guardarContenedorEnBolsa(
                bolsaAbierta,
                contenedorBolsa
        );

        super.removed(
                player
        );
    }
}