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
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
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
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.event.GameEvent;
import org.joml.Matrix4f;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.blocks.ModularWorkBench;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.blocks.StatProvidingBlockEntity;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.craft.stat.CraftingStat;
import smartin.miapi.craft.stat.SimpleCraftingStat;
import smartin.miapi.effects.CryoStatusEffect;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.item.modular.items.*;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.*;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.modules.conditions.*;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.PropertyInjectionDev;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.compat.BetterCombatProperty;
import smartin.miapi.modules.properties.material.AllowedMaterial;
import smartin.miapi.modules.properties.material.MaterialInscribeProperty;
import smartin.miapi.modules.properties.material.MaterialProperties;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.modules.properties.render.*;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.client.render.RenderPhase.*;
import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.attributes.AttributeRegistry.*;
import static smartin.miapi.modules.abilities.util.ItemAbilityManager.useAbilityRegistry;
import static smartin.miapi.modules.conditions.ConditionManager.moduleConditionRegistry;

public class RegistryInventory {
    public static final Supplier<RegistrarManager> registrar = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final MiapiRegistrar<Item> modularItems = MiapiRegistrar.of(registrar.get().get(RegistryKeys.ITEM));
    public static final Registrar<Item> items = registrar.get().get(RegistryKeys.ITEM);
    public static final Registrar<Block> blocks = registrar.get().get(RegistryKeys.BLOCK);
    public static final Registrar<BlockEntityType<?>> blockEntities = registrar.get().get(RegistryKeys.BLOCK_ENTITY_TYPE);
    public static final Registrar<EntityAttribute> attributes = registrar.get().get(RegistryKeys.ATTRIBUTE);
    public static final Registrar<EntityType<?>> entityTypes = registrar.get().get(RegistryKeys.ENTITY_TYPE);
    public static final Registrar<ScreenHandlerType<?>> screenHandlers = registrar.get().get(RegistryKeys.SCREEN_HANDLER);
    public static final Registrar<StatusEffect> statusEffects = registrar.get().get(RegistryKeys.STATUS_EFFECT);
    public static final Registrar<ItemGroup> tab = registrar.get().get(RegistryKeys.ITEM_GROUP);
    public static final Registrar<GameEvent> gameEvents = registrar.get().get(RegistryKeys.GAME_EVENT);
    public static final Registrar<RecipeSerializer<?>> RECIPE_SERIALIZERS = registrar.get().get(RegistryKeys.RECIPE_SERIALIZER);
    public static final MiapiRegistry<ModuleProperty> moduleProperties = MiapiRegistry.getInstance(ModuleProperty.class);
    public static final MiapiRegistry<ItemModule> modules = MiapiRegistry.getInstance(ItemModule.class);
    public static final MiapiRegistry<EditOption> editOptions = MiapiRegistry.getInstance(EditOption.class);
    public static final MiapiRegistry<CraftingStat> craftingStats = MiapiRegistry.getInstance(CraftingStat.class);

    public static <T> RegistrySupplier<T> registerAndSupply(Registrar<T> rg, Identifier id, Supplier<T> object) {
        return rg.register(id, object);
    }

    public static <T> RegistrySupplier<T> registerAndSupply(Registrar<T> rg, String id, Supplier<T> object) {
        return registerAndSupply(rg, new Identifier(MOD_ID, id), object);
    }

    public static <T> void register(Registrar<T> rg, Identifier id, Supplier<T> object, Consumer<T> onRegister) {
        rg.register(id, object).listen(onRegister);
    }

    public static <T> void register(Registrar<T> rg, String id, Supplier<T> object, Consumer<T> onRegister) {
        register(rg, new Identifier(MOD_ID, id), object, onRegister);
    }

    public static <T> void register(Registrar<T> rg, Identifier id, Supplier<T> object) {
        rg.register(id, object);
    }

    public static <T> void register(Registrar<T> rg, String id, Supplier<T> object) {
        register(rg, new Identifier(MOD_ID, id), object);
    }

    public static <T> void registerMiapi(MiapiRegistry<T> rg, String id, T object) {
        rg.register(id, object);
    }

    public static <T> void registerMiapi(MiapiRegistry<T> rg, String id, T object, Consumer<T> onRegister) {
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
        Identifier rl = new Identifier(MOD_ID, id);
        String stringId = rl.toString();

        RegistrySupplier<EntityAttribute> obj = attributes.register(rl, sup); // actually register the object
        obj.listen(onRegister); // attach the onRegister callback, usually used to set the value of fields.

        if (attach) // if it should automatically attach to an entity, add another listener to do that (this is equivalent to the old registerOnEntity)
            obj.listen(att -> AttributeRegistry.entityAttributeMap.put(stringId, att));
    }

    public static Block modularWorkBench;
    public static Block exampleStatProviderBlock;
    public static BlockEntityType<ModularWorkBenchEntity> modularWorkBenchEntityType;
    public static BlockEntityType<StatProvidingBlockEntity> exampleStatProviderBlockEntityType;
    public static Item modularItem;
    public static GameEvent statUpdateEvent;
    public static StatusEffect cryoStatusEffect;
    public static GameEvent statProviderUpdatedEvent;
    public static SimpleCraftingStat exampleCraftingStat;
    public static RecipeSerializer serializer;
    public static RegistrySupplier<EntityType<ItemProjectileEntity>> itemProjectileType = (RegistrySupplier) registerAndSupply(entityTypes, "thrown_item", () ->
            EntityType.Builder.create(ItemProjectileEntity::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_item"));

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

        //ENTITY
        // commented out because RegistrySupplier is needed
        /*register(entityTypes, "thrown_item", () ->
                EntityType.builder.create(ItemProjectile::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_item"),
                type -> itemProjectileType = (EntityType<ItemProjectile>) type);*/

        register(RECIPE_SERIALIZERS, "smithing", MaterialSmithingRecipe.Serializer::new, i -> serializer = i);


        //BLOCK
        register(blocks, "modular_work_bench", () -> new ModularWorkBench(
                AbstractBlock.Settings.create().
                        mapColor(MapColor.IRON_GRAY).
                        instrument(Instrument.IRON_XYLOPHONE).
                        requiresTool().
                        strength(2.0F, 6.0F).
                        sounds(BlockSoundGroup.METAL).
                        nonOpaque().
                        pistonBehavior(PistonBehavior.IGNORE)), b -> modularWorkBench = b);
        register(blockEntities, "modular_work_bench", () -> BlockEntityType.Builder.create(
                ModularWorkBenchEntity::new, modularWorkBench
        ).build(null), be -> {
            modularWorkBenchEntityType = (BlockEntityType<ModularWorkBenchEntity>) be;
            if (Platform.getEnvironment() == Env.CLIENT) MiapiClient.registerBlockEntityRenderer();
        });
        register(items, "modular_work_bench", () -> new BlockItem(modularWorkBench, new Item.Settings()));


        /*register(blocks, "example_stat_provider", () ->
                new StatProvidingBlock(AbstractBlock.Settings.create(), StatProvidingBlockEntity.Example::new), b -> exampleStatProviderBlock = b);
        register(blockEntities, "example_stat_provider", () -> BlockEntityType.builder.create(
                StatProvidingBlockEntity.Example::new, exampleStatProviderBlock
        ).build(null), be -> {
            exampleStatProviderBlockEntityType = (BlockEntityType<StatProvidingBlockEntity>) be;
        });
        register(items, "example_stat_provider", () -> new BlockItem(exampleStatProviderBlock, new Item.Settings()));*/


        // CREATIVE TAB
        register(tab, "miapi_tab", () -> CreativeTabRegistry.create
                (b -> {
                    b.displayName(Text.translatable("tab.miapi"));
                    b.icon(() -> new ItemStack(modularWorkBench));
                    b.entries((displayContext, entries) -> {
                        entries.add(modularWorkBench);
                    });
                }));
        //ITEM
        register(modularItems, "modular_item", ExampleModularItem::new, i -> modularItem = i);
        register(modularItems, "modular_handle", ModularWeapon::new);
        register(modularItems, "modular_sword", ModularWeapon::new);
        register(modularItems, "modular_katana", ModularWeapon::new);
        register(modularItems, "modular_naginata", ModularWeapon::new);
        register(modularItems, "modular_greatsword", ModularWeapon::new);
        register(modularItems, "modular_dagger", ModularWeapon::new);
        register(modularItems, "modular_spear", ModularWeapon::new);
        register(modularItems, "modular_throwing_knife", ModularWeapon::new);
        register(modularItems, "modular_rapier", ModularWeapon::new);
        register(modularItems, "modular_longsword", ModularWeapon::new);

        register(modularItems, "modular_shovel", ModularWeapon::new);
        register(modularItems, "modular_pickaxe", ModularWeapon::new);
        register(modularItems, "modular_axe", ModularWeapon::new);
        register(modularItems, "modular_hoe", ModularWeapon::new);
        register(modularItems, "modular_mattock", ModularWeapon::new);

        register(modularItems, "modular_bow", ModularBow::new);
        register(modularItems, "modular_bow_part", ExampleModularItem::new);
        register(modularItems, "modular_arrow", ModularArrow::new);
        register(modularItems, "modular_arrow_part", ExampleModularItem::new);

        register(modularItems, "modular_helmet", ModularHelmet::new);
        register(modularItems, "modular_chestplate", ModularChestPlate::new);
        register(modularItems, "modular_leggings", ModularLeggings::new);
        register(modularItems, "modular_boots", ModularBoots::new);

        //STATUS EFFECTS
        register(statusEffects, "cryo", CryoStatusEffect::new, eff -> cryoStatusEffect = eff);

        //ATTRIBUTE
        registerAtt("generic.durability", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.durability", 1.0, 1.0, 16777216).setTracked(true),
                att -> ITEM_DURABILITY = att);

        // reach
        registerAtt("generic.reach", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.reach", 0.0, -1024.0, 1024.0).setTracked(true),
                att -> REACH = att);
        registerAtt("generic.attack_range", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.attack_range", 0.0, -1024.0, 1024.0).setTracked(true),
                att -> ATTACK_RANGE = att);

        // mining
        registerAtt("generic.mining_speed.pickaxe", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.mining_speed.pickaxe", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_PICKAXE = att);
        registerAtt("generic.mining_speed.axe", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.mining_speed.axe", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_AXE = att);
        registerAtt("generic.mining_speed.shovel", false, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.mining_speed.shovel", 0.0, 0.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_SHOVEL = att);
        registerAtt("generic.mining_speed.hoe", false, () ->
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
        registerAtt("generic.shield_break", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.shield_break", 0.0, 0.0, 1024.0).setTracked(true),
                att -> SHIELD_BREAK = att);

        //projectile based
        registerAtt("generic.bow_draw_time", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.bow_draw_time", 20.0, 1.0, 1024.0).setTracked(true),
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


        // GAME EVENTS
        register(gameEvents, "request_crafting_stat_update", () -> new GameEvent(MOD_ID + ":crafting_stat_update", 16), ev -> statUpdateEvent = ev);
        register(gameEvents, "stat_provider_updated", () -> new GameEvent(MOD_ID + ":stat_provider_updated", 16), ev -> statProviderUpdatedEvent = ev);

        LifecycleEvent.SETUP.register(() -> {
            //EDITPROPERTIES
            registerMiapi(editOptions, "dev", new PropertyInjectionDev());
            registerMiapi(editOptions, "skin", new SkinOptions());

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
            registerMiapi(moduleConditionRegistry, "tag", new TagCondition());
            registerMiapi(moduleConditionRegistry, "miapi_perm", new MiapiPerm());

            //MODULEPROPERTIES
            try {
                registerMiapi(moduleProperties, ModelProperty.KEY, new ModelProperty());
                registerMiapi(moduleProperties, ModelTransformationProperty.KEY, new ModelTransformationProperty());
                registerMiapi(moduleProperties, ModelMergeProperty.KEY, new ModelMergeProperty());
                registerMiapi(moduleProperties, GuiOffsetProperty.KEY, new GuiOffsetProperty());
            } catch (Exception surpressed) {
                registerMiapi(moduleProperties, "texture", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "modelTransform", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "modelMerge", new ServerReplaceProperty());
                registerMiapi(moduleProperties, "guiOffset", new ServerReplaceProperty());
            }
            registerMiapi(moduleProperties, NameProperty.KEY, new NameProperty());
            registerMiapi(moduleProperties, SlotProperty.KEY, new SlotProperty());
            registerMiapi(moduleProperties, AllowedSlots.KEY, new AllowedSlots());
            registerMiapi(moduleProperties, MaterialProperty.KEY, new MaterialProperty());
            registerMiapi(moduleProperties, AllowedMaterial.KEY, new AllowedMaterial());
            registerMiapi(moduleProperties, AttributeProperty.KEY, new AttributeProperty());
            registerMiapi(moduleProperties, PotionEffectProperty.KEY, new PotionEffectProperty());
            //registerMiapi(moduleProperties, ParticleShapingProperty.KEY, new ParticleShapingProperty());
            registerMiapi(moduleProperties, PlaySoundProperty.KEY, new PlaySoundProperty());
            registerMiapi(moduleProperties, ExecuteProperty.KEY, new ExecuteProperty());
            registerMiapi(moduleProperties, ImmolateProperty.KEY, new ImmolateProperty());
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
            registerMiapi(moduleProperties, ItemModelProperty.KEY, new ItemModelProperty());
            registerMiapi(moduleProperties, EnderpearlProperty.KEY, new EnderpearlProperty());
            registerMiapi(moduleProperties, TeleportTarget.KEY, new TeleportTarget());
            registerMiapi(moduleProperties, ExplosionProperty.KEY, new ExplosionProperty());
            registerMiapi(moduleProperties, MaterialInscribeProperty.KEY, new MaterialInscribeProperty());
            registerMiapi(moduleProperties, ProjectileTriggerProperty.KEY, new ProjectileTriggerProperty());
            registerMiapi(moduleProperties, ChannelingProperty.KEY, new ChannelingProperty());

            //compat
            registerMiapi(moduleProperties, BetterCombatProperty.KEY, new BetterCombatProperty());

            // CRAFTING STATS
            //registerMiapi(craftingStats, "hammering", new SimpleCraftingStat(0), stat -> exampleCraftingStat = (SimpleCraftingStat) stat);

            // ABILITIES
            registerMiapi(useAbilityRegistry, "throw", new ThrowingAbility());
            registerMiapi(useAbilityRegistry, "block", new BlockAbility());
            registerMiapi(useAbilityRegistry, RiptideProperty.KEY, new RiptideAbility());
            registerMiapi(useAbilityRegistry, HeavyAttackProperty.KEY, new HeavyAttackAbility());
            registerMiapi(useAbilityRegistry, CircleAttackProperty.KEY, new CircleAttackAbility());
            registerMiapi(useAbilityRegistry, CrossbowProperty.KEY, new CrossbowAbility());
        });
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static final Identifier customGlintTexture = new Identifier(MOD_ID, "textures/custom_glint.png");

        public static ShaderProgram translucentMaterialShader;
        public static ShaderProgram entityTranslucentMaterialShader;
        public static ShaderProgram glintShader;

        public static final RenderLayer translucentMaterialRenderType = RenderLayer.of(
                "miapi_translucent_material", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS,
                0x200000, true, true,
                RenderLayer.MultiPhaseParameters.builder()
                        .lightmap(ENABLE_LIGHTMAP).program(new RenderPhase.ShaderProgram(() -> translucentMaterialShader))
                        .texture(MIPMAP_BLOCK_ATLAS_TEXTURE)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .target(TRANSLUCENT_TARGET).build(true));
        public static final RenderLayer entityTranslucentMaterialRenderType = RenderLayer.of(
                "miapi_entity_translucent_material",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                256, true, true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(new RenderPhase.ShaderProgram(() -> entityTranslucentMaterialShader))
                        .texture(BLOCK_ATLAS_TEXTURE)
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .lightmap(ENABLE_LIGHTMAP)
                        .overlay(ENABLE_OVERLAY_COLOR).build(true)
        );

        public static final Texturing ENTITY_GLINT_TEXTURING = new Texturing("miapi_glint_direct", () -> setupGlintTexturing(0.16f), () -> RenderSystem.resetTextureMatrix());

        public static final RenderLayer modularItemGlint = RenderLayer.of(
                "miapi_glint_direct",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                256, true, true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(new RenderPhase.ShaderProgram(() -> {
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
                        .texture(BLOCK_ATLAS_TEXTURE)
                        .depthTest(EQUAL_DEPTH_TEST)
                        .transparency(GLINT_TRANSPARENCY)
                        .lightmap(ENABLE_LIGHTMAP)
                        .cull(DISABLE_CULLING)
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
