package smartin.miapi.modules.properties.enchanment;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.mixin.NamedAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

/**
 * This property allows modules to define and restrict which enchantments are allowed on items.
 *
 * @header Allowed Enchantments Property
 * @path /data_types/properties/enchantments/allowed_enchantments
 * @description_start The Allowed Enchantments Property is used to specify and manage enchantments that can or cannot be applied to certain items.
 * It handles enchantment restrictions based on item types and also allows extensions to be detected and dynamically added.
 * This property is crucial for controlling item enchantment compatibility within the modular item system.
 * @descriptino_end
 * @data allowed: a list of allowed enchantments (ResourceLocation).
 * @data forbidden: a list of forbidden enchantments (ResourceLocation).
 */

public class AllowedEnchantments extends CodecProperty<AllowedEnchantments.AllowedEnchantsData> implements CraftingProperty {
    public static final ResourceLocation KEY = Miapi.id("enchantments");
    public static AllowedEnchantments property;
    public static Map<ResourceLocation, List<ResourceLocation>> enchantmentExtentionsMap = new HashMap<>();

    public AllowedEnchantments() {
        super(AllowedEnchantsData.CODEC);
        property = this;
        ReloadEvents.END.subscribe((isClient, registryAccess) -> {
            enchantmentExtentionsMap = new HashMap<>(Map.of(ResourceLocation.parse("c:enchantable/pickaxe"), new ArrayList<>(), ResourceLocation.parse("c:enchantable/axe"), new ArrayList<>(), ResourceLocation.parse("c:enchantable/shovel"), new ArrayList<>(), ResourceLocation.parse("c:enchantable/hoe"), new ArrayList<>()));
            if (Miapi.registryAccess != null && !isClient) {
                Miapi.registryAccess = Miapi.server.registryAccess();
            }

            if (registryAccess != null) {
                detectEnchantments(registryAccess);
            } else if (isClient) {
                if (Miapi.clientRegistryAccess != null) {
                    detectEnchantments(Miapi.clientRegistryAccess);
                } else {
                    if (Miapi.registryAccess != null) {
                        detectEnchantments(Miapi.registryAccess);
                    }
                }
            } else if (Miapi.registryAccess != null) {
                detectEnchantments(Miapi.registryAccess);
            }
        });
    }

    public void detectEnchantments(RegistryAccess access) {
        // Tools
        List<Item> swords = List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        List<Item> pickaxes = List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        List<Item> axes = List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        List<Item> shovels = List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
        List<Item> hoes = List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE);

        List<Item> sharp = new ArrayList<>(swords);
        sharp.addAll(axes);

        List<Item> weapons = new ArrayList<>(swords);
        weapons.add(Items.MACE);

        List<Item> allTools = new ArrayList<>(pickaxes);
        allTools.addAll(axes);
        allTools.addAll(shovels);
        allTools.addAll(hoes);

        // Armor
        List<Item> helmets = List.of(Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET);
        List<Item> chestplates = List.of(Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        List<Item> leggings = List.of(Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        List<Item> boots = List.of(Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);

        List<Item> allArmor = new ArrayList<>();
        allArmor.addAll(helmets);
        allArmor.addAll(chestplates);
        allArmor.addAll(leggings);
        allArmor.addAll(boots);

        // Ranged
        List<Item> bows = List.of(Items.BOW);
        List<Item> crossbows = List.of(Items.CROSSBOW);
        

        access.registry(Registries.ENCHANTMENT).get().forEach(enchantment -> {
            ResourceLocation enchantmentID = access.registry(Registries.ENCHANTMENT).get().getKey(enchantment);

            if (allSupportEnchantment(sharp, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/sharp_weapon"),(s)->new ArrayList<>()).add(enchantmentID);
            } else if (allSupportEnchantment(weapons, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/weapon"),(s)->new ArrayList<>()).add(enchantmentID);
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("c:enchantable/weapon"),(s)->new ArrayList<>()).add(enchantmentID);
            } else if (allSupportEnchantment(swords, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/sword"),(s)->new ArrayList<>()).add(enchantmentID);
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("c:enchantable/sword"),(s)->new ArrayList<>()).add(enchantmentID);
            }

            if (allSupportEnchantment(pickaxes, enchantment)) {
                enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/pickaxe")).add(enchantmentID);
            }
            if (allSupportEnchantment(axes, enchantment)) {
                enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/axe")).add(enchantmentID);
            }
            if (allSupportEnchantment(shovels, enchantment)) {
                enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/shovel")).add(enchantmentID);
            }
            if (allSupportEnchantment(hoes, enchantment)) {
                enchantmentExtentionsMap.get(ResourceLocation.parse("c:enchantable/hoe")).add(enchantmentID);
            }

            // Armor stuff
            if (allSupportEnchantment(allArmor, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/armor"),(s)->new ArrayList<>()).add(enchantmentID);
            }
            if (allSupportEnchantment(helmets, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/head_armor"),(s)->new ArrayList<>()).add(enchantmentID);
            }
            if (allSupportEnchantment(chestplates, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/chest_armor"),(s)->new ArrayList<>()).add(enchantmentID);
            }
            if (allSupportEnchantment(leggings, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/leg_armor"),(s)->new ArrayList<>()).add(enchantmentID);
            }
            if (allSupportEnchantment(boots, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/foot_armor"),(s)->new ArrayList<>()).add(enchantmentID);
            }

            // Ranged stuff
            if (allSupportEnchantment(bows, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/bow"),(s)->new ArrayList<>()).add(enchantmentID);
            }
            if (allSupportEnchantment(crossbows, enchantment)) {
                enchantmentExtentionsMap.computeIfAbsent(ResourceLocation.parse("minecraft:enchantable/crossbow"),(s)->new ArrayList<>()).add(enchantmentID);
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

    /**
     * whether the modular item is allowed
     *
     * @param itemStack
     * @param enchantment
     * @param oldValue
     * @return
     */
    public static boolean isSupported(ItemStack itemStack, Enchantment enchantment, boolean oldValue) {
        Optional<AllowedEnchantsData> optional = property.getData(itemStack);
        RegistryAccess access;
        ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
        if (moduleInstance != null && moduleInstance.registryAccess != null) {
            access = moduleInstance.registryAccess;
        } else {
            access = null;
        }
        return optional.map(allowedEnchantsData -> allowedEnchantsData
                        .isSupported(enchantment, access, Miapi.registryAccess)
                        .orElse(oldValue && MiapiConfig.getServerConfig().enchants.lenientEnchantments))
                .orElse(oldValue);
    }

    public static boolean canEnchant(ItemStack itemStack, Enchantment enchantment, boolean oldValue) {
        Optional<AllowedEnchantsData> optional = property.getData(itemStack);
        RegistryAccess access;
        ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
        if (moduleInstance != null && moduleInstance.registryAccess != null) {
            access = moduleInstance.registryAccess;
        } else {
            access = null;
        }
        return optional.map(allowedEnchantsData -> allowedEnchantsData
                        .canEnchant(enchantment, access, Miapi.registryAccess)
                        .orElse(oldValue && MiapiConfig.getServerConfig().enchants.lenientEnchantments))
                .orElse(oldValue);
    }

    @Override
    public AllowedEnchantsData merge(AllowedEnchantsData left, AllowedEnchantsData right, MergeType mergeType) {
        return new AllowedEnchantsData(
                MergeAble.mergeList(left.allowed(), right.allowed(), mergeType),
                MergeAble.mergeList(left.anvilAllowed(), right.anvilAllowed(), mergeType),
                MergeAble.mergeList(left.forbidden(), right.forbidden(), mergeType));
    }

    @Override
    public AllowedEnchantsData initialize(AllowedEnchantsData property, ModuleInstance context) {
        return new AllowedEnchantsData(initialize(property.allowed()), initialize(property.anvilAllowed()), initialize(property.forbidden()));
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

    @Override
    public ItemStack preview(ItemStack oldStack, ItemStack itemStack, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> moduleData) {
        if (itemStack.has(DataComponents.ENCHANTMENTS)) {
            ItemEnchantments enchantments = itemStack.getEnchantments();
            itemStack.update(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY, (old -> {
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(old);
                getData(itemStack).ifPresent(data -> {
                    enchantments.keySet().forEach(e -> {
                        if (!e.value().isSupportedItem(itemStack)) {
                            mutable.removeIf((enchant -> enchant.value().equals(e.value())));
                        }
                    });
                });
                return mutable.toImmutable();
            }));
        }
        return itemStack;
    }

    public record AllowedEnchantsData(List<ResourceLocation> allowed,
                                      List<ResourceLocation> anvilAllowed,
                                      List<ResourceLocation> forbidden) {
        public static Codec<Either<Holder<Enchantment>, ResourceLocation>> codec = Codec.either(Enchantment.CODEC, ResourceLocation.CODEC);
        public static Codec<AllowedEnchantsData> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                                Codec.list(ResourceLocation.CODEC)
                                        .optionalFieldOf("allowed", List.of())
                                        .forGetter(AllowedEnchantsData::allowed),
                                Codec.list(ResourceLocation.CODEC)
                                        .optionalFieldOf("anvil_allowed", List.of())
                                        .forGetter(AllowedEnchantsData::anvilAllowed),
                                Codec.list(ResourceLocation.CODEC)
                                        .optionalFieldOf("forbidden", List.of())
                                        .forGetter(AllowedEnchantsData::forbidden))
                        .apply(instance, AllowedEnchantsData::new));


        Optional<Boolean> isSupported(Enchantment enchantment, RegistryAccess main, RegistryAccess fallback) {
            if (contains(enchantment, forbidden(), main, fallback)) {
                return Optional.of(false);
            }
            if (contains(enchantment, allowed(), main, fallback)) {
                return Optional.of(true);
            }
            if (contains(enchantment, anvilAllowed(), main, fallback)) {
                return Optional.of(true);
            }
            return Optional.empty();
        }

        Optional<Boolean> canEnchant(Enchantment enchantment, RegistryAccess main, RegistryAccess fallback) {
            if (contains(enchantment, forbidden(), main, fallback)) {
                return Optional.of(false);
            }
            if (contains(enchantment, allowed(), main, fallback)) {
                return Optional.of(true);
            }
            return Optional.empty();
        }

        private boolean contains(Enchantment enchantment, List<ResourceLocation> ids, RegistryAccess main, RegistryAccess fallback) {
            ResourceLocation mainID = null;
            Holder<Enchantment> enchantmentHolder = null;
            if (main != null) {
                mainID = main.registry(Registries.ENCHANTMENT).get().getKey(enchantment);
                if (mainID == null) {
                    var asd = main.registry(Registries.ENCHANTMENT).get().getHolder(mainID);
                    if (asd.isPresent()) {
                        enchantmentHolder = asd.get();
                    }
                }
            }
            if (mainID == null && fallback != null) {
                mainID = fallback.registry(Registries.ENCHANTMENT).get().getKey(enchantment);
                var asd = fallback.registry(Registries.ENCHANTMENT).get().getHolder(mainID);
                if (asd.isPresent()) {
                    enchantmentHolder = asd.get();
                }
            }
            if (mainID == null && fallback != null) {
                mainID = Miapi.registryAccess.registry(Registries.ENCHANTMENT).get().getKey(enchantment);
                var asd = Miapi.registryAccess.registry(Registries.ENCHANTMENT).get().getHolder(mainID);
                if (asd.isPresent()) {
                    enchantmentHolder = asd.get();
                }
            }
            if (mainID == null && Miapi.clientRegistryAccess != null) {
                mainID = Miapi.clientRegistryAccess.registry(Registries.ENCHANTMENT).get().getKey(enchantment);
                var asd = Miapi.clientRegistryAccess.registry(Registries.ENCHANTMENT).get().getHolder(mainID);
                if (asd.isPresent()) {
                    enchantmentHolder = asd.get();
                }
            }
            for (ResourceLocation id : ids) {
                if (enchantmentHolder != null && enchantmentHolder.is(id)) {
                    return true;
                }
                if (mainID != null && id.equals(mainID)) {
                    return true;
                }
                if (enchantment.definition().supportedItems() instanceof HolderSet.Named<Item> set) {
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
