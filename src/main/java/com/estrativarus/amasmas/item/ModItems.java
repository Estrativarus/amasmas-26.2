package com.estrativarus.amasmas.item;

import com.estrativarus.amasmas.Amasmas;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;

import java.util.Map;

public class ModItems {

    /*
     * Registro que contiene los objetos del mod.
     */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Amasmas.MOD_ID);

    /*
     * Identificador del archivo:
     *
     * assets/amasmas/equipment/lana.json
     */
    public static final ResourceKey<EquipmentAsset> LANA_ASSET =
            ResourceKey.create(
                    EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(
                            Amasmas.MOD_ID,
                            "lana"
                    )
            );

    /*
     * Material utilizado por las botas lanudas.
     */
    public static final ArmorMaterial LANA_MATERIAL =
            new ArmorMaterial(
                    5,

                    Map.of(
                            ArmorType.BOOTS, 1,
                            ArmorType.LEGGINGS, 2,
                            ArmorType.CHESTPLATE, 3,
                            ArmorType.HELMET, 1,
                            ArmorType.BODY, 3
                    ),

                    15,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    0.0F,
                    0.0F,
                    ItemTags.WOOL,
                    LANA_ASSET
            );

    /*
     * Registro de las botas lanudas.
     */
    public static final DeferredItem<Item> BOTAS_LANUDAS =
            ITEMS.registerItem(
                    "botas_lanudas",
                    properties -> new Item(
                            properties.humanoidArmor(
                                    LANA_MATERIAL,
                                    ArmorType.BOOTS
                            )
                    )
            );
    /*
     * Propiedades alimenticias.
     *
     * La función principal no es recuperar hambre,
     * pero le damos cuatro puntos de nutrición.
     */
    public static final FoodProperties
            MANZANA_NETHERITA_FOOD =
            new FoodProperties.Builder()
                    .nutrition(4)
                    .saturationModifier(1.2F)
                    .alwaysEdible()
                    .build();

    /*
     * Componente que permite consumir el objeto.
     */
    public static final Consumable
            MANZANA_NETHERITA_CONSUMABLE =
            Consumables
                    .defaultFood()
                    .build();

    /*
     * Registro de la Manzana de Netherita.
     */
    public static final DeferredItem<Item>
            MANZANA_NETHERITA =
            ITEMS.registerItem(
                    "manzana_netherita",
                    properties ->
                            new NetheriteAppleItem(
                                    properties
                                            .stacksTo(1)
                                            .fireResistant()
                                            .food(
                                                    MANZANA_NETHERITA_FOOD,
                                                    MANZANA_NETHERITA_CONSUMABLE
                                            )
                            )
            );
    public static final DeferredItem<Item>
    FRAGMENTO_RESONANTITA =
            ITEMS.registerSimpleItem("fragmento_resonantita");

    public static final DeferredItem<ResonantiteBowItem>
            ARCO_RESONANTITA =
            ITEMS.registerItem(
                    "arco_resonantita",
                    properties ->
                            new ResonantiteBowItem(
                                    properties.durability(750)
                            )
            );
    public static final FoodProperties
            CHORUS_FLOWER_STEW_FOOD =
            new FoodProperties.Builder()
                    .nutrition(10)
                    .saturationModifier(1.2F)
                    .alwaysEdible()
                    .build();

    public static final Consumable
            CHORUS_FLOWER_STEW_CONSUMABLE =
            Consumables
                    .defaultFood()
                    .build();

    public static final DeferredItem<Item>
            CHORUS_FLOWER_STEW =
            ITEMS.registerItem(
                    "chorus_flower_stew",
                    properties ->
                            new ChorusFlowerStewItem(
                                    properties
                                            .stacksTo(1)
                                            .food(
                                                    CHORUS_FLOWER_STEW_FOOD,
                                                    CHORUS_FLOWER_STEW_CONSUMABLE
                                            )
                            )
            );
    /*
     * Conecta el registro de objetos con el bus del mod.
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    private ModItems() {
    }
}