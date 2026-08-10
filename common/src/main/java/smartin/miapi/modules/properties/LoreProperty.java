package smartin.miapi.modules.properties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import dev.architectury.event.EventResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItemPart;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.generated.SmithingRecipeUtil;
import smartin.miapi.modules.properties.attributes.AttributeToolTipHelper;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

/**
 * @header Lore Property
 * @path /data_types/properties/item_lore
 * @description_start The LoreProperty manages the lore (or descriptive text) of an item. This property allows items to display custom lore
 * that can be added either at the top or bottom of the item's tooltip. The lore is defined as a list of {@link Holder} objects,
 * each specifying the text, position, and priority of the lore entry.
 * <p>
 * The lore can be customized based on whether the item is modular or not, and additional configurations are available
 * through {@link MiapiConfig}. Depending on the environment (client or server), different lore might be injected.
 * @description_end
 * @data a list of Lore Entries, compromised of:
 * @data text: The {@link Component} text to display.
 * @data position: The position of the lore ("top" or "bottom").
 * @data priority: The priority of the lore entry, used for sorting.
 */

public class LoreProperty extends CodecProperty<List<LoreProperty.Holder>> {
    public static final ResourceLocation KEY = Miapi.id("item_lore");
    public static LoreProperty property;
    public static List<LoreSupplier> bottomLoreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static List<ToolTipSupplierSupplier> loreSuppliers = Collections.synchronizedList(new ArrayList<>());
    public static Map<ItemStack, Material> materialLookupTable = Collections.synchronizedMap(new WeakHashMap<>());
    public static Map<Item, List<Component>> smithingTemplate = Collections.synchronizedMap(new WeakHashMap<>());
    public static final Codec<Holder> CODEC_BASE = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(holder -> holder.text),
            Codec.STRING.fieldOf("position").forGetter(holder -> holder.position),
            Codec.FLOAT.optionalFieldOf("priority", 0.0f).forGetter(holder -> holder.priority)
    ).apply(instance, Holder::new));
    public static final Codec<Holder> CODEC = Codec.withAlternative(CODEC_BASE, ComponentSerialization.CODEC.xmap(Holder::new, h -> h.text));


    public LoreProperty() {
        super(Miapi.toListOrSimple(CODEC));
        property = this;
        loreSuppliers.add((ItemStack itemStack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) -> {
            if (hasModularItemDescription(itemStack)) {
                tooltip.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
            }
            getHolders(itemStack).stream().filter(h -> h.position.equals("top")).forEach(holder -> tooltip.add(holder.getText()));
        });
        MiapiEvents.CLEAR_CACHE.register(new MiapiEvents.EmptyEvent() {
            @Override
            public EventResult onReload() {
                materialLookupTable.clear();
                return EventResult.pass();
            }
        });
        ReloadEvents.END.subscribe((isClient, registryAccess, worker) -> {
            try {
                smithingTemplate.clear();
                var recipeManager = SmithingRecipeUtil.findManager(isClient);
                if (recipeManager != null) {
                    recipeManager.getAllRecipesFor(RecipeType.SMITHING).forEach(recipeHolder -> {
                        if (recipeHolder.value() instanceof MaterialSmithingRecipe smithingRecipe) {
                            List<Component> list = smithingTemplate.computeIfAbsent(smithingRecipe.smithingTemplate.getItems()[0].getItem(), (i) -> new ArrayList<>());
                            Material ingredient = MaterialProperty.MATERIAL_REGISTRY.get(smithingRecipe.startMaterial);
                            Material target = MaterialProperty.MATERIAL_REGISTRY.get(smithingRecipe.resultMaterial);
                            if (ingredient != null && target != null) {
                                Component materialDescription = Component.translatable("miapi.material_template.smithing", ingredient.getTranslation().getString(), target.getTranslation().getString()).withStyle(ChatFormatting.GRAY);
                                list.add(materialDescription);
                            }
                        }
                    });
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("could not setup smithing lore injection", e);
            }
        });
        AttributeToolTipHelper.addToolTip("projectile");
    }

    public List<Holder> getHolders(ItemStack itemStack) {
        return getData(itemStack).orElse(new ArrayList<>());
    }

    public static Component gray(Component text) {
        return format(text, ChatFormatting.GRAY);
    }

    public static Component format(Component text, ChatFormatting... formatting) {
        if (text instanceof MutableComponent) {
            return ((MutableComponent) text).withStyle(Style.EMPTY.applyFormats(formatting));
        }
        List<Component> components = text.toFlatList(Style.EMPTY.applyFormats(formatting));
        if(components.size()>0){
            return components.getFirst();
        }
        return Component.literal(text.getString()).withStyle(Style.EMPTY.applyFormats(formatting));
    }

    public void injectTooltipOnNonModularItems(List<Component> tooltip, ItemStack itemStack) {
        if (!smartin.miapi.Environment.isClient() && MiapiConfig.getServerConfig().other.serverLoreInjection) {
            return;
        }
        synchronized (property) {
            if (smartin.miapi.Environment.isClient()) {
                tooltip.addAll(addToolTipsClient(itemStack));
            } else {
                tooltip.addAll(addToolTipsServer(itemStack));
            }
        }
    }

    @Environment(EnvType.CLIENT)
    List<Component> addToolTipsClient(ItemStack itemStack) {
        List<Component> lines = new ArrayList<>();
        if (MiapiConfig.getClientConfig().loreConfig.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {

                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.getClientConfig().loreConfig.injectLoreWithoutGroup) {
                        lines.add(gray(Component.translatable("miapi.ui.material_desc")));
                    }
                } else {
                    Component materialDesc = gray(Component.translatable("miapi.ui.material_desc_alt"));
                    lines.add(materialDesc);
                    if (smartin.miapi.Environment.isClient()) {
                        lines.addAll(getAltClient(material));
                    }
                }
            }
        }
        if (MiapiConfig.getClientConfig().loreConfig.injectLoreModularItem) {
            if (hasModularItemDescription(itemStack)) {
                lines.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
                return lines;
            }
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (hasModularItemDescription(converted)) {
                lines.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
                return lines;
            }
        }
        if (MiapiConfig.getClientConfig().loreConfig.injectLoreModularTemplate) {
            var description = smithingTemplate.get(itemStack.getItem());
            if (description != null) {
                lines.add(Component.translatable("miapi.material_template.smithing.header").withStyle(ChatFormatting.GRAY));
                lines.addAll(description);
            }
        }
        return lines;
    }

    public static boolean hasModularItemDescription(ItemStack itemstack) {
        return VisualModularItem.isVisualModularItem(itemstack) && !isModularPart(itemstack);
    }

    public static boolean isModularPart(ItemStack itemstack) {
        return (itemstack.getItem() instanceof ModularItemPart);
    }

    @Environment(EnvType.CLIENT)
    List<Component> getAltClient(Material material) {
        return new ArrayList<>(material.getDescription(net.minecraft.client.gui.screens.Screen.hasAltDown()));
    }

    List<Component> addToolTipsServer(ItemStack itemStack) {
        List<Component> lines = new ArrayList<>();
        if (MiapiConfig.getClientConfig().loreConfig.injectLoreModularMaterial) {
            Material material = materialLookupTable.computeIfAbsent(itemStack, itemStack1 -> MaterialProperty.getMaterialFromIngredient(itemStack));
            if (material != null) {
                int i = material.getGroups().size();
                if (i == 1) {
                    if (MiapiConfig.getClientConfig().loreConfig.injectLoreWithoutGroup) {
                        lines.add(gray(Component.translatable("miapi.ui.material_desc")));
                    }
                } else {
                    Component materialDesc = gray(Component.translatable("miapi.ui.material_desc_alt"));
                    lines.add(materialDesc);
                }
            }
        }
        if (MiapiConfig.getClientConfig().loreConfig.injectLoreModularItem) {
            ItemStack converted = ModularItemStackConverter.getModularVersion(itemStack);
            if (!ItemStack.matches(converted, itemStack) && hasModularItemDescription(converted)) {
                lines.add(format(Component.translatable("miapi.ui.modular_item"), ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    public static void appendLoreTop(ItemStack stack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) {
        loreSuppliers.forEach(supplierSupplier -> supplierSupplier.getLore(stack, tooltip, context, tooltipType));
    }

    public void appendLoreBottom(List<Component> oldLore, ItemStack itemStack) {
        bottomLoreSuppliers.forEach(loreSupplier -> oldLore.addAll(loreSupplier.getLore(itemStack)));
        getHolders(itemStack).stream().filter(h -> h.position.equals("bottom")).forEach(holder -> oldLore.add(holder.getText()));
    }

    @Override
    public List<Holder> merge(List<Holder> left, List<Holder> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

    public static class Holder implements Comparable<Holder> {
        @CodecBehavior.Optional
        public Component text;
        @CodecBehavior.Optional(false)
        public String position;
        @CodecBehavior.Optional
        public float priority = 0;

        public Holder() {

        }

        public Holder(Component component, String position, float priority) {
            this.text = component;
            this.position = position;
            this.priority = priority;
        }

        public Holder(Component component) {
            this.text = component;
            this.position = "top";
        }

        public Component getText() {
            if (text != null) {
                return text;
                //return Codecs.TEXT.parse(JsonOpsBooleanPatched.INSTANCE, text).result().orElse(Text.empty());
            }
            return net.minecraft.network.chat.Component.empty();
        }

        @Override
        public int compareTo(@NotNull Holder o) {
            return Float.compare(priority, o.priority);
        }
    }

    //@Environment(EnvType.CLIENT)
    public interface LoreSupplier {
        List<Component> getLore(ItemStack itemStack);
    }

    public interface ToolTipSupplierSupplier {
        void getLore(ItemStack itemStack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType);
    }
}
