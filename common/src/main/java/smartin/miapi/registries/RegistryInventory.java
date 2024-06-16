package smartin.miapi.registries;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.event.GameEvent;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.blocks.ModularWorkBench;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.blueprint.BlueprintProperty;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialAtlasManager;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.effects.StunResistanceStatusEffect;
import smartin.miapi.effects.StunStatusEffect;
import smartin.miapi.effects.TeleportBlockEffect;
import smartin.miapi.entity.BoomerangItemProjectileEntity;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.item.modular.ModularItemPart;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.item.modular.items.*;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.*;
import smartin.miapi.modules.abilities.toolabilities.AxeAbility;
import smartin.miapi.modules.abilities.toolabilities.HoeAbility;
import smartin.miapi.modules.abilities.toolabilities.ShovelAbility;
import smartin.miapi.modules.conditions.*;
import smartin.miapi.modules.edit_options.CosmeticEditOption;
import smartin.miapi.modules.edit_options.CreateItemOption.CreateItemOption;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.PropertyInjectionDev;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.material.*;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.compat.apoli.ApoliPowersProperty;
import smartin.miapi.modules.properties.compat.better_combat.BetterCombatProperty;
import smartin.miapi.modules.properties.compat.ht_treechop.TreechopProperty;
import smartin.miapi.modules.properties.damage_boosts.AquaticDamage;
import smartin.miapi.modules.properties.damage_boosts.IllagerBane;
import smartin.miapi.modules.properties.damage_boosts.SmiteDamage;
import smartin.miapi.modules.properties.damage_boosts.SpiderDamage;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;
import smartin.miapi.modules.properties.potion.OnDamagedEffects;
import smartin.miapi.modules.properties.potion.OnHitTargetEffects;
import smartin.miapi.modules.properties.potion.OnKillEffects;
import smartin.miapi.modules.properties.render.*;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.synergies.SynergyManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.client.render.RenderPhase.*;
import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.attributes.AttributeRegistry.*;
import static smartin.miapi.modules.ItemModule.componentType;
import static smartin.miapi.modules.abilities.util.ItemAbilityManager.useAbilityRegistry;
import static smartin.miapi.modules.conditions.ConditionManager.moduleConditionRegistry;

public class RegistryInventory {
    public static final Supplier<RegistrarManager> registrar = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final MiapiRegistrar<Item> modularItems = MiapiRegistrar.of(registrar.get().get(RegistryKeys.ITEM));
    public static final Registrar<Item> items = registrar.get().get(RegistryKeys.ITEM);
    public static final Registrar<ComponentType<?>> components = registrar.get().get(RegistryKeys.DATA_COMPONENT_TYPE);
    public static final Registrar<Block> blocks = registrar.get().get(RegistryKeys.BLOCK);
    public static final Registrar<BlockEntityType<?>> blockEntities = registrar.get().get(RegistryKeys.BLOCK_ENTITY_TYPE);
    public static final Registrar<EntityAttribute> attributes = registrar.get().get(RegistryKeys.ATTRIBUTE);
    public static final Registrar<EntityType<?>> entityTypes = registrar.get().get(RegistryKeys.ENTITY_TYPE);
    public static final Registrar<ScreenHandlerType<?>> screenHandlers = registrar.get().get(RegistryKeys.SCREEN_HANDLER);
    public static final Registrar<StatusEffect> statusEffects = registrar.get().get(RegistryKeys.STATUS_EFFECT);
    public static final Registrar<ItemGroup> tab = registrar.get().get(RegistryKeys.ITEM_GROUP);
    public static final Registrar<GameEvent> gameEvents = registrar.get().get(RegistryKeys.GAME_EVENT);
    public static final Registrar<RecipeSerializer<?>> recipeSerializers = registrar.get().get(RegistryKeys.RECIPE_SERIALIZER);
    public static final MiapiRegistry<ModuleProperty> moduleProperties = MiapiRegistry.getInstance(ModuleProperty.class);
    public static final MiapiRegistry<ItemModule> modules = MiapiRegistry.getInstance(ItemModule.class);
    public static final MiapiRegistry<EditOption> editOptions = MiapiRegistry.getInstance(EditOption.class);
    public static final MiapiRegistry<CraftingStat> craftingStats = MiapiRegistry.getInstance(CraftingStat.class);
    public static final TagKey<Item> MIAPI_FORBIDDEN_TAG = TagKey.of(RegistryKeys.ITEM, Identifier.of("miapi_forbidden"));

    public static <T> RegistrySupplier<T> registerAndSupply(Registrar<T> rg, Identifier id, Supplier<T> object) {
        return rg.register(id, object);
    }

    public static <T> RegistrySupplier<T> registerAndSupply(Registrar<T> rg, String id, Supplier<T> object) {
        return registerAndSupply(rg, Identifier.of(MOD_ID, id), object);
    }

    public static <T, E extends T> void register(Registrar<T> rg, Identifier id, Supplier<E> object, Consumer<E> onRegister) {
        rg.register(id, object).listen(onRegister);
    }

    public static <T, E extends T> void register(Registrar<T> rg, String id, Supplier<E> object, Consumer<E> onRegister) {
        register(rg, Identifier.of(MOD_ID, id), object, onRegister);
    }

    public static <T> void register(Registrar<T> rg, Identifier id, Supplier<T> object) {
        rg.register(id, object);
    }

    public static <T> void register(Registrar<T> rg, String id, Supplier<T> object) {
        register(rg, Identifier.of(MOD_ID, id), object);
    }

    public static <T> void registerMiapi(MiapiRegistry<T> rg, String id, T object) {
        rg.register(id, object);
    }

    public static <T, E extends T> void registerMiapi(MiapiRegistry<T> rg, String id, E object, Consumer<E> onRegister) {
        rg.register(id, object);
        onRegister.accept(object);
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
    public static void registerAtt(String id, boolean attach, Supplier<EntityAttribute> sup, Consumer<EntityAttribute> onRegister) {
        Identifier rl = Identifier.of(MOD_ID, id);
        String stringId = rl.toString();

        RegistrySupplier<EntityAttribute> obj = attributes.register(rl, sup); // actually register the object
        obj.listen(onRegister); // attach the onRegister callback, usually used to set the value of fields.

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
    public static StatusEffect cryoStatusEffect;
    public static StatusEffect teleportBlockEffect;
    public static StatusEffect stunEffect;
    public static StatusEffect stunResistanceEffect;
    public static GameEvent statProviderCreatedEvent;
    public static GameEvent statProviderRemovedEvent;
    //public static SimpleCraftingStat exampleCraftingStat;
    public static RecipeSerializer serializer;
    public static RegistrySupplier<EntityType<ItemProjectileEntity>> itemProjectileType = (RegistrySupplier) registerAndSupply(entityTypes, "thrown_item", () ->
            EntityType.Builder.create(ItemProjectileEntity::new, SpawnGroup.MISC).dimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_item"));
    public static RegistrySupplier<EntityType<BoomerangItemProjectileEntity>> itemBoomerangProjectileType = (RegistrySupplier) registerAndSupply(entityTypes, "thrown_boomerang_item", () ->
            EntityType.Builder.create(BoomerangItemProjectileEntity::new, SpawnGroup.MISC).dimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_boomerang_item"));

    static {
        itemProjectileType.listen(e -> {
            if (Platform.getEnvironment() == Env.CLIENT)
                MiapiClient.registerEntityRenderer();
        });
    }

    public static ScreenHandlerType<CraftingScreenHandler> craftingScreenHandler;

    public static void setup() {
        //SCREEN
        register(screenHandlers, "default_crafting", () ->
                        new ScreenHandlerType<>(CraftingScreenHandler::new, FeatureSet.empty()),
                scr -> {
                    craftingScreenHandler = (ScreenHandlerType<CraftingScreenHandler>) scr;
                    if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerScreenHandler();
                });

        RegistryInventory.components.register(
                Miapi.MiapiIdentifier("item_module"),() -> componentType);

        //ENTITY
        // commented out because RegistrySupplier is needed... see itemProjectileType field definition above
        /*register(entityTypes, "thrown_item", () ->
                EntityType.builder.create(ItemProjectile::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_item"),
                type -> itemProjectileType = (EntityType<ItemProjectile>) type);*/

        //RECIPE SERIALIZERS
        register(recipeSerializers, "smithing", MaterialSmithingRecipe.Serializer::new, i -> serializer = i);


        //BLOCK
        register(blocks, "modular_work_bench", () -> new ModularWorkBench(
                AbstractBlock.Settings.create().
                        mapColor(MapColor.IRON_GRAY).
                        instrument(NoteBlockInstrument.IRON_XYLOPHONE).
                        requiresTool().
                        strength(2.5F, 6.0F).
                        sounds(BlockSoundGroup.METAL).
                        nonOpaque().
                        pistonBehavior(PistonBehavior.IGNORE)), b -> modularWorkBench = b);
        register(blockEntities, "modular_work_bench", () -> BlockEntityType.Builder.create(
                ModularWorkBenchEntity::new, modularWorkBench
        ).build(null), be -> {
            modularWorkBenchEntityType = be;
            if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerBlockEntityRenderer();
        });
        register(items, "modular_work_bench", () -> new BlockItem(modularWorkBench, new Item.Settings()));


//        registerMiapi(craftingStats, "hammering", new SimpleCraftingStat(0), stat -> exampleCraftingStat = stat);
//        register(blocks, "example_stat_provider", () ->
//                new StatProvidingBlock(AbstractBlock.Settings.create(), new StatProvidersMap().set(exampleCraftingStat, StatActorType.ADD, 2d)), b -> exampleStatProviderBlock = b);
//        register(items, "example_stat_provider", () -> new BlockItem(exampleStatProviderBlock, new Item.Settings()));


        // CREATIVE TAB
        register(tab, "miapi_tab", () -> CreativeTabRegistry.create
                (b -> {
                    b.displayName(Text.translatable("miapi.tab.name"));
                    b.icon(() -> new ItemStack(modularWorkBench));
                    b.entries((displayContext, entries) -> {
                        entries.add(modularWorkBench);
                    });
                }));

        //ITEM
        register(modularItems, "modular_broken_item", ModularVisualOnlyItem::new, i -> visualOnlymodularItem = i);

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
        register(statusEffects, "cryo", CryoStatusEffect::new, eff -> cryoStatusEffect = eff);
        register(statusEffects, "teleport_block", TeleportBlockEffect::new, eff -> teleportBlockEffect = eff);
        register(statusEffects, "stun", StunStatusEffect::new, eff -> stunEffect = eff);
        register(statusEffects, "stun_resistance", StunResistanceStatusEffect::new, eff -> stunResistanceEffect = eff);

        //ATTRIBUTE

        // mining
        registerAtt("remove.mining_speed.pickaxe", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.mining_speed.pickaxe", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_PICKAXE = att);
        registerAtt("remove.mining_speed.axe", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.mining_speed.axe", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_AXE = att);
        registerAtt("remove.mining_speed.shovel", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.mining_speed.shovel", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_SHOVEL = att);
        registerAtt("remove.mining_speed.hoe", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.mining_speed.hoe", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_HOE = att);

        // entity attached
        registerAtt("generic.resistance", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.resistance", 0.0, 0.0, 100).setTracked(true),
                att -> DAMAGE_RESISTANCE = att);
        registerAtt("generic.back_stab", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.back_stab", 0.0, 0.0, 1024.0).setTracked(true),
                att -> BACK_STAB = att);
        registerAtt("generic.armor_crushing", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.armor_crushing", 0.0, 0.0, 1024.0).setTracked(true),
                att -> ARMOR_CRUSHING = att);
        registerAtt("generic.projectile_armor", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.projectile_armor", 0.0, 0.0, 1024.0).setTracked(true),
                att -> PROJECTILE_ARMOR = att);
        registerAtt("generic.shield_break", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.shield_break", 0.0, 0.0, 1024.0).setTracked(true),
                att -> SHIELD_BREAK = att);

        registerAtt("generic.player_item_use_speed", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.player_item_use_speed", -0.8, -1.0, 0.0).setTracked(true),
                att -> PLAYER_ITEM_USE_MOVEMENT_SPEED = att);

        registerAtt("generic.magic_damage", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.magic_damage", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MAGIC_DAMAGE = att);

        registerAtt("generic.stun_damage", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.stun_damage", 0.0, 0.0, 1024.0).setTracked(true),
                att -> STUN_DAMAGE = att);

        registerAtt("generic.stun_max_health", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.stun_max_health", MiapiConfig.INSTANCE.server.stunEffectCategory.stunHealth, 0.0, 1024.0).setTracked(true),
                att -> STUN_MAX_HEALTH = att);

        registerAtt("generic.crit_damage", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.crit_damage", 0.0, 0.0, 1024.0).setTracked(true),
                att -> CRITICAL_DAMAGE = att);

        registerAtt("generic.crit_chance", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.crit_chance", 0.0, 0.0, 1024.0).setTracked(true),
                att -> CRITICAL_CHANCE = att);

        //projectile based
        registerAtt("generic.bow_draw_time", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.bow_draw_time", 0.0, -1024.0, 1024.0).setTracked(true),
                att -> BOW_DRAW_TIME = att);
        registerAtt("generic.projectile_damage", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.projectile_damage", 0.0, -1024.0, 1024.0).setTracked(true),
                att -> PROJECTILE_DAMAGE = att);
        registerAtt("generic.projectile_speed", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.projectile_speed", 0.0, -1024.0, 1024.0).setTracked(true),
                att -> PROJECTILE_SPEED = att);
        registerAtt("generic.projectile_accuracy", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.projectile_accuracy", 0.0, -1024.0, 1024.0).setTracked(true),
                att -> PROJECTILE_ACCURACY = att);
        registerAtt("generic.projectile_piercing", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.projectile_piercing", 0.0, 0.0, 1024.0).setTracked(true),
                att -> PROJECTILE_PIERCING = att);
        registerAtt("generic.projectile_crit_multiplier", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.projectile_crit_multiplier", 1.5, 0.0, 1024.0).setTracked(true),
                att -> PROJECTILE_CRIT_MULTIPLIER = att);

        registerAtt("generic.elytra_turn_efficiency", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.elytra_turn_efficiency", 0.0, -1024.0, 100.0).setTracked(true),
                att -> ELYTRA_TURN_EFFICIENCY = att);
        registerAtt("generic.elytra_glide_efficiency", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.elytra_glide_efficiency", 0.0, -1024.0, 100.0).setTracked(true),
                att -> ELYTRA_GLIDE_EFFICIENCY = att);
        registerAtt("generic.elytra_rocket_efficiency", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.elytra_rocket_efficiency", 1.0, 0.0, 1024.0).setTracked(true),
                att -> ELYTRA_ROCKET_EFFICIENCY = att);
        registerAtt("generic.shielding_armor", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.shielding_armor", 0.0, 0.0, 1024.0).setTracked(true),
                att -> SHIELDING_ARMOR = att);


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
            registerMiapi(moduleConditionRegistry, "true", new TrueCondition());
            registerMiapi(moduleConditionRegistry, "not", new NotCondition());
            registerMiapi(moduleConditionRegistry, "or", new OrCondition());
            registerMiapi(moduleConditionRegistry, "and", new AndCondition());
            registerMiapi(moduleConditionRegistry, "child", new ChildCondition());
            registerMiapi(moduleConditionRegistry, "parent", new ParentCondition());
            registerMiapi(moduleConditionRegistry, "otherModule", new OtherModuleModuleCondition());
            registerMiapi(moduleConditionRegistry, "module", new ModuleTypeCondition());
            registerMiapi(moduleConditionRegistry, "material", new MaterialCondition());
            registerMiapi(moduleConditionRegistry, "material_count", new MaterialCountCondition());
            registerMiapi(moduleConditionRegistry, "tag", new TagCondition());
            registerMiapi(moduleConditionRegistry, "miapi_perm", new MiapiPerm());
            registerMiapi(moduleConditionRegistry, "item_in_inventory", new ItemInInventoryCondition());
            registerMiapi(moduleConditionRegistry, "mod_loaded", new IsModLoadedCondition());
            registerMiapi(moduleConditionRegistry, "advancement", new AdvancementCondition());

            //MODULEPROPERTIES
            if (smartin.miapi.Environment.isClient()) {
                registerMiapi(moduleProperties, ModelProperty.KEY, new ModelProperty());
                registerMiapi(moduleProperties, ModelTransformationProperty.KEY, new ModelTransformationProperty());
                registerMiapi(moduleProperties, ModelMergeProperty.KEY, new ModelMergeProperty());
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
            registerMiapi(moduleProperties, NameProperty.KEY, new NameProperty());
            registerMiapi(moduleProperties, SlotProperty.KEY, new SlotProperty());
            registerMiapi(moduleProperties, AllowedSlots.KEY, new AllowedSlots());
            registerMiapi(moduleProperties, MaterialProperty.KEY, new MaterialProperty());
            registerMiapi(moduleProperties, AllowedMaterial.KEY, new AllowedMaterial());
            registerMiapi(moduleProperties, AttributeProperty.KEY, new AttributeProperty());
            //registerMiapi(moduleProperties, ParticleShapingProperty.KEY, new ParticleShapingProperty());
            registerMiapi(moduleProperties, DisplayNameProperty.KEY, new DisplayNameProperty());
            registerMiapi(moduleProperties, ItemIdProperty.KEY, new ItemIdProperty());
            registerMiapi(moduleProperties, EquipmentSlotProperty.KEY, new EquipmentSlotProperty());
            registerMiapi(moduleProperties, FlexibilityProperty.KEY, new FlexibilityProperty());
            registerMiapi(moduleProperties, AbilityProperty.KEY, new AbilityProperty());
            registerMiapi(moduleProperties, BlockProperty.KEY, new BlockProperty());
            registerMiapi(moduleProperties, RiptideProperty.KEY, new RiptideProperty());
            registerMiapi(moduleProperties, HealthPercentDamage.KEY, new HealthPercentDamage());
            registerMiapi(moduleProperties, ArmorPenProperty.KEY, new ArmorPenProperty());
            registerMiapi(moduleProperties, HeavyAttackProperty.KEY, new HeavyAttackProperty());
            registerMiapi(moduleProperties, CircleAttackProperty.KEY, new CircleAttackProperty());
            registerMiapi(moduleProperties, CrossbowProperty.KEY, new CrossbowProperty());
            registerMiapi(moduleProperties, ToolOrWeaponProperty.KEY, new ToolOrWeaponProperty());
            registerMiapi(moduleProperties, MiningLevelProperty.KEY, new MiningLevelProperty());
            registerMiapi(moduleProperties, TagProperty.KEY, new TagProperty());
            registerMiapi(moduleProperties, EnchantmentProperty.KEY, new EnchantmentProperty());
            registerMiapi(moduleProperties, MaterialProperties.KEY, new MaterialProperties());
            registerMiapi(moduleProperties, CraftingConditionProperty.KEY, new CraftingConditionProperty());
            registerMiapi(moduleProperties, StatRequirementProperty.KEY, new StatRequirementProperty());
            registerMiapi(moduleProperties, StatProvisionProperty.KEY, new StatProvisionProperty());
            registerMiapi(moduleProperties, GlintProperty.KEY, new GlintProperty());
            registerMiapi(moduleProperties, EnderpearlProperty.KEY, new EnderpearlProperty());
            registerMiapi(moduleProperties, TeleportTarget.KEY, new TeleportTarget());
            registerMiapi(moduleProperties, ExplosionProperty.KEY, new ExplosionProperty());
            registerMiapi(moduleProperties, MaterialInscribeProperty.KEY, new MaterialInscribeProperty());
            registerMiapi(moduleProperties, ProjectileTriggerProperty.KEY, new ProjectileTriggerProperty());
            registerMiapi(moduleProperties, ChannelingProperty.KEY, new ChannelingProperty());
            registerMiapi(moduleProperties, AirDragProperty.KEY, new AirDragProperty());
            registerMiapi(moduleProperties, WaterDragProperty.KEY, new WaterDragProperty());
            registerMiapi(moduleProperties, ArrowProperty.KEY, new ArrowProperty());
            registerMiapi(moduleProperties, DurabilityProperty.KEY, new DurabilityProperty());
            registerMiapi(moduleProperties, FracturingProperty.KEY, new FracturingProperty());
            registerMiapi(moduleProperties, FortuneProperty.KEY, new FortuneProperty());
            registerMiapi(moduleProperties, MendingProperty.KEY, new MendingProperty());
            registerMiapi(moduleProperties, IsPiglinGold.KEY, new IsPiglinGold());
            registerMiapi(moduleProperties, CanWalkOnSnow.KEY, new CanWalkOnSnow());
            registerMiapi(moduleProperties, FireProof.KEY, new FireProof());
            registerMiapi(moduleProperties, RepairPriority.KEY, new RepairPriority());
            registerMiapi(moduleProperties, PriorityProperty.KEY, new PriorityProperty());
            registerMiapi(moduleProperties, ImmolateProperty.KEY, new ImmolateProperty());
            registerMiapi(moduleProperties, LeechingProperty.KEY, new LeechingProperty());
            registerMiapi(moduleProperties, IsCrossbowShootAble.KEY, new IsCrossbowShootAble());
            registerMiapi(moduleProperties, EdibleProperty.KEY, new EdibleProperty());
            registerMiapi(moduleProperties, CryoProperty.KEY, new CryoProperty());
            registerMiapi(moduleProperties, AquaticDamage.KEY, new AquaticDamage());
            registerMiapi(moduleProperties, SpiderDamage.KEY, new SpiderDamage());
            registerMiapi(moduleProperties, SmiteDamage.KEY, new SmiteDamage());
            registerMiapi(moduleProperties, IllagerBane.KEY, new IllagerBane());
            registerMiapi(moduleProperties, PillagesGuard.KEY, new PillagesGuard());
            registerMiapi(moduleProperties, LuminousLearningProperty.KEY, new LuminousLearningProperty());
            registerMiapi(moduleProperties, BlueprintProperty.KEY, new BlueprintProperty());
            registerMiapi(moduleProperties, WaterGravityProperty.KEY, new WaterGravityProperty());
            registerMiapi(moduleProperties, CraftingEnchantProperty.KEY, new CraftingEnchantProperty());
            registerMiapi(moduleProperties, FakeEnchantmentProperty.KEY, new FakeEnchantmentProperty());
            registerMiapi(moduleProperties, ExhaustionProperty.KEY, new ExhaustionProperty());
            registerMiapi(moduleProperties, MaterialInscribeDataProperty.KEY, new MaterialInscribeDataProperty());
            registerMiapi(moduleProperties, FakeItemTagProperty.KEY, new FakeItemTagProperty());
            registerMiapi(moduleProperties, RarityProperty.KEY, new RarityProperty());
            registerMiapi(moduleProperties, HideFlagsProperty.KEY, new HideFlagsProperty());
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
            registerMiapi(moduleProperties, EmissiveProperty.KEY, new EmissiveProperty());
            registerMiapi(moduleProperties, EnchantmentTransformerProperty.KEY, new EnchantmentTransformerProperty());
            registerMiapi(moduleProperties, RapidfireCrossbowProperty.KEY, new RapidfireCrossbowProperty());
            registerMiapi(moduleProperties, MagazineCrossbowShotDelay.KEY, new MagazineCrossbowShotDelay());
            registerMiapi(moduleProperties, HandheldItemProperty.KEY, new HandheldItemProperty());
            registerMiapi(moduleProperties, AttributeSplitProperty.KEY, new AttributeSplitProperty());
            //compat
            registerMiapi(moduleProperties, BetterCombatProperty.KEY, new BetterCombatProperty());
            registerMiapi(moduleProperties, ApoliPowersProperty.KEY, new ApoliPowersProperty());
            registerMiapi(moduleProperties, TreechopProperty.KEY, new TreechopProperty());

            // CRAFTING STATS
            //registerMiapi(craftingStats, "hammering", new SimpleCraftingStat(0), stat -> exampleCraftingStat = stat);

            // ABILITIES
            registerMiapi(useAbilityRegistry, "throw", new ThrowingAbility());
            registerMiapi(useAbilityRegistry, "boomerang_throw", new BoomerangThrowingAbility());
            registerMiapi(useAbilityRegistry, "block", new BlockAbility());
            registerMiapi(useAbilityRegistry, "full_block", new ShieldBlockAbility());
            registerMiapi(useAbilityRegistry, RiptideProperty.KEY, new RiptideAbility());
            registerMiapi(useAbilityRegistry, HeavyAttackProperty.KEY, new HeavyAttackAbility());
            registerMiapi(useAbilityRegistry, CircleAttackProperty.KEY, new CircleAttackAbility());
            registerMiapi(useAbilityRegistry, CrossbowProperty.KEY, new CrossbowAbility());
            registerMiapi(useAbilityRegistry, AxeAbility.KEY, new AxeAbility());
            registerMiapi(useAbilityRegistry, HoeAbility.KEY, new HoeAbility());
            registerMiapi(useAbilityRegistry, ShovelAbility.KEY, new ShovelAbility());
            registerMiapi(useAbilityRegistry, EatAbility.KEY, new EatAbility());
            registerMiapi(useAbilityRegistry, AreaHarvestReplant.KEY, new AreaHarvestReplant());

            Miapi.LOGGER.info("Registered Truly Modulars Property resolvers:");
            PropertyResolver.registry.forEach((pair) -> {
                Miapi.LOGGER.info("registered resolver: " + pair.getLeft().toString());
            });
        });
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static final Identifier customGlintTexture = Identifier.of(MOD_ID, "textures/custom_glint.png");

        //public static ShaderProgram translucentMaterialShader;
        public static ShaderProgram entityTranslucentMaterialShader;
        public static ShaderProgram glintShader;

        public static final RenderLayer modularItemGlint = RenderLayer.of(
                "miapi_glint_direct|immediatelyfast:renderlast",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                256, true, true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(new RenderPhase.ShaderProgram(() -> {
                            if (!RenderSystem.isOnRenderThreadOrInit()) {
                                throw new RuntimeException("attempted miapi glint setup on-non-render thread. Please report this to Truly Modular");
                            }
                            glintShader.bind();
                            //glintShader.addSampler("CustomGlintTexture",BLOCK_ATLAS_TEXTURE);
                            int id = 10;
                            RenderSystem.setShaderTexture(id, RegistryInventory.Client.customGlintTexture);
                            RenderSystem.bindTexture(id);
                            int j = RenderSystem.getShaderTexture(id);
                            glintShader.addSampler("CustomGlintTexture", j);
                            var a = new RenderPhase.Texture(customGlintTexture, true, false);
                            //glintShader.getUniformOrDefault("glintSize").set();
                            //NativeImage.
                            return glintShader;
                        }))
                        .texture(Textures.create().add(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, false, false)
                                .add(MaterialAtlasManager.MATERIAL_ID, false, false).build())
                        .depthTest(EQUAL_DEPTH_TEST)
                        .transparency(GLINT_TRANSPARENCY)
                        .lightmap(ENABLE_LIGHTMAP)
                        //.cull(DISABLE_CULLING)
                        .writeMaskState(COLOR_MASK)
                        .texturing(RenderLayer.ENTITY_GLINT_TEXTURING)
                        .overlay(ENABLE_OVERLAY_COLOR).build(false));

        public static final RenderLayer TRANSLUCENT_NO_CULL = RenderLayer.of(
                "miapi_translucent_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
                0x200000, true, true, RenderLayer.MultiPhaseParameters.builder()
                        .lightmap(ENABLE_LIGHTMAP).program(TRANSLUCENT_PROGRAM).texture(MIPMAP_BLOCK_ATLAS_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY)
                        .target(TRANSLUCENT_TARGET).cull(DISABLE_CULLING).build(true));

        private static void setupGlintTexturing(float scale) {
            long l = (long) ((double) Util.getMeasuringTimeMs() * MinecraftClient.getInstance().options.getGlintSpeed().getValue() * 8.0);
            float f = (float) (l % 110000L) / 110000.0f;
            float g = (float) (l % 30000L) / 30000.0f;
            Matrix4f matrix4f = new Matrix4f().translation(-f, g, 0.0f);
            matrix4f.rotateZ(0.17453292f).scale(scale);
            RenderSystem.setTextureMatrix(matrix4f);
        }
    }


}
