package smartin.miapi.client.gui.crafting.statdisplay;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.gui.BoxList;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollList;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.item.modular.CustomDrawTimeItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.damage_boosts.AquaticDamage;
import smartin.miapi.modules.properties.damage_boosts.IllagerBane;
import smartin.miapi.modules.properties.damage_boosts.SmiteDamage;
import smartin.miapi.modules.properties.damage_boosts.SpiderDamage;
import smartin.miapi.modules.properties.util.GuiWidgetSupplier;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

@Environment(EnvType.CLIENT)
public class StatListWidget extends InteractAbleWidget {
    private static final List<InteractAbleWidget> statDisplays = new ArrayList<>();
    private static final List<StatWidgetSupplier> statWidgetSupplier = new ArrayList<>();
    public static final Map<String, JsonConverter> jsonConverterMap = new HashMap<>();
    private final BoxList boxList;
    private final TransformableWidget transformableWidget;
    private final TransformableWidget hoverText;
    private ItemStack original = ItemStack.EMPTY;
    private ItemStack compareTo = ItemStack.EMPTY;

    static {
        statWidgetSupplier.add(new StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                Set<GuiWidgetSupplier> suppliers = new HashSet<>();
                suppliers.addAll(
                        ItemModule.getModules(original).itemMergedProperties.keySet().stream()
                                .filter(property -> property instanceof GuiWidgetSupplier)
                                .map(property -> (GuiWidgetSupplier) property)
                                .toList());
                suppliers.addAll(
                        ItemModule.getModules(compareTo).itemMergedProperties.keySet().stream()
                                .filter(property -> property instanceof GuiWidgetSupplier)
                                .map(property -> (GuiWidgetSupplier) property)
                                .toList());
                return suppliers.stream()
                        .map(guiWidgetSupplier -> (T)
                                new JsonStatDisplay(
                                        guiWidgetSupplier.getTitle(),
                                        guiWidgetSupplier.getDescription(),
                                        guiWidgetSupplier.getStatReader(),
                                        guiWidgetSupplier.getMinValue(),
                                        guiWidgetSupplier.getMaxValue())).toList();
            }
        });
    }

    public static void onReload() {
        statDisplays.clear();
        addStatDisplay(SinglePropertyStatDisplay
                .builder(NemesisProperty.property)
                .setMin(0)
                .setMax(1)
                .setTranslationKey(NemesisProperty.KEY).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(Attributes.ATTACK_DAMAGE)
                .setTranslationKey("damage")
                .setDefault(1)
                .setMax(13.0).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(Attributes.ATTACK_SPEED)
                .setTranslationKey("attack_speed")
                .setDefault(4)
                .setMax(4.0).build());
        addStatDisplay(new DpsStatDisplay());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.BACK_STAB)
                .setTranslationKey("back_stab")
                .setDefault(1)
                .setFormat("##.#")
                .setMax(5).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.SHIELD_BREAK)
                .setTranslationKey("shield_break")
                .setDefault(0)
                .setFormat("##.#")
                .setMax(5).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.REACH)
                .setTranslationKey("reach")
                .setDefault(0)
                .setFormat("##.##")
                .setMax(2).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.ATTACK_RANGE)
                .setTranslationKey("attack_range")
                .setDefault(0)
                .setFormat("##.##")
                .setMax(2).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_DAMAGE)
                .setTranslationKey("projectile_damage")
                .setFormat("##.##")
                .setDefault(0)
                .setMax(10).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_SPEED)
                .setTranslationKey("projectile_speed")
                .setFormat("##.##")
                .setDefault(0)
                .setMin(-3)
                .setMax(10).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_ACCURACY)
                .setTranslationKey("projectile_accuracy")
                .setFormat("##.##")
                .setDefault(0)
                .setMin(-2)
                .setMax(2).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_PIERCING)
                .setTranslationKey("projectile_piercing")
                .setFormat("##.##")
                .setMax(10).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER)
                .setTranslationKey("projectile_crit_multiplier")
                .setFormat("##.##")
                .setMax(10).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(HealthPercentDamage.property)
                .setMax(50)
                .setTranslationKey(HealthPercentDamage.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(ArmorPenProperty.property)
                .setMin(-20)
                .setMax(50)
                .setTranslationKey(ArmorPenProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(BlockProperty.property)
                .setMax(50)
                .setTranslationKey(BlockProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(AirDragProperty.property)
                .setMax(1)
                .setTranslationKey(AirDragProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(WaterDragProperty.property)
                .setMax(1)
                .setTranslationKey(WaterDragProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(RapidfireCrossbowProperty.property)
                .setMax(3)
                .setFormat("##")
                .setTranslationKey(RapidfireCrossbowProperty.KEY).build());


        addStatDisplay(SinglePropertyStatDisplay
                .builder(DurabilityProperty.property)
                .setMax(2000)
                .setFormat("##")
                .setTranslationKey(DurabilityProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(FracturingProperty.property)
                .setMax(50)
                .setTranslationKey(FracturingProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(ImmolateProperty.property)
                .setMax(4)
                .setTranslationKey(ImmolateProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(LeechingProperty.property)
                .setMax(2)
                .setTranslationKey(LeechingProperty.KEY).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(Attributes.ARMOR)
                .setTranslationKey("armor")
                .setMax(8).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PROJECTILE_ARMOR)
                .setTranslationKey("projectile_armor")
                .setMax(8).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(Attributes.ARMOR_TOUGHNESS)
                .setTranslationKey("armor_toughness")
                .setMax(3).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(Attributes.KNOCKBACK_RESISTANCE)
                .setTranslationKey("knockback_resistance")
                .setMax(1).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.SWIM_SPEED)
                .setTranslationKey("swim_speed")
                .setMax(1.5)
                .setDefault(1)
                .setMin(0).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.ELYTRA_GLIDE_EFFICIENCY)
                .setTranslationKey("elytra_glide")
                .setMax(20)
                .setMin(-20).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.ELYTRA_TURN_EFFICIENCY)
                .setTranslationKey("elytra_turn")
                .setMax(20)
                .setMin(-20).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.ELYTRA_ROCKET_EFFICIENCY)
                .setTranslationKey("rocket_efficiency")
                .setMax(5)
                .setMin(-5).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.PLAYER_ITEM_USE_MOVEMENT_SPEED)
                .setTranslationKey("player_item_use_speed")
                .setMax(0)
                .inverseNumber(true)
                .setMin(-1).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.CRITICAL_DAMAGE)
                .setTranslationKey("crit_damage")
                .setMax(3)
                .setMin(0).build());
        addStatDisplay(AttributeSingleDisplay
                .builder(AttributeRegistry.CRITICAL_CHANCE)
                .setTranslationKey("crit_chance")
                .setMax(1)
                .setMin(0).build());

        addStatDisplay(SinglePropertyStatDisplay
                .builder(IllagerBane.property)
                .setMax(3)
                .setFormat("##.#")
                .setTranslationKey(IllagerBane.KEY).build());

        addStatDisplay(SinglePropertyStatDisplay
                .builder(LuminousLearningProperty.property)
                .setMax(2)
                .setFormat("##.#")
                .setTranslationKey(LuminousLearningProperty.KEY).build());

        addStatDisplay(SinglePropertyStatDisplay
                .builder(ExhaustionProperty.property)
                .setMax(50)
                .setFormat("##.#")
                .setTranslationKey(ExhaustionProperty.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(WaterGravityProperty.property)
                .setMax(100)
                .setFormat("##.#")
                .setTranslationKey(WaterGravityProperty.KEY).build());

        addStatDisplay(SinglePropertyStatDisplay
                .builder(AquaticDamage.property)
                .setMax(5)
                .setTranslationKey(AquaticDamage.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(SpiderDamage.property)
                .setMax(5)
                .setTranslationKey(SpiderDamage.KEY).build());
        addStatDisplay(SinglePropertyStatDisplay
                .builder(SmiteDamage.property)
                .setMax(5)
                .setTranslationKey(SmiteDamage.KEY).build());

        addStatDisplay(ComplexBooleanStatDisplay
                .builder(CanWalkOnSnow.property)
                .setTranslationKey(CanWalkOnSnow.KEY).build());
        addStatDisplay(ComplexBooleanStatDisplay
                .builder(FireProof.property)
                .setTranslationKey(FireProof.KEY).build());
        addStatDisplay(ComplexBooleanStatDisplay
                .builder(IsCrossbowShootAble.property)
                .setTranslationKey(IsCrossbowShootAble.KEY).build());
        addStatDisplay(ComplexBooleanStatDisplay
                .builder(IsPiglinGold.property)
                .setTranslationKey(IsPiglinGold.KEY).build());
        addStatDisplay(ComplexBooleanStatDisplay
                .builder(StepCancelingProperty.property)
                .setTranslationKey(StepCancelingProperty.KEY).build());

        addStatDisplay(SinglePropertyStatDisplay
                .builder(PillagesGuard.property)
                .setMax(3)
                .setFormat("##.#")
                .setTranslationKey(PillagesGuard.KEY)
                .setHoverDescription(
                        (stack -> {
                            double value = PillagesGuard.valueRemap(PillagesGuard.property.getValue(stack).orElse(0.0));
                            value = (double) Math.round((1 - value) * 1000) / 10;
                            return Component.translatable("miapi.stat.pillagerGuard.description", value);
                        })).build());

        AttributeSingleDisplay.attributesWithDisplay.add(AttributeRegistry.ARMOR_CRUSHING.value());
        RegistryInventory.moduleProperties.getFlatMap().values().stream()
                .filter(StatWidgetSupplier.class::isInstance)
                .map(StatWidgetSupplier.class::cast)
                .filter(statWidgetSupplier::contains)
                .forEach(statWidgetSupplier::add);
    }

    public static void reloadEnd() {
        BuiltInRegistries.ATTRIBUTE.forEach(entityAttribute -> {
            if (!AttributeSingleDisplay.attributesWithDisplay.contains(entityAttribute)) {
                addStatDisplay(AttributeSingleDisplay
                        .builder(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entityAttribute)).build());
            }
        });
    }

    public StatListWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("miapi.module.statdisplay"));
        transformableWidget = new TransformableWidget(x, y, width, height, Component.empty());
        boxList = new BoxList(x, y, width, height, Component.empty(), new ArrayList<>());
        ScrollList list = new ScrollList(x, y, width, height, List.of(boxList));
        list.altDesign = true;
        list.alwaysEnableScrollbar = true;
        transformableWidget.addChild(list);
        transformableWidget.rawProjection = new Matrix4f();
        //transformableWidget.rawProjection.scale(0.5f, 0.5f, 0.5f);
        addChild(transformableWidget);
        hoverText = new TransformableWidget(x, y, width, height, Component.empty());
        hoverText.rawProjection = new Matrix4f().scale(0.667f, 0.667f, 0.667f);
        addChild(hoverText);
    }

    public static MutableComponent statTranslation(String statName) {
        return Component.translatable(Miapi.MOD_ID + ".stat." + statName);
    }

    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        super.renderWidget(drawContext, mouseX, mouseY, delta);
        Vector4f vector4f = transformableWidget.transFormMousePos(mouseX, mouseY);
        InteractAbleWidget hoverDisplay = null;
        for (GuiEventListener children : boxList.children()) {
            if (children.isMouseOver(vector4f.x(), vector4f.y()) && (children instanceof SingleStatDisplay widget)) {
                InteractAbleWidget ableWidget = widget.getHoverWidget();
                if (ableWidget != null) {
                    hoverDisplay = ableWidget;
                }

            }
        }
        if (hoverDisplay != null && isMouseOver(mouseX, mouseY)) {
            float scale = 1.0f;
            hoverDisplay.setX((int) ((mouseX + 5) * (1 / scale)));
            hoverDisplay.setY((int) ((mouseY - hoverDisplay.getHeight() / 2 * scale) * (1 / scale)));
            hoverText.renderWidget(hoverDisplay, drawContext, mouseX, mouseY, delta);
        }
    }

    private <T extends InteractAbleWidget & SingleStatDisplay> void update() {
        setAttributeCaches(original, compareTo);
        boxList.setWidgets(collectWidgets(original, compareTo), 0);
    }

    public static void setAttributeCaches(ItemStack original, ItemStack compareTo) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> oldAttr = AttributeUtil.getAttribute(original, equipmentSlot);
            Multimap<Attribute, AttributeModifier> compAttr = AttributeUtil.getAttribute(compareTo, equipmentSlot);
            AttributeSingleDisplay.oldItemCache.put(equipmentSlot, oldAttr);
            AttributeSingleDisplay.compareItemCache.put(equipmentSlot, compAttr);
        }
    }

    public static <T extends InteractAbleWidget & SingleStatDisplay> List<InteractAbleWidget> collectWidgets(ItemStack original, ItemStack compareTo) {
        List<InteractAbleWidget> widgets = new ArrayList<>();
        for (StatWidgetSupplier supplier : statWidgetSupplier) {
            List<T> statWidgets = supplier.currentList(original, compareTo);
            for (T statDisplay : statWidgets) {
                if (statDisplay.shouldRender(original, compareTo)) {
                    statDisplay.setHeight(statDisplay.getHeightDesired());
                    statDisplay.setWidth(statDisplay.getWidthDesired());
                    widgets.add(statDisplay);
                }
            }
        }
        for (InteractAbleWidget statDisplay : statDisplays) {
            if (statDisplay instanceof SingleStatDisplay singleStatDisplay && (singleStatDisplay.shouldRender(original, compareTo))) {
                statDisplay.setHeight(singleStatDisplay.getHeightDesired());
                statDisplay.setWidth(singleStatDisplay.getWidthDesired());
                widgets.add(statDisplay);

            }
        }
        List<InteractAbleWidget> sortedWidgets = new ArrayList<>();
        int shortSize = 80;
        InteractAbleWidget buffered = null;
        for (InteractAbleWidget widget : widgets) {
            if (widget.getWidth() <= shortSize) {
                if (buffered == null) {
                    buffered = widget;
                } else {
                    sortedWidgets.add(buffered);
                    sortedWidgets.add(widget);
                    buffered = null;
                }
            } else {
                sortedWidgets.add(widget);
            }
        }
        if (buffered != null) {
            sortedWidgets.add(buffered);
        }
        return sortedWidgets;
    }

    public interface StatWidgetSupplier {
        <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo);
    }

    public static <T extends InteractAbleWidget & SingleStatDisplay> void addStatDisplay(T statDisplay) {
        statDisplays.add(statDisplay);
    }

    public static <T extends InteractAbleWidget & SingleStatDisplay> void addStatDisplay(T... statDisplay) {
        statDisplays.addAll(Arrays.asList(statDisplay));
    }

    public static void addStatDisplaySupplier(StatWidgetSupplier supplier) {
        statWidgetSupplier.add(supplier);
    }

    public void setOriginal(ItemStack original) {
        this.original = original;
        update();
    }

    public void setCompareTo(ItemStack compareTo) {
        this.compareTo = compareTo;
        update();
    }

    public void setItemsOriginal(ItemStack original, ItemStack compareTo) {
        this.original = original;
        this.compareTo = compareTo;
        update();
    }

    public interface TextGetter {
        Component resolve(ItemStack stack);
    }

    public interface MultiTextGetter {
        List<Component> resolve(ItemStack stack);
    }

    public interface JsonConverter<T extends InteractAbleWidget & SingleStatDisplay> {
        T fromJson(JsonElement element, SingleStatDisplayDouble.StatReaderHelper statReader);
    }
}
