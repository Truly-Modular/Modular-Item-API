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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.blocks.ModularWorkBench;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.effects.StunResistanceStatusEffect;
import smartin.miapi.effects.StunStatusEffect;
import smartin.miapi.effects.TeleportBlockEffect;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.craft.BlueprintComponent;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.item.modular.ModularItemPart;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.items.*;
import smartin.miapi.item.modular.items.armor.*;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.*;
import smartin.miapi.modules.abilities.toolabilities.AxeAbility;
import smartin.miapi.modules.abilities.toolabilities.HoeAbility;
import smartin.miapi.modules.abilities.toolabilities.ShovelAbility;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.conditions.*;
import smartin.miapi.modules.edit_options.CosmeticEditOption;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.PropertyInjectionDev;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.material.*;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.armor.*;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.attributes.AttributeSplitProperty;
import smartin.miapi.modules.properties.enchanment.AllowedEnchantments;
import smartin.miapi.modules.properties.enchanment.CraftingEnchantProperty;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.enchanment.FakeEnchantmentProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;
import smartin.miapi.modules.properties.onHit.*;
import smartin.miapi.modules.properties.onHit.entity.AquaticDamage;
import smartin.miapi.modules.properties.onHit.entity.IllagerBane;
import smartin.miapi.modules.properties.onHit.entity.SmiteDamage;
import smartin.miapi.modules.properties.onHit.entity.SpiderDamage;
import smartin.miapi.modules.properties.potion.OnDamagedEffects;
import smartin.miapi.modules.properties.potion.OnHitTargetEffects;
import smartin.miapi.modules.properties.potion.OnKillEffects;
import smartin.miapi.modules.properties.projectile.*;
import smartin.miapi.modules.properties.render.*;
import smartin.miapi.modules.properties.slot.AllowedSlots;
import smartin.miapi.modules.properties.slot.CanChangeParentModule;
import smartin.miapi.modules.properties.slot.CanChildBeEmpty;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.synergies.SynergyManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.modules.abilities.util.ItemAbilityManager.useAbilityRegistry;

public class RegistryInventory {
    public static final Supplier<RegistrarManager> registrar = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final MiapiRegistrar<Item> modularItems = MiapiRegistrar.of(registrar.get().get(Registries.ITEM));
    public static final Registrar<Item> items = registrar.get().get(Registries.ITEM);
    public static final Registrar<DataComponentType<?>> components = registrar.get().get(Registries.DATA_COMPONENT_TYPE);
    public static final Registrar<Block> blocks = registrar.get().get(Registries.BLOCK);
    public static final Registrar<BlockEntityType<?>> blockEntities = registrar.get().get(Registries.BLOCK_ENTITY_TYPE);
    //TODO:make entity attached attributes work again
    public static final Registrar<Attribute> attributes = registrar.get().get(Registries.ATTRIBUTE);
    public static final Registrar<ArmorMaterial> armorMaterials = registrar.get().get(Registries.ARMOR_MATERIAL);
    public static final Registrar<EntityType<?>> entityTypes = registrar.get().get(Registries.ENTITY_TYPE);
    public static final Registrar<MenuType<?>> screenHandlers = registrar.get().get(Registries.MENU);
    public static final Registrar<MobEffect> statusEffects = registrar.get().get(Registries.MOB_EFFECT);
    public static final Registrar<CreativeModeTab> tab = registrar.get().get(Registries.CREATIVE_MODE_TAB);
    public static final Registrar<GameEvent> gameEvents = registrar.get().get(Registries.GAME_EVENT);
    public static final Registrar<RecipeSerializer<?>> recipeSerializers = registrar.get().get(Registries.RECIPE_SERIALIZER);
    public static final MiapiRegistry<ModuleProperty> moduleProperties = MiapiRegistry.getInstance(ModuleProperty.class);
    public static final MiapiRegistry<ItemModule> modules = MiapiRegistry.getInstance(ItemModule.class);
    public static final MiapiRegistry<EditOption> editOptions = MiapiRegistry.getInstance(EditOption.class);
    public static final MiapiRegistry<CraftingStat> craftingStats = MiapiRegistry.getInstance(CraftingStat.class);
    public static final TagKey<Item> MIAPI_FORBIDDEN_TAG = TagKey.create(Registries.ITEM, ResourceLocation.parse("miapi_forbidden"));

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
        String stringId = rl.toString();

        RegistrySupplier<Attribute> obj = attributes.register(rl, sup); // actually register the object
        obj.listen((attribute -> {
            onRegister.accept(obj.getRegistrar().getHolder(obj.getKey()));
        })); // attach the onRegister callback, usually used to set the value of fields.

        if (attach) // if it should automatically attach to an entity, add another listener to do that (this is equivalent to the old registerOnEntity)
            obj.listen(att -> AttributeRegistry.entityAttributeMap.put(stringId, att));
    }

    public static Block modularWorkBench;
    //public static Block exampleStatProviderBlock;
    public static BlockEntityType<ModularWorkBenchEntity> modularWorkBenchEntityType;
    public static Item modularItem;
    public static Item visualOnlymodularItem;
    public static Item modularAxe;
    public static Item modularMattock;
    public static Holder<MobEffect> cryoStatusEffect;
    public static Holder<MobEffect> teleportBlockEffect;
    public static Holder<MobEffect> stunEffect;
    public static Holder<MobEffect> stunResistanceEffect;
    public static GameEvent statProviderCreatedEvent;
    public static GameEvent statProviderRemovedEvent;
    public static Holder<ArmorMaterial> armorMaterial;
    //public static SimpleCraftingStat exampleCraftingStat;
    public static RecipeSerializer serializer;
    public static RegistrySupplier<EntityType<ItemProjectileEntity>> itemProjectileType = (RegistrySupplier) registerAndSupply(entityTypes, "thrown_item", () ->
            EntityType.Builder.of(ItemProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build("miapi:thrown_item"));
    public static EntityType<ItemProjectileEntity> registeredItemProjectileType;

    static {
        itemProjectileType.listen(e -> {
            registeredItemProjectileType = e;
            if (Platform.getEnvironment() == Env.CLIENT)
                MiapiClient.registerEntityRenderer();
        });
    }

    public static MenuType<CraftingScreenHandler> craftingScreenHandler;

    public static void setup() {
        //SCREEN
        register(screenHandlers, "default_crafting", () ->
                        new MenuType<>(CraftingScreenHandler::new, FeatureFlagSet.of()),
                scr -> {
                    craftingScreenHandler = scr;
                    if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerScreenHandler();
                });

        RegistryInventory.components.register(
                Miapi.id("item_module"), () -> ModuleInstance.MODULE_INSTANCE_COMPONENT);
        RegistryInventory.components.register(
                Miapi.id("module_blueprint"), () -> BlueprintComponent.BLUEPRINT_COMPONENT);
        RegistryInventory.components.register(
                Miapi.id("nemesis_property"), () -> NemesisProperty.NEMESIS_COMPONENT);
        RegistryInventory.components.register(
                Miapi.id("magazine_property"), () -> RapidfireCrossbowProperty.ADDITIONAL_PROJECTILES_COMPONENT);
        RegistryInventory.components.register(
                Miapi.id("item_module_property"), () -> ItemModelProperty.ITEM_MODEL_COMPONENT);

        register(armorMaterials, "modular_armor_material", () ->
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
                        new ArrayList<>(),
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
        register(recipeSerializers, "smithing", MaterialSmithingRecipe.Serializer::new, i -> serializer = i);


        //BLOCK
        register(blocks, "modular_work_bench", () -> new ModularWorkBench(
                BlockBehaviour.Properties.of().
                        mapColor(MapColor.METAL).
                        instrument(NoteBlockInstrument.IRON_XYLOPHONE).
                        requiresCorrectToolForDrops().
                        strength(2.5F, 6.0F).
                        sound(SoundType.METAL).
                        noOcclusion().
                        pushReaction(PushReaction.IGNORE)), b -> modularWorkBench = b);
        register(blockEntities, "modular_work_bench", () -> BlockEntityType.Builder.of(
                ModularWorkBenchEntity::new, modularWorkBench
        ).build(null), be -> {
            modularWorkBenchEntityType = be;
            if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerBlockEntityRenderer();
        });
        register(items, "modular_work_bench", () -> new BlockItem(modularWorkBench, new Item.Properties()));


//        registerMiapi(craftingStats, "hammering", new SimpleCraftingStat(0), stat -> exampleCraftingStat = stat);
//        register(blocks, "example_stat_provider", () ->
//                new StatProvidingBlock(AbstractBlock.Settings.create(), new StatProvidersMap().set(exampleCraftingStat, StatActorType.ADD, 2d)), b -> exampleStatProviderBlock = b);
//        register(items, "example_stat_provider", () -> new BlockItem(exampleStatProviderBlock, new Item.Settings()));


        // CREATIVE TAB
        register(tab, "miapi_tab", () -> CreativeTabRegistry.create
                (b -> {
                    b.title(Component.translatable("miapi.tab.name"));
                    b.icon(() -> new ItemStack(modularWorkBench));
                    b.displayItems((displayContext, entries) -> {
                        entries.accept(modularWorkBench);
                    });
                }));

        //ITEM
        register(modularItems, "modular_broken_item", ModularVisualOnlyItem::new, i -> visualOnlymodularItem = i);
        register(modularItems, "modular_part_visual", ModularVisualOnlyItem::new, i -> visualOnlymodularItem = i);

        register(modularItems, "modular_item", ExampleModularItem::new, i -> modularItem = i);
        register(modularItems, "modular_part", ModularItemPart::new);

        register(modularItems, "modular_handheld", ModularWeapon::new);
        register(modularItems, "modular_katars", ModularSword::new);
        register(modularItems, "modular_gauntlets", ModularWeapon::new);
        register(modularItems, "modular_knuckles", ModularWeapon::new);
        register(modularItems, "modular_tonfa", ModularWeapon::new);

        register(modularItems, "modular_handle", ModularWeapon::new);
        register(modularItems, "modular_sword", ModularSword::new);
        register(modularItems, "twin_blade", ModularSword::new);
        register(modularItems, "modular_katana", ModularSword::new);
        register(modularItems, "modular_naginata", ModularSword::new);
        register(modularItems, "modular_greatsword", ModularSword::new);
        register(modularItems, "modular_dagger", ModularSword::new);
        register(modularItems, "modular_spear", ModularSword::new);
        register(modularItems, "modular_throwing_knife", ModularSword::new);
        register(modularItems, "modular_rapier", ModularSword::new);
        register(modularItems, "modular_longsword", ModularSword::new);
        register(modularItems, "modular_trident", ModularSword::new);
        register(modularItems, "modular_scythe", ModularSword::new);
        register(modularItems, "modular_sickle", ModularSword::new);

        register(modularItems, "modular_shovel", ModularShovel::new);
        register(modularItems, "modular_pickaxe", ModularPickaxe::new);
        register(modularItems, "modular_hammer", ModularPickaxe::new);
        register(modularItems, "modular_axe", ModularAxe::new, i -> modularAxe = i);
        register(modularItems, "modular_hoe", ModularHoe::new);
        register(modularItems, "modular_mattock", ModularAxe::new, i -> modularMattock = i);

        register(modularItems, "modular_bow", ModularBow::new);
        register(modularItems, "modular_small_bow", ModularBow::new);
        register(modularItems, "modular_large_bow", ModularBow::new);
        register(modularItems, "modular_bow_part", ExampleModularItem::new);
        register(modularItems, "modular_crossbow", ModularCrossbow::new);
        register(modularItems, "modular_small_crossbow", ModularCrossbow::new);
        register(modularItems, "modular_large_crossbow", ModularCrossbow::new);
        register(modularItems, "modular_crossbow_part", ExampleModularItem::new);
        register(modularItems, "modular_arrow", ModularArrow::new);
        register(modularItems, "modular_arrow_part", ExampleModularStrackableItem::new);

        register(modularItems, "modular_helmet", ModularHelmet::new);
        register(modularItems, "modular_chestplate", ModularChestPlate::new);
        register(modularItems, "modular_leggings", ModularLeggings::new);
        register(modularItems, "modular_boots", ModularBoots::new);

        register(modularItems, "modular_elytra", ModularElytraItem::getInstance);

        //STATUS EFFECTS
        register(statusEffects, "cryo", CryoStatusEffect::new, eff -> {
            cryoStatusEffect = statusEffects.getHolder(statusEffects.getId(eff));
        });
        register(statusEffects, "teleport_block", TeleportBlockEffect::new, eff -> {
            teleportBlockEffect = statusEffects.getHolder(statusEffects.getId(eff));
        });
        register(statusEffects, "stun", StunStatusEffect::new, eff -> {
            stunEffect = statusEffects.getHolder(statusEffects.getId(eff));
        });
        register(statusEffects, "stun_resistance", StunResistanceStatusEffect::new, eff -> {
            stunResistanceEffect = statusEffects.getHolder(statusEffects.getId(eff));
        });

        smartin.miapi.registries.AttributeRegistry.registerAttributes();


        // GAME EVENTS
        register(gameEvents, "stat_provider_added", () -> new GameEvent(16), ev -> statProviderCreatedEvent = ev);
        register(gameEvents, "stat_provider_removed", () -> new GameEvent(16), ev -> statProviderRemovedEvent = ev);


        LifecycleEvent.SETUP.register(() -> {
            //EDITPROPERTIES
            registerMiapi(editOptions, "replace", new ReplaceOption());
            registerMiapi(editOptions, "dev", new PropertyInjectionDev());
            registerMiapi(editOptions, "skin", new SkinOptions());
            registerMiapi(editOptions, "create", new CreateItemOption());
            registerMiapi(editOptions, "cosmetic", new CosmeticEditOption());
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
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("item_in_inventory"), ItemInInventoryCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("advancement"), AdvancementCondition.CODEC);
            ConditionManager.CONDITION_REGISTRY.put(Miapi.id("number"), NumberCondition.CODEC);

            //MODULEPROPERTIES
            if (smartin.miapi.Environment.isClient()) {
                registerMiapi(moduleProperties, ModelProperty.KEY, new ModelProperty());
                registerMiapi(moduleProperties, ModelTransformationProperty.KEY, new ModelTransformationProperty());
                registerMiapi(moduleProperties, GuiOffsetProperty.KEY, new GuiOffsetProperty());
                registerMiapi(moduleProperties, ItemModelProperty.KEY, new ItemModelProperty());
                registerMiapi(moduleProperties, BannerModelProperty.KEY, new BannerModelProperty());
                registerMiapi(moduleProperties, BlockModelProperty.KEY, new BlockModelProperty());
                registerMiapi(moduleProperties, EntityModelProperty.KEY, new EntityModelProperty());
                registerMiapi(moduleProperties, CrystalModelProperty.KEY, new CrystalModelProperty());
                registerMiapi(moduleProperties, ConduitModelProperty.KEY, new ConduitModelProperty());
                registerMiapi(moduleProperties, OverlayModelProperty.KEY, new OverlayModelProperty());
            } else {
                registerMiapi(moduleProperties, "texture", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "modelTransform", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "modelMerge", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "guiOffset", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "item_model", new ServerReplaceProperty());
                //registerMiapi(moduleProperties, "itemLore", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "banner", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "crystal_model", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "block_model", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "entity_model", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "conduit_model", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "overlay_texture_model", new ServerReplaceProperty());
            }
            registerMiapi(moduleProperties, CanChildBeEmpty.KEY, new CanChildBeEmpty());
            registerMiapi(moduleProperties, LoreProperty.KEY, new LoreProperty());
            registerMiapi(moduleProperties, OldNameProperty.KEY, new OldNameProperty());
            registerMiapi(moduleProperties, SlotProperty.KEY, new SlotProperty());
            registerMiapi(moduleProperties, AllowedSlots.KEY, new AllowedSlots());
            registerMiapi(moduleProperties, MaterialProperty.KEY, new MaterialProperty());
            registerMiapi(moduleProperties, AllowedMaterial.KEY, new AllowedMaterial());
            registerMiapi(moduleProperties, AttributeProperty.KEY, new AttributeProperty());
            //registerMiapi(moduleProperties, ParticleShapingProperty.KEY, new ParticleShapingProperty());
            registerMiapi(moduleProperties, DisplayNameProperty.KEY, new DisplayNameProperty());
            registerMiapi(moduleProperties, ItemIdProperty.KEY, new ItemIdProperty());
            registerMiapi(moduleProperties, EquipmentSlotProperty.KEY, new EquipmentSlotProperty());
            registerMiapi(moduleProperties, BlockProperty.KEY, new BlockProperty());
            registerMiapi(moduleProperties, HealthPercentDamage.KEY, new HealthPercentDamage());
            registerMiapi(moduleProperties, ArmorPenProperty.KEY, new ArmorPenProperty());
            registerMiapi(moduleProperties, ToolOrWeaponProperty.KEY, new ToolOrWeaponProperty());
            registerMiapi(moduleProperties, MiningLevelProperty.KEY, new MiningLevelProperty());
            registerMiapi(moduleProperties, TagProperty.KEY, new TagProperty());
            registerMiapi(moduleProperties, MaterialProperties.KEY, new MaterialProperties());
            registerMiapi(moduleProperties, CraftingConditionProperty.KEY, new CraftingConditionProperty());
            registerMiapi(moduleProperties, StatRequirementProperty.KEY, new StatRequirementProperty());
            registerMiapi(moduleProperties, GlintProperty.KEY, new GlintProperty());
            registerMiapi(moduleProperties, EnderpearlProperty.KEY, new EnderpearlProperty());
            registerMiapi(moduleProperties, TeleportTarget.KEY, new TeleportTarget());
            registerMiapi(moduleProperties, ExplosionProperty.KEY, new ExplosionProperty());
            registerMiapi(moduleProperties, ProjectileTriggerProperty.KEY, new ProjectileTriggerProperty());
            registerMiapi(moduleProperties, ChannelingProperty.KEY, new ChannelingProperty());
            registerMiapi(moduleProperties, AirDragProperty.KEY, new AirDragProperty());
            registerMiapi(moduleProperties, WaterDragProperty.KEY, new WaterDragProperty());
            registerMiapi(moduleProperties, ArrowProperty.KEY, new ArrowProperty());
            registerMiapi(moduleProperties, DurabilityProperty.KEY, new DurabilityProperty());
            registerMiapi(moduleProperties, FracturingProperty.KEY, new FracturingProperty());
            registerMiapi(moduleProperties, IsPiglinGold.KEY, new IsPiglinGold());
            registerMiapi(moduleProperties, CanWalkOnSnow.KEY, new CanWalkOnSnow());
            registerMiapi(moduleProperties, FireProof.KEY, new FireProof());
            registerMiapi(moduleProperties, RepairPriority.KEY, new RepairPriority());
            registerMiapi(moduleProperties, PriorityProperty.KEY, new PriorityProperty());
            registerMiapi(moduleProperties, ImmolateProperty.KEY, new ImmolateProperty());
            registerMiapi(moduleProperties, LeechingProperty.KEY, new LeechingProperty());
            registerMiapi(moduleProperties, IsCrossbowShootAble.KEY, new IsCrossbowShootAble());
            registerMiapi(moduleProperties, CryoProperty.KEY, new CryoProperty());
            registerMiapi(moduleProperties, AquaticDamage.KEY, new AquaticDamage());
            registerMiapi(moduleProperties, SpiderDamage.KEY, new SpiderDamage());
            registerMiapi(moduleProperties, SmiteDamage.KEY, new SmiteDamage());
            registerMiapi(moduleProperties, IllagerBane.KEY, new IllagerBane());
            registerMiapi(moduleProperties, PillagesGuard.KEY, new PillagesGuard());
            registerMiapi(moduleProperties, LuminousLearningProperty.KEY, new LuminousLearningProperty());
            registerMiapi(moduleProperties, WaterGravityProperty.KEY, new WaterGravityProperty());
            registerMiapi(moduleProperties, CraftingEnchantProperty.KEY, new CraftingEnchantProperty());
            registerMiapi(moduleProperties, ExhaustionProperty.KEY, new ExhaustionProperty());
            registerMiapi(moduleProperties, MaterialInscribeDataProperty.KEY, new MaterialInscribeDataProperty());
            registerMiapi(moduleProperties, FakeItemTagProperty.KEY, new FakeItemTagProperty());
            registerMiapi(moduleProperties, RarityProperty.KEY, new RarityProperty());
            registerMiapi(moduleProperties, MiningShapeProperty.KEY, new MiningShapeProperty());
            registerMiapi(moduleProperties, ModuleStats.KEY, new ModuleStats());
            registerMiapi(moduleProperties, EnchantAbilityProperty.KEY, new EnchantAbilityProperty());
            registerMiapi(moduleProperties, StepCancelingProperty.KEY, new StepCancelingProperty());
            registerMiapi(moduleProperties, LightningOnHit.KEY, new LightningOnHit());
            registerMiapi(moduleProperties, GuiStatProperty.KEY, new GuiStatProperty());
            registerMiapi(moduleProperties, AbilityMangerProperty.KEY, new AbilityMangerProperty());
            registerMiapi(moduleProperties, OnHitTargetEffects.KEY, new OnHitTargetEffects());
            registerMiapi(moduleProperties, OnDamagedEffects.KEY, new OnDamagedEffects());
            registerMiapi(moduleProperties, OnKillEffects.KEY, new OnKillEffects());
            registerMiapi(moduleProperties, OnKillExplosion.KEY, new OnKillExplosion());
            registerMiapi(moduleProperties, CanChangeParentModule.KEY, new CanChangeParentModule());
            registerMiapi(moduleProperties, NemesisProperty.KEY, new NemesisProperty());
            registerMiapi(moduleProperties, CopyParentMaterialProperty.KEY, new CopyParentMaterialProperty());
            registerMiapi(moduleProperties, EmissivityProperty.KEY, new EmissivityProperty());
            registerMiapi(moduleProperties, RapidfireCrossbowProperty.KEY, new RapidfireCrossbowProperty());
            registerMiapi(moduleProperties, MagazineCrossbowShotDelay.KEY, new MagazineCrossbowShotDelay());
            registerMiapi(moduleProperties, HandheldItemProperty.KEY, new HandheldItemProperty());
            registerMiapi(moduleProperties, AttributeSplitProperty.KEY, new AttributeSplitProperty());
            registerMiapi(moduleProperties, FakeEnchantmentProperty.KEY, new FakeEnchantmentProperty());
            registerMiapi(moduleProperties, AllowedEnchantments.KEY, new AllowedEnchantments());
            registerMiapi(moduleProperties, BlueprintCrafting.KEY, new BlueprintCrafting());
            //compat
            //registerMiapi(moduleProperties, BetterCombatProperty.KEY, new BetterCombatProperty());
            //TODO: added this to cleanup logs. this needs to be revisited later
            registerMiapi(moduleProperties, Miapi.id("better_combat_config"), new ServerReplaceProperty());
            //registerMiapi(moduleProperties, ApoliPowersProperty.KEY, new ApoliPowersProperty());
            //registerMiapi(moduleProperties, TreechopProperty.KEY, new TreechopProperty());

            // CRAFTING STATS
            //registerMiapi(craftingStats, "hammering", new SimpleCraftingStat(0), stat -> exampleCraftingStat = stat);

            // ABILITIES
            registerMiapi(useAbilityRegistry, "throw", new ThrowingAbility());
            registerMiapi(useAbilityRegistry, "block", new BlockAbility());
            registerMiapi(useAbilityRegistry, "full_block", new ShieldBlockAbility());
            registerMiapi(useAbilityRegistry, "riptide", new RiptideAbility());
            registerMiapi(useAbilityRegistry, "heavy_attack", new SpecialAttackAbility());
            registerMiapi(useAbilityRegistry, AxeAbility.KEY, new AxeAbility());
            registerMiapi(useAbilityRegistry, HoeAbility.KEY, new HoeAbility());
            registerMiapi(useAbilityRegistry, ShovelAbility.KEY, new ShovelAbility());
            registerMiapi(useAbilityRegistry, EatAbility.KEY, new EatAbility());
            registerMiapi(useAbilityRegistry, AreaHarvestReplant.KEY, new AreaHarvestReplant());

            Miapi.LOGGER.info("Registered Truly Modulars Property resolvers:");
            PropertyResolver.registry.forEach((pair) -> {
                Miapi.LOGGER.info("registered resolver: " + pair.getA());
            });
        });
    }


}
