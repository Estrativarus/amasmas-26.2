package com.estrativarus.amasmas.client.screen;

import com.estrativarus.amasmas.menu.GiantBagMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BolsaGiganteScreen
        extends AbstractContainerScreen<GiantBagMenu> {

    private static final int ANCHO_INTERFAZ =
            176;

    private static final int ALTO_INTERFAZ =
            132;

    private static final int COLOR_FONDO =
            0xFFC6C6C6;

    private static final int COLOR_BORDE_CLARO =
            0xFFFFFFFF;

    private static final int COLOR_BORDE_OSCURO =
            0xFF555555;

    private static final int COLOR_SLOT =
            0xFF8B8B8B;

    public BolsaGiganteScreen(
            GiantBagMenu menu,
            Inventory inventory,
            Component title
    ) {

        super(
                menu,
                inventory,
                title,
                ANCHO_INTERFAZ,
                ALTO_INTERFAZ
        );

        this.inventoryLabelY =
                40;
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        int x =
                this.getLeftPos();

        int y =
                this.getTopPos();

        graphics.fill(
                x,
                y,
                x + ANCHO_INTERFAZ,
                y + ALTO_INTERFAZ,
                COLOR_FONDO
        );

        graphics.horizontalLine(
                x,
                x + ANCHO_INTERFAZ - 1,
                y,
                COLOR_BORDE_CLARO
        );

        graphics.verticalLine(
                x,
                y,
                y + ALTO_INTERFAZ - 1,
                COLOR_BORDE_CLARO
        );

        graphics.horizontalLine(
                x,
                x + ANCHO_INTERFAZ - 1,
                y + ALTO_INTERFAZ - 1,
                COLOR_BORDE_OSCURO
        );

        graphics.verticalLine(
                x + ANCHO_INTERFAZ - 1,
                y,
                y + ALTO_INTERFAZ - 1,
                COLOR_BORDE_OSCURO
        );

        dibujarSlotsBolsa(
                graphics,
                x,
                y
        );

        dibujarSlotsInventario(
                graphics,
                x,
                y
        );
    }

    private static void dibujarSlotsBolsa(
            GuiGraphicsExtractor graphics,
            int xInterfaz,
            int yInterfaz
    ) {

        int xInicial =
                xInterfaz + 52;

        int yInicial =
                yInterfaz + 19;

        for (int slot = 0;
             slot < GiantBagMenu.SLOTS_BOLSA;
             slot++) {

            int x =
                    xInicial + slot * 18;

            dibujarFondoSlot(
                    graphics,
                    x,
                    yInicial
            );
        }
    }

    private static void dibujarSlotsInventario(
            GuiGraphicsExtractor graphics,
            int xInterfaz,
            int yInterfaz
    ) {

        int xInicial =
                xInterfaz + 7;

        int yInventario =
                yInterfaz + 51;

        for (int fila = 0;
             fila < 3;
             fila++) {

            for (int columna = 0;
                 columna < 9;
                 columna++) {

                int x =
                        xInicial + columna * 18;

                int y =
                        yInventario + fila * 18;

                dibujarFondoSlot(
                        graphics,
                        x,
                        y
                );
            }
        }

        int yBarraRapida =
                yInterfaz + 109;

        for (int columna = 0;
             columna < 9;
             columna++) {

            int x =
                    xInicial + columna * 18;

            dibujarFondoSlot(
                    graphics,
                    x,
                    yBarraRapida
            );
        }
    }

    private static void dibujarFondoSlot(
            GuiGraphicsExtractor graphics,
            int x,
            int y
    ) {

        graphics.fill(
                x,
                y,
                x + 18,
                y + 18,
                COLOR_BORDE_OSCURO
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + 17,
                y + 17,
                COLOR_BORDE_CLARO
        );

        graphics.fill(
                x + 2,
                y + 2,
                x + 16,
                y + 16,
                COLOR_SLOT
        );
    }
}