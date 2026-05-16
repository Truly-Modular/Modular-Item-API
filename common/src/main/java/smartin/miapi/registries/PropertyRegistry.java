package smartin.miapi.registries;

import smartin.miapi.material.*;
import smartin.miapi.material.properties.*;
import smartin.miapi.modules.abilities.key.KeyBindAbilityManagerProperty;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.abilities.util.AbilityProperty;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.armor.*;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.attributes.AttributeSplitProperty;
import smartin.miapi.modules.properties.compat.ht_treechop.TreechopProperty;
import smartin.miapi.modules.properties.enchanment.*;
import smartin.miapi.modules.properties.mining.AutoSmeltProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;
import smartin.miapi.modules.properties.mining.MiningTelekinesisProperty;
import smartin.miapi.modules.properties.onHit.*;
import smartin.miapi.modules.properties.onHit.entity.*;
import smartin.miapi.modules.properties.potion.OnHitDefensiveEffects;
import smartin.miapi.modules.properties.potion.OnHitOffensiveEffects;
import smartin.miapi.modules.properties.potion.OnKillEffects;
import smartin.miapi.modules.properties.projectile.*;
import smartin.miapi.modules.properties.projectile.stat.bow.BowAccuracyProperty;
import smartin.miapi.modules.properties.projectile.stat.bow.BowDrawTimeProperty;
import smartin.miapi.modules.properties.projectile.stat.bow.BowSpeedProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileAccuracyProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileDamageProperty;
import smartin.miapi.modules.properties.projectile.stat.projectile.ProjectileSpeedProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowDamageProperty;
import smartin.miapi.modules.properties.projectile.stat.throwable.ThrowSpeedProperty;
import smartin.miapi.modules.properties.render.*;
import smartin.miapi.modules.properties.render.baked.ModelProperty;
import smartin.miapi.modules.properties.slot.*;
import smartin.miapi.modules.properties.tag.ModuleTagLegacyProperty;
import smartin.miapi.modules.properties.tag.ModuleTagMaterialLegacyProperty;
import smartin.miapi.modules.properties.tag.ModuleTagProperty;
import smartin.miapi.modules.properties.trinket.TrinketSlotProperty;

public class PropertyRegistry {
    static void registerProperties() {

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ChainModelProperty.KEY, new ChainModelProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, DynamicTrailModelProperty.KEY, new DynamicTrailModelProperty());
        if (smartin.miapi.Environment.isClient()) {
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ModelProperty.KEY, new ModelProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ModelTransformationProperty.KEY, new ModelTransformationProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, GuiOffsetProperty.KEY, new GuiOffsetProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ItemModelProperty.KEY, new ItemModelProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, BannerModelProperty.KEY, new BannerModelProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, BlockModelProperty.KEY, new BlockModelProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, EntityModelProperty.KEY, new EntityModelProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CrystalModelProperty.KEY, new CrystalModelProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ConduitModelProperty.KEY, new ConduitModelProperty());
            //registerMiapi(MODULE_PROPERTY_MIAPI_REGISTRY, OverlayModelProperty.KEY, new OverlayModelProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ProjectileRenderAnimation.KEY, new ProjectileRenderAnimation());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY,
                    smartin.miapi.modules.properties.render.overlay.OverlayModelProperty.KEY,
                    new smartin.miapi.modules.properties.render.overlay.OverlayModelProperty());
        } else {
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "model", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "model_transform", new ServerReplaceProperty());
            //registerMiapi(moduleProperties, "modelMerge", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "gui_offset", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "item_model", new ServerReplaceProperty());
            //registerMiapi(moduleProperties, "itemLore", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "banner", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "crystal_model", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "block_model", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "entity_model", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "conduit_model", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "overlay_texture_model", new ServerReplaceProperty());
            RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, "projectile_animation", new ServerReplaceProperty());
        }
        //STRUCTURE
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CanChildBeEmpty.KEY, new CanChildBeEmpty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, SlotProperty.KEY, new SlotProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AllowedSlots.KEY, new AllowedSlots());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MaterialProperty.KEY, new MaterialProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AllowedMaterial.KEY, new AllowedMaterial());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, DisplayNameProperty.KEY, new DisplayNameProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ItemIdProperty.KEY, new ItemIdProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, EquipmentSlotProperty.KEY, new EquipmentSlotProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ModuleTagProperty.KEY, new ModuleTagProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ModuleTagLegacyProperty.KEY, new ModuleTagLegacyProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ModuleTagMaterialLegacyProperty.KEY, new ModuleTagMaterialLegacyProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CraftingConditionProperty.KEY, new CraftingConditionProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, StatRequirementProperty.KEY, new StatRequirementProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, GlintProperty.KEY, new GlintProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, FakeItemTagProperty.KEY, new FakeItemTagProperty());

        // PROJECTILE
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, BowDrawTimeProperty.KEY, new BowDrawTimeProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, BowAccuracyProperty.KEY, new BowAccuracyProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, BowSpeedProperty.KEY, new BowSpeedProperty());

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ArrowRetrievalProperty.KEY, new ArrowRetrievalProperty());

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ProjectileDamageProperty.KEY, new ProjectileDamageProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ProjectileSpeedProperty.KEY, new ProjectileSpeedProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ProjectileAccuracyProperty.KEY, new ProjectileAccuracyProperty());

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ThrowDamageProperty.KEY, new ThrowDamageProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ThrowSpeedProperty.KEY, new ThrowSpeedProperty());


        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AirDragProperty.KEY, new AirDragProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, WaterDragProperty.KEY, new WaterDragProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ArrowProperty.KEY, new ArrowProperty());

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, RapidfireCrossbowProperty.KEY, new RapidfireCrossbowProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MagazineCrossbowShotDelay.KEY, new MagazineCrossbowShotDelay());

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, InAirProjectileTransform.KEY, new InAirProjectileTransform());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ShotVelocityOffsetProperty.KEY, new ShotVelocityOffsetProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ExplosionProperty.KEY, new ExplosionProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ProjectileTriggerProperty.KEY, new ProjectileTriggerProperty());

        //ON HIT
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CustomDamageOnHitProperty.KEY, CustomDamageOnHitProperty.property);
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ModularAttackCommandProperty.KEY, ModularAttackCommandProperty.property);

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, LoreProperty.KEY, new LoreProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, OldNameProperty.KEY, new OldNameProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AttributeProperty.KEY, new AttributeProperty());
        //registerMiapi(moduleProperties, ParticleShapingProperty.KEY, new ParticleShapingProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, HealthPercentDamage.KEY, new HealthPercentDamage());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ArmorPenProperty.KEY, new ArmorPenProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ToolOrWeaponProperty.KEY, new ToolOrWeaponProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MiningLevelProperty.KEY, new MiningLevelProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, EnderpearlProperty.KEY, new EnderpearlProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, TeleportTarget.KEY, new TeleportTarget());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ChannelingProperty.KEY, new ChannelingProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, DurabilityProperty.KEY, new DurabilityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, FracturingProperty.KEY, new FracturingProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, IsPiglinGold.KEY, new IsPiglinGold());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CanWalkOnSnow.KEY, new CanWalkOnSnow());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, FireProof.KEY, new FireProof());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, RepairPriority.KEY, new RepairPriority());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, PriorityProperty.KEY, new PriorityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ImmolateProperty.KEY, new ImmolateProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, LeechingProperty.KEY, new LeechingProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, IsCrossbowShootAble.KEY, new IsCrossbowShootAble());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CryoProperty.KEY, new CryoProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AquaticDamage.KEY, new AquaticDamage());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, SpiderDamage.KEY, new SpiderDamage());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, SmiteDamage.KEY, new SmiteDamage());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, IllagerBane.KEY, new IllagerBane());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, PillagesGuard.KEY, new PillagesGuard());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, LuminousLearningProperty.KEY, new LuminousLearningProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, WaterGravityProperty.KEY, new WaterGravityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CraftingEnchantProperty.KEY, new CraftingEnchantProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ExhaustionProperty.KEY, new ExhaustionProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MaterialInscribeDataProperty.KEY, new MaterialInscribeDataProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, RarityProperty.KEY, new RarityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MiningShapeProperty.KEY, new MiningShapeProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ModuleStats.KEY, new ModuleStats());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, EnchantAbilityProperty.KEY, new EnchantAbilityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, StepCancelingProperty.KEY, new StepCancelingProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, LightningOnHit.KEY, new LightningOnHit());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, GuiStatProperty.KEY, new GuiStatProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AbilityMangerProperty.KEY, new AbilityMangerProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, OnHitDefensiveEffects.KEY, new OnHitDefensiveEffects());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, OnHitOffensiveEffects.KEY, new OnHitOffensiveEffects());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, OnKillEffects.KEY, new OnKillEffects());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, OnKillExplosion.KEY, new OnKillExplosion());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CanChangeParentModule.KEY, new CanChangeParentModule());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CanChangeSelfModule.KEY, new CanChangeSelfModule());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, NemesisProperty.KEY, new NemesisProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CopyParentMaterialProperty.KEY, new CopyParentMaterialProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, EmissivityProperty.KEY, new EmissivityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, HandheldItemProperty.KEY, new HandheldItemProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AttributeSplitProperty.KEY, new AttributeSplitProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, FakeEnchantmentProperty.KEY, new FakeEnchantmentProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AllowedEnchantments.KEY, new AllowedEnchantments());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, BlueprintCrafting.KEY, new BlueprintCrafting());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, SlashingProperty.KEY, new SlashingProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ComponentProperty.KEY, new ComponentProperty());
        //registerMiapi(MODULE_PROPERTY_MIAPI_REGISTRY, AdvancedComponentProperty.KEY, new AdvancedComponentProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MaterialInscribeProperty.KEY, new MaterialInscribeProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MaterialInscribeModuleProperty.KEY, new MaterialInscribeModuleProperty());

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MaterialOverwriteProperty.KEY, new MaterialOverwriteProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CopyItemOnHit.KEY, new CopyItemOnHit());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AllowedInLootProperty.KEY, new AllowedInLootProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, PogoAbility.KEY, new PogoAbility());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, CopyItemLoreProperty.KEY, new CopyItemLoreProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ColorProperty.KEY, new ColorProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, KeyBindAbilityManagerProperty.KEY, new KeyBindAbilityManagerProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, FakeEitherEnchantmentProperty.KEY, new FakeEitherEnchantmentProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MakesImpactSoundProperty.KEY, new MakesImpactSoundProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ProjectileDropItemProperty.KEY, new ProjectileDropItemProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MaterialStatIndicatorProperty.KEY, new MaterialStatIndicatorProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ComponentMaterialProperty.KEY, new ComponentMaterialProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AlphaOverwriteProperty.KEY, new AlphaOverwriteProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, IconRenderProperty.KEY, new IconRenderProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AssumeItemIdentityProperty.KEY, new AssumeItemIdentityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, TreechopProperty.KEY, new TreechopProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AutoSmeltProperty.KEY, new AutoSmeltProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, MiningTelekinesisProperty.KEY, new MiningTelekinesisProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, BludgeonProperty.KEY, new BludgeonProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, AbilityProperty.KEY, new AbilityProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, EntityDamageStrength.KEY, new EntityDamageStrength());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, EntityArmorStrength.KEY, new EntityArmorStrength());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ComboProperty.KEY, new ComboProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, ComboTimeProperty.KEY, new ComboTimeProperty());
        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, InertiaProperty.KEY, new InertiaProperty());

        RegistryInventory.registerMiapi(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY, TrinketSlotProperty.KEY, TrinketSlotProperty.property);

    }
}
