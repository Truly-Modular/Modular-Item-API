package smartin.miapi.modules.properties.enchanment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.mixin.NamedAccessor;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

/**
 * This property allows modules to define and restrict which enchantments are allowed on items.
 * @header Allowed Enchantments Property
 * @path /data_types/properties/enchantments/allowed_enchantments
 * @description_start
 * The Allowed Enchantments Property is used to specify and manage enchantments that can or cannot be applied to certain items.
 * It handles enchantment restrictions based on item types and also allows extensions to be detected and dynamically added.
 * This property is crucial for controlling item enchantment compatibility within the modular item system.
 * @descriptino_end
 * @data allowed: a list of allowed enchantments (ResourceLocation).
 * @data forbidden: a list of forbidden enchantments (ResourceLocation).
 */

public class AllowedEnchantments extends CodecProperty<AllowedEnchantments.AllowedEnchantsData> {
    public static final ResourceLocation KEY = Miapi.id("enchantments");
    public static AllowedEnchantments property;
    public static Map<ResourceLocation, List<ResourceLocation>> enchantmentExtentionsMap = new HashMap<>();

    public AllowedEnchantments() {
        super(AllowedEnchantsData.CODEC);
        property = this;
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            enchantmentExtentionsMap = new HashMap<>(Map.of(
                    ResourceLocation.parse("c:enchantable/pickaxe"), new ArrayList<>(),
                    ResourceLocation.parse("c:enchantable/axe"), new ArrayList<>(),
                    ResourceLocation.parse("c:enchantable/shovel"), new ArrayList<>(),
                    ResourceLocation.parse("c:enchantable/hoe"), new ArrayList<>()
            ));
            if (registryAccess != null) {
                detectEnchantments(registryAccess);
            }
        });
    }

    public void detectEnchantments(RegistryAccess access) {
        List<Item> pickaxes = List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        List<Item> axe = List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        List<Item> shovel = List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
        List<Item> hoe = List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
        List<Item> allTools = new ArrayList<>(pickaxes);
        allTools.addAll(axe);
        allTools.addAll(shovel);
        allTools.addAll(hoe);

        access.registry(Registries.ENCHANTMENT).get().forEach(enchantment -> {
            if (!allSupportEnchantment(allTools, enchantment)) {
                ResourceLocation enchantmentID = access.registry(Registries.ENCHANTMENT).get().getKey(enchantment);
                if (allSupportEnchantment(pickaxes, enchantment)) {
                    enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/pickaxe")).add(enchantmentID);
                }
                if (allSupportEnchantment(axe, enchantment)) {
                    enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/axe")).add(enchantmentID);
                }
                if (allSupportEnchantment(shovel, enchantment)) {
                    enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/shovel")).add(enchantmentID);
                }
                if (allSupportEnchantment(hoe, enchantment)) {
                    enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/hoe")).add(enchantmentID);
                }
            }
        });
    }

    public boolean allSupportEnchantment(List<Item> items, Enchantment enchantment) {
        for (Item item : items) {
            if (!enchantment.isSupportedItem(item.getDefaultInstance())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllowed(ItemStack itemStack, Holder<Enchantment> enchantment, boolean oldValue) {
        Optional<AllowedEnchantsData> optional = property.getData(itemStack);
        return optional.map(allowedEnchantsData -> allowedEnchantsData.isAllowed(enchantment).orElse(oldValue)).orElse(oldValue);
    }

    @Override
    public AllowedEnchantsData merge(AllowedEnchantsData left, AllowedEnchantsData right, MergeType mergeType) {
        return new AllowedEnchantsData(
                MergeAble.mergeList(left.allowed(), right.allowed(), mergeType),
                MergeAble.mergeList(left.forbidden(), right.forbidden(), mergeType)
        );
    }

    @Override
    public AllowedEnchantsData initialize(AllowedEnchantsData property, ModuleInstance context) {
        return new AllowedEnchantsData(initialize(property.allowed()), initialize(property.forbidden()));
    }

    public List<ResourceLocation> initialize(List<ResourceLocation> ids) {
        List<ResourceLocation> expanded = new ArrayList<>(ids);
        for (ResourceLocation id : ids) {
            if (enchantmentExtentionsMap.containsKey(id)) {
                expanded.addAll(enchantmentExtentionsMap.get(id));
            }
        }
        return expanded.stream().distinct().toList();
    }

    public record AllowedEnchantsData(List<ResourceLocation> allowed,
                                      List<ResourceLocation> forbidden) {
        public static Codec<Either<Holder<Enchantment>, ResourceLocation>> codec = Codec.either(
                Enchantment.CODEC,
                ResourceLocation.CODEC
        );
        public static Codec<AllowedEnchantsData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                        Codec.list(ResourceLocation.CODEC)
                                .optionalFieldOf("allowed", List.of())
                                .forGetter(AllowedEnchantsData::allowed),
                        Codec.list(ResourceLocation.CODEC)
                                .optionalFieldOf("forbidden", List.of())
                                .forGetter(AllowedEnchantsData::allowed)
                )
                .apply(instance, AllowedEnchantsData::new));


        Optional<Boolean> isAllowed(Holder<Enchantment> enchantment) {
            if (contains(enchantment, forbidden())) {
                return Optional.of(false);
            }
            if (contains(enchantment, allowed)) {
                return Optional.of(true);
            }
            return Optional.empty();
        }

        private boolean contains(Holder<Enchantment> enchantment, List<ResourceLocation> ids) {
            for (ResourceLocation id : ids) {
                if (enchantment.is(id)) {
                    return true;
                }
                if (enchantment.value().definition().supportedItems() instanceof HolderSet.Named<Item> set) {
                    ResourceLocation tagID = ((NamedAccessor) set).getKey().location();
                    if (tagID.equals(id)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
