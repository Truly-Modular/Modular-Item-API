package smartin.miapi.modules.properties.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.*;

import java.util.*;

/**
 * The `MiningLevelProperty` class defines the property for controlling mining speed and levels of tools.
 * This property allows for detailed control over how tools interact with different blocks, including mining speed adjustments and block compatibility rules.
 *
 * @header Mining Level Property
 * @path /data_types/properties/mining/mining
 * @description_start
 * The MiningLevelProperty manages the mining capabilities of tools, determining their effectiveness based on various rules and configurations.
 * These configurations include block-specific mining speeds, block blacklists, and conditions for correct tool usage.
 *
 * By default, the mining level is influenced by the material properties associated with the tool.
 * Custom rules can be defined to adjust mining speeds and tool compatibilities dynamically.
 * @description_end
 * @path /data_types/properties/mining/mining_level
 * @data mining_capabilities: A map linking mining rules to specific block tags for determining tool effectiveness.
 * @data codec: A codec for serializing and deserializing mining rules, allowing for flexible data handling.
 * @data caching: The property uses a caching mechanism to optimize performance when accessing mining rules.
 *
 * @data mining_rules:
 * @data `blocks`: List of blocks that the tool can mine.
 * @data `block_list | blacklist_tag`: List of blocks that the tool cannot mine.
 * @data `speed`: A resolvable value that determines the mining speed for the tool.
 * @data `correctForDrops`: Optional boolean indicating whether the tool is correct for drops.
 * @data `useMaterial`: Boolean indicating if material properties should affect mining rules.
 *
 */

public class MiningLevelProperty extends CodecProperty<Map<String, MiningLevelProperty.MiningRule>> implements ComponentApplyProperty {
    public static MiningLevelProperty property;
    public static final ResourceLocation KEY = Miapi.id("mining_level");
    public static Map<String, TagKey<Block>> miningCapabilities = new HashMap<>();
    public static Codec<Map<String, MiningRule>> CODEC = Codec.unboundedMap(Codec.STRING, MiningRule.CODEC);
    public static String CACHEKEY = KEY + "finished_component";


    public MiningLevelProperty() {
        super(CODEC);
        property = this;
        ModularItemCache.setSupplier(CACHEKEY, this::asComponent);
    }

    @Override
    public Map<String, MiningRule> merge(Map<String, MiningRule> left, Map<String, MiningRule> right, MergeType mergeType) {
        return ModuleProperty.mergeMap(left, right, mergeType, (triple) -> MiningRule.merge(triple.getLeft(), triple.getMiddle(), triple.getRight()));
    }

    @Override
    public Map<String, MiningLevelProperty.MiningRule> initialize(Map<String, MiningLevelProperty.MiningRule> data, ModuleInstance context) {
        Map<String, MiningLevelProperty.MiningRule> initialized = new HashMap<>();
        data.forEach((key, entry) -> initialized.put(key, entry.initialize(context)));
        return initialized;
    }

    Tool asComponent(ItemStack itemStack) {
        List<Tool.Rule> rules = new ArrayList<>();
        getData(itemStack).orElse(new HashMap<>()).values().forEach(miningRule -> {
            rules.addAll(miningRule.asRules());
        });
        return new Tool(rules, 1.0f, 1);
    }

    Tool asComponentCached(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, CACHEKEY, new Tool(new ArrayList<>(), 1, 1));
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        itemStack.set(DataComponents.TOOL, new Tool(new ArrayList<>(), 1.0f, 1));
    }

    public static float getDestroySpeed(ItemStack stack, BlockState state) {
        return Math.max(property.asComponentCached(stack).getMiningSpeed(state), 1.0F);
    }

    public static boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return property.asComponentCached(stack).isCorrectForDrops(state);
    }

    public static boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        Tool ourComponent = property.asComponentCached(stack);
        int toolDamage = ourComponent.damagePerBlock();
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F && toolDamage > 0) {
            stack.hurtAndBreak(toolDamage, miningEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public record MiningRule(HolderSet<Block> blocks, HolderSet<Block> blacklist, DoubleOperationResolvable speed,
                             Optional<Boolean> correctForDrops, boolean useMaterial,
                             List<Material> respectMaterialBlacklists) {
        public static final Codec<MiningRule> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(
                            RegistryCodecs
                                    .homogeneousList(Registries.BLOCK)
                                    .optionalFieldOf("block_list", HolderSet.direct())
                                    .forGetter(MiningRule::blocks),
                            ResourceLocation.CODEC
                                    .optionalFieldOf("blocks")
                                    .forGetter((rule) -> Optional.empty()),
                            RegistryCodecs
                                    .homogeneousList(Registries.BLOCK)
                                    .optionalFieldOf("blacklist", HolderSet.direct())
                                    .forGetter(MiningRule::blacklist),
                            ResourceLocation.CODEC
                                    .optionalFieldOf("blacklist_tag")
                                    .forGetter((rule) -> Optional.empty()),
                            DoubleOperationResolvable.CODEC
                                    .optionalFieldOf("speed", new DoubleOperationResolvable(1))
                                    .forGetter(MiningRule::speed),
                            Codec.BOOL
                                    .optionalFieldOf("correct_for_drops")
                                    .forGetter(MiningRule::correctForDrops),
                            Codec.BOOL
                                    .optionalFieldOf("use_material", false)
                                    .forGetter(MiningRule::useMaterial))
                    .apply(instance, (blockList, blockTag, blacklist, blackTag, speed, correct, useMaterial) -> {
                        List<Holder<Block>> blocks = new ArrayList<>();
                        blockTag.ifPresent(location -> BuiltInRegistries.BLOCK.getTags().forEach(tagKeyNamedPair -> {
                            if (tagKeyNamedPair.getFirst().location().equals(location)) {
                                tagKeyNamedPair.getSecond().forEach(blocks::add);
                            }
                        }));
                        blockList.stream().forEach(blocks::add);
                        List<Holder<Block>> black = new ArrayList<>();
                        blackTag.ifPresent(location -> BuiltInRegistries.BLOCK.getTags().forEach(tagKeyNamedPair -> {
                            if (tagKeyNamedPair.getFirst().location().equals(location)) {
                                tagKeyNamedPair.getSecond().forEach(black::add);
                            }
                        }));
                        blacklist.stream().forEach(black::add);
                        return new MiningRule(
                                HolderSet.direct(blocks),
                                HolderSet.direct(black), speed, correct, useMaterial, new ArrayList<>());
                    });
        });

        public static MiningRule merge(MiningRule left, MiningRule right, MergeType mergeType) {
            List<Holder<Block>> blocks = new ArrayList<>(left.blocks().stream().toList());
            blocks.addAll(right.blocks().stream().toList());
            List<Holder<Block>> blacklist = new ArrayList<>(left.blacklist().stream().toList());
            blacklist.addAll(right.blacklist().stream().toList());
            DoubleOperationResolvable merged = left.speed().merge(right.speed(), mergeType);
            Optional<Boolean> mergedBoolean = Optional.empty();
            if (left.correctForDrops().isPresent()) {
                mergedBoolean = left.correctForDrops();
            }
            if (right.correctForDrops().isPresent()) {
                mergedBoolean = right.correctForDrops();
            }
            List<Material> mergedMaterials = new ArrayList<>(left.respectMaterialBlacklists());
            mergedMaterials.addAll(right.respectMaterialBlacklists);
            return new MiningRule(HolderSet.direct(blocks), HolderSet.direct(blacklist), merged, mergedBoolean, left.useMaterial() || right.useMaterial(), mergedMaterials);
        }

        public MiningRule initialize(ModuleInstance moduleInstance) {
            List<Holder<Block>> blockBlackList = new ArrayList<>(blacklist().stream().toList());
            Optional<Boolean> correctForDrops = correctForDrops();
            List<Material> mergedMaterials = new ArrayList<>(this.respectMaterialBlacklists());
            if (useMaterial()) {
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null) {
                    mergedMaterials.add(material);
                }
            }
            return new MiningRule(HolderSet.direct(blocks().stream().toList()), HolderSet.direct(blockBlackList), speed().initialize(moduleInstance), correctForDrops, useMaterial(), mergedMaterials);
        }

        public List<Tool.Rule> asRules() {
            float speedEvaluated = (float) speed().evaluate(0.0, 1.0);
            if (speedEvaluated < 1) {
                speedEvaluated = 1.0f;
            }
            if (useMaterial()) {
                List<Block> canDropBlocks = new ArrayList<>(blocks().stream().map(Holder::value).distinct().toList());
                if (!canDropBlocks.isEmpty()) {
                    blacklist()
                            .stream()
                            .map(Holder::value)
                            .distinct()
                            .forEach(canDropBlocks::remove);
                }
                List<Block> blocksWithMiningSpeed = new ArrayList<>(canDropBlocks);
                List<Block> toRemoveFromMaterial = new ArrayList<>();
                respectMaterialBlacklists().forEach(material -> {
                    if (toRemoveFromMaterial.isEmpty()) {
                        BuiltInRegistries.BLOCK.getTag(
                                material.getIncorrectBlocksForDrops()
                        ).ifPresent(named -> {
                            named.stream().map(Holder::value).distinct().forEach(toRemoveFromMaterial::add);
                        });
                    } else {
                        List<Block> notShared = new ArrayList<>(toRemoveFromMaterial);
                        BuiltInRegistries.BLOCK.getTag(
                                material.getIncorrectBlocksForDrops()
                        ).ifPresent(named -> {
                            named.stream().map(Holder::value).distinct().forEach(notShared::remove);
                        });
                        notShared.forEach(toRemoveFromMaterial::remove);
                    }
                });
                toRemoveFromMaterial.forEach(canDropBlocks::remove);
                List<Block> rawBlocks = canDropBlocks.stream().distinct().toList();
                Tool.Rule mineAndDrop = Tool.Rule.minesAndDrops(rawBlocks, speedEvaluated);
                Tool.Rule overrideSpeed = Tool.Rule.overrideSpeed(blocksWithMiningSpeed, speedEvaluated);
                return List.of(mineAndDrop, overrideSpeed);
            }
            return List.of(
                    new Tool.Rule(blacklist, Optional.of(speedEvaluated), Optional.of(false)),
                    new Tool.Rule(blocks, Optional.of(speedEvaluated), correctForDrops())
            );
        }
    }
}
