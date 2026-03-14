package smartin.miapi.registries;

import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.blocks.ModularWorkBench;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.blueprint.BlueprintComponent;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.effects.StunResistanceStatusEffect;
import smartin.miapi.effects.StunStatusEffect;
import smartin.miapi.effects.TeleportBlockEffect;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.ModularItemPart;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.items.BrokenModularVisualOnlyItem;
import smartin.miapi.item.modular.items.ExampleModularItem;
import smartin.miapi.item.modular.items.ExampleModularStrackableItem;
import smartin.miapi.item.modular.items.ModularVisualOnlyItem;
import smartin.miapi.item.modular.items.armor.*;
import smartin.miapi.item.modular.items.bows.ModularArrow;
import smartin.miapi.item.modular.items.bows.ModularBow;
import smartin.miapi.item.modular.items.bows.ModularCrossbow;
import smartin.miapi.item.modular.items.shield.ModularNonVanillaShield;
import smartin.miapi.item.modular.items.shield.ModularVanillaShield;
import smartin.miapi.item.modular.items.shield.TowerShieldComponent;
import smartin.miapi.item.modular.items.tools.*;
import smartin.miapi.loot.*;
import smartin.miapi.loot.condition.LootTableCondition;
import smartin.miapi.material.ComponentMaterial;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.composite.CompositeMaterial;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.StackStorageComponent;
import smartin.miapi.modules.abilities.*;
import smartin.miapi.modules.abilities.gun.GunContextProperty;
import smartin.miapi.modules.abilities.gun.GunMagazineComponent;
import smartin.miapi.modules.abilities.shield.BlockAbility;
import smartin.miapi.modules.abilities.shield.ParryBlock;
import smartin.miapi.modules.abilities.shield.TowerShieldBlock;
import smartin.miapi.modules.abilities.toolabilities.AxeAbility;
import smartin.miapi.modules.abilities.toolabilities.HoeAbility;
import smartin.miapi.modules.abilities.toolabilities.ShovelAbility;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.conditions.*;
import smartin.miapi.modules.edit_options.*;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.material.MaterialViewer;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.properties.compat.better_combat.BetterCombatHelper;
import smartin.miapi.modules.properties.inventory.InventoryComponent;
import smartin.miapi.modules.properties.inventory.screen.DefaultInventoryScreenHandler;
import smartin.miapi.modules.properties.onHit.NemesisProperty;
import smartin.miapi.modules.properties.projectile.RapidfireCrossbowProperty;
import smartin.miapi.modules.properties.render.ItemModelProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.synergies.SynergyManager;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.modules.abilities.util.ItemAbilityManager.useAbilityRegistry;

public class RegistryInventory {
    public static final Supplier<RegistrarManager> registrar = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final MiapiRegistrar<Item> MODULAR_ITEMS = MiapiRegistrar.of(registrar.get().get(Registries.ITEM));
    public static final Registrar<Item> ITEM_REGISTRAR = registrar.get().get(Registries.ITEM);
    public static final Registrar<DataComponentType<?>> COMPONENT_TYPE_REGISTRAR = registrar.get().get(Registries.DATA_COMPONENT_TYPE);
    public static final Registrar<Block> BLOCK_REGISTRAR = registrar.get().get(Registries.BLOCK);
    public static final Registrar<BlockEntityType<?>> BLOCK_ENTITY_TYPE_REGISTRAR = registrar.get().get(Registries.BLOCK_ENTITY_TYPE);
    public static final Registrar<Attribute> ATTRIBUTE_REGISTRAR = registrar.get().get(Registries.ATTRIBUTE);
    public static final Registrar<ArmorMaterial> ARMOR_MATERIAL_REGISTRAR = registrar.get().get(Registries.ARMOR_MATERIAL);
    public static final Registrar<EntityType<?>> ENTITY_TYPE_REGISTRAR = registrar.get().get(Registries.ENTITY_TYPE);
    public static final Registrar<MenuType<?>> MENU_TYPE_REGISTRAR = registrar.get().get(Registries.MENU);
    public static final Registrar<MobEffect> MOB_EFFECT_REGISTRAR = registrar.get().get(Registries.MOB_EFFECT);
    public static final Registrar<CreativeModeTab> CREATIVE_MODE_TAB_REGISTRAR = registrar.get().get(Registries.CREATIVE_MODE_TAB);
    public static final Registrar<GameEvent> GAME_EVENT_REGISTRAR = registrar.get().get(Registries.GAME_EVENT);
    public static final Registrar<RecipeSerializer<?>> RECIPE_SERIALIZER_REGISTRAR = registrar.get().get(Registries.RECIPE_SERIALIZER);
    public static final MiapiRegistry<ModuleProperty> MODULE_PROPERTY_MIAPI_REGISTRY = MiapiRegistry.getInstance(ModuleProperty.class);
    public static final MiapiRegistry<ItemModule> ITEM_MODULE_MIAPI_REGISTRY = MiapiRegistry.getInstance(ItemModule.class);
    public static final MiapiRegistry<ItemModule> modules = ITEM_MODULE_MIAPI_REGISTRY;
    public static final MiapiRegistry<ItemUseAbility> ITEM_USE_ABILITY_MIAPI_REGISTRY = useAbilityRegistry;
    public static final MiapiRegistry<EditOption> EDIT_OPTION_MIAPI_REGISTRY = MiapiRegistry.getInstance(EditOption.class);
    public static final MiapiRegistry<CraftingStat> CRAFTING_STATS_REGISTRY = MiapiRegistry.getInstance(CraftingStat.class);
    public static final MiapiRegistry<Material> MATERIAL_REGISTRY = MiapiRegistry.getInstance(Material.class);
    public static final Registrar<LootItemFunctionType<?>> LOOT_ITEM_FUNCTION_TYPE_REGISTRAR = registrar.get().get(Registries.LOOT_FUNCTION_TYPE);
    public static final Registrar<LootItemConditionType> LOOT_ITEM_CONDITION_TYPE_REGISTRAR = registrar.get().get(Registries.LOOT_CONDITION_TYPE);
    public static final TagKey<Item> MIAPI_FORBIDDEN_TAG = TagKey.create(Registries.ITEM, Miapi.id("forbidden"));
    public static final TagKey<Item> MIAPI_MATERIALS = TagKey.create(Registries.ITEM, ResourceLocation.parse("miapi_materials"));

    public static <T> RegistrySupplier<T> registerAndSupply(Registrar<T> rg, ResourceLocation id, Supplier<T> object) {
        return rg.register(id, object);
    }

    public static <T> RegistrySupplier<T> registerAndSupply(Registrar<T> rg, String id, Supplier<T> object) {
        return registerAndSupply(rg, ResourceLocation.fromNamespaceAndPath(MOD_ID, id), object);
    }

    public static <T, E extends T> void register(Registrar<T> rg, ResourceLocation id, Supplier<E> object, Consumer<E> onRegister) {
        rg.register(id, object).listen(onRegister);
    }

    public static <T, E extends T> void register(Registrar<T> rg, String id, Supplier<E> object, Consumer<E> onRegister) {
        register(rg, ResourceLocation.fromNamespaceAndPath(MOD_ID, id), object, onRegister);
    }

    public static <T> void register(Registrar<T> rg, ResourceLocation id, Supplier<T> object) {
        rg.register(id, object);
    }

    public static <T> void register(Registrar<T> rg, String id, Supplier<T> object) {
        register(rg, ResourceLocation.fromNamespaceAndPath(MOD_ID, id), object);
    }

    public static <T> void registerMiapi(MiapiRegistry<T> rg, String id, T object) {
        rg.register(id, object);
    }

    public static <T> void registerMiapi(MiapiRegistry<T> rg, ResourceLocation id, T object) {
        rg.register(id, object);
    }

    public static <T> void addCallback(Registrar<T> rg, Consumer<T> consumer) {
        rg.getIds().forEach(id -> rg.listen(id, consumer));
    }

    /**
     * Registers an attribute.
     *
     * @param id         The id of the attribute. Miapi namespace is inferred
     * @param attach     Whether this attribute should automatically attach to living entities
     * @param sup        Supplier of the actual attribute
     * @param onRegister Callback for after the attribute is actually registered. Use this to set static fields
     */
    public static void registerAtt(String id, boolean attach, Supplier<Attribute> sup, Consumer<Holder<Attribute>> onRegister) {
        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(MOD_ID, id);

        RegistrySupplier<Attribute> obj = ATTRIBUTE_REGISTRAR.register(rl, sup); // actually register the object
        obj.listen((attribute -> {
            onRegister.accept(ATTRIBUTE_REGISTRAR.getHolder(rl));
            if (attach) {
                AttributeRegistry.entityAttributeMap.put(rl, ATTRIBUTE_REGISTRAR.getHolder(rl));
            }
        }));
    }

    public static Block modularWorkBench;
    //public static Block exampleStatProviderBlock;
    public static BlockEntityType<ModularWorkBenchEntity> modularWorkBenchEntityType;
    public static Item modularItem;
    public static Item modularStackableItem;
    public static Item visualOnlymodularItem;
    public static Item brokenModualrItem;
    public static Item modularAxe;
    public static Item modularMattock;
    public static Item modularArrow;
    public static Holder<MobEffect> cryoStatusEffect;
    public static Holder<MobEffect> teleportBlockEffect;
    public static Holder<MobEffect> stunEffect;
    public static Holder<MobEffect> stunResistanceEffect;
    public static GameEvent statProviderCreatedEvent;
    public static GameEvent statProviderRemovedEvent;
    public static Holder<ArmorMaterial> armorMaterial;
    //public static SimpleCraftingStat exampleCraftingStat;
    public static RecipeSerializer serializer;
    public static RecipeType<?> recipeType;
    public static RegistrySupplier<EntityType<ItemProjectileEntity>> itemProjectileType = (RegistrySupplier) registerAndSupply(ENTITY_TYPE_REGISTRAR, "thrown_item", () ->
            EntityType.Builder.of(ItemProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("miapi:thrown_item"));
    public static EntityType<ItemProjectileEntity> registeredItemProjectileType;
    public static LootItemFunctionType<ModuleSwapLootFunction> moduleSwapLootFunctionLootItemFunctionType = new LootItemFunctionType<>(ModuleSwapLootFunction.CODEC);
    public static LootItemFunctionType<MaterialSwapLootFunction> materialSwapLootFunctionLootItemFunctionType = new LootItemFunctionType<>(MaterialSwapLootFunction.CODEC);
    public static LootItemFunctionType<AutoSmeltFunction> autoSmeltFunctionLootItemFunctionType = new LootItemFunctionType<>(AutoSmeltFunction.CODEC);
    public static LootItemFunctionType<GlobalLootModularisationFunction> globalLootItemFunctionType = new LootItemFunctionType<>(GlobalLootModularisationFunction.CODEC);


    static {
        itemProjectileType.listen(e -> {
            registeredItemProjectileType = e;
            if (Platform.getEnvironment() == Env.CLIENT)
                MiapiClient.registerEntityRenderer();
        });
    }

    public static MenuType<CraftingScreenHandler> craftingScreenHandler;
    public static MenuType<DefaultInventoryScreenHandler> backpackScreenHandler;

    public static void setup() {
        //SCREEN
        register(MENU_TYPE_REGISTRAR, "default_crafting", () ->
                        new MenuType<>(CraftingScreenHandler::new, FeatureFlagSet.of()),
                scr -> {
                    RegistryInventory.craftingScreenHandler = scr;
                    if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerScreenHandler();
                });
        register(MENU_TYPE_REGISTRAR, "inventory", () ->
                        new MenuType<>(DefaultInventoryScreenHandler::new, FeatureFlagSet.of()),
                scr -> {
                    RegistryInventory.backpackScreenHandler = scr;
                    if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerBackPackHandler();
                });

        RegistryInventory.LOOT_ITEM_CONDITION_TYPE_REGISTRAR.register(
                LootHelper.LOOT_TABLE_ID, () -> LootTableCondition.TYPE);

        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("item_module"), () -> ModuleInstance.MODULE_INSTANCE_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("modular_material"), () -> ComponentMaterial.NBT_MATERIAL_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("module_blueprint"), () -> BlueprintComponent.BLUEPRINT_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("nemesis_property"), () -> NemesisProperty.NEMESIS_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("magazine_property"), () -> RapidfireCrossbowProperty.ADDITIONAL_PROJECTILES_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("item_module_property"), () -> ItemModelProperty.ITEM_MODEL_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("stack_storage"), () -> StackStorageComponent.STACK_STORAGE_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("tower_shield"), () -> TowerShieldComponent.TOWER_SHIELD_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                CompositeMaterial.KEY, () -> CompositeMaterial.COMPOSITE_MATERIAL_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("gun_magazine"), () -> GunMagazineComponent.STACK_STORAGE_COMPONENT);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("module_fallback"), () -> ModuleInstance.MODULE_BACKUP);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("force_visual_only"), () -> ModularItem.IS_VISUAL_ONLY);
        RegistryInventory.COMPONENT_TYPE_REGISTRAR.register(
                Miapi.id("inventory"), () -> InventoryComponent.ITEM_INVENTORIES);


        RegistryInventory.LOOT_ITEM_FUNCTION_TYPE_REGISTRAR.register(
                Miapi.id("module_swap"), () -> moduleSwapLootFunctionLootItemFunctionType);

        RegistryInventory.LOOT_ITEM_FUNCTION_TYPE_REGISTRAR.register(
                Miapi.id("material_swap"), () -> materialSwapLootFunctionLootItemFunctionType);
        RegistryInventory.LOOT_ITEM_FUNCTION_TYPE_REGISTRAR.register(
                Miapi.id("auto_smelt"), () -> autoSmeltFunctionLootItemFunctionType);
        RegistryInventory.LOOT_ITEM_FUNCTION_TYPE_REGISTRAR.register(
                Miapi.id("global_loot_adjuster"), () -> globalLootItemFunctionType);

        register(ARMOR_MATERIAL_REGISTRAR, "modular_armor_material", () ->
                new ArmorMaterial(
                        Util.make(new EnumMap(ArmorItem.Type.class), (enumMap) -> {
                            enumMap.put(ArmorItem.Type.BOOTS, 1);
                            enumMap.put(ArmorItem.Type.LEGGINGS, 4);
                            enumMap.put(ArmorItem.Type.CHESTPLATE, 5);
                            enumMap.put(ArmorItem.Type.HELMET, 2);
                            enumMap.put(ArmorItem.Type.BODY, 4);
                        }),
                        5,
                        SoundEvents.ARMOR_EQUIP_IRON,
                        () -> Ingredient.EMPTY,
                        List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "miapi_modular_armor"))),
                        5.0f, 5.0f
                ), (s) -> {
            armorMaterial = BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(s);
        });


        //ENTITY
        // commented out because RegistrySupplier is needed... see itemProjectileType field definition above
        /*register(entityTypes, "thrown_item", () ->
                EntityType.builder.create(ItemProjectile::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_item"),
                type -> itemProjectileType = (EntityType<ItemProjectile>) type);*/

        //RECIPE SERIALIZERS
        register(RECIPE_SERIALIZER_REGISTRAR, "material_smithing", MaterialSmithingRecipe.Serializer::new, i -> serializer = i);


        //BLOCK
        register(BLOCK_REGISTRAR, "modular_work_bench", () -> new ModularWorkBench(
                BlockBehaviour.Properties.of().
                        mapColor(MapColor.METAL).
                        instrument(NoteBlockInstrument.IRON_XYLOPHONE).
                        requiresCorrectToolForDrops().
                        strength(2.5F, 6.0F).
                        sound(SoundType.METAL).
                        noOcclusion().
                        pushReaction(PushReaction.IGNORE)), b -> modularWorkBench = b);
        register(BLOCK_ENTITY_TYPE_REGISTRAR, "modular_work_bench", () -> BlockEntityType.Builder.of(
                ModularWorkBenchEntity::new, modularWorkBench
        ).build(null), be -> {
            modularWorkBenchEntityType = be;
            if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerBlockEntityRenderer();
        });
        register(ITEM_REGISTRAR, "modular_work_bench", () -> new BlockItem(modularWorkBench, new Item.Properties()));


//        registerMiapi(craftingStats, "hammering", new SimpleCraftingStat(0), stat -> exampleCraftingStat = stat);
//        register(blocks, "example_stat_provider", () ->
//                new StatProvidingBlock(AbstractBlock.Settings.create(), new StatProvidersMap().set(exampleCraftingStat, StatActorType.ADD, 2d)), b -> exampleStatProviderBlock = b);
//        register(items, "example_stat_provider", () -> new BlockItem(exampleStatProviderBlock, new Item.Settings()));


        // CREATIVE TAB
        register(CREATIVE_MODE_TAB_REGISTRAR, "miapi_tab", () -> CreativeTabRegistry.create
                (b -> {
                    b.title(Component.translatable("miapi.tab.name"));
                    b.icon(() -> new ItemStack(modularWorkBench));
                    b.displayItems((displayContext, entries) -> {
                        entries.accept(modularWorkBench);
                    });
                }));

        //ITEM
        register(MODULAR_ITEMS, "modular_broken_item", BrokenModularVisualOnlyItem::new, i -> brokenModualrItem = i);
        register(MODULAR_ITEMS, "modular_part_visual", ModularVisualOnlyItem::new, i -> visualOnlymodularItem = i);

        register(MODULAR_ITEMS, "modular_item", ExampleModularItem::new, i -> modularItem = i);
        register(MODULAR_ITEMS, "modular_stackable_item", ExampleModularStrackableItem::new, (i) -> modularStackableItem = i);
        register(MODULAR_ITEMS, "modular_part", ModularItemPart::new);

        register(MODULAR_ITEMS, "modular_handheld", ModularWeapon::new);
        register(MODULAR_ITEMS, "modular_katars", ModularSword::new);
        register(MODULAR_ITEMS, "modular_gauntlets", ModularWeapon::new);
        register(MODULAR_ITEMS, "modular_knuckles", ModularWeapon::new);
        register(MODULAR_ITEMS, "modular_tonfa", ModularWeapon::new);

        register(MODULAR_ITEMS, "modular_handle", ModularWeapon::new);
        register(MODULAR_ITEMS, "modular_sword", ModularSword::new);
        register(MODULAR_ITEMS, "twin_blade", ModularSword::new);
        register(MODULAR_ITEMS, "modular_katana", ModularSword::new);
        register(MODULAR_ITEMS, "modular_naginata", ModularSword::new);
        register(MODULAR_ITEMS, "modular_greatsword", ModularSword::new);
        register(MODULAR_ITEMS, "modular_dagger", ModularSword::new);
        register(MODULAR_ITEMS, "modular_spear", ModularSword::new);
        register(MODULAR_ITEMS, "modular_throwing_knife", ModularSword::new);
        register(MODULAR_ITEMS, "modular_rapier", ModularSword::new);
        register(MODULAR_ITEMS, "modular_longsword", ModularSword::new);
        register(MODULAR_ITEMS, "modular_trident", ModularSword::new);
        register(MODULAR_ITEMS, "modular_scythe", ModularSword::new);
        register(MODULAR_ITEMS, "modular_sickle", ModularSword::new);

        register(MODULAR_ITEMS, "modular_shovel", ModularShovel::new);
        register(MODULAR_ITEMS, "modular_pickaxe", ModularPickaxe::new);
        register(MODULAR_ITEMS, "modular_hammer", ModularPickaxe::new);
        register(MODULAR_ITEMS, "modular_axe", ModularAxe::new, i -> modularAxe = i);
        register(MODULAR_ITEMS, "modular_hoe", ModularHoe::new);
        register(MODULAR_ITEMS, "modular_mattock", ModularAxe::new, i -> modularMattock = i);

        register(MODULAR_ITEMS, "modular_mace", ModularMace::new);

        register(MODULAR_ITEMS, "modular_bow", ModularBow::new);
        register(MODULAR_ITEMS, "modular_small_bow", ModularBow::new);
        register(MODULAR_ITEMS, "modular_large_bow", ModularBow::new);
        register(MODULAR_ITEMS, "modular_bow_part", ExampleModularItem::new);
        register(MODULAR_ITEMS, "modular_crossbow", ModularCrossbow::new);
        register(MODULAR_ITEMS, "modular_small_crossbow", ModularCrossbow::new);
        register(MODULAR_ITEMS, "modular_large_crossbow", ModularCrossbow::new);
        register(MODULAR_ITEMS, "modular_crossbow_part", ExampleModularItem::new);
        register(MODULAR_ITEMS, "modular_arrow", ModularArrow::new, i -> modularArrow = i);
        register(MODULAR_ITEMS, "modular_arrow_part", ExampleModularStrackableItem::new);

        register(MODULAR_ITEMS, "modular_helmet", ModularHelmet::new);
        register(MODULAR_ITEMS, "modular_chestplate", ModularChestPlate::new);
        register(MODULAR_ITEMS, "modular_leggings", ModularLeggings::new);
        register(MODULAR_ITEMS, "modular_boots", ModularBoots::new);

        register(MODULAR_ITEMS, "modular_tower_shield", ModularVanillaShield::new);
        register(MODULAR_ITEMS, "modular_shield", ModularNonVanillaShield::new);
        register(MODULAR_ITEMS, "modular_round_shield", ModularNonVanillaShield::new);
        register(MODULAR_ITEMS, "modular_heater_shield", ModularNonVanillaShield::new);

        register(MODULAR_ITEMS, "modular_elytra", ModularElytraItem::getInstance);

        //STATUS EFFECTS
        register(MOB_EFFECT_REGISTRAR, "cryo", CryoStatusEffect::new, eff -> {
            cryoStatusEffect = MOB_EFFECT_REGISTRAR.getHolder(MOB_EFFECT_REGISTRAR.getId(eff));
        });
        register(MOB_EFFECT_REGISTRAR, "teleport_block", TeleportBlockEffect::new, eff -> {
            teleportBlockEffect = MOB_EFFECT_REGISTRAR.getHolder(MOB_EFFECT_REGISTRAR.getId(eff));
        });
        register(MOB_EFFECT_REGISTRAR, "stun", StunStatusEffect::new, eff -> {
            stunEffect = MOB_EFFECT_REGISTRAR.getHolder(MOB_EFFECT_REGISTRAR.getId(eff));
        });
        register(MOB_EFFECT_REGISTRAR, "stun_resistance", StunResistanceStatusEffect::new, eff -> {
            stunResistanceEffect = MOB_EFFECT_REGISTRAR.getHolder(MOB_EFFECT_REGISTRAR.getId(eff));
        });

        smartin.miapi.registries.AttributeRegistry.registerAttributes();


        // GAME EVENTS
        register(GAME_EVENT_REGISTRAR, "stat_provider_added", () -> new GameEvent(16), ev -> statProviderCreatedEvent = ev);
        register(GAME_EVENT_REGISTRAR, "stat_provider_removed", () -> new GameEvent(16), ev -> statProviderRemovedEvent = ev);


        LifecycleEvent.SETUP.register(() -> {
            //EDITPROPERTIES
            registerMiapi(EDIT_OPTION_MIAPI_REGISTRY, "replace", new ReplaceOption());
            registerMiapi(EDIT_OPTION_MIAPI_REGISTRY, "dev", new PropertyInjectionDev());
            registerMiapi(EDIT_OPTION_MIAPI_REGISTRY, "skin", new SkinOptions());
            registerMiapi(EDIT_OPTION_MIAPI_REGISTRY, "create", new CreateItemOption());
            registerMiapi(EDIT_OPTION_MIAPI_REGISTRY, "cosmetic", new CosmeticEditOption());
            registerMiapi(EDIT_OPTION_MIAPI_REGISTRY, "glint_settings", new GlintEditOption());
            registerMiapi(EDIT_OPTION_MIAPI_REGISTRY, "material_lexica", new MaterialViewer());
            SynergyManager.setup();

            //CONDITIONS
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("true"), TrueCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("not"), NotCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("or"), OrCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("and"), AndCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("child"), ChildCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("parent"), ParentCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("other_module"), OtherModuleModuleCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("module"), ModuleTypeCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("tag"), TagCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("mod_loaded"), IsModLoadedCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("material"), MaterialCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("miapi_perm"), MiapiPerm.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("material_count"), MaterialCountCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("material_group"), MaterialGroupCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("item_in_inventory"), ItemInInventoryCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("advancement"), AdvancementCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("number"), NumberCondition.CODEC);

            //MODULEPROPERTIES
            PropertyRegistry.registerProperties();

            //registerMiapi(MODULE_PROPERTY_MIAPI_REGISTRY, ReturnIngredientProperty.KEY, new ReturnIngredientProperty());
            //compat
            //registerMiapi(moduleProperties, BetterCombatProperty.KEY, new BetterCombatProperty());
            BetterCombatHelper.setup();
            //registerMiapi(moduleProperties, ApoliPowersProperty.KEY, new ApoliPowersProperty());
            //registerMiapi(moduleProperties, TreechopProperty.KEY, new TreechopProperty());

            // CRAFTING STATS
            //registerMiapi(craftingStats, "hammering", new SimpleCraftingStat(0), stat -> exampleCraftingStat = stat);

            // ABILITIES
            registerMiapi(useAbilityRegistry, "throw", new ThrowingAbility());
            registerMiapi(useAbilityRegistry, "sword_block", new BlockAbility());
            registerMiapi(useAbilityRegistry, "block", new BlockAbility());
            registerMiapi(useAbilityRegistry, "tower_block", new TowerShieldBlock());
            registerMiapi(useAbilityRegistry, CopyItemAbility.KEY, new CopyItemAbility());
            registerMiapi(useAbilityRegistry, RiptideAbility.KEY, new RiptideAbility());
            registerMiapi(useAbilityRegistry, "heavy_attack", new SpecialAttackAbility());
            registerMiapi(useAbilityRegistry, AxeAbility.KEY, new AxeAbility());
            registerMiapi(useAbilityRegistry, HoeAbility.KEY, new HoeAbility());
            registerMiapi(useAbilityRegistry, ShovelAbility.KEY, new ShovelAbility());
            registerMiapi(useAbilityRegistry, EatAbility.KEY, new EatAbility());
            registerMiapi(useAbilityRegistry, AreaHarvestReplant.KEY, new AreaHarvestReplant());
            registerMiapi(useAbilityRegistry, CastLightingAbility.KEY, new CastLightingAbility());
            registerMiapi(useAbilityRegistry, SonicBoomAbility.KEY, new SonicBoomAbility());
            registerMiapi(useAbilityRegistry, CommandExecuteAbility.KEY, new CommandExecuteAbility());

            registerMiapi(MODULE_PROPERTY_MIAPI_REGISTRY, GunContextProperty.KEY, new GunContextProperty());

            registerMiapi(useAbilityRegistry, "full_block", new ShieldBlockAbility());
            registerMiapi(useAbilityRegistry, ParryBlock.KEY, new ParryBlock());

            smartin.miapi.registries.AttributeRegistry.registerAttributes();

            Miapi.LOGGER.info("Registered Truly Modulars Property resolvers:");
            PropertyResolver.registry.forEach((pair) -> {
                Miapi.LOGGER.info("registered resolver: " + pair.getA());
            });
        });
    }

}
