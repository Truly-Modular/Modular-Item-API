package smartin.miapi.registries;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.blocks.ModularWorkBench;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.item.modular.items.*;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.*;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.PropertyInjectionDev;
import smartin.miapi.modules.edit_options.skins.SkinOptions;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.render.GuiOffsetProperty;
import smartin.miapi.modules.properties.render.ModelMergeProperty;
import smartin.miapi.modules.properties.render.ModelProperty;
import smartin.miapi.modules.properties.render.ModelTransformationProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static smartin.miapi.Miapi.MOD_ID;
import static smartin.miapi.modules.abilities.util.ItemAbilityManager.useAbilityRegistry;
import static smartin.miapi.attributes.AttributeRegistry.*;

public class RegistryInventory {
    public static final Supplier<RegistrarManager> registrar = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final MiapiRegistrar<Item> modularItems = MiapiRegistrar.of(registrar.get().get(RegistryKeys.ITEM));
    public static final Registrar<Item> items = registrar.get().get(RegistryKeys.ITEM);
    public static final Registrar<Block> blocks = registrar.get().get(RegistryKeys.BLOCK);
    public static final Registrar<EntityAttribute> attributes = registrar.get().get(RegistryKeys.ATTRIBUTE);
    public static final Registrar<EntityType<?>> entityTypes = registrar.get().get(RegistryKeys.ENTITY_TYPE);
    public static final Registrar<ScreenHandlerType<?>> screenHandlers = registrar.get().get(RegistryKeys.SCREEN_HANDLER);
    public static final MiapiRegistry<ModuleProperty> moduleProperties = MiapiRegistry.getInstance(ModuleProperty.class);
    public static final MiapiRegistry<ItemModule> modules = MiapiRegistry.getInstance(ItemModule.class);
    public static final MiapiRegistry<EditOption> editOptions = MiapiRegistry.getInstance(EditOption.class);

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
    public static <T> void addCallback(Registrar<T> rg, Consumer<T> consumer) {
        rg.getIds().forEach(id -> rg.listen(id, consumer));
    }

    /**
     * Registers an attribute.
     *
     * @param id The id of the attribute. Miapi namespace is inferred
     * @param attach Whether this attribute should automatically attach to living entities
     * @param sup Supplier of the actual attribute
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
    public static Item modularItem;
    public static RegistrySupplier<EntityType<ItemProjectile>> itemProjectileType = (RegistrySupplier) registerAndSupply(entityTypes, "thrown_item", () ->
            EntityType.Builder.create(ItemProjectile::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_item"));
    public static ScreenHandlerType<CraftingScreenHandler> craftingScreenHandler;

    public static void setup() {
        //SCREEN
        register(screenHandlers, "default_crafting", () ->
                new ScreenHandlerType<>(CraftingScreenHandler::new, null), // TODO: check this null again
                scr -> craftingScreenHandler = (ScreenHandlerType<CraftingScreenHandler>) scr);

        //ENTITY
            // commented out because RegistrySupplier is needed
        /*register(entityTypes, "thrown_item", () ->
                EntityType.Builder.create(ItemProjectile::new, SpawnGroup.MISC).setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("miapi:thrown_item"),
                type -> itemProjectileType = (EntityType<ItemProjectile>) type);*/

        //BLOCK
        register(blocks, "modular_work_bench", () -> new ModularWorkBench(
                AbstractBlock.Settings.create().
                        mapColor(MapColor.IRON_GRAY).
                        instrument(Instrument.IRON_XYLOPHONE).
                        requiresTool().
                        strength(5.0F, 6.0F).
                        sounds(BlockSoundGroup.METAL).
                        nonOpaque()), b -> modularWorkBench = b);
        register(items, "modular_work_bench", () -> new BlockItem(modularWorkBench, new Item.Settings()));

        //ITEM
        register(modularItems, "modular_item", ExampleModularItem::new, i -> modularItem = i);
        register(modularItems, "modular_handle", ModularWeapon::new);
        register(modularItems, "modular_sword", ModularWeapon::new);
        register(modularItems, "modular_katana", ModularWeapon::new);
        register(modularItems, "modular_naginata", ModularWeapon::new);
        register(modularItems, "modular_greatsword", ModularWeapon::new);
        register(modularItems, "modular_dagger", ModularWeapon::new);
        register(modularItems, "modular_throwing_knife", ModularWeapon::new);
        register(modularItems, "modular_rapier", ModularWeapon::new);
        register(modularItems, "modular_longsword", ModularWeapon::new);

        register(modularItems, "modular_shovel", ModularWeapon::new);
        register(modularItems, "modular_pickaxe", ModularWeapon::new);
        register(modularItems, "modular_axe", ModularWeapon::new);
        register(modularItems, "modular_hoe", ModularWeapon::new);
        register(modularItems, "modular_mattock", ModularWeapon::new);

        register(modularItems, "modular_bow", ExampleModularBowItem::new);

        register(modularItems, "modular_helmet", ModularHelmet::new);
        register(modularItems, "modular_chestplate", ModularChestPlate::new);
        register(modularItems, "modular_leggings", ModularLeggings::new);
        register(modularItems, "modular_boots", ModularBoots::new);

        //ATTRIBUTE
        registerAtt("generic.durability", false, () ->
                new ClampedEntityAttribute("miapi.attribute.name.durability", 300.0, 1.0, 16777216).setTracked(true),
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
                new ClampedEntityAttribute("miapi.attribute.name.mining_speed.pickaxe", 1.0, 1.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_PICKAXE = att);
        registerAtt("generic.mining_speed.axe", false, () ->
                new ClampedEntityAttribute("miapi.attribute.name.mining_speed.axe", 1.0, 1.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_AXE = att);
        registerAtt("generic.mining_speed.shovel", false, () ->
                new ClampedEntityAttribute("miapi.attribute.name.mining_speed.shovel", 1.0, 1.0, 1024.0).setTracked(true),
                att -> MINING_SPEED_SHOVEL = att);
        registerAtt("generic.mining_speed.hoe", false, () ->
                new ClampedEntityAttribute("miapi.attribute.name.mining_speed.hoe", 1.0, 1.0, 1024.0).setTracked(true),
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


        //EDITPROPERTIES
        registerMiapi(editOptions, "dev", new PropertyInjectionDev());
        registerMiapi(editOptions, "skin", new SkinOptions());

        //MODULEPROPERTIES
        registerMiapi(moduleProperties, NameProperty.KEY, new NameProperty());
        registerMiapi(moduleProperties, ModelProperty.KEY, new ModelProperty());
        registerMiapi(moduleProperties, SlotProperty.KEY, new SlotProperty());
        registerMiapi(moduleProperties, AllowedSlots.KEY, new AllowedSlots());
        registerMiapi(moduleProperties, MaterialProperty.KEY, new MaterialProperty());
        registerMiapi(moduleProperties, AllowedMaterial.KEY, new AllowedMaterial());
        registerMiapi(moduleProperties, AttributeProperty.KEY, new AttributeProperty());
        registerMiapi(moduleProperties, PotionEffectProperty.KEY, new PotionEffectProperty());
        registerMiapi(moduleProperties, ModelTransformationProperty.KEY, new ModelTransformationProperty());
        registerMiapi(moduleProperties, DisplayNameProperty.KEY, new DisplayNameProperty());
        registerMiapi(moduleProperties, ItemIdProperty.KEY, new ItemIdProperty());
        registerMiapi(moduleProperties, GuiOffsetProperty.KEY, new GuiOffsetProperty());
        registerMiapi(moduleProperties, EquipmentSlotProperty.KEY, new EquipmentSlotProperty());
        registerMiapi(moduleProperties, ModelMergeProperty.KEY, new ModelMergeProperty());
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

        registerMiapi(useAbilityRegistry, "throw", new ThrowingAbility());
        registerMiapi(useAbilityRegistry, "block", new BlockAbility());
        registerMiapi(useAbilityRegistry, RiptideProperty.KEY, new RiptideAbility());
        registerMiapi(useAbilityRegistry, HeavyAttackProperty.KEY, new HeavyAttackAbility());
        registerMiapi(useAbilityRegistry, CircleAttackProperty.KEY, new CircleAttackAbility());
        registerMiapi(useAbilityRegistry, CrossbowProperty.KEY, new CrossbowAbility());
    }
}
